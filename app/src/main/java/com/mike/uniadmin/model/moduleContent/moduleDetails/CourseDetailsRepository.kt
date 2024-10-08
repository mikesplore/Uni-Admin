package com.mike.uniadmin.model.moduleContent.moduleDetails

import com.google.firebase.database.*
import com.mike.uniadmin.CourseManager
import com.mike.uniadmin.model.announcements.uniConnectScope
import com.mike.uniadmin.model.modules.ModuleEntity
import kotlinx.coroutines.launch

class ModuleDetailRepository(private val moduleDetailDao: ModuleDetailDao) {
    private val courseCode = CourseManager.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")
    
    init {
        // Start listening for changes in Firebase
        setupRealtimeUpdates()
    }

    private fun setupRealtimeUpdates() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
               // val moduleID = snapshot.key ?: return
                val details = snapshot.child("Module Details").children.mapNotNull { it.getValue(
                    ModuleDetail::class.java) }
                uniConnectScope.launch {
                    details.forEach { moduleDetailDao.deleteModuleDetail(it.detailID) }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                println("Error setting up real-time updates: ${error.message}")
            }

            private fun updateLocalDatabase(snapshot: DataSnapshot) {
               // val moduleID = snapshot.key ?: return
                val details = snapshot.child("Module Details").children.mapNotNull { it.getValue(
                    ModuleDetail::class.java) }
                uniConnectScope.launch {
                    details.forEach { moduleDetailDao.insertModuleDetail(it) }
                }
            }
        })
    }

    fun getModuleDetailsByModuleID(moduleCode: String, onResult: (ModuleEntity?) -> Unit) {
        val moduleDetailsRef = FirebaseDatabase.getInstance().reference.child("Modules").child(moduleCode)
        uniConnectScope.launch {
            val cachedData = moduleDetailDao.getModuleDetailsByID(moduleCode)
            if (cachedData != null) {
                onResult(cachedData)
            } else {
                moduleDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val moduleInfo = snapshot.getValue(ModuleEntity::class.java)
                        onResult(moduleInfo)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error fetching module details: ${error.message}")
                        onResult(null) // Indicate failure by returning null
                    }
                })
            }
        }
    }

    fun writeModuleDetail(moduleID: String, moduleDetail: ModuleDetail, onResult: (Boolean) -> Unit) {
        uniConnectScope.launch {
            try {
                // Insert into local database
                moduleDetailDao.insertModuleDetail(moduleDetail)
                // Insert into Firebase
                val moduleDetailRef = database.child(moduleID).child("Module Details")
                moduleDetailRef.child(moduleDetail.detailID).setValue(moduleDetail)
                    .addOnSuccessListener { onResult(true) } // Indicate success
                    .addOnFailureListener { exception ->
                        println("Error writing detail: ${exception.message}")
                        onResult(false) // Indicate failure
                    }
            } catch (e: Exception) {
                println("Error writing detail: ${e.message}")
                onResult(false) // Indicate failure
            }
        }
    }

    fun getModuleDetails(moduleID: String, onResult: (ModuleDetail?) -> Unit) {
        uniConnectScope.launch {
            val cachedData = moduleDetailDao.getModuleDetail(moduleID)
            if (cachedData != null) {
                onResult(cachedData)
            } else {
                val moduleDetailRef = database.child(moduleID).child("Module Details").limitToFirst(1)
                moduleDetailRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChildren()) {
                            val detail = snapshot.children.first().getValue(ModuleDetail::class.java)
                            detail?.let {
                                uniConnectScope.launch {
                                    moduleDetailDao.insertModuleDetail(it)
                                }
                                onResult(it)
                            }
                        } else {
                            onResult(null) // No module detail found
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error reading details: ${error.message}")
                        onResult(null)
                    }
                })
            }
        }
    }
}

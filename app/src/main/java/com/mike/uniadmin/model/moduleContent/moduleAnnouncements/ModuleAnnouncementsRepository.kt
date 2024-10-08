package com.mike.uniadmin.model.moduleContent.moduleAnnouncements

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.CourseManager
import com.mike.uniadmin.model.announcements.uniConnectScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModuleAnnouncementRepository(private val moduleAnnouncementDao: ModuleAnnouncementDao) {
    private val courseCode = CourseManager.courseCode.value
    private val database =
        FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")

    fun writeModuleAnnouncement(
        moduleID: String,
        moduleAnnouncement: ModuleAnnouncement,
        onResult: (Boolean) -> Unit
    ) {
        uniConnectScope.launch {
            // Save to local database first
            try {
                moduleAnnouncementDao.insertModuleAnnouncement(moduleAnnouncement)

                // Save to Firebase
                database.child(moduleID).child("Module Announcements")
                    .child(moduleAnnouncement.announcementID)
                    .setValue(moduleAnnouncement)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { exception ->
                        println("Error writing announcement: ${exception.message}")
                        onResult(false)
                    }
            } catch (e: Exception) {
                println("Error writing to local database: ${e.message}")
                onResult(false)
            }
        }
    }

    fun getModuleAnnouncements(moduleID: String): LiveData<List<ModuleAnnouncementsWithAuthor>> {
        listenToFirebaseChanges(moduleID)  // Pass moduleID to ensure correct updates
        return moduleAnnouncementDao.getModuleAnnouncements(moduleID)
    }

    private fun listenToFirebaseChanges(moduleID: String) {
        val moduleAnnouncementRef = database.child(moduleID).child("Module Announcements")
        moduleAnnouncementRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<ModuleAnnouncement>()
                for (childSnapshot in snapshot.children) {
                    val announcement = childSnapshot.getValue(ModuleAnnouncement::class.java)
                    announcement?.let { announcements.add(it) }
                }
                // Call the update function
                updateLocalDatabase(announcements)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading announcements: ${error.message}")
            }
        })
    }

    private fun updateLocalDatabase(announcements: List<ModuleAnnouncement>) {
        uniConnectScope.launch {
            try {
                // To handle updates correctly:
                // 2. Insert the updated list of announcements
                withContext(Dispatchers.IO) {
                    moduleAnnouncementDao.insertModuleAnnouncements(announcements)
                }
            } catch (e: Exception) {
                println("Error updating local database: ${e.message}")
            }
        }
    }
}

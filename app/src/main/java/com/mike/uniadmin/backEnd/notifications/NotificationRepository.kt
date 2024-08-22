package com.mike.uniadmin.backEnd.notifications

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.courses.CourseCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class NotificationRepository(private val notificationDao: NotificationDao) {
    private val courseCode = CourseCode.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("Notifications")
    private val valueListeners = mutableMapOf<String, ValueEventListener>()
    private val childListeners = mutableMapOf<String, ChildEventListener>()

    init {
        // Add a listener to keep the local database updated
        addRealtimeListener()
    }

    fun getNotifications(onComplete: (List<NotificationEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = notificationDao.getAllNotifications()
            if (cachedData.isNotEmpty()) {
                onComplete(cachedData)
            } else {
                // Use ValueEventListener for initial data load
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val notifications = mutableListOf<NotificationEntity>()
                        for (childSnapshot in snapshot.children) {
                            val notification = childSnapshot.getValue(NotificationEntity::class.java)
                            notification?.let { notifications.add(it) }
                        }
                        viewModelScope.launch {
                            notificationDao.insertNotifications(notifications)
                            onComplete(notifications)
                        }
                        stopListening("initialLoad") // Stop the listener after initial load
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                }
                database.addListenerForSingleValueEvent(listener)
                valueListeners["initialLoad"] = listener // Store listener with a key
            }
        }
    }

    private fun addRealtimeListener() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notification = snapshot.getValue(NotificationEntity::class.java)
                notification?.let {
                    viewModelScope.launch {
                        notificationDao.insertNotification(it)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val notification = snapshot.getValue(NotificationEntity::class.java)
                notification?.let {
                    viewModelScope.launch {
                        notificationDao.insertNotification(it)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val notificationId = snapshot.key
                viewModelScope.launch {
                    notificationId?.let { notificationDao.deleteNotification(it) }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moves if needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        database.addChildEventListener(childEventListener)
        childListeners["realtimeUpdates"] = childEventListener // Store listener with a key
    }

    fun writeNotification(notificationEntity: NotificationEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            notificationDao.insertNotification(notificationEntity)
            database.child(notificationEntity.id).setValue(notificationEntity)
                .addOnSuccessListener {
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    fun stopListening(key: String) {
        valueListeners[key]?.let {
            database.removeEventListener(it)
            valueListeners.remove(key)
        }
        childListeners[key]?.let {
            database.removeEventListener(it)
            childListeners.remove(key)
        }
    }
}

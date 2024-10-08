package com.mike.uniadmin.model.groupchat

import androidx.lifecycle.LiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.announcements.uniConnectScope
import kotlinx.coroutines.launch


class GroupChatRepository(private val groupChatDao: GroupChatDao, private val groupDao: GroupDao) {
    private val database = FirebaseDatabase.getInstance().getReference()

    private val chatsCache = HashMap<String, GroupChatEntity>() // Local cache

    fun fetchGroupChats(path: String) {
        database.child(path).addChildEventListener(object :
            ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(GroupChatEntity::class.java)
                chat?.let {
                    chatsCache[snapshot.key!!] = it
                    uniConnectScope.launch {
                        groupChatDao.insertOrUpdateChat(it) // Use insertOrUpdate for updates
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(GroupChatEntity::class.java)
                chat?.let {
                    chatsCache[snapshot.key!!] = it
                    uniConnectScope.launch {
                        groupChatDao.insertOrUpdateChat(it)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val chatId = snapshot.key!!
                chatsCache.remove(chatId)
                uniConnectScope.launch {
                    groupChatDao.deleteChat(chatId) // Delete from Room
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle if needed
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading chats: ${error.message}")
            }
        })
    }

    fun fetchGroupChatWithDetails(): LiveData<List<GroupChatEntityWithDetails>> {
        return groupChatDao.getChatsWithDetails()

    }

    fun saveGroupChat(chat: GroupChatEntity, path: String, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch {
            try {
                // Save to Room database first
                groupChatDao.insertChats(listOf(chat))

                // Then save to Firebase
                database.child(path).child(chat.chatId).setValue(chat)
                    .addOnCompleteListener { task ->
                        onComplete(task.isSuccessful)
                    }
                    .addOnFailureListener { exception ->
                        println("Error saving chat: ${exception.message}")
                        onComplete(false)
                    }
            } catch (e: Exception) {
                println("Error saving chat locally: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun fetchGroups(onResult: (List<GroupEntity>) -> Unit) {
        uniConnectScope.launch {
            // Fetch from Room database first
            val cachedGroups = groupDao.getGroups()
            if (cachedGroups.isNotEmpty()) {
                onResult(cachedGroups)
            }

            // Fetch from Firebase and update Room database
            database.child("Groups").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groups = mutableListOf<GroupEntity>()
                    for (childSnapshot in snapshot.children) {
                        val group = childSnapshot.getValue(GroupEntity::class.java)
                        group?.let { groups.add(it) }
                    }

                    // Update Room database and notify UI with fresh data
                    uniConnectScope.launch {
                        groupDao.insertGroups(groups)
                        onResult(groups)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error reading groups: ${error.message}")
                }
            })
        }
    }

    fun saveGroup(group: GroupEntity, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch {
            try {
                // Save to Room database first
                groupDao.insertGroups(listOf(group))

                // Then save to Firebase
                database.child("Groups").child(group.id).setValue(group)
                    .addOnCompleteListener { task ->
                        onComplete(task.isSuccessful)
                    }
                    .addOnFailureListener { exception ->
                        println("Error saving group: ${exception.message}")
                        onComplete(false)
                    }
            } catch (e: Exception) {
                println("Error saving group locally: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun fetchGroupByID(groupID: String, onResult: (GroupEntity?) -> Unit) {
        uniConnectScope.launch {
            // Fetch from Room database first
            val cachedGroup = groupDao.getGroups().find { it.id == groupID }
            if (cachedGroup != null) {
                onResult(cachedGroup)
            }

            // Fetch from Firebase and update Room database
            database.child("Groups").child(groupID).get().addOnSuccessListener { snapshot ->
                val group = snapshot.getValue(GroupEntity::class.java)
                uniConnectScope.launch {
                    if (group != null) {
                        groupDao.insertGroups(listOf(group))
                    }
                    onResult(group)
                }
            }.addOnFailureListener { exception ->
                println("Error fetching group: ${exception.message}")
                onResult(null)
            }
        }
    }

    fun deleteChat(chatId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        uniConnectScope.launch {
            try {
                // Delete from Room database first
                groupChatDao.deleteChat(chatId)

                // Then delete from Firebase
                database.child(chatId).removeValue()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            } catch (e: Exception) {
                println("Error deleting chat locally: ${e.message}")
                onFailure(e)
            }
        }
    }

    fun deleteGroup(groupId: String, onComplete: () -> Unit) {
        uniConnectScope.launch {
            try {
                // Delete from Room database first
                groupDao.deleteGroup(groupId)

                // Then delete from Firebase
                database.child("Groups").child(groupId).removeValue()
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { exception ->
                        println("Error deleting group: ${exception.message}")
                    }
            } catch (e: Exception) {
                println("Error deleting group locally: ${e.message}")
            }
        }
    }
}


package com.mike.uniadmin.model.users

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.announcements.uniConnectScope
import com.mike.uniadmin.model.localDatabase.DatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class UserRepository(
    private val userDao: UserDao,
    private val userStateDao: UserStateDao,
    private val accountDeletionDao: AccountDeletionDao,
    private val databaseDao: DatabaseDao,
) {

    private val database = FirebaseDatabase.getInstance().reference

    init {
        startUserListener()
        startUserStateListener()
    }

    fun deleteAllTables() {
        uniConnectScope.launch(Dispatchers.Main) {
            databaseDao.deleteAllTables()
        }
    }

    private fun <T> startDatabaseListener(
        path: String, convert: (DataSnapshot) -> T?, onResult: suspend (List<T>) -> Unit
    ) {
        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<T>()
                for (childSnapshot in snapshot.children) {
                    val item = convert(childSnapshot)
                    item?.let { items.add(it) }
                }
                uniConnectScope.launch(Dispatchers.Main) {
                    onResult(items)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading $path: ${error.message}")
            }
        })
    }

    private fun startUserListener() {
        startDatabaseListener("Users",
            convert = { it.getValue(UserEntity::class.java) },
            onResult = { users ->
                userDao.insertUsers(users)
            })
    }

    private fun startUserStateListener() {
        startDatabaseListener("Users Online Status",
            convert = { it.getValue(UserStateEntity::class.java) },
            onResult = { userStates ->
                userStateDao.insertUserStates(userStates)
            })
    }

    fun fetchUsers(onResult: (List<UserEntity>) -> Unit) {
        uniConnectScope.launch(Dispatchers.Main) {
            // 1. Fetch users from local database first
            val localUsers = userDao.getUsers()
            onResult(localUsers)

            // 2. Then, fetch from remote and update local if needed
            try {
                val remoteUsers = fetchUsersFromRemoteDatabase()
                if (remoteUsers != localUsers) { // Check for differences
                    userDao.insertUsers(remoteUsers)
                    onResult(remoteUsers) // Update UI if there are changes
                }
            } catch (e: Exception) {
                println("Error fetching users from remote database: ${e.message}")
            }
        }
    }

    private suspend fun fetchUsersFromRemoteDatabase(): List<UserEntity> {
        return suspendCoroutine { continuation ->
            val allUsers = mutableListOf<UserEntity>()
            val adminIds = mutableSetOf<String>()

            // Fetch users from "Users" node first to capture all admin IDs
            database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val admin = childSnapshot.getValue(UserEntity::class.java)
                        admin?.let {
                            allUsers.add(it)
                            adminIds.add(it.id) // Store admin IDs for later comparison
                        }
                    }

                    // Fetch users from "Users" node after fetching from "Users"
                    database.child("Users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (childSnapshot in snapshot.children) {
                                    val user = childSnapshot.getValue(UserEntity::class.java)
                                    user?.let {
                                        // Add user only if not present in adminIds
                                        if (!adminIds.contains(it.id)) {
                                            allUsers.add(it)
                                        }
                                    }
                                }
                                continuation.resume(allUsers) // Resume with filtered list
                            }

                            override fun onCancelled(error: DatabaseError) {
                                continuation.resumeWithException(Exception("Error reading users: ${error.message}"))
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception("Error reading users: ${error.message}"))
                }
            })
        }
    }


    fun saveUser(user: UserEntity, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch(Dispatchers.Main) {
            userDao.insertUser(user)
            database.child("Users").child(user.id).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                    Log.d("UserRepository", "User saved successfully")
                } else {
                    onComplete(false)
                    Log.e("UserRepository", "Error saving user: ${task.exception}")
                }
            }
        }
    }

    fun fetchUserDataByEmail(email: String, callback: (UserEntity?) -> Unit) {
        uniConnectScope.launch(Dispatchers.Main) {
            val databaseUser = userDao.getUserByEmail(email)
            if (databaseUser != null) {
                callback(databaseUser)
            } else {
                database.child("Users").orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userSnapshot =
                                snapshot.children.firstOrNull() // Get the first matching user
                            val user = userSnapshot?.getValue(UserEntity::class.java)
                            callback(user) // Return the User object or null if not found
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(null) // Handle or log the error as needed
                        }
                    })
            }
        }
    }

    fun fetchUserDataByAdmissionNumber(admissionNumber: String, callback: (UserEntity?) -> Unit) {
        uniConnectScope.launch(Dispatchers.Main) {
            val databaseUser = userDao.getUserByID(admissionNumber)
            if (databaseUser != null) {
                callback(databaseUser)
            } else {
                // Check Users node
                val usersQuery = database.child("Users").orderByChild("id").equalTo(admissionNumber)
                usersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userSnapshot = snapshot.children.firstOrNull()
                        val user = userSnapshot?.getValue(UserEntity::class.java)
                        if (user != null) {
                            callback(user) // Found in Users node
                        } else {
                            // Check Users node if not found in Users
                            val userQuery = database.child("Users").orderByChild("id").equalTo(admissionNumber)
                            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val adminSnapshot = snapshot.children.firstOrNull()
                                    val admin = adminSnapshot?.getValue(UserEntity::class.java)
                                    callback(admin) // Return admin or null
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    callback(null) // Handle or log the error
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(null) // Handle or log the error
                    }
                })
            }
        }
    }


    fun deleteUser(userId: String, onSuccess: (Boolean) -> Unit) {
        uniConnectScope.launch(Dispatchers.Main) {
            userDao.deleteUser(userId)
            database.child("Users").child(userId).removeValue().addOnSuccessListener {
                onSuccess(true)
            }.addOnFailureListener { exception ->
                onSuccess(false)
                Log.e("Error", "$exception")
            }
        }
    }


    fun writeAccountDeletionData(
        accountDeletion: AccountDeletionEntity, onSuccess: (Boolean) -> Unit
    ) {
        uniConnectScope.launch(Dispatchers.Main) {
            try {
                // Insert the account deletion data into the local database first
                accountDeletionDao.insertAccountDeletion(accountDeletion)
                Log.d(
                    "AccountDeletionStatus",
                    "Inserted account deletion data into local database for userId: ${accountDeletion.id}"
                )

                // Write to Firebase
                database.child("Account Deletion").child(accountDeletion.admissionNumber)
                    .setValue(accountDeletion).addOnSuccessListener {
                        Log.d(
                            "AccountDeletionStatus",
                            "Account deletion data written to Firebase successfully for userId: ${accountDeletion.id}"
                        )
                        onSuccess(true)
                    }.addOnFailureListener { exception ->
                        Log.e(
                            "AccountDeletionStatus",
                            "Failed to write account deletion data to Firebase: ${exception.message}"
                        )
                        onSuccess(false)
                    }
            } catch (e: Exception) {
                Log.e(
                    "AccountDeletionStatus",
                    "Error during account deletion write operation: ${e.message}"
                )
                onSuccess(false)
            }
        }
    }

    fun checkAccountDeletionData(userId: String, onComplete: (AccountDeletionEntity?) -> Unit) {
        uniConnectScope.launch(Dispatchers.Main) {
            try {
                // Attempt to retrieve the account deletion data from the local database first
                val cachedData = accountDeletionDao.getAccountDeletion(userId)
                if (cachedData != null) {
                    Log.d(
                        "AccountDeletionStatus",
                        "Fetched account deletion data from local database for userId: $userId"
                    )
                    onComplete(cachedData)
                } else {
                    // Fetch from Firebase if not found locally
                    database.child("Account Deletion").child(userId).get()
                        .addOnSuccessListener { snapshot ->
                            val accountDeletion =
                                snapshot.getValue(AccountDeletionEntity::class.java)
                            if (accountDeletion != null) {
                                Log.d(
                                    "AccountDeletionStatus",
                                    "Fetched account deletion data from Firebase for userId: $userId"
                                )
                                onComplete(accountDeletion)
                            } else {
                                Log.d(
                                    "AccountDeletionStatus",
                                    "No account deletion data found in Firebase for userId: $userId"
                                )
                                onComplete(null)
                            }
                        }.addOnFailureListener { exception ->
                            Log.e(
                                "AccountDeletionStatus",
                                "Error reading account deletion from Firebase: ${exception.message}"
                            )
                            onComplete(null)
                        }
                }
            } catch (e: Exception) {
                Log.e(
                    "AccountDeletionStatus",
                    "Error during account deletion fetch operation: ${e.message}"
                )
                onComplete(null)
            }
        }
    }


    fun fetchAllUserStatuses(onUserStatesFetched: (List<UserStateEntity>) -> Unit) {
        uniConnectScope.launch {
           val cachedData =  userStateDao.getAllUserStates()
            onUserStatesFetched(cachedData)
        }

        database.child("Users Online Status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userStates = mutableListOf<UserStateEntity>()
                for (childSnapshot in snapshot.children) {
                    val userState = childSnapshot.getValue(UserStateEntity::class.java)
                    userState?.let { userStates.add(it) }
                }
                onUserStatesFetched(userStates)
            }

            override fun onCancelled(error: DatabaseError) {
                // Firebase connection failed, fetch from local database
                uniConnectScope.launch(Dispatchers.Main) {
                    val cachedUserStates = userStateDao.getAllUserStates()
                    onUserStatesFetched(cachedUserStates)
                }
            }
        })
    }

    fun fetchUserStateByUserId(userId: String, onUserStateFetched: (UserStateEntity?) -> Unit) {
        database.child("Users Online Status").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userState = snapshot.getValue(UserStateEntity::class.java)
                    onUserStateFetched(userState)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Firebase connection failed, fetch from local database
                    uniConnectScope.launch(Dispatchers.Main) {
                        val cachedUserState = userStateDao.getUserState(userId)
                        onUserStateFetched(cachedUserState)
                    }
                }
            })
    }


}
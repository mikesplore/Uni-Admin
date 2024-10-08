package com.mike.uniadmin.model.localDatabase

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.mike.uniadmin.model.announcements.AnnouncementEntity
import com.mike.uniadmin.model.groupchat.GroupChatEntity
import com.mike.uniadmin.model.groupchat.GroupEntity
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.model.moduleContent.moduleTimetable.ModuleTimetable
import com.mike.uniadmin.model.modules.ModuleEntity
import com.mike.uniadmin.model.notifications.NotificationEntity
import com.mike.uniadmin.model.userchat.UserChatEntity
import com.mike.uniadmin.model.users.UserEntity


@Dao
interface DatabaseDao {
    @Query("SELECT * FROM announcements")
    suspend fun getAnnouncements(): List<AnnouncementEntity>

    @Query("DELETE FROM announcements")
    suspend fun deleteAnnouncements()

    @Query("SELECT * FROM notifications")
    suspend fun getNotifications(): List<NotificationEntity>

    @Query("DELETE FROM notifications")
    suspend fun deleteNotifications()


    @Query("DELETE FROM moduleAnnouncements")
    suspend fun deleteModuleAnnouncements()

    @Query("SELECT * FROM moduleAssignments")
    suspend fun getModuleAssignments(): List<ModuleAssignment>

    @Query("DELETE FROM moduleAssignments")
    suspend fun deleteModuleAssignments()


    @Query("DELETE FROM moduleDetails")
    suspend fun deleteModuleDetails()

    @Query("SELECT * FROM moduleTimetable")
    suspend fun getModuleTimetables(): List<ModuleTimetable>

    @Query("DELETE FROM moduleTimetable")
    suspend fun deleteModuleTimetables()

    @Query("DELETE FROM attendanceStates")
    suspend fun deleteAttendanceStates()

    @Query("DELETE FROM modules")
    suspend fun deleteModules()

    @Query("SELECT * FROM groupChats")
    suspend fun getChats(): List<GroupChatEntity>

    @Query("DELETE FROM groupChats")
    suspend fun deleteChats()

    @Query("SELECT * FROM groups")
    suspend fun getGroups(): List<GroupEntity>

    @Query("DELETE FROM groups")
    suspend fun deleteGroups()

    @Query("SELECT * FROM userChats")
    suspend fun getMessages(): List<UserChatEntity>

    @Query("DELETE FROM userChats")
    suspend fun deleteMessages()

    @Query("SELECT * FROM users")
    suspend fun getUsers(): List<UserEntity>

    @Query("DELETE FROM users")
    suspend fun deleteUsers()

    @Query("DELETE FROM accountDeletion")
    suspend fun deleteAccountDeletions()

    @Query("DELETE FROM userPreferences")
    suspend fun deleteUserPreferences()

    @Query("DELETE FROM userState")
    suspend fun deleteUserStates()

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("DELETE FROM moduleAnnouncements")
    suspend fun deleteAllModuleAnnouncements()

    @Query("DELETE FROM moduleAssignments")
    suspend fun deleteAllModuleAssignments()

    @Query("DELETE FROM moduleDetails")
    suspend fun deleteAllModuleDetails()

    @Query("DELETE FROM moduleTimetable")
    suspend fun deleteAllModuleTimetables()

    @Query("DELETE FROM modules")
    suspend fun deleteAllModules()

    @Query("SELECT * FROM modules")
    suspend fun getAllModules(): List<ModuleEntity>

    @Query("DELETE FROM groupChats")
    suspend fun deleteAllChats()

    @Query("DELETE FROM groups")
    suspend fun deleteAllGroups()

    @Query("DELETE FROM userChats")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM accountDeletion")
    suspend fun deleteAllAccountDeletions()

    @Query("DELETE FROM userPreferences")
    suspend fun deleteAllUserPreferences()

    @Query("DELETE FROM userState")
    suspend fun deleteAllUserStates()

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()

    @Query("Delete FROM academicYears")
    suspend fun deleteAllAcademicYears()

    @Query("Delete FROM courses")
    suspend fun deleteAllCourses()


    @Transaction
    suspend fun deleteAllTables() {
        deleteAllCourses()
        deleteAllAcademicYears()
        deleteAllAttendance()
        deleteAllUserStates()
        deleteAllUserPreferences()
        deleteAllAccountDeletions()
        deleteAllUsers()
        deleteAllMessages()
        deleteAllGroups()
        deleteAllChats()
        deleteAllModules()
        deleteAllModuleTimetables()
        deleteAllModuleDetails()
        deleteAllModuleAssignments()
        deleteAllModuleAnnouncements()
        deleteAllNotifications()
        deleteAnnouncements()
        deleteNotifications()
        deleteModuleAnnouncements()
        deleteModuleAssignments()
        deleteModuleDetails()
        deleteModuleTimetables()
        deleteAttendanceStates()
        deleteModules()
        deleteChats()
        deleteGroups()
        deleteMessages()
        deleteUsers()
        deleteAccountDeletions()
        deleteUserPreferences()
        deleteUserStates()
    }

    suspend fun loadCrucialData() {
        getUsers()
        getMessages()
        getChats()
        getGroups()
        getAllModules()
        getAllNotifications()
        getModuleTimetables()
        getNotifications()
        getAnnouncements()
        getModuleAssignments()
    }
}


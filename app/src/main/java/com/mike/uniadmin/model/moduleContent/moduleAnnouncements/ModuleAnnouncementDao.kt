package com.mike.uniadmin.model.moduleContent.moduleAnnouncements

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleAnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleAnnouncement(announcement: ModuleAnnouncement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleAnnouncements(moduleAnnouncements: List<ModuleAnnouncement>)

    @Query("DELETE FROM moduleAnnouncements WHERE moduleID = :moduleID")
    suspend fun clearAnnouncementsForModule(moduleID: String)

    @Query(
        "SELECT moduleAnnouncements.*, users.firstName AS authorName, users.profileImageLink AS profileImageLink " +
                "FROM moduleAnnouncements INNER JOIN users ON moduleAnnouncements.authorID = users.id " +
                "WHERE moduleAnnouncements.moduleID = :moduleID"
    )
     fun getModuleAnnouncements(moduleID: String): LiveData <List<ModuleAnnouncementsWithAuthor>>

    @Query("DELETE FROM moduleAnnouncements WHERE moduleID = :moduleID")
    suspend fun clearAnnouncements(moduleID: String)

}


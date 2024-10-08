package com.mike.uniadmin.model.announcements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val authorID: String = "",
){
    constructor(): this("", "", "", "", "")
}


data class AnnouncementsWithAuthor(
    val id: String,
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val authorID: String = "",
    val authorName: String = "",
    val profileImageLink: String = ""

){
    constructor(): this("", "", "", "", "", "", "")
}


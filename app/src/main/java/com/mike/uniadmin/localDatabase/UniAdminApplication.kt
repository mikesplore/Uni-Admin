package com.mike.uniadmin.localDatabase

import android.app.Application
import com.mike.uniadmin.backEnd.announcements.AnnouncementRepository
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncementRepository
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignmentRepository
import com.mike.uniadmin.backEnd.moduleContent.moduleDetails.ModuleDetailRepository
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetableRepository
import com.mike.uniadmin.backEnd.modules.ModuleRepository
import com.mike.uniadmin.backEnd.groupchat.GroupChatRepository
import com.mike.uniadmin.backEnd.notifications.NotificationRepository
import com.mike.uniadmin.backEnd.programs.CourseRepository
import com.mike.uniadmin.backEnd.userchat.UserGroupChatRepository
import com.mike.uniadmin.backEnd.users.UserRepository


class UniAdmin : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val chatRepository by lazy { GroupChatRepository(database.chatDao(), database.groupDao()) }
    val messageRepository by lazy { UserGroupChatRepository(database.messageDao()) }
    val userRepository by lazy { UserRepository(database.userDao(), database.userStateDao(), database.accountDeletionDao(), database.userPreferencesDao(), database.databaseDao()) }
    val announcementRepository by lazy { AnnouncementRepository(database.announcementsDao()) }
    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    val moduleRepository by lazy { ModuleRepository(database.moduleDao(), database.attendanceStateDao()) }
    val moduleAnnouncementRepository by lazy { ModuleAnnouncementRepository(database.moduleAnnouncementDao()) }
    val moduleAssignmentRepository by lazy { ModuleAssignmentRepository(database.moduleAssignmentDao()) }
    val moduleDetailRepository by lazy { ModuleDetailRepository(database.moduleDetailsDao()) }
    val moduleTimetableRepository by lazy { ModuleTimetableRepository(database.moduleTimetableDao()) }
    val courseRepository by lazy { CourseRepository(database.courseDao(), database.courseStateDao()) }

}
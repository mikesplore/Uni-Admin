package com.mike.uniadmin.dataModel.groupchat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mike.uniadmin.dataModel.userchat.MessageDao
import com.mike.uniadmin.dataModel.userchat.MessageEntity
import com.mike.uniadmin.dataModel.users.UserDao
import com.mike.uniadmin.dataModel.users.UserEntity

@Database(entities = [ChatEntity::class, GroupEntity::class, MessageEntity::class, UserEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun groupDao(): GroupDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uni_admin_database"
                ).addMigrations(MIGRATION_1_2) // Add this line
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}



val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE groups ADD COLUMN new_column TEXT")
    }
}
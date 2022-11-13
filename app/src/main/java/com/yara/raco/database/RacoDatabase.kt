package com.yara.raco.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yara.raco.database.file.FileDAO
import com.yara.raco.database.notice.NoticeDAO
import com.yara.raco.database.schedule.ScheduleDAO
import com.yara.raco.database.subject.SubjectDAO
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.Notice
import com.yara.raco.model.schedule.Schedule
import com.yara.raco.model.subject.Subject

@Database(
    entities = [Subject::class, Notice::class, File::class, Schedule::class],
    version = 2,
    exportSchema = false
)
abstract class RacoDatabase : RoomDatabase() {
    abstract val subjectDAO: SubjectDAO
    abstract val noticeDAO: NoticeDAO
    abstract val fileDAO: FileDAO
    abstract val scheduleDAO: ScheduleDAO

    companion object {
        @Volatile
        private var INSTANCE: RacoDatabase? = null

        fun getInstance(context: Context): RacoDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RacoDatabase::class.java,
                        "raco.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
package com.yara.raco.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yara.raco.database.evaluation.EvaluationDAO
import com.yara.raco.database.event.EventDAO
import com.yara.raco.database.exam.ExamDAO
import com.yara.raco.database.file.FileDAO
import com.yara.raco.database.grade.GradeDAO
import com.yara.raco.database.notice.NoticeDAO
import com.yara.raco.database.schedule.ScheduleDAO
import com.yara.raco.database.subject.SubjectDAO
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.event.Event
import com.yara.raco.model.exam.Exam
import com.yara.raco.model.files.File
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.notices.Notice
import com.yara.raco.model.schedule.Schedule
import com.yara.raco.model.subject.Subject

@Database(
    entities = [Subject::class, Notice::class, File::class, Schedule::class, Grade::class, Evaluation::class, Event::class, Exam::class],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8)
    ]
)
abstract class RacoDatabase : RoomDatabase() {
    abstract val subjectDAO: SubjectDAO
    abstract val noticeDAO: NoticeDAO
    abstract val fileDAO: FileDAO
    abstract val scheduleDAO: ScheduleDAO
    abstract val evaluationDAO: EvaluationDAO
    abstract val gradeDAO: GradeDAO
    abstract val eventDAO: EventDAO
    abstract val examDAO: ExamDAO

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
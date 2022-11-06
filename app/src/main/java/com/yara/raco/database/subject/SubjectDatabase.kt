package com.yara.raco.database.subject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yara.raco.model.subject.Subject

@Database(entities = [Subject::class], version = 8, exportSchema = false)
abstract class SubjectDatabase : RoomDatabase () {
    abstract val subjectDatabaseDAO: SubjectDatabaseDAO

    companion object {
        @Volatile
        private var INSTANCE: SubjectDatabase? = null

        fun getInstance(context: Context): SubjectDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SubjectDatabase::class.java,
                        "subjects.db"
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
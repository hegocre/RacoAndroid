package com.yara.raco.model.grades

import android.content.Context
import com.yara.raco.database.RacoDatabase
import com.yara.raco.model.subject.Subject

class GradeController private constructor(context: Context) {
    private val racoDatabase = RacoDatabase.getInstance(context)

    suspend fun addGrade(subject: Subject, grade: Grade) {

    }

    companion object {
        private var INSTANCE: GradeController? = null

        /**
         * Get the instance of [GradeController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): GradeController {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = GradeController(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
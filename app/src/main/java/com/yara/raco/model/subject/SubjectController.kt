package com.yara.raco.model.subject

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.database.subject.SubjectDatabase

class SubjectController private constructor(context: Context)  {
    private val subjectDatabase = SubjectDatabase.getInstance(context)
    private val apiController = ApiController.getInstance()

    suspend fun syncSubjects() {

    }

    companion object {
        private var INSTANCE: SubjectController? = null

        /**
         * Get the instance of [SubjectController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): SubjectController {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = SubjectController(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
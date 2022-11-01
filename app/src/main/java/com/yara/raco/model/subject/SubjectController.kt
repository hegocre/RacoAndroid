package com.yara.raco.model.subject

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.database.subject.SubjectDatabase
import com.yara.raco.api.Result

class SubjectController private constructor(context: Context)  {
    private val subjectDatabase = SubjectDatabase.getInstance(context)
    private val apiController = ApiController.getInstance()

    suspend fun syncSubjects() {
        val result = apiController.listSubjects()
        if (result is Result.Success) {
            val savedSubjectSet = subjectDatabase.subjectDatabaseDAO.fetchAllSubjectIds().toHashSet()
            for (subject in result.data) {
                if (!savedSubjectSet.contains(subject.id)) {
                    subjectDatabase.subjectDatabaseDAO.insertSubject(subject)
                }
                savedSubjectSet.remove(subject.id)
            }
            for (id in savedSubjectSet) {
                subjectDatabase.subjectDatabaseDAO.deleteSubject(id)
            }
        }
    }

    fun getSubjects() = subjectDatabase.subjectDatabaseDAO.fetchAllSubjects()

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
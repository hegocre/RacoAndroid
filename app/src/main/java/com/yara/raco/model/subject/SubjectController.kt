package com.yara.raco.model.subject

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.database.RacoDatabase
import com.yara.raco.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubjectController private constructor(context: Context)  {
    private val racoDatabase = RacoDatabase.getInstance(context)
    private val apiController = ApiController.getInstance()

    suspend fun syncSubjects() {
        withContext(Dispatchers.IO) {
            val result = apiController.listSubjects()
            if (result is Result.Success) {
                val savedSubjectSet = racoDatabase.subjectDAO.fetchAllSubjectIds().toHashSet()
                for (subject in result.data) {
                    if (!savedSubjectSet.contains(subject.id)) {
                        racoDatabase.subjectDAO.insertSubject(subject)
                    }
                    savedSubjectSet.remove(subject.id)
                }
                for (id in savedSubjectSet) {
                    racoDatabase.subjectDAO.deleteSubject(id)
                }
            }
        }
    }

    suspend fun deleteAllSubjects() {
        withContext(Dispatchers.IO) {
            racoDatabase.subjectDAO.deleteAllSubjects()
        }
    }

    fun getSubjects() = racoDatabase.subjectDAO.fetchAllSubjects()

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
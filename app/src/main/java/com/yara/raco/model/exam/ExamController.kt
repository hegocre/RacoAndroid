package com.yara.raco.model.exam

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.database.RacoDatabase
import com.yara.raco.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExamController private constructor(context: Context) {
    private val racoDatabase = RacoDatabase.getInstance(context)
    private val apiController = ApiController.getInstance()

    suspend fun syncExams() {
        withContext(Dispatchers.IO) {
            val subjectIds = racoDatabase.subjectDAO.fetchAllSubjectIds()
            val result = apiController.listExams(subjectIds)
            if (result is Result.Success) {
                val savedExamSet = racoDatabase.examDAO.fetchAllExamsIds().toHashSet()
                for (exam in result.data) {
                    if (!savedExamSet.contains(exam.id) ||
                        racoDatabase.examDAO.getExamStartDate(exam.id) != exam.inici
                    ) {
                        racoDatabase.examDAO.insertExam(exam)
                    }
                    savedExamSet.remove(exam.id)
                }
                for (id in savedExamSet) {
                    racoDatabase.examDAO.deleteExam(id)
                }
            }
        }
    }

    fun getExams() = racoDatabase.examDAO.fetchAllExams()

    companion object {
        private var INSTANCE: ExamController? = null

        /**
         * Get the instance of [ExamController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): ExamController {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = ExamController(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
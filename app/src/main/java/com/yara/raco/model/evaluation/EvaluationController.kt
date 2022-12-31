package com.yara.raco.model.evaluation

import android.content.Context
import com.yara.raco.database.RacoDatabase
import com.yara.raco.model.grade.Grade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EvaluationController private constructor(context: Context) {

    private val racoDatabase = RacoDatabase.getInstance(context)

    fun getEvaluations() = racoDatabase.evaluationDAO.getEvaluations()

    suspend fun addEvaluation(subjectId: String, evaluationName: String = "") {
        val evaluation = Evaluation(
            id = 0,
            subjectId = subjectId,
            name = evaluationName,
            listOfGrade = arrayListOf()
        )
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.insertEvaluation(evaluation)
        }
    }

    suspend fun getEvaluationWithGrades(id: Int) =
        racoDatabase.evaluationDAO.getEvaluationWithGrades(id)

    fun getLiveEvaluationWithGrades(id: Int) =
        racoDatabase.evaluationDAO.getLiveEvaluationWithGrades(id)

    suspend fun deleteEvaluation(evaluationId: Int) {
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.deleteEvaluation(evaluationId)
        }
    }

    suspend fun saveEvaluation(evaluationWithGrades: EvaluationWithGrades) {
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.insertEvaluation(evaluationWithGrades.evaluation)
            racoDatabase.gradeDAO.deleteEvaluationGrades(evaluationWithGrades.evaluation.id)
            for (grade in evaluationWithGrades.listOfGrade) {
                racoDatabase.gradeDAO.insertGrade(grade)
            }
        }
    }

    suspend fun updateGrade(grade: Grade) {
        withContext(Dispatchers.IO) {
            racoDatabase.gradeDAO.insertGrade(grade)
        }
    }

    suspend fun deleteAllEvaluations() {
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.deleteAllEvaluations()
            racoDatabase.gradeDAO.deleteAllGrades()
        }
    }

    companion object {
        private var INSTANCE: EvaluationController? = null

        /**
         * Get the instance of [EvaluationController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): EvaluationController {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = EvaluationController(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
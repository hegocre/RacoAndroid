package com.yara.raco.model.evaluation

import android.content.Context
import com.yara.raco.database.RacoDatabase
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

    suspend fun deleteEvaluation(evaluationId: Int) {
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.deleteEvaluation(evaluationId)
        }
    }

    suspend fun evaluationSave(evaluationWithGrade: EvaluationWithGrade) {
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.insertEvaluation(evaluationWithGrade.evaluation)
            racoDatabase.gradeDAO.deleteEvaluationGrades(evaluationWithGrade.evaluation.id)
            for (grade in evaluationWithGrade.listOfGrade) {
                racoDatabase.gradeDAO.insertGrade(grade)
            }
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
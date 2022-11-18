package com.yara.raco.model.evaluation

import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import com.yara.raco.database.RacoDatabase
import com.yara.raco.model.grade.Grade

class EvaluationController private constructor(context: Context) {

    private val racoDatabase = RacoDatabase.getInstance(context)

    fun getEvaluations() = racoDatabase.evaluationDAO.getEvaluations()

    suspend fun getEvaluation(id: Int) = racoDatabase.evaluationDAO.getEvaluation(id)

    suspend fun getEvaluationIds() = racoDatabase.evaluationDAO.getEvaluationIds()

    suspend fun insertEvaluation(evaluation: Evaluation) =
        racoDatabase.evaluationDAO.insertEvaluation(evaluation)

    suspend fun deleteEvaluation(evaluation: Evaluation) =
        racoDatabase.evaluationDAO.deleteEvaluation(evaluation)

    suspend fun deleteEvaluation(id: Int) = racoDatabase.evaluationDAO.deleteEvaluation(id)

    suspend fun deleteAllEvaluations() = racoDatabase.evaluationDAO.deleteAllEvaluations()

    suspend fun addGradeToEvaluation(grade: Grade, evaluation: Evaluation) {
        evaluation.listOfGrade.add(grade)
        insertEvaluation(evaluation)
    }

    suspend fun removeGradeFromEvaluation(grade: Grade, evaluation: Evaluation) {
        evaluation.listOfGrade.remove(grade)
        insertEvaluation(evaluation)
    }

    suspend fun updateMarkFromGradeInEvaluation(
        gradeId: String,
        evaluation: Evaluation,
        mark: Double
    ) {
        evaluation.listOfGrade.find { it.id == gradeId }?.mark = mark
        insertEvaluation(evaluation)
    }

    suspend fun updateWeightFromGradeInEvaluation(
        gradeId: String,
        evaluation: Evaluation,
        weight: Double
    ) {
        evaluation.listOfGrade.find { it.id == gradeId }?.weight = weight
        insertEvaluation(evaluation)
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
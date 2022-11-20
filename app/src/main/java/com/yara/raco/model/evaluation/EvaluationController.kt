package com.yara.raco.model.evaluation

import android.content.Context
import com.yara.raco.database.RacoDatabase
import com.yara.raco.model.grade.Grade
import kotlinx.coroutines.launch

class EvaluationController private constructor(context: Context) {

    private val racoDatabase = RacoDatabase.getInstance(context)

    fun getEvaluations() = racoDatabase.evaluationDAO.getEvaluations()

    fun getEvaluation(id: Int) = racoDatabase.evaluationDAO.getEvaluation(id)

    fun getEvaluationIds() = racoDatabase.evaluationDAO.getEvaluationIds()

    fun addEvaluation(subjectId: String) {
        var ids = getEvaluationIds()
        var evaluation = Evaluation(
            id = (ids.last() + 1),
            subjectId = subjectId,
            name = "",
            listOfGrade = ArrayList()
        )
        racoDatabase.evaluationDAO.insertEvaluation(evaluation)
    }

    fun deleteEvaluation(evaluation: Evaluation) =
        racoDatabase.evaluationDAO.deleteEvaluation(evaluation)

    fun deleteEvaluation(id: Int) = racoDatabase.evaluationDAO.deleteEvaluation(id)

    fun deleteAllEvaluations() = racoDatabase.evaluationDAO.deleteAllEvaluations()

    fun addOrUpdateGradeToEvaluation(grade: Grade, evaluation: Evaluation) {
        evaluation.listOfGrade.add(grade)
        //insertEvaluation(evaluation)
    }

    fun removeGradeFromEvaluation(grade: Grade, evaluation: Evaluation) {
        evaluation.listOfGrade.remove(grade)
        //insertEvaluation(evaluation)
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
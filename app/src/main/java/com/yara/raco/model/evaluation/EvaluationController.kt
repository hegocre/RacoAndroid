package com.yara.raco.model.evaluation

import android.content.Context
import androidx.lifecycle.LiveData
import com.yara.raco.database.RacoDatabase
import com.yara.raco.model.grade.Grade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EvaluationController private constructor(context: Context) {

    private val racoDatabase = RacoDatabase.getInstance(context)

    fun getEvaluations() = racoDatabase.evaluationDAO.getEvaluations()

    suspend fun addEvaluation(subjectId: String) {
        var evaluation = Evaluation(
            id = 0,
            subjectId = subjectId,
            name = "",
            listOfGrade = arrayListOf()
        )
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.insertEvaluation(evaluation)
        }
    }

    suspend fun addGradeToEvaluation(evaluationId: Int) {
        var grade = Grade(
            id = 0,
            gradesId = evaluationId,
            name = "",
            mark = -1.0,
            weight = -1.0
        )
        withContext(Dispatchers.IO) {
            racoDatabase.gradeDAO.insertGrade(grade)
        }
    }

    suspend fun deleteGrade(gradeId: Int) {
        withContext(Dispatchers.IO) {
            racoDatabase.gradeDAO.deleteGrade(gradeId)
        }
    }

    suspend fun deleteEvaluation(evaluationId: Int) {
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.deleteEvaluation(evaluationId)
        }
    }

    suspend fun evaluationSave(evaluation: Evaluation) {
        withContext(Dispatchers.IO) {
            racoDatabase.evaluationDAO.insertEvaluation(evaluation)
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
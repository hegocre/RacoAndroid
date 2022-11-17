package com.yara.raco.model.evaluation

import android.content.Context
import com.yara.raco.database.RacoDatabase

class EvaluationController private constructor(context: Context) {

    private val racoDatabase = RacoDatabase.getInstance(context)

    fun getEvaluations() = racoDatabase.evaluationDAO.getEvaluations()

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
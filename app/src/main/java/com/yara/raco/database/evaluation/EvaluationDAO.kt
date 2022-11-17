package com.yara.raco.database.evaluation

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.evaluation.EvaluationWithGrade

@Dao
interface EvaluationDAO {

    @Query("SELECT * FROM evaluation")
    fun getEvaluations(): List<EvaluationWithGrade>
}
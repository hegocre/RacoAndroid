package com.yara.raco.database.evaluation

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.evaluation.EvaluationWithGrade

@Dao
interface EvaluationDAO {

    @Transaction
    @Query("SELECT * FROM evaluation")
    fun getEvaluations(): LiveData<List<EvaluationWithGrade>>

    @Query("SELECT * FROM evaluation WHERE id = :id")
    fun getEvaluation(id: Int): Evaluation

    @Query("SELECT id FROM evaluation")
    fun getEvaluationIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvaluation(evaluation: Evaluation)

    @Delete
    fun deleteEvaluation(evaluation: Evaluation)

    @Query("DELETE FROM evaluation WHERE id = :id")
    fun deleteEvaluation(id: Int)

    @Query("DELETE FROM evaluation")
    fun deleteAllEvaluations()
}
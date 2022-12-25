package com.yara.raco.database.evaluation

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yara.raco.model.evaluation.Evaluation
import com.yara.raco.model.evaluation.EvaluationWithGrades

@Dao
interface EvaluationDAO {

    @Transaction
    @Query("SELECT * FROM evaluation")
    fun getEvaluations(): LiveData<List<EvaluationWithGrades>>

    @Transaction
    @Query("SELECT * FROM evaluation WHERE id = :id")
    suspend fun getEvaluationWithGrades(id: Int): EvaluationWithGrades?

    @Transaction
    @Query("SELECT * FROM evaluation WHERE id = :id")
    fun getLiveEvaluationWithGrades(id: Int): LiveData<EvaluationWithGrades?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: Evaluation)

    @Query("DELETE FROM evaluation WHERE id = :id")
    suspend fun deleteEvaluation(id: Int)

    @Query("DELETE FROM evaluation")
    suspend fun deleteAllEvaluations()
}
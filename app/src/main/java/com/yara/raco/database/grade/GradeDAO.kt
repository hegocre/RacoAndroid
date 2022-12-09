package com.yara.raco.database.grade

import androidx.room.*
import com.yara.raco.model.grade.Grade

@Dao
interface GradeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade)

    @Query("DELETE FROM grade WHERE evaluationId = :evaluationId")
    suspend fun deleteEvaluationGrades(evaluationId: Int)

    @Query("DELETE FROM grade")
    suspend fun deleteAllGrades()
}
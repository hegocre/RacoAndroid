package com.yara.raco.database.grade

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.yara.raco.model.grade.Grade

interface GradeDAO {

    @Transaction
    @Query("SELECT * FROM grade")
    fun getGrades(): LiveData<List<Grade>>

    @Query("SELECT * FROM grade WHERE id = :id")
    suspend fun getGrade(id: Int): Grade

    @Query("SELECT id FROM grade")
    suspend fun getGradeIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade)

    @Delete
    suspend fun deleteGrade(grade: Grade)

    @Query("DELETE FROM grade WHERE id = :id")
    suspend fun deleteGrade(id: Int)

    @Query("DELETE FROM grade")
    suspend fun deleteAllGrades()
}
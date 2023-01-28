package com.yara.raco.database.exam

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yara.raco.model.exam.Exam

@Dao
interface ExamDAO {
    @Query("SELECT * FROM exams")
    fun fetchAllExams(): LiveData<List<Exam>>

    @Query("SELECT id FROM exams")
    suspend fun fetchAllExamsIds(): List<Int>

    @Query("SELECT inici FROM exams WHERE id = :id")
    suspend fun getExamStartDate(id: Int): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExam(id: Int)
}
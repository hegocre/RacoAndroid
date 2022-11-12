package com.yara.raco.database.grades

import androidx.room.*
import com.yara.raco.model.grades.Grades

@Dao
interface GradesDAO {
    @Query("SELECT * FROM grades WHERE subject = :subject")
    suspend fun getGrades(subject: String): Grades

}
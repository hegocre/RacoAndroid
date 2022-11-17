package com.yara.raco.database.grade

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yara.raco.model.grade.Grade

interface GradeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGrade(grade: Grade)

    @Query("DELETE from evaluation WHERE id = :id")
    fun deleteSubjectGrade(id: Int)
}
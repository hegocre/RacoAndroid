package com.yara.raco.database.subject

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yara.raco.model.subject.Subject

@Dao
interface SubjectDAO {
    @Query("SELECT * FROM subjects")
    fun fetchAllSubjects(): LiveData<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun fetchSubject(id: String): Subject

    @Query("SELECT id FROM subjects")
    suspend fun fetchAllSubjectIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: String)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

}
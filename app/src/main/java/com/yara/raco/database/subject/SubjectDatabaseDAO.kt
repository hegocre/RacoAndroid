package com.yara.raco.database.subject

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yara.raco.data.subject.Subject

@Dao
interface SubjectDatabaseDAO {
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
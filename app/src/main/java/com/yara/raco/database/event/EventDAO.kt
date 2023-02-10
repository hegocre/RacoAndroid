package com.yara.raco.database.event

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yara.raco.model.event.Event

@Dao
interface EventDAO {
    data class EventPrimaryKey(
        @ColumnInfo(name = "nom") val nom: String,
        @ColumnInfo(name = "inici") val inici: String
    )

    @Query("SELECT * FROM events")
    fun fetchAllEvents(): LiveData<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Query("SELECT nom, inici FROM events")
    suspend fun fetchAllEventPrimaryKeys(): List<EventPrimaryKey>

    @Query("DELETE FROM events WHERE nom = :nom and inici = :inici")
    suspend fun deleteEvent(nom: String, inici: String)
}
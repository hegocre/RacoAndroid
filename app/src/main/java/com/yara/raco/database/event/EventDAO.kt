package com.yara.raco.database.event

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.yara.raco.model.event.Event

@Dao
interface EventDAO {
    @Query("SELECT * FROM events")
    fun fetchAllEvents(): LiveData<List<Event>>
}
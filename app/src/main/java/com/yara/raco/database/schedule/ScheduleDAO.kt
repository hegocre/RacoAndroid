package com.yara.raco.database.schedule

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yara.raco.model.schedule.Schedule

@Dao
interface ScheduleDAO {
    @Query("SELECT * FROM schedule")
    fun fetchAllSchedules(): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedule WHERE codiAssig = :codiAssig and diaSetmana = :diaSetmana and inici = :inici")
    suspend fun fetchSchedule(codiAssig: String, diaSetmana: Int, inici: Int): Schedule

    data class SchedulePrimaryKey(
        @ColumnInfo(name = "codiAssig") val codiAssig: String,
        @ColumnInfo(name = "diaSetmana") val diaSetmana: Int,
        @ColumnInfo(name = "inici") val inici: String
    )

    @Query("SELECT codiAssig, diaSetmana, inici FROM schedule")
    suspend fun fetchAllSchedulePrimaryKeys(): List<SchedulePrimaryKey>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(subject: Schedule)

    @Delete
    suspend fun deleteSchedule(subject: Schedule)

    @Query("DELETE FROM schedule WHERE codiAssig = :codiAssig and diaSetmana = :diaSetmana and inici = :inici")
    suspend fun deleteSchedule(codiAssig: String, diaSetmana: Int, inici: String)

    @Query("DELETE FROM schedule")
    suspend fun deleteAllSchedules()

}
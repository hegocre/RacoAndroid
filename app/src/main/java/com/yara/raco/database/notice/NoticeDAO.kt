package com.yara.raco.database.notice

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yara.raco.model.notices.Notice
import com.yara.raco.model.notices.NoticeWithFiles

@Dao
interface NoticeDAO {
    @Transaction
    @Query("SELECT * FROM notices")
    fun fetchAllNotices(): LiveData<List<NoticeWithFiles>>

    @Transaction
    @Query("SELECT * FROM notices WHERE id = :id")
    suspend fun fetchNoticeWithFiles(id: Int): NoticeWithFiles?

    @Query("SELECT * FROM notices WHERE id = :id")
    suspend fun fetchNotice(id: Int): Notice

    @Query("SELECT id FROM notices")
    suspend fun fetchAllNoticeIds(): List<Int>

    @Query("SELECT dataModificacio FROM notices WHERE id = :id")
    suspend fun getNoticeModificationDate(id: Int): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice)

    @Delete
    suspend fun deleteNotice(notice: Notice)

    @Query("UPDATE notices SET llegit = true WHERE id = :id")
    suspend fun setReadNotice(id: Int)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Int)

    @Query("DELETE FROM notices")
    suspend fun deleteAllNotices()
}
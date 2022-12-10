package com.yara.raco.database.file

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yara.raco.model.files.File

@Dao
interface FileDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFile(file: File)

    @Query("DELETE from files WHERE noticeId = :id")
    fun deleteNoticeFiles(id: Int)

    @Query("DELETE from files")
    fun deleteAllFiles()
}
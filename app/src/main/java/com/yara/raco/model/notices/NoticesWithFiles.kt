package com.yara.raco.model.notices

import androidx.room.Embedded
import androidx.room.Relation
import com.yara.raco.model.files.File

data class NoticesWithFiles(
    @Embedded val notice: Notice,
    @Relation(
        parentColumn = "id",
        entityColumn = "noticeId"
    )
    val files: List<File>
)

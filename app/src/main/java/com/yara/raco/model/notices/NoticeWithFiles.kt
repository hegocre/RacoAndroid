package com.yara.raco.model.notices

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.room.Embedded
import androidx.room.Relation
import com.yara.raco.model.files.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class NoticeWithFiles(
    @Embedded val notice: Notice,
    @Relation(
        parentColumn = "id",
        entityColumn = "noticeId"
    )
    val files: List<File>
) {
    companion object {
        val Saver: Saver<MutableState<NoticeWithFiles?>, *> = listSaver(
            save = { listOf(Json.encodeToString(it.value)) },
            restore = { mutableStateOf(Json.decodeFromString(it[0])) }
        )
    }
}

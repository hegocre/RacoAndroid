package com.yara.raco.model.evaluation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.room.Embedded
import androidx.room.Relation
import com.yara.raco.model.grade.Grade
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class EvaluationWithGrade(
    @Embedded val evaluation: Evaluation,
    @Relation(
        parentColumn = "id",
        entityColumn = "evaluationId"
    )
    val listOfGrade: List<Grade>
) {
    companion object {
        val Saver: Saver<MutableState<EvaluationWithGrade?>, *> = listSaver(
            save = { listOf(Json.encodeToString(it.value)) },
            restore = { mutableStateOf(Json.decodeFromString(it[0])) }
        )
    }
}

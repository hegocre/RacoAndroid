package com.yara.raco.model.evaluation

import androidx.room.Embedded
import androidx.room.Relation
import com.yara.raco.model.grade.Grade

@kotlinx.serialization.Serializable
data class EvaluationWithGrades(
    @Embedded val evaluation: Evaluation,
    @Relation(
        parentColumn = "id",
        entityColumn = "evaluationId"
    )
    val listOfGrade: List<Grade>
) {
    fun getFinalMark(): Double {
        var mark = 0.0
        for (grade in listOfGrade) {
            grade.mark?.let {
                mark += it * (grade.weight / 100)
            }
        }
        return mark
    }

    fun getPassMark(): Double {
        var remainingWeight = 0.0
        for (grade in listOfGrade) {
            if (grade.mark == null) {
                remainingWeight += grade.weight
            }
        }
        val currentMark = getFinalMark()

        return ((5.0 - currentMark) / (remainingWeight / 100)).coerceIn(0.0, 10.0)
    }
}

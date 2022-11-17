package com.yara.raco.model.evaluation

import androidx.room.Embedded
import androidx.room.Relation
import com.yara.raco.model.grade.Grade

data class EvaluationWithGrade(
    @Embedded val evaluation: Evaluation,
    @Relation(
        parentColumn = "id",
        entityColumn = "gradesId"
    )
    val listOfGrade: List<Grade>
)

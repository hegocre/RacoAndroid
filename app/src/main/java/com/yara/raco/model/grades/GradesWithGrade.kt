package com.yara.raco.model.grades

import androidx.room.Embedded
import androidx.room.Relation
import com.yara.raco.model.files.File
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.notices.Notice

data class GradesWithGrade(
    @Embedded val grades: Grades,
    @Relation(
        parentColumn = "id",
        entityColumn = "gradesId"
    )
    val listOfGrade: List<Grade>
)

package com.yara.raco.model.evaluation

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.yara.raco.model.grade.Grade

/**
 * Class that represents a grade.
 *
 * @param id Id of the evaluation
 * @param subjectId Subject instance.
 * @param name Name of the evaluation
 * @param listOfGrade List of evaluation.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "evaluation")
data class Evaluation(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var subjectId: String,
    var name: String,
    @Ignore
    val listOfGrade: ArrayList<Grade> = arrayListOf()
) {
    // Constructor for ksp to not fail building
    constructor(id: Int, subjectId: String, name: String) : this(id, subjectId, name, arrayListOf())
}
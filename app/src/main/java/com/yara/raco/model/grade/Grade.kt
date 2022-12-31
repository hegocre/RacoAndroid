package com.yara.raco.model.grade

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class that represents a grade.
 *
 * @param name Name of the grade.
 * @param weight Weight of the grade.
 * @param mark Mark of the grade.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "grade")
data class Grade(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val weight: Double,
    val mark: Double?,
    val evaluationId: Int
)
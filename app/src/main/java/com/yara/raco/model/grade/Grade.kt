package com.yara.raco.model.grade

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class that represents a grade.
 *
 * @param name Name of the grade.
 * @param weight Weight of the grade.
 * @param mark Mark of the grade.
 * @param description Description of the grade.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "grade")
data class Grade @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var weight: Double,
    var mark: Double?,
    var description: String = "",
    var evaluationId: Int
)
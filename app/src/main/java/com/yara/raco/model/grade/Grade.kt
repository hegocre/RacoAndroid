package com.yara.raco.model.grade

import androidx.room.Entity
import kotlinx.serialization.SerialName

/**
 * Class that represents a grade.
 *
 * @param name Name of the grade.
 * @param weight Weight of the grade.
 * @param mark Mark of the grade.
 * @param description Description of the grade.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "grade", primaryKeys = ["id"])
data class Grade @JvmOverloads constructor(
    @SerialName("id")
    val id: String,
    @SerialName("Name")
    val name: String,
    @SerialName("weight")
    var weight: Double,
    @SerialName("Mark")
    var mark: Double,
    @SerialName("Description")
    val description: String = "",
    val gradesId: Int = 0
)
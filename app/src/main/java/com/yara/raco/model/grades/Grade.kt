package com.yara.raco.model.grades

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
    val weight: Double,
    @SerialName("Mark")
    val mark: Double,
    @SerialName("Description")
    val description: String? = null,
)
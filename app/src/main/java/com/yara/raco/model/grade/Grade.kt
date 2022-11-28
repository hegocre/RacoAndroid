package com.yara.raco.model.grade

import androidx.room.Entity
import androidx.room.PrimaryKey
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
@Entity(tableName = "grade")
data class Grade @JvmOverloads constructor(
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @SerialName("Name")
    var name: String,
    @SerialName("weight")
    var weight: Double,
    @SerialName("Mark")
    var mark: Double,
    @SerialName("Description")
    var description: String = "",
    var gradesId: Int = 0
)
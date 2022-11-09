package com.yara.raco.model.files

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

/**
 * Class that represents a file.
 *
 * @param tipusMime Mime type.
 * @param nom Name of the file.
 * @param url API URL of the file.
 * @param dataModificacio Date of modification.
 * @param mida Size of the file.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "files", primaryKeys = ["url"])
data class File(
    @SerialName("tipus_mime")
    val tipusMime: String,
    @SerialName("nom")
    val nom: String,
    @SerialName("url")
    val url: String,
    @SerialName("data_modificacio")
    val dataModificacio: String,
    @SerialName("mida")
    val mida: Int,
    @Transient
    val noticeId: Int = 0
)
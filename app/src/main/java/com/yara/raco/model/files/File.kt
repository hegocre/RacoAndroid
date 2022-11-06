package com.yara.raco.model.files

import androidx.room.Entity
import com.yara.raco.model.notices.Notice

/**
 * Class that represents a file.
 *
 * @param id Id of the file.
 * @param avis Notice that contains the file.
 * @param tipus_mime Mime type.
 * @param nom Name of the file.
 * @param url API URL of the file.
 * @param data_modificacio Date of modification.
 * @param mida Size of the file.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "files", primaryKeys = ["id"])
data class File (
    val id: String,
    val avis: Notice,
    val tipus_mime: String,
    val nom: String,
    val url: String,
    val data_modificacio: String,
    val mida: Int
)
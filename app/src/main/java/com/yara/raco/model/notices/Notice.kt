package com.yara.raco.model.notices

import androidx.room.Entity
import com.yara.raco.model.files.File

/**
 * Class that represents a notice.
 *
 * @param id Id of the notice.
 * @param titol Title of the notice.
 * @param codi_assig Acronym of the subject.
 * @param text Body of the notice.
 * @param data_insercio Date of creation.
 * @param data_modificacio Date of modification.
 * @param data_caducitat Date of expiration.
 * @param adjunts Files attached to the notice.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "notices", primaryKeys = ["id"])
data class Notice (
    val id: String,
    val titol: String,
    val codi_assig: String,
    val text: String,
    val data_insercio: String,
    val data_modificacio: String,
    val data_caducitat: String,
    val adjunts: ArrayList<File>
)
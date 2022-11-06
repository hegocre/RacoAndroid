package com.yara.raco.model.notices

import androidx.room.Entity
import com.yara.raco.model.files.File
import kotlinx.serialization.SerialName

/**
 * Class that represents a notice.
 *
 * @param id Id of the notice.
 * @param titol Title of the notice.
 * @param codiAssig Acronym of the subject.
 * @param text Body of the notice.
 * @param dataInsercio Date of creation.
 * @param dataModificacio Date of modification.
 * @param dataCaducitat Date of expiration.
 * @param adjunts List of id from files attached to the notice.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "notices", primaryKeys = ["id"])
data class Notice (
    @SerialName("id")
    val id: String,
    @SerialName("titol")
    val titol: String,
    @SerialName("codi_assig")
    val codiAssig: String,
    @SerialName("text")
    val text: String,
    @SerialName("data_insercio")
    val dataInsercio: String,
    @SerialName("data_modificacio")
    val dataModificacio: String,
    @SerialName("data_caducitat")
    val dataCaducitat: String,
    @SerialName("adjunts")
    val adjunts: ArrayList<File>
)
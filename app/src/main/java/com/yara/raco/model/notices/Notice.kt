package com.yara.raco.model.notices

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.yara.raco.model.files.File
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

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
data class Notice(
    @SerialName("id")
    val id: Int,
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
    @Ignore
    val adjunts: ArrayList<File> = arrayListOf(),
    @Transient
    @ColumnInfo(defaultValue = "1")
    var llegit: Boolean = false
) {
    // Constructor for ksp to not fail building
    @Suppress("unused")
    constructor(
        id: Int,
        titol: String,
        codiAssig: String,
        text: String,
        dataInsercio: String,
        dataModificacio: String,
        dataCaducitat: String,
        llegit: Boolean
    ) :
            this(
                id,
                titol,
                codiAssig,
                text,
                dataInsercio,
                dataModificacio,
                dataCaducitat,
                arrayListOf(),
                llegit
            )
}
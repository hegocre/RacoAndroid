package com.yara.raco.model.subject

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class that represents a subject.
 *
 * @param id Id of the subject.
 * @param url API URL for gathering more detailed information.
 * @param guia Url of the subject guide.
 * @param grup Group number.
 * @param sigles Acronym of the subject.
 * @param codiUpc Subject code.
 * @param semestre Semester of the subject.
 * @param credits Credits of the subject.
 * @param vigent Whether the subject is active or not.
 * @param nom Name of the subject.
 */
@Serializable
@Entity(tableName = "subjects", primaryKeys = ["id"])
data class Subject (
    @SerialName("id")
    val id: String,
    @SerialName("url")
    val url: String,
    @SerialName("guia")
    val guia: String?,
    @SerialName("grup")
    val grup: String,
    @SerialName("sigles")
    val sigles: String,
    @SerialName("codi_upc")
    val codiUpc: Int,
    @SerialName("semestre")
    val semestre: String?,
    @SerialName("credits")
    val credits: Double,
    @SerialName("vigent")
    val vigent: String,
    @SerialName("nom")
    val nom: String)

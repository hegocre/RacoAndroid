package com.yara.raco.model.subject

import androidx.room.Entity
import kotlinx.serialization.Serializable

/**
 * Class that represents a subject.
 *
 * @param id Id of the subject.
 * @param url API URL for gathering more detailed information.
 * @param guia Url of the subject guide.
 * @param grup Group number.
 * @param sigles Acronym of the subject.
 * @param codi_upc Subject code.
 * @param semestre Semester of the subject.
 * @param credits Credits of the subject.
 * @param vigent Whether the subject is active or not.
 * @param nom Name of the subject.
 */
@Serializable
@Entity(tableName = "subjects", primaryKeys = ["id"])
data class Subject (
    val id: String,
    val url: String,
    val guia: String,
    val grup: String,
    val sigles: String,
    val codi_upc: Int,
    val semestre: String?,
    val credits: Double,
    val vigent: String,
    val nom: String)

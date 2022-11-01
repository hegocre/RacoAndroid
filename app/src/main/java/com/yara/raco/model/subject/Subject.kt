package com.yara.raco.model.subject

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
 * @param name Name of the subject.
 */
@Serializable
data class Subject (
    val id: String,
    val url: String,
    val guia: String,
    val grup: String,
    val sigles: String,
    val codi_upc: Int,
    val semestre: String?,
    val credits: Int,
    val vigent: Boolean,
    val name: String)

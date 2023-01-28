package com.yara.raco.model.event

import androidx.room.Entity

@kotlinx.serialization.Serializable
@Entity(tableName = "events", primaryKeys = ["nom", "inici"])
data class Event(
    val nom: String,
    val inici: String,
    val fi: String,
    val categoria: String
)
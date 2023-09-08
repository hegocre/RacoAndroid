package com.yara.raco.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

class Json {
    companion object {
        inline fun <reified T> decodeFromString(
            string: String
        ): T = json.decodeFromString(string)

        inline fun <reified T> encodeToString(
            value: T
        ): String = json.encodeToString(value)
    }
}
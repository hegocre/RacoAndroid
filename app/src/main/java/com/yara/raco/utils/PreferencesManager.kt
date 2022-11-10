package com.yara.raco.utils

import android.content.Context
import com.yara.raco.model.user.AccessToken
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PreferencesManager private constructor(context: Context) {
    private val _sharedPreferences = context.getSharedPreferences(
        "racoPreferences",
        Context.MODE_PRIVATE
    )

    fun getAccessToken(): AccessToken? = _sharedPreferences.getString("accessToken", null)?.let {
        Json.decodeFromString(it)
    }

    fun setAccessToken(accessToken: AccessToken?) =
        _sharedPreferences.edit()
            .putString("accessToken", accessToken?.let { Json.encodeToString(it) }).apply()

    fun getUserLastLanguage(): String = _sharedPreferences.getString("last_language", "_") ?: "_"

    fun setUserLastLanguage(language: String) = _sharedPreferences.edit()
        .putString("last_language", language).apply()

    companion object {
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            if (instance == null) instance = PreferencesManager(context)

            return instance as PreferencesManager
        }
    }
}
package com.yara.raco.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.yara.raco.model.user.AccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

class PreferencesManager private constructor(context: Context) {
    private val _sharedPreferences = context.getSharedPreferences(
        "racoPreferences",
        Context.MODE_PRIVATE
    )
    private val Context._dataStore by preferencesDataStore("preferences")
    private val _dataStore = context._dataStore

    fun getAccessToken(): AccessToken? = _sharedPreferences.getString("accessToken", null)?.let {
        Json.decodeFromString(it)
    }

    fun setAccessToken(accessToken: AccessToken?) =
        _sharedPreferences.edit()
            .putString("accessToken", accessToken?.let { Json.encodeToString(it) }).apply()

    fun getUserLastLanguage(): String = _sharedPreferences.getString("last_language", "_") ?: "_"
    fun setUserLastLanguage(language: String) = _sharedPreferences.edit()
        .putString("last_language", language).apply()

    fun getFirstTimeNotification(): Boolean =
        _sharedPreferences.getBoolean("first_time_notification", true)

    fun setFirstTimeNotification(isFirstTime: Boolean) = _sharedPreferences.edit()
        .putBoolean("first_time_notification", isFirstTime).apply()

    fun getLastStartedVersionCode(): Int =
        _sharedPreferences.getInt("last_started_version", -1)

    fun setLastStartedVersionCode(lastStartedVersion: Int) = _sharedPreferences.edit()
        .putInt("last_started_version", lastStartedVersion).apply()

    fun getLastRefreshWorkerStatusCode(): Int =
        _sharedPreferences.getInt("last_refresh_worker_token", ResultCode.UNKNOWN)

    fun setLastRefreshWorkerStatusCode(lastRefreshStatus: Int) = _sharedPreferences.edit()
        .putInt("last_refresh_worker_token", lastRefreshStatus).apply()

    fun getDayCalendarViewSelected(): Flow<Boolean> =
        _dataStore.getPreference(PreferenceKeys.DAY_CALENDAR_VIEW_SELECTED, false)

    suspend fun setDayCalendarViewSelected(selected: Boolean) =
        _dataStore.setPreference(PreferenceKeys.DAY_CALENDAR_VIEW_SELECTED, selected)

    fun getShowAllNoticesSelected(): Flow<Boolean> =
        _dataStore.getPreference(PreferenceKeys.SHOW_ALL_NOTICES_SELECTED, true)

    suspend fun setShowAllNoticesSelected(selected: Boolean) =
        _dataStore.setPreference(PreferenceKeys.SHOW_ALL_NOTICES_SELECTED, selected)

    private fun <T> DataStore<Preferences>.getPreference(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> =
        data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }

    private suspend fun <T> DataStore<Preferences>.setPreference(
        key: Preferences.Key<T>,
        value: T
    ) {
        edit { preferences ->
            preferences[key] = value
        }
    }

    companion object {
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            if (instance == null) instance = PreferencesManager(context)

            return instance as PreferencesManager
        }

        private object PreferenceKeys {
            val DAY_CALENDAR_VIEW_SELECTED = booleanPreferencesKey("day_calendar_view_selected")
            val SHOW_ALL_NOTICES_SELECTED = booleanPreferencesKey("show_all_notices_selected")
        }
    }
}
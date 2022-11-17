package com.yara.raco.model.user

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.api.Result
import com.yara.raco.model.subject.SubjectController
import com.yara.raco.utils.PreferencesManager
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.*
import java.util.*

class UserController private constructor(context: Context) {
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val apiController = ApiController.getInstance()
    private val subjectController = SubjectController.getInstance(context)
    val isLoggedIn: Boolean
        get() = apiController.accessToken != null

    init {
        apiController.accessToken = preferencesManager.getAccessToken()
        val lastLanguage = preferencesManager.getUserLastLanguage()
        val deviceLanguage = Locale.getDefault().language
        if (lastLanguage != deviceLanguage) {
            CoroutineScope(Dispatchers.IO).launch {
                //Delete databases which can be translated
                subjectController.deleteAllSubjects()

                //Update last language
                preferencesManager.setUserLastLanguage(deviceLanguage)
            }
        }
        if (deviceLanguage in listOf("ca", "es", "en")) {
            apiController.language = deviceLanguage
        } else {
            apiController.language = "ca"
        }
    }

    suspend fun logIn(authorizationCode: String): Boolean {
        val loginResult = apiController.login(authorizationCode)

        if (loginResult is Result.Success) {
            val accessToken = loginResult.data
            preferencesManager.setAccessToken(accessToken)

            val refreshJob = SupervisorJob()
            CoroutineScope(Dispatchers.IO + refreshJob).launch {
                delay((accessToken.expiresIn - 100) * 1000)
                refreshToken()
            }

            return true
        }

        return false
    }

    suspend fun refreshToken(): Int {
        val refreshResult = apiController.refreshToken()

        if (refreshResult is Result.Success) {
            val accessToken = refreshResult.data
            preferencesManager.setAccessToken(accessToken)

            val refreshJob = SupervisorJob()
            CoroutineScope(Dispatchers.IO + refreshJob).launch {
                delay((accessToken.expiresIn - 100) * 1000)
                refreshToken()
            }

            return ResultCode.SUCCESS
        }
        if (refreshResult is Result.Error) {
            return if (refreshResult.code == 400) {
                ResultCode.INVALID_TOKEN
            } else {
                val refreshJob = SupervisorJob()
                CoroutineScope(Dispatchers.IO + refreshJob).launch {
                    delay(10 * 1000)
                    refreshToken()
                }
                ResultCode.ERROR_API_BAD_RESPONSE
            }
        }
        return ResultCode.UNKNOWN
    }

    fun logOut() {
        preferencesManager.setAccessToken(null)
        apiController.accessToken = null
    }

    companion object {
        private var INSTANCE: UserController? = null

        fun getInstance(context: Context): UserController {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = UserController(context)
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
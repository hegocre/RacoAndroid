package com.yara.raco.model.user

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.api.Result
import com.yara.raco.utils.PreferencesManager
import kotlinx.coroutines.*

class UserController private constructor(context: Context) {
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val apiController = ApiController.getInstance()
    val isLoggedIn: Boolean
        get() = apiController.accessToken != null

    init {
        apiController.accessToken = preferencesManager.getAccessToken()
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

    suspend fun refreshToken(): Boolean {
        val refreshResult = apiController.refreshToken()

        if (refreshResult is Result.Success) {
            val accessToken = refreshResult.data
            preferencesManager.setAccessToken(accessToken)

            val refreshJob = SupervisorJob()
            CoroutineScope(Dispatchers.IO + refreshJob).launch {
                delay((accessToken.expiresIn - 100) * 1000)
                refreshToken()
            }

            return true
        }
        if (refreshResult is Result.Error) {
            val refreshJob = SupervisorJob()
            CoroutineScope(Dispatchers.IO + refreshJob).launch {
                delay(10 * 1000)
                refreshToken()
            }
        }

        return false
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
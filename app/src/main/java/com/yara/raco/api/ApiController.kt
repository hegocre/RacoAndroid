package com.yara.raco.api

import com.yara.raco.model.user.AccessToken

class ApiController private constructor() {
    var accessToken: AccessToken? = null
    private val tokenApi = TokenApi.getInstance()

    suspend fun login(authorizationCode: String): Result<AccessToken> {
        val loginResult = tokenApi.getToken(authorizationCode)

        if (loginResult is Result.Success) {
            accessToken = loginResult.data
        }

        return loginResult
    }

    suspend fun refreshToken(): Result<AccessToken> {
        accessToken?.let {
            val refreshResult = tokenApi.refreshToken(it.refreshToken)

            if (refreshResult is Result.Success) {
                accessToken = refreshResult.data
            }

            return refreshResult

        } ?: return Result.Error(2)
    }

    companion object {
        private var instance: ApiController? = null

        fun getInstance(): ApiController {
            synchronized(this) {
                var tempInstance = instance

                if (tempInstance == null) {
                    tempInstance = ApiController()
                    instance = tempInstance
                }

                return tempInstance
            }
        }
    }
}
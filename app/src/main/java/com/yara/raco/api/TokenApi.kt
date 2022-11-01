package com.yara.raco.api

import com.yara.raco.BuildConfig
import com.yara.raco.model.user.AccessToken
import com.yara.raco.utils.OkHttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class TokenApi private constructor() {

    /**
     * Sends a request with an authorizationCode to the api to get the access token.
     *
     * @return Result with an [AccessToken] if success, or with an error code otherwise.
     */
    suspend fun getToken(authorizationCode: String): Result<AccessToken> = try {
        val tokenResponse = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().post(
                sUrl = TOKEN_URL,
                body = String.format(TOKEN_BODY, authorizationCode),
                mediaType = "application/x-www-form-urlencoded"
            )
        }

        val code = tokenResponse.code
        val body = withContext(Dispatchers.IO) {
            tokenResponse.body?.string()
        }

        withContext(Dispatchers.IO) {
            tokenResponse.close()
        }

        if (code == 200 && body != null) {
            Result.Success(Json.decodeFromString(body))
        } else {
            Result.Error(0)
        }

    } catch (e: Exception) {
        Result.Error(1)
    }

    /**
     * Sends a request with a refreshToken to the api to get the access token.
     *
     * @return Result with an [AccessToken] if success, or with an error code otherwise.
     */
    suspend fun refreshToken(refreshToken: String): Result<AccessToken> = try {
        val tokenResponse = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().post(
                sUrl = REFRESH_URL,
                body = String.format(REFRESH_BODY, refreshToken),
                mediaType = "application/x-www-form-urlencoded"
            )
        }


        val code = tokenResponse.code
        val body = withContext(Dispatchers.IO) {
            tokenResponse.body?.string()
        }

        withContext(Dispatchers.IO) {
            tokenResponse.close()
        }

        if (code == 200 && body != null) {
            Result.Success(Json.decodeFromString(body))
        } else {
            Result.Error(0)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Result.Error(1)
    }

    companion object {
        private const val TOKEN_URL =
            "https://api.fib.upc.edu/v2/o/token/"
        private const val TOKEN_BODY =
            "grant_type=authorization_code" +
                    "&redirect_uri=apifib://yara" +
                    "&code=%s" +
                    "&client_id=${BuildConfig.CLIENT_ID}" +
                    "&client_secret=${BuildConfig.CLIENT_SECRET}"
        private const val REFRESH_URL =
            "https://api.fib.upc.edu/v2/o/token/"
        private const val REFRESH_BODY =
            "grant_type=refresh_token" +
                    "&refresh_token=%s" +
                    "&client_id=${BuildConfig.CLIENT_ID}" +
                    "&client_secret=${BuildConfig.CLIENT_SECRET}"

        private var instance: TokenApi? = null

        fun getInstance(): TokenApi {
            synchronized(this) {
                var tempInstance = instance

                if (tempInstance == null) {
                    tempInstance = TokenApi()
                    instance = tempInstance
                }

                return tempInstance
            }
        }
    }
}
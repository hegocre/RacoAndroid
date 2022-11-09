package com.yara.raco.api

import com.yara.raco.model.notices.Notice
import com.yara.raco.utils.Error
import com.yara.raco.utils.OkHttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NoticesApi private constructor() {

    suspend fun getNotices(accessToken: String): Result<List<Notice>> = try {
        val response = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().get(
                sUrl = NOTICE_URL,
                accessToken = accessToken,
            )
        }

        val statusCode = response.code
        val body = withContext(Dispatchers.IO) {
            response.body?.string()
        }

        withContext(Dispatchers.IO) {
            response.close()
        }

        if (statusCode == 200 && body != null) {
            val aux = Json.decodeFromString<NoticeResponse>(body).results
            Result.Success(aux)
        }
        else {
            Result.Error(Error.API_BAD_RESPONSE)
        }

    }
    catch (e: Exception) {
        e.printStackTrace()
        Result.Error(Error.API_BAD_REQUEST)
    }

    @Serializable
    data class NoticeResponse (
        val count: Int,
        val results: List<Notice>
    )

    companion object {
        private const val NOTICE_URL = "https://api.fib.upc.edu/v2/jo/avisos/?format=json"

        private var INSTANCE: NoticesApi? = null

        fun getInstance(): NoticesApi {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = NoticesApi()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
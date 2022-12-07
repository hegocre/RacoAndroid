package com.yara.raco.api

import com.yara.raco.model.schedule.Schedule
import com.yara.raco.utils.ResultCode
import com.yara.raco.utils.OkHttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ScheduleApi private constructor() {

    suspend fun getSchedule(accessToken: String): Result<List<Schedule>> = try {
        val response = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().get(
                sUrl = SCHEDULE_URL,
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
            val aux = Json.decodeFromString<ScheduleResponse>(body).results
            Result.Success(aux)
        } else {
            Result.Error(ResultCode.ERROR_API_BAD_RESPONSE)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
    }

    @Serializable
    data class ScheduleResponse(
        val count: Int,
        val results: List<Schedule>
    )

    companion object {
        private const val SCHEDULE_URL = "https://api.fib.upc.edu/v2/jo/classes/?format=json"

        private var INSTANCE: ScheduleApi? = null

        fun getInstance(): ScheduleApi {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ScheduleApi()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
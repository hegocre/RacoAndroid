package com.yara.raco.api

import com.yara.raco.model.event.Event
import com.yara.raco.utils.OkHttpRequest
import com.yara.raco.utils.Result
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class EventsApi private constructor() {

    suspend fun getEvents(accessToken: String): Result<List<Event>> = try {
        val response = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().get(
                sUrl = EVENTS_URL,
                accessToken = accessToken
            )
        }

        val status = response.code
        val body = withContext(Dispatchers.IO) {
            response.body?.string()
        }

        withContext(Dispatchers.IO) {
            response.close()
        }

        if (status == 200 && body != null) {
            val results = Json.decodeFromString<EventsResponse>(body).results
            Result.Success(results)
        } else {
            Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
        }

    } catch (e: Exception) {
        Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
    }

    @kotlinx.serialization.Serializable
    private data class EventsResponse(
        val count: Int,
        val results: List<Event>
    )

    companion object {
        private const val EVENTS_URL = "https://api.fib.upc.edu/v2/events/?format=json"

        private var INSTANCE: EventsApi? = null

        fun getInstance(): EventsApi {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = EventsApi()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
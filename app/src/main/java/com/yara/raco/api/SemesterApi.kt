package com.yara.raco.api

import com.yara.raco.model.exam.Exam
import com.yara.raco.utils.OkHttpRequest
import com.yara.raco.utils.Result
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SemesterApi private constructor() {

    private suspend fun getCurrentSemester(accessToken: String): Result<String> = try {
        val response = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().get(
                sUrl = CURRENT_SEMESTER_URL,
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
            val id = Json.decodeFromString<CurrentSemesterResponse>(body).id
            Result.Success(id)
        } else {
            Result.Error(ResultCode.ERROR_API_BAD_RESPONSE)
        }

    } catch (e: Exception) {
        Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
    }

    suspend fun getExams(accessToken: String, subjects: List<String>? = null): Result<List<Exam>> =
        when (val currentSemesterResponse = getCurrentSemester(accessToken)) {
            is Result.Error -> Result.Error(currentSemesterResponse.code)
            !is Result.Success -> Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
            else -> try {
                val currentSemester = currentSemesterResponse.data
                val requestUrl = String.format(EXAMS_URL, currentSemester).let {
                    if (subjects != null) it + "&assig=${subjects.joinToString(",")}"
                    else it
                }
                val response = withContext(Dispatchers.IO) {
                    OkHttpRequest.getInstance().get(
                        sUrl = requestUrl,
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
                    val id = Json.decodeFromString<ExamsResponse>(body).results
                    Result.Success(id)
                } else {
                    Result.Error(ResultCode.ERROR_API_BAD_RESPONSE)
                }

            } catch (e: Exception) {
                Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
            }
        }

    @kotlinx.serialization.Serializable
    private data class CurrentSemesterResponse(
        val id: String,
        val url: String,
        val actual: String,
        @SerialName("actual_horaris")
        val actualHoraris: String,
        val classes: String,
        val examens: String,
        val assignatures: String
    )

    @kotlinx.serialization.Serializable
    private data class ExamsResponse(
        val count: Int,
        val results: List<Exam>
    )

    companion object {
        private const val CURRENT_SEMESTER_URL =
            "https://api.fib.upc.edu/v2/quadrimestres/actual/?format=json"
        private const val EXAMS_URL =
            "https://api.fib.upc.edu/v2/quadrimestres/%s/examens/?format=json"

        private var INSTANCE: SemesterApi? = null

        fun getInstance(): SemesterApi {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = SemesterApi()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
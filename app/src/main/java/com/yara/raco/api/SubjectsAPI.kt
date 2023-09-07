package com.yara.raco.api

import com.yara.raco.model.subject.Subject
import com.yara.raco.utils.Json
import com.yara.raco.utils.OkHttpRequest
import com.yara.raco.utils.Result
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class SubjectsAPI private constructor() {

    suspend fun getSubjects(accessToken: String, language: String = "ca"): Result<List<Subject>> =
        try {
            val response = withContext(Dispatchers.IO) {
                OkHttpRequest.getInstance().get(
                    sUrl = SUBJECT_URL,
                    accessToken = accessToken,
                    language = language
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
            val aux = Json.decodeFromString<SubjectResponse>(body).results
            Result.Success(aux)
        }
        else {
            Result.Error(ResultCode.ERROR_API_BAD_RESPONSE)
        }

    }
    catch (e: Exception) {
        e.printStackTrace()
        Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
    }

    @Serializable
    data class SubjectResponse (
        val count: Int,
        val results: List<Subject>
    )



    companion object {
        private const val SUBJECT_URL = "https://api.fib.upc.edu/v2/jo/assignatures/?format=json"

        private var INSTANCE: SubjectsAPI? = null

        fun getInstance(): SubjectsAPI {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = SubjectsAPI()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
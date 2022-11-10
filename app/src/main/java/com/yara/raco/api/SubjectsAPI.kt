package com.yara.raco.api

import android.util.Log
import com.yara.raco.model.subject.Subject
import com.yara.raco.utils.Error
import com.yara.raco.utils.OkHttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SubjectsAPI private constructor() {

    suspend fun getSubjects(accessToken: String): Result<List<Subject>> = try {
        val response = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().get(
                sUrl = SUBJECT_URL,
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
            val aux = Json.decodeFromString<SubjectResponse>(body).results
            Result.Success(aux)
        }
        else {
            Result.Error(Error.API_BAD_RESPONSE)
        }

    }
    catch (e: Exception) {
        Log.d("ERROR", e.toString())
        Result.Error(Error.API_BAD_REQUEST)
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
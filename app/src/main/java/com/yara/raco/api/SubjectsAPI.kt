package com.yara.raco.api

import com.yara.raco.model.subject.Subject
import com.yara.raco.utils.Error
import com.yara.raco.utils.OkHttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SubjectsAPI private constructor() {

    suspend fun getSubjects(accessToken: String): Result<List<Subject>> = try {
        val response = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().post(
                sUrl = SubjectsAPI.SUBJECT_URL,
                body = String.format(SubjectsAPI.SUBJECT_BODY, accessToken),
                mediaType = "application/x-www-form-urlencoded"
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
            Result.Success(Json.decodeFromString(body))
        }
        else {
            Result.Error(Error.API_BAD_RESPONSE)
        }

    }
    catch (e: Exception) {
        Result.Error(Error.API_BAD_REQUEST)
    }

    companion object {
        private const val SUBJECT_URL = "https://api.fib.upc.edu/v2/jo/assignatures/"
        private const val SUBJECT_BODY = "Content-Type=application/json" +
                "&redirect_uri=apifib://yara" +
                "&Authorization=Bearer %s"

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
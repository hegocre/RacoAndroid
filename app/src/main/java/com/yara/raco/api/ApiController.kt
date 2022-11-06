package com.yara.raco.api

import com.yara.raco.model.notices.Notice
import com.yara.raco.model.subject.Subject
import com.yara.raco.model.user.AccessToken

class ApiController private constructor() {
    var accessToken: AccessToken? = null
    private val tokenApi = TokenApi.getInstance()
    private val subjectApi = SubjectsAPI.getInstance()
    private val noticeApi = NoticesApi.getInstance()

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

    suspend fun listSubjects(): Result<List<Subject>> {
        accessToken?.let {
            return subjectApi.getSubjects(it.accessToken)
        } ?: return Result.Error(2)
    }

    suspend fun listNotices(): Result<List<Notice>> {
        accessToken?.let {
            return noticeApi.getNotices(it.accessToken)
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
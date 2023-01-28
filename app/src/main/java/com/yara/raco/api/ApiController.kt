package com.yara.raco.api

import android.content.Context
import com.yara.raco.model.exam.Exam
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.Notice
import com.yara.raco.model.schedule.Schedule
import com.yara.raco.model.subject.Subject
import com.yara.raco.model.user.AccessToken
import com.yara.raco.utils.Result

class ApiController private constructor() {
    var accessToken: AccessToken? = null
    var language = "ca"
    private val tokenApi = TokenApi.getInstance()
    private val subjectApi = SubjectsAPI.getInstance()
    private val noticeApi = NoticesApi.getInstance()
    private val scheduleApi = ScheduleApi.getInstance()
    private val semesterApi = SemesterApi.getInstance()

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
            return subjectApi.getSubjects(it.accessToken, language)
        } ?: return Result.Error(2)
    }

    suspend fun listNotices(): Result<List<Notice>> {
        accessToken?.let {
            return noticeApi.getNotices(it.accessToken)
        } ?: return Result.Error(2)
    }

    suspend fun listSchedule(): Result<List<Schedule>> {
        accessToken?.let {
            return scheduleApi.getSchedule(it.accessToken)
        } ?: return Result.Error(2)
    }

    suspend fun listExams(subjects: List<String>? = null): Result<List<Exam>> {
        accessToken?.let {
            return semesterApi.getExams(it.accessToken, subjects)
        } ?: return Result.Error(2)
    }

    fun downloadAttachment(context: Context, file: File): Result<Nothing?> {
        accessToken?.let {
            noticeApi.getAttachment(context, file, it.accessToken)
            return Result.Success(null)
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
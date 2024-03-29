package com.yara.raco.model.user

import android.content.Context
import com.yara.raco.BuildConfig
import com.yara.raco.api.ApiController
import com.yara.raco.model.evaluation.EvaluationController
import com.yara.raco.model.event.EventController
import com.yara.raco.model.exam.ExamController
import com.yara.raco.model.notices.NoticeController
import com.yara.raco.model.schedule.ScheduleController
import com.yara.raco.model.subject.SubjectController
import com.yara.raco.utils.PreferencesManager
import com.yara.raco.utils.Result
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class UserController private constructor(context: Context) {
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val apiController = ApiController.getInstance()
    private val subjectController = SubjectController.getInstance(context)
    private val noticeController = NoticeController.getInstance(context)
    private val evaluationController = EvaluationController.getInstance(context)
    private val scheduleController = ScheduleController.getInstance(context)
    private val eventController = EventController.getInstance(context)
    private val examController = ExamController.getInstance(context)

    val isLoggedIn: Boolean
        get() = apiController.accessToken != null

    init {
        apiController.accessToken = preferencesManager.getAccessToken()
        val lastLanguage = preferencesManager.getUserLastLanguage()
        val deviceLanguage = Locale.getDefault().language
        if (lastLanguage != deviceLanguage) {
            CoroutineScope(Dispatchers.IO).launch {
                //Delete databases which can be translated
                subjectController.deleteAllSubjects()

                //Update last language
                preferencesManager.setUserLastLanguage(deviceLanguage)
            }
        }
        if (deviceLanguage in listOf("ca", "es", "en")) {
            apiController.language = deviceLanguage
        } else {
            apiController.language = "ca"
        }

        // System aimed to apply changes on version update
        val lastStartedVersion = preferencesManager.getLastStartedVersionCode()

        if (lastStartedVersion < BuildConfig.VERSION_CODE) {
            //Fixed deleted notice files not being deleted, clear notices to force refresh
            if (lastStartedVersion < 12) {
                CoroutineScope(Dispatchers.IO).launch {
                    noticeController.deleteAllNotices()
                }
            }

            preferencesManager.setLastStartedVersionCode(BuildConfig.VERSION_CODE)
        }
    }

    suspend fun logIn(authorizationCode: String): Boolean {
        val loginResult = apiController.login(authorizationCode)

        if (loginResult is Result.Success) {
            val accessToken = loginResult.data
            preferencesManager.setAccessToken(accessToken)
            preferencesManager.setLastRefreshWorkerStatusCode(ResultCode.UNKNOWN)
            return true
        }

        return false
    }

    suspend fun refreshToken(): Int {
        val refreshResult = apiController.refreshToken()

        if (refreshResult is Result.Success) {
            val accessToken = refreshResult.data
            preferencesManager.setAccessToken(accessToken)
            preferencesManager.setLastRefreshWorkerStatusCode(ResultCode.SUCCESS)
            return ResultCode.SUCCESS
        }
        if (refreshResult is Result.Error) {
            return if (refreshResult.code == 400) {
                preferencesManager.setLastRefreshWorkerStatusCode(ResultCode.INVALID_TOKEN)
                ResultCode.INVALID_TOKEN
            } else {
                preferencesManager.setLastRefreshWorkerStatusCode(ResultCode.ERROR_API_BAD_RESPONSE)
                ResultCode.ERROR_API_BAD_RESPONSE
            }
        }
        preferencesManager.setLastRefreshWorkerStatusCode(ResultCode.UNKNOWN)
        return ResultCode.UNKNOWN
    }

    suspend fun logOut() {
        preferencesManager.setAccessToken(null)
        apiController.accessToken = null
        subjectController.deleteAllSubjects()
        noticeController.deleteAllNotices()
        evaluationController.deleteAllEvaluations()
        scheduleController.deleteAllSchedules()
        examController.deleteAllExams()
        eventController.deleteAllEvents()
    }

    companion object {
        private var INSTANCE: UserController? = null

        fun getInstance(context: Context): UserController {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = UserController(context)
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
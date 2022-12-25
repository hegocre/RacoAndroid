package com.yara.raco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yara.raco.model.evaluation.EvaluationController
import com.yara.raco.model.evaluation.EvaluationWithGrades
import com.yara.raco.model.files.File
import com.yara.raco.model.grade.Grade
import com.yara.raco.model.notices.NoticeController
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.schedule.Schedule
import com.yara.raco.model.schedule.ScheduleController
import com.yara.raco.model.subject.Subject
import com.yara.raco.model.subject.SubjectController
import com.yara.raco.model.user.UserController
import com.yara.raco.utils.Result
import com.yara.raco.utils.ResultCode
import com.yara.raco.workers.LogOutWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RacoViewModel(application: Application) : AndroidViewModel(application) {
    private val userController = UserController.getInstance(application)
    private val subjectController = SubjectController.getInstance(application)
    private val noticeController = NoticeController.getInstance(application)
    private val scheduleController = ScheduleController.getInstance(application)
    private val evaluationController = EvaluationController.getInstance(application)

    private var shouldRefreshToken = false

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    private val _shouldLogOut = MutableLiveData(false)
    val shouldLogOut: LiveData<Boolean>
        get() = _shouldLogOut

    val subjects: LiveData<List<Subject>>
        get() = subjectController.getSubjects()
    val notices: LiveData<List<NoticeWithFiles>>
        get() = noticeController.getNotices()
    val schedules: LiveData<List<Schedule>>
        get() = scheduleController.getSchedule()
    val evaluation: LiveData<List<EvaluationWithGrades>>
        get() = evaluationController.getEvaluations()

    init {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            when (LogOutWorker.getLastExecutionResult(application)) {
                ResultCode.INVALID_TOKEN -> _shouldLogOut.value = true
                ResultCode.SUCCESS -> {
                    shouldRefreshToken = false
                    refresh()
                }
                ResultCode.UNKNOWN, ResultCode.ERROR_API_BAD_RESPONSE -> _isRefreshing.emit(false)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            if (shouldRefreshToken) {
                when (userController.refreshToken()) {
                    ResultCode.INVALID_TOKEN -> {
                        _shouldLogOut.value = true
                        _isRefreshing.emit(false)
                        return@launch
                    }
                    ResultCode.UNKNOWN, ResultCode.ERROR_API_BAD_RESPONSE -> return@launch
                    ResultCode.SUCCESS -> shouldRefreshToken = false
                }
            }
            subjectController.syncSubjects()
            noticeController.syncNotices()
            scheduleController.syncSchedule()
            _isRefreshing.emit(false)
        }
    }

    suspend fun getNoticeDetails(noticeId: Int): Result<NoticeWithFiles> {
        val noticeWithFiles = noticeController.getNoticeWithFiles(noticeId)
        return if (noticeWithFiles != null) Result.Success(noticeWithFiles) else Result.Error(0)
    }

    suspend fun getEvaluationDetails(evaluationId: Int): Result<EvaluationWithGrades> {
        val evaluationWithGrades = evaluationController.getEvaluationWithGrades(evaluationId)
        return if (evaluationWithGrades != null) Result.Success(evaluationWithGrades) else Result.Error(
            0
        )
    }

    fun getLiveEvaluationDetails(evaluationId: Int): LiveData<EvaluationWithGrades?> {
        return evaluationController.getLiveEvaluationWithGrades(evaluationId)
    }

    fun downloadFile(file: File) {
        noticeController.downloadAttachment(getApplication(), file)
    }

    fun addEvaluation(subjectId: String, evaluationName: String) {
        viewModelScope.launch {
            evaluationController.addEvaluation(subjectId, evaluationName)
        }
    }

    fun deleteEvaluation(evaluationId: Int) {
        viewModelScope.launch {
            evaluationController.deleteEvaluation(evaluationId)

        }
    }

    fun saveEvaluation(evaluationWithGrades: EvaluationWithGrades) {
        viewModelScope.launch {
            evaluationController.saveEvaluation(evaluationWithGrades)
        }
    }

    fun updateGrade(grade: Grade) {
        viewModelScope.launch {
            evaluationController.updateGrade(grade)
        }
    }
}
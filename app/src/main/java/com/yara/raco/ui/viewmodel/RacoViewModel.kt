package com.yara.raco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.NoticeController
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.subject.Subject
import com.yara.raco.model.subject.SubjectController
import com.yara.raco.model.user.UserController
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RacoViewModel(application: Application) : AndroidViewModel(application) {
    private val userController = UserController.getInstance(application)
    private val subjectController = SubjectController.getInstance(application)
    private val noticeController = NoticeController.getInstance(application)

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

    init {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            when (userController.refreshToken()) {
                ResultCode.INVALID_TOKEN -> _shouldLogOut.value = true
                ResultCode.SUCCESS -> {
                    shouldRefreshToken = false
                    refresh()
                }
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
                }

            }
            subjectController.syncSubjects()
            noticeController.syncNotices()
            _isRefreshing.emit(false)
        }
    }

    fun downloadFile(file: File) {
        noticeController.downloadAttachment(getApplication(), file)
    }
}
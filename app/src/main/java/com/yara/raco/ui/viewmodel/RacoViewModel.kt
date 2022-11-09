package com.yara.raco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.yara.raco.model.notices.NoticeController
import com.yara.raco.model.notices.NoticesWithFiles
import com.yara.raco.model.subject.Subject
import com.yara.raco.model.subject.SubjectController
import com.yara.raco.model.user.UserController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RacoViewModel(application: Application) : AndroidViewModel(application) {
    private val userController = UserController.getInstance(application)
    private val subjectController = SubjectController.getInstance(application)
    private val noticeController = NoticeController.getInstance(application)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    val subjects: LiveData<List<Subject>>
        get() = subjectController.getSubjects()
    val notices: LiveData<List<NoticesWithFiles>>
        get() = noticeController.getNotices()

    init {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            userController.refreshToken()
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            subjectController.syncSubjects()
            noticeController.syncNotices()
            _isRefreshing.emit(false)
        }
    }
}
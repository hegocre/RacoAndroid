package com.yara.raco.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yara.raco.model.user.UserController
import kotlinx.coroutines.launch
import kotlin.random.Random

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _loggedIn = MutableLiveData<Boolean?>(null)
    val loggedIn: LiveData<Boolean?>
        get() = _loggedIn

    val state = randomStringByKotlinRandom()

    fun processCredentials(url: String) {
        val matcher = "apifib:.*code=(.*)&state=(.*)".toRegex()
        val match = matcher.matchEntire(url)?.groups
        val authorizationCode = match?.get(1)?.value
        val state = match?.get(2)?.value
        if (authorizationCode == null) {
            _loggedIn.value = false
            return
        }
        if (state == null || state != this.state) {
            _loggedIn.value = false
            return
        }
        val userController = UserController.getInstance(getApplication())
        viewModelScope.launch {
            _loggedIn.value = userController.logIn(authorizationCode)
        }
    }

    private fun randomStringByKotlinRandom() =
        (1..STATE_LENGTH).map {
            Random.nextInt(0, charPool.size).let {
                charPool[it]
            }
        }.joinToString("")

    companion object {
        private const val STATE_LENGTH = 16
        private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    }
}
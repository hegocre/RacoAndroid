package com.yara.raco.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.yara.raco.BuildConfig.CLIENT_ID
import com.yara.raco.ui.components.RacoLoginWebScreen
import com.yara.raco.ui.viewmodel.LoginViewModel

class WebLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loginViewModel by viewModels<LoginViewModel>()

        loginViewModel.loggedIn.observe(this) {
            when (it) {
                true, false -> {
                    val intent = Intent()
                    intent.putExtra("loggedIn", it)
                    setResult(if (it) RESULT_OK else RESULT_CANCELED, intent)
                    finish()
                }
                else -> {}
            }
        }

        val randomUrl = String.format(AUTH_URL, loginViewModel.state)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RacoLoginWebScreen(onLoginUrl = loginViewModel::processCredentials, url = randomUrl)
        }
    }

    companion object {
        private const val AUTH_URL =
            "https://api.fib.upc.edu/v2/o/authorize/" +
                    "?client_id=$CLIENT_ID" +
                    "&redirect_uri=apifib://yara" +
                    "&response_type=code" +
                    "&scope=read" +
                    "&state=%s" +
                    "&approval_prompt=force"

    }
}
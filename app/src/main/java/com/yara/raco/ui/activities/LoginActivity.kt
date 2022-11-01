package com.yara.raco.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.yara.raco.R
import com.yara.raco.ui.components.RacoLoginScreen

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loginIntent = Intent(this, WebLoginActivity::class.java)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RacoLoginScreen(
                loginIntent = loginIntent,
                onLoginSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onLoginFailed = {
                    Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_LONG)
                        .show()
                }
            )
        }
    }
}
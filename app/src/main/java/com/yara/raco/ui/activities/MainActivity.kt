package com.yara.raco.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.yara.raco.model.user.UserController
import com.yara.raco.ui.components.RacoMainScreen
import com.yara.raco.ui.viewmodel.RacoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!UserController.getInstance(this).isLoggedIn) {
            // Login activity
            launchLogin()
            finish()
            return
        }

        val racoViewModel by viewModels<RacoViewModel>()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RacoMainScreen(racoViewModel = racoViewModel, onLogOut = this::logout)
        }
    }

    private fun logout() {
        UserController.getInstance(this).logOut()
        launchLogin()
        finish()
    }

    private fun launchLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
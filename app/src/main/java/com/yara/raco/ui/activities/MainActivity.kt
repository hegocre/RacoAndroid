package com.yara.raco.ui.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.yara.raco.R
import com.yara.raco.model.user.UserController
import com.yara.raco.ui.components.RacoMainScreen
import com.yara.raco.ui.viewmodel.RacoViewModel
import com.yara.raco.workers.LogOutWorker
import com.yara.raco.workers.NoticeNotificationWorker
import com.yara.raco.workers.RefreshTokenWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!UserController.getInstance(this).isLoggedIn) {
            // Login activity
            launchLogin()
            finish()
            return
        }

        RefreshTokenWorker.enqueueSelf(this)

        //Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Permission granted
                setupNotification()
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                val requestPermissionLauncher =
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                        if (isGranted) setupNotification()
                    }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            setupNotification()
        }

        val racoViewModel by viewModels<RacoViewModel>()

        racoViewModel.shouldLogOut.observe(this) { shouldLogOut ->
            if (shouldLogOut) {
                logout()
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val notificationNoticeId = intent.getIntExtra("NOTICE_ID", -1)

        setContent {
            RacoMainScreen(
                racoViewModel = racoViewModel,
                notificationNoticeId = notificationNoticeId,
                onLogOut = this::logout
            )
        }
    }

    override fun onResume() {
        super.onResume()

        //Dismiss existing notifications
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun setupNotification() {
        //Create notices notification channel (can be executed always, no effect when present)
        val name = getString(R.string.notices)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        notificationManager.cancelAll()

        //Create periodic task
        NoticeNotificationWorker.enqueueSelf(this)
    }

    private fun logout() {
        LogOutWorker.executeSelf(this)
        launchLogin()
        finish()
    }

    private fun launchLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
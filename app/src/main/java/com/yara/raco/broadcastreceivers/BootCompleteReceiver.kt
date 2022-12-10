package com.yara.raco.broadcastreceivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.yara.raco.model.user.UserController
import com.yara.raco.workers.NoticeNotificationWorker
import com.yara.raco.workers.RefreshTokenWorker

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == null ||
            !intent.action.equals("android.intent.action.BOOT_COMPLETED") ||
            context == null
        )
            return

        if (!UserController.getInstance(context).isLoggedIn) {
            return
        }

        RefreshTokenWorker.enqueueSelf(context)

        //Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Permission granted
                NoticeNotificationWorker.enqueueSelf(context)
            }
        } else {
            NoticeNotificationWorker.enqueueSelf(context)
        }

    }
}
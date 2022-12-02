package com.yara.raco.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yara.raco.workers.NoticeNotificationWorker

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == null ||
            !intent.action.equals("android.intent.action.BOOT_COMPLETED") ||
            context == null
        )
            return
        NoticeNotificationWorker.enqueueSelf(context)
    }
}
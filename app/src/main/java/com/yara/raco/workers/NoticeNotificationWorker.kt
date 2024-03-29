package com.yara.raco.workers

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.work.*
import com.yara.raco.R
import com.yara.raco.database.RacoDatabase
import com.yara.raco.model.notices.NoticeController
import com.yara.raco.model.user.UserController
import com.yara.raco.ui.activities.MainActivity
import com.yara.raco.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NoticeNotificationWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            // Check for notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //Permission not granted
                    return@withContext Result.failure()
                }
            }

            val userController = UserController.getInstance(applicationContext)
            if (!userController.isLoggedIn) {
                return@withContext Result.failure()
            }
            val preferencesManager = PreferencesManager.getInstance(applicationContext)
            if (preferencesManager.getFirstTimeNotification()) {
                preferencesManager.setFirstTimeNotification(false)
                return@withContext Result.success()
            }

            val racoDatabase = RacoDatabase.getInstance(applicationContext)
            val currentNoticesId = racoDatabase.noticeDAO.fetchAllNoticeIds()

            val noticeController = NoticeController.getInstance(applicationContext)
            noticeController.syncNotices()

            val newNoticesId = racoDatabase.noticeDAO.fetchAllNoticeIds()
            val notifications = mutableListOf<Pair<Int, Notification>>()
            val lines = mutableListOf<String>()

            for (newNotice in newNoticesId) {
                if (!currentNoticesId.contains(newNotice)) {
                    val notice = racoDatabase.noticeDAO.fetchNotice(newNotice)
                    val title = HtmlCompat.fromHtml(notice.titol, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        .toString()
                    val text = HtmlCompat.fromHtml(notice.text, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        .toString()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss", Locale.getDefault())
                    val date =
                        dateFormat.parse(notice.dataModificacio) ?: Date(System.currentTimeMillis())
                    val subText = if (notice.codiAssig.startsWith("#"))
                        "FIB" else notice.codiAssig
                    notifications.add(
                        Pair(
                            notice.id,
                            generateNotification(
                                applicationContext,
                                title,
                                text,
                                date,
                                subText,
                                newNotice
                            )
                        )
                    )
                    lines.add("<b>$subText:</b> $title")
                }
            }

            if (notifications.isNotEmpty()) {
                NotificationManagerCompat.from(applicationContext).apply {
                    notifications.forEach { notification ->
                        notify(notification.first, notification.second)
                    }
                    val groupSummary = generateGroupSummary(
                        applicationContext,
                        applicationContext.getString(R.string.app_name),
                        applicationContext.getString(R.string.you_have_new_notices),
                        lines
                    )
                    notify(0, groupSummary)
                }
            }

            return@withContext Result.success()
        }
    }

    private fun generateGroupSummary(
        context: Context,
        title: String,
        text: String,
        lines: List<String>
    ): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val contentIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, intentFlags
        )

        val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle(title)
        lines.take(6).forEach { line ->
            inboxStyle.addLine(line)
        }

        return NotificationCompat.Builder(context, context.getString(R.string.notices))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(inboxStyle)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setGroup("com.yara.raco.NOTICES_NOTIFICATION")
            .setGroupSummary(true)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun generateNotification(
        context: Context,
        title: String,
        text: String,
        date: Date,
        subText: String,
        noticeId: Int
    ): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("NOTICE_ID", noticeId)
        }
        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val contentIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, intentFlags
        )

        return NotificationCompat.Builder(context, context.getString(R.string.notices))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setSubText(subText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setWhen(date.time)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setGroup("com.yara.raco.NOTICES_NOTIFICATION")
            .setContentIntent(contentIntent)
            .build()
    }

    companion object {
        private fun getOnWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return PeriodicWorkRequest.Builder(
                NoticeNotificationWorker::class.java,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            )
                .addTag("com.yara.raco.NOTICE_NOTIFICATION_WORK")
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }

        fun enqueueSelf(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "com.yara.raco.NOTICE_NOTIFICATION_WORKER",
                ExistingPeriodicWorkPolicy.KEEP,
                getOnWorkRequest()
            )
        }
    }
}
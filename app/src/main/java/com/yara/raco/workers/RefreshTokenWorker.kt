package com.yara.raco.workers

import android.content.Context
import androidx.work.*
import com.yara.raco.model.user.UserController
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class RefreshTokenWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val userController = UserController.getInstance(applicationContext)

            if (!userController.isLoggedIn) {
                return@withContext Result.failure()
            }

            when (userController.refreshToken()) {
                ResultCode.SUCCESS -> return@withContext Result.success()
                ResultCode.INVALID_TOKEN -> return@withContext Result.failure()
                else -> return@withContext Result.retry()
            }
        }
    }

    companion object {
        private fun getOnWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return PeriodicWorkRequest.Builder(
                RefreshTokenWorker::class.java, 32400, TimeUnit.SECONDS
            )
                .addTag("com.yara.raco.REFRESH_TOKEN_WORK")
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }

        fun enqueueSelf(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "com.yara.raco.REFRESH_TOKEN_WORKER",
                ExistingPeriodicWorkPolicy.KEEP,
                getOnWorkRequest()
            )
        }
    }
}
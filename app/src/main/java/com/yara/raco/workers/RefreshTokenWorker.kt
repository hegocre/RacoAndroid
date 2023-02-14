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
            if (getLastExecutionResult(applicationContext) == ResultCode.INVALID_TOKEN) {
                return@withContext Result.failure(
                    workDataOf(
                        Pair(
                            "RESULT_CODE",
                            ResultCode.INVALID_TOKEN
                        )
                    )
                )
            }

            val userController = UserController.getInstance(applicationContext)

            if (!userController.isLoggedIn) {
                return@withContext Result.failure()
            }

            when (userController.refreshToken()) {
                ResultCode.SUCCESS -> return@withContext Result.success()
                ResultCode.INVALID_TOKEN -> return@withContext Result.failure(
                    workDataOf(
                        Pair(
                            "RESULT_CODE",
                            ResultCode.INVALID_TOKEN
                        )
                    )
                )
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
                    WorkRequest.MIN_BACKOFF_MILLIS,
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

        fun getLastExecutionResult(context: Context): Int {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosByTag("com.yara.raco.REFRESH_TOKEN_WORK").get()
            val lastWork = workInfos.lastOrNull {
                it.state != WorkInfo.State.RUNNING && it.state != WorkInfo.State.ENQUEUED
            } ?: return ResultCode.SUCCESS

            return if (lastWork.state == WorkInfo.State.SUCCEEDED) {
                ResultCode.SUCCESS
            } else {
                if (lastWork.outputData.getInt(
                        "RESULT_CODE",
                        ResultCode.UNKNOWN
                    ) == ResultCode.INVALID_TOKEN
                ) {
                    ResultCode.INVALID_TOKEN
                } else ResultCode.UNKNOWN
            }
        }
    }
}
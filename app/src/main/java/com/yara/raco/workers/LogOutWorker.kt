package com.yara.raco.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yara.raco.model.user.UserController

class LogOutWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        UserController.getInstance(applicationContext).logOut()
        return Result.success()
    }

    companion object {
        private fun getOnWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(LogOutWorker::class.java)
                .addTag("com.yara.raco.LOG_OUT_WORK")
                .build()
        }

        fun executeSelf(context: Context) {
            WorkManager.getInstance(context).enqueue(
                getOnWorkRequest()
            )
        }
    }
}
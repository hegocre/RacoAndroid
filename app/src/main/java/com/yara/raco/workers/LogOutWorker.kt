package com.yara.raco.workers

import android.content.Context
import androidx.work.*
import com.yara.raco.model.user.UserController
import com.yara.raco.utils.ResultCode

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

        fun getLastExecutionResult(context: Context): Int {
            val workInfos =
                WorkManager.getInstance(context).getWorkInfosByTag("com.yara.raco.LOG_OUT_WORK")
                    .get()
            val lastWork = workInfos.lastOrNull() ?: return ResultCode.SUCCESS
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

        fun executeSelf(context: Context) {
            WorkManager.getInstance(context).enqueue(
                getOnWorkRequest()
            )
        }
    }
}
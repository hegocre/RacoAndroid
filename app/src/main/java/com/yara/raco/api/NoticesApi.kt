package com.yara.raco.api

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.yara.raco.R
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.Notice
import com.yara.raco.utils.OkHttpRequest
import com.yara.raco.utils.Result
import com.yara.raco.utils.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NoticesApi private constructor() {

    suspend fun getNotices(accessToken: String): Result<List<Notice>> = try {
        val response = withContext(Dispatchers.IO) {
            OkHttpRequest.getInstance().get(
                sUrl = NOTICE_URL,
                accessToken = accessToken,
            )
        }

        val statusCode = response.code
        val body = withContext(Dispatchers.IO) {
            response.body?.string()
        }

        withContext(Dispatchers.IO) {
            response.close()
        }

        if (statusCode == 200 && body != null) {
            val aux = Json.decodeFromString<NoticeResponse>(body).results
            Result.Success(aux)
        } else {
            Result.Error(ResultCode.ERROR_API_BAD_RESPONSE)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Result.Error(ResultCode.ERROR_API_BAD_REQUEST)
    }

    fun getAttachment(context: Context, file: File, accessToken: String) {
        val request = DownloadManager.Request(Uri.parse(file.url))
            .setTitle(file.nom)
            .setMimeType(file.tipusMime)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.nom)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED or DownloadManager.Request.VISIBILITY_VISIBLE)
            .addRequestHeader("Authorization", "Bearer $accessToken")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val downloadId = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (downloadId == -1L) {
                    return
                }

                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    var columIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (columIndex < 0) return
                    val status = cursor.getInt(columIndex)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        columIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        if (columIndex < 0) return
                        val filePath = Uri.parse(cursor.getString(columIndex))

                        columIndex = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
                        if (columIndex < 0) return
                        val mimeType = cursor.getString(columIndex)

                        val uriPath = FileProvider.getUriForFile(
                            context,
                            context.applicationContext.packageName + ".provider",
                            filePath.path?.let { java.io.File(it) } ?: return
                        )

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uriPath, mimeType)
                        intent.flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK

                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.cannot_open_file),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.download_failed),
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }

        context.registerReceiver(downloadReceiver, filter)

        downloadManager.enqueue(request)

        Toast.makeText(
            context,
            context.getString(R.string.download_started),
            Toast.LENGTH_LONG,
        ).show()
    }

    @Serializable
    data class NoticeResponse(
        val count: Int,
        val results: List<Notice>
    )

    companion object {
        private const val NOTICE_URL = "https://api.fib.upc.edu/v2/jo/avisos/?format=json"

        private var INSTANCE: NoticesApi? = null

        fun getInstance(): NoticesApi {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = NoticesApi()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
package com.yara.raco.model.notices

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.api.Result
import com.yara.raco.database.RacoDatabase

class NoticeController private constructor(context: Context)  {
    private val racoDatabase = RacoDatabase.getInstance(context)
    private val apiController = ApiController.getInstance()

    suspend fun syncNotices() {
        val result = apiController.listNotices()
        if (result is Result.Success) {
            val savedNoticeSet = racoDatabase.noticeDAO.fetchAllNoticeIds().toHashSet()
            for (notice in result.data) {
                if (!savedNoticeSet.contains(notice.id)) {
                    racoDatabase.noticeDAO.insertNotice(notice)
                }
                savedNoticeSet.remove(notice.id)
            }
            for (id in savedNoticeSet) {
                racoDatabase.noticeDAO.deleteNotice(id)
            }
        }
    }

    fun getNotices() = racoDatabase.noticeDAO.fetchAllNotices()

    companion object {
        private var INSTANCE: NoticeController? = null

        /**
         * Get the instance of [NoticeController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): NoticeController {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = NoticeController(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
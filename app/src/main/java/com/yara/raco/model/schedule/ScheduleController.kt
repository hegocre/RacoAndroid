package com.yara.raco.model.schedule

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.database.RacoDatabase
import com.yara.raco.database.schedule.ScheduleDAO
import com.yara.raco.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleController private constructor(context: Context)  {
    private val racoDatabase = RacoDatabase.getInstance(context)
    private val apiController = ApiController.getInstance()

    suspend fun syncSchedule() {
        withContext(Dispatchers.IO) {
            val result = apiController.listSchedule()
            if (result is Result.Success) {
                val savedScheduleSet = racoDatabase.scheduleDAO.fetchAllSchedulePrimaryKeys().toHashSet()
                for (schedule in result.data) {
                    var primarykey = ScheduleDAO.SchedulePrimaryKey(
                        schedule.codiAssig,
                        schedule.diaSetmana,
                        schedule.inici
                    )
                    if (!savedScheduleSet.contains(primarykey)) {
                        racoDatabase.scheduleDAO.insertSchedule(schedule)
                    }
                    savedScheduleSet.remove(primarykey)
                }
                for (pk in savedScheduleSet) {
                    racoDatabase.scheduleDAO.deleteSchedule(pk.codiAssig, pk.diaSetmana, pk.inici)
                }
            }
        }
    }

    fun getSchedule() = racoDatabase.scheduleDAO.fetchAllSchedules()

    companion object {
        private var INSTANCE: ScheduleController? = null

        /**
         * Get the instance of [ScheduleController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): ScheduleController {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = ScheduleController(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
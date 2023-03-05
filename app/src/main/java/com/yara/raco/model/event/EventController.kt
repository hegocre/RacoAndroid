package com.yara.raco.model.event

import android.content.Context
import com.yara.raco.api.ApiController
import com.yara.raco.database.RacoDatabase
import com.yara.raco.database.event.EventDAO
import com.yara.raco.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventController private constructor(context: Context) {
    private val racoDatabase = RacoDatabase.getInstance(context)
    private val apiController = ApiController.getInstance()

    suspend fun syncEvents() {
        withContext(Dispatchers.IO) {
            val result = apiController.listEvents()
            if (result is Result.Success) {
                val savedEventSet = racoDatabase.eventDAO.fetchAllEventPrimaryKeys().toHashSet()
                for (event in result.data) {
                    if (event.categoria == "CALENDARI") {
                        val primaryKey = EventDAO.EventPrimaryKey(
                            nom = event.nom,
                            inici = event.inici
                        )
                        if (!savedEventSet.contains(primaryKey)
                        ) {
                            racoDatabase.eventDAO.insertEvent(event)
                        }
                        savedEventSet.remove(primaryKey)
                    }
                }
                for (key in savedEventSet) {
                    racoDatabase.eventDAO.deleteEvent(key.nom, key.inici)
                }
            }
        }
    }

    fun getEvents() = racoDatabase.eventDAO.fetchAllEvents()

    suspend fun deleteAllEvents() {
        withContext(Dispatchers.IO) {
            racoDatabase.eventDAO.deleteAllEvents()
        }
    }

    companion object {
        private var INSTANCE: EventController? = null

        /**
         * Get the instance of [EventController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): EventController {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = EventController(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
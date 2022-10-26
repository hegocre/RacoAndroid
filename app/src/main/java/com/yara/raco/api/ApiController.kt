package com.yara.raco.api

import android.content.Context
import android.util.Log

/**
 * Class with methods used to interact with [the API](https://api.fib.upc.edu/v2/)
 * classes. This is a Singleton class and will have only one instance.
 *
 * @param context Context of the application.
 */
class ApiController private constructor(context: Context){





    companion object {
        private var instance: ApiController? = null
        private const val API_URL = "https://api.fib.upc.edu"

        /**
         * Get the instance of the [ApiController], and create it if null.
         *
         * @param context Context of the application.
         * @return The instance of the controller.
         */
        fun getInstance(context: Context): ApiController {
            synchronized(this) {
                var tempInstance = instance

                if (tempInstance == null) {
                    tempInstance = ApiController(context)
                    instance = tempInstance
                }

                return tempInstance
            }
        }
    }

}
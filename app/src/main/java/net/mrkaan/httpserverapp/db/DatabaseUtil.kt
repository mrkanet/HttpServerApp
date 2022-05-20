package net.mrkaan.httpserverapp.db

import android.content.Context
import androidx.room.Room

class DatabaseUtil {

    companion object {
        private var passwordDatabase: PasswordDatabase? = null
        private lateinit var applicationContext: Context

        fun initDatabaseUtil(applicationContext: Context) {
            this.applicationContext = applicationContext
        }

        fun getPasswordDatabase(): PasswordDatabase {
            if (passwordDatabase == null) {
                passwordDatabase = Room.databaseBuilder(
                    applicationContext,
                    PasswordDatabase::class.java,
                    "passwords"
                ).build()
            }
            return passwordDatabase!!
        }
    }

    fun buildDatabaseUtil(applicationContext: Context): DatabaseUtil {
        DatabaseUtil.initDatabaseUtil(applicationContext)
        return this
    }

}
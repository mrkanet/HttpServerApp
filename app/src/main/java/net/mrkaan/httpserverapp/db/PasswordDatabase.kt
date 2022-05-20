package net.mrkaan.httpserverapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.mrkaan.httpserverapp.models.Password

@Database(entities = arrayOf(Password::class), version = 1)
abstract class PasswordDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
}
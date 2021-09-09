package net.mrkaan.httpserverapp.utils.db

import androidx.room.*
import net.mrkaan.httpserverapp.models.Password

@Dao
interface PasswordDao {
    @Query("SELECT * FROM password")
    fun getAllSites(): List<Password>

    @Query("SELECT * FROM password WHERE url in (:url)")
    fun getSelectedSite(url: String): List<Password>

    @Insert
    fun addNewSite(password: Password)

    @Delete
    fun deleteSite(password: Password)

    @Update
    fun updateSite(password: Password)

    @Query("DELETE FROM password")
    fun deleteAllSites()
}
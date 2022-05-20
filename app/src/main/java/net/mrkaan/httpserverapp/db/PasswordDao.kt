package net.mrkaan.httpserverapp.db

import androidx.room.*
import net.mrkaan.httpserverapp.models.Password

@Dao
interface PasswordDao {
    @Query("SELECT * FROM password")
    fun getAllSites(): List<Password>

    @Query("SELECT * FROM password WHERE url LIKE '%' || :url || '%'")
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
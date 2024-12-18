package com.example.projectuas

import androidx.room.*
import com.example.projectuas.model.Penginapan

@Dao
interface PenginapanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenginapan(penginapan: PenginapanFavorite)

    @Query("SELECT * FROM penginapan_favorit WHERE id = :id")
    suspend fun getPenginapanById(id: String): PenginapanFavorite?

    @Delete
    suspend fun deletePenginapan(penginapan: PenginapanFavorite)

    @Query("SELECT id FROM penginapan_favorit ORDER BY id ASC")
    suspend fun getAllFavoriteIds(): List<String>

    @Delete
    suspend fun deleteFavorite(penginapan: PenginapanFavorite)

    @Query("DELETE FROM penginapan_favorit WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM penginapan_favorit WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

}


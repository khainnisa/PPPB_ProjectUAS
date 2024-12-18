package com.example.projectuas

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "penginapan_favorit")
data class PenginapanFavorite(
    @PrimaryKey val id: String // Hanya menyimpan ID
)

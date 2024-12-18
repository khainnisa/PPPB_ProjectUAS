package com.example.projectuas

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PenginapanFavorite::class], version = 2, exportSchema = false)
abstract class PenginapanDatabase : RoomDatabase() {
    abstract fun penginapanDao(): PenginapanDao

    companion object {
        @Volatile
        private var INSTANCE: PenginapanDatabase? = null

        fun getDatabase(context: android.content.Context): PenginapanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PenginapanDatabase::class.java,
                    "penginapan_db"
                ).fallbackToDestructiveMigration() // Tambahkan ini untuk reset database jika schema berubah
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


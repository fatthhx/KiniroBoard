package com.example.kiniroboard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Obra::class, Capitulo::class, Cronologia::class, EntradaCronologia::class],
    version = 4,
    exportSchema = false
)
abstract class KiniroDatabase : RoomDatabase() {

    abstract fun kiniroDao(): KiniroDao

    companion object {
        @Volatile
        private var INSTANCE: KiniroDatabase? = null

        fun getDatabase(context: Context): KiniroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KiniroDatabase::class.java,
                    "kiniro_board_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
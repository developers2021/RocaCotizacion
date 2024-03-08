package com.example.rocacotizacion.DAO

import android.app.Application
import android.content.Context
import androidx.room.Room

class DatabaseApplication : Application() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "RocaPedidosDB"
                )
                    // Migration strategy for when the schema changes
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = getDatabase(applicationContext)
    }
}

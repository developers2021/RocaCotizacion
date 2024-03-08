package com.example.rocacotizacion.DAO

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Agente::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AgenteDAO(): AgenteDAO
}
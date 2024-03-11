package com.example.rocacotizacion.DAO

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Agente::class,Clientes::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AgenteDAO(): AgenteDAO
    abstract fun ClientesDAO():ClientesDAO
}
package com.example.rocacotizacion.DAO

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Agente::class,Clientes::class,Productos::class,Grupos::class,PreciosNivelTipoVenta::class,NivelPrecioPredeterminado::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AgenteDAO(): AgenteDAO
    abstract fun ClientesDAO():ClientesDAO
    abstract fun ProductosDAO():ProductosDAO
    abstract fun GrupoDAO():GrupoDAO
    abstract fun PreciosNivelTipoVentaDAO():PreciosNivelTipoVentaDAO
    abstract fun NivelPrecioPredeterminadoDAO():NivelPrecioPredeterminadoDAO
}
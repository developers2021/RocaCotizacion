package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rocacotizacion.DTO.ProductoConPrecio

@Dao
interface ProductosDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(productos: MutableList<Productos>)
    @Query("SELECT * FROM productos")
    fun getProductos(): List<Productos>

    @Query("DELETE FROM productos")
     fun deleteAll()

    @Query(" SELECT p.*, pn.precio FROM Productos p JOIN PreciosNivelTipoVenta pn ON p.idproducto = pn.idproducto WHERE pn.codigotipoventa = :codigoTipoVenta")
    fun getProductosConPrecio(codigoTipoVenta: String): List<ProductoConPrecio>

    @Query("""
        SELECT p.idproducto, p.codigoproducto, p.producto, p.idgrupo, p.grupo, 
        p.idtipoproducto, p.costoactual, p.idimpuesto, p.porcentajeimpuesto,
        pn.precio,p.descuento 
        FROM Productos p 
        INNER JOIN PreciosNivelTipoVenta pn ON p.idproducto = pn.idproducto 
        WHERE p.idproducto = :idProducto 
        AND pn.codigotipoventa = :codigoTipoVenta
    """)
    fun getProductoConPrecio(idProducto: Int, codigoTipoVenta: String): ProductoConPrecio

}
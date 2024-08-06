package com.example.rocacotizacion.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rocacotizacion.DTO.ProductoDetalleDTO
import com.example.rocacotizacion.ui.PrintClass.ProductDetail

@Dao
interface PedidoDtlDAO {
    @Insert
    fun insertPedidoDtl(pedidoDtl: PedidoDtl)
    @Query("SELECT SUM(cantidad * precio) FROM pedido_dtl WHERE idhdr = :idhdr")
    fun getSumCantidadPrecioByHdrId(idhdr: Int): LiveData<Double>
    @Query("DELETE FROM pedido_dtl")
    fun deleteAll()
    @Query("SELECT * FROM pedido_dtl WHERE idhdr = :idhdr")
    fun getDetailsByPedidoId(idhdr: Int): LiveData<List<PedidoDtl>>
    @Query ("DELETE FROM pedido_dtl where idhdr=:idhdr")
    fun deletedtlid(idhdr: Int)
    @Query("""
        SELECT p.idproducto, p.codigoproducto, p.producto, p.idgrupo, p.grupo,
               p.idtipoproducto, p.costoactual, p.idimpuesto, p.porcentajeimpuesto,
               pn.precio
        FROM pedido_dtl d
        INNER JOIN Productos p ON d.codigoproducto = p.codigoproducto
        INNER JOIN PreciosNivelTipoVenta pn ON p.idproducto = pn.idproducto
        WHERE d.idhdr = :idhdr AND pn.codigotipoventa = :codigoTipoVenta
    """)
    fun getDetailedProductsByPedidoId(idhdr: Int, codigoTipoVenta: String): LiveData<List<ProductoDetalleDTO>>
    @Query("SELECT * FROM pedido_dtl WHERE idhdr = :idhdr")
    fun getAllDetailsByHeaderId(idhdr: Int): List<PedidoDtl>
    @Query("SELECT COUNT(*) FROM pedido_dtl")
    fun countAll(): Int

    @Query("""
        SELECT d.nombre as prod,d.cantidad as und,d.precio,d.precio*d.cantidad as monto,d.descuento,d.impuesto 
        FROM pedido_dtl d
        INNER JOIN Productos p ON d.codigoproducto = p.codigoproducto
        INNER JOIN PreciosNivelTipoVenta pn ON p.idproducto = pn.idproducto
        WHERE d.idhdr = :idhdr """)
    fun getDetallePrint(idhdr: Int): List<ProductDetail>

}

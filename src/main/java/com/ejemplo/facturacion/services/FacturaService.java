package com.ejemplo.facturacion.services;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ejemplo.facturacion.valueobjects.Articulo;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;

@Service
public class FacturaService {
    private Map<String, Optional<Factura>> facturas = new HashMap<>();

    @Autowired @Lazy
    private FacturaService autoReferencia;

    public Factura generarFactura(final Orden orden) throws InterruptedException {
        Factura factura = new Factura();

        String idFactura = generarIdFactura();
        facturas.put(idFactura, Optional.empty());

        Thread.sleep(5000);

        BigDecimal subtotal = calcularSubtotal(orden.getArticulos());
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.16)).setScale(2, RoundingMode.UP);
        BigDecimal total = subtotal.add(iva);

        factura.setId(idFactura);
        factura.setArticulos(orden.getArticulos());
        factura.setSubtotal(subtotal);
        factura.setIva(iva);
        factura.setTotal(total);

        facturas.put(factura.getId(), Optional.of(factura));

        return factura;
    }

    public String iniciarFacturaAsincrona(final Orden orden) throws InterruptedException {
        String idFactura = generarIdFactura();
        return idFactura;
    }

    public Optional<Factura> obtenerFacturaAsincrona(final String idFactura) {
        return facturas.get(idFactura);
    }

    public Factura generarFactura(final String idFactura, final Orden orden) throws InterruptedException {
        Factura factura = new Factura();

        String id = idFactura;
        facturas.put(id, Optional.empty());

        Thread.sleep(20000);

        BigDecimal subtotal = calcularSubtotal(orden.getArticulos());
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.16)).setScale(2, RoundingMode.UP);
        BigDecimal total = subtotal.add(iva);

        factura.setId(id);
        factura.setArticulos(orden.getArticulos());
        factura.setSubtotal(subtotal);
        factura.setIva(iva);
        factura.setTotal(total);

        facturas.put(factura.getId(), Optional.of(factura));

        return factura;
    }

    @Async 
    public CompletableFuture<Void> crearFacturaAsincrona(final String idFactura, final Orden orden) throws InterruptedException {
        return CompletableFuture.runAsync(() -> {
            try{
                generarFactura(idFactura, orden);
            }
            catch(InterruptedException e){
                throw new RuntimeException("Operation interrupted", e);
            }
        });
    }

    private BigDecimal calcularSubtotal(List<Articulo> articulos) {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (articulos != null) {
            for (final Articulo articulo : articulos) {
                BigDecimal precioUnitario = articulo.getPrecioUnitario();
                BigDecimal cantidad = BigDecimal.valueOf(articulo.getCantidad());
                BigDecimal totalArticulo = precioUnitario.multiply(cantidad);
                subtotal = subtotal.add(totalArticulo);
            }
        }

        return subtotal;
    }

    private String generarIdFactura() {
        return String.valueOf(Instant.now().toEpochMilli());
    }

    public Map<String, Optional<Factura>> getFacturas() {
        return facturas;
    }

    public void setFacturas(Map<String, Optional<Factura>> facturas) {
        this.facturas = facturas;
    }
}

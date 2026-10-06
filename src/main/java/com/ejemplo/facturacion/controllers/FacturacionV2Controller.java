package com.ejemplo.facturacion.controllers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.management.RuntimeErrorException;

import java.util.Map;

import org.aspectj.apache.bcel.generic.RET;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ejemplo.facturacion.services.FacturaService;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;

@RestController
public class FacturacionV2Controller {
    @Autowired FacturaService facturaService;

    @PostMapping("/v2/factura")
    public ResponseEntity<String> calcularFactura(@RequestBody Orden orden) {
        try{
            String id = facturaService.iniciarFacturaAsincrona(orden);
            facturaService.crearFacturaAsincrona(id, orden);
            return ResponseEntity.accepted().body(id);
        }
        catch(InterruptedException e){
            throw new RuntimeException("Error: ",e);
        }   
    }

    @GetMapping("/v2/factura/{idFactura}")
    public ResponseEntity<Factura> buscarFactura(@PathVariable String idFactura) {
        Optional<Factura> factura = facturaService.obtenerFacturaAsincrona(idFactura);

        if (factura == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  //404
        }
        else if(factura.isPresent()){
            return new ResponseEntity<>(factura.get(), HttpStatus.OK); //200
        }
        else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);  //204
        }
    }
}

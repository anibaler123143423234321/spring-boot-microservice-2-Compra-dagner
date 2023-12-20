package com.dagnerchuman.springbootmicroservice2Compra.controller;

import com.dagnerchuman.springbootmicroservice2Compra.model.Compra;
import com.dagnerchuman.springbootmicroservice2Compra.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("api/compra")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    public ResponseEntity<?> saveCompra(@RequestBody Compra compra) {
        // Verifica si el cargo por entrega ya se aplicó para este userId en los últimos 1 minuto
        if (!compra.isCargoDeliveryAplicado() || ChronoUnit.MINUTES.between(compra.getUltimaVezDelivery(), LocalDateTime.now()) >= 1) {
            // Actualiza la marca de tiempo y marca el cargo como aplicado
            compra.setUltimaVezDelivery(LocalDateTime.now());
            compra.setCargoDeliveryAplicado(true);

            // Calcula el total multiplicando precio por cantidad
            double total = compra.getPrecioCompra() * compra.getCantidad();

            // Verifica si el tipo de envío es igual a "Delivery"
            if ("Delivery".equals(compra.getTipoEnvio())) {
                // Si es "Delivery", suma el cargo por entrega
                total += compra.getCargoDelivery();
            }

            compra.setPrecioCompra(total);

            Compra nuevaCompra = compraService.saveCompra(compra);
            return ResponseEntity.ok(nuevaCompra);
        } else {
            // Si el cargo ya se aplicó en los últimos 1 minuto, realiza la compra sin aplicar el cargo por entrega
            Compra nuevaCompra = compraService.saveCompra(compra);
            return ResponseEntity.ok(nuevaCompra);
        }
    }


    @GetMapping("{userId}")
    public ResponseEntity<?> getAllComprasOfUser(@PathVariable Long userId)
    {
        return ResponseEntity.ok(compraService.findAllComprasOfUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompra(@PathVariable Long id, @RequestBody Compra compra) {
        try {
            Compra updatedCompra = compraService.updateCompra(id, compra);
            if (updatedCompra != null) {
                return ResponseEntity.ok(updatedCompra);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la compra: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCompras() {
        return ResponseEntity.ok(compraService.findAllCompras());
    }

}

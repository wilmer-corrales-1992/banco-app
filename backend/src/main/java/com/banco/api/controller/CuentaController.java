package com.banco.api.controller;

import com.banco.api.dto.CuentaPatchRequest;
import com.banco.api.dto.CuentaRequest;
import com.banco.api.dto.CuentaResponse;
import com.banco.api.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CuentaResponse> listar(@RequestParam(required = false) String clienteId) {
        return service.listar(clienteId);
    }

    @GetMapping("/{id}")
    public CuentaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        CuentaResponse creada = service.crear(request);
        return ResponseEntity.created(UbicacionRecurso.de(creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public CuentaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CuentaRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    public CuentaResponse actualizarParcial(@PathVariable Long id, @Valid @RequestBody CuentaPatchRequest request) {
        return service.actualizarParcial(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

package com.banco.api.controller;

import com.banco.api.dto.MovimientoPatchRequest;
import com.banco.api.dto.MovimientoRequest;
import com.banco.api.dto.MovimientoResponse;
import com.banco.api.service.MovimientoService;
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
@RequestMapping("/movimientos")
public class MovimientoController {

    private final MovimientoService service;

    public MovimientoController(MovimientoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MovimientoResponse> listar(@RequestParam(required = false) String numeroCuenta) {
        return service.listar(numeroCuenta);
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        MovimientoResponse registrado = service.registrar(request);
        return ResponseEntity.created(UbicacionRecurso.de(registrado.id())).body(registrado);
    }

    @PutMapping("/{id}")
    public MovimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}")
    public MovimientoResponse actualizarParcial(@PathVariable Long id,
                                                @Valid @RequestBody MovimientoPatchRequest request) {
        return service.actualizarParcial(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

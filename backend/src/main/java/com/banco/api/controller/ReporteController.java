package com.banco.api.controller;

import com.banco.api.dto.RangoFechas;
import com.banco.api.reporte.FormatoReporte;
import com.banco.api.reporte.ReporteFormateadorResolver;
import com.banco.api.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService service;
    private final ReporteFormateadorResolver formateadores;

    public ReporteController(ReporteService service, ReporteFormateadorResolver formateadores) {
        this.service = service;
        this.formateadores = formateadores;
    }

    /**
     * Ejemplo: /reportes?fecha=2026-09-01,2026-09-30&clienteId=jlema&formato=pdf
     */
    @GetMapping
    public Object generar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> fecha,
                          @RequestParam String clienteId,
                          @RequestParam(defaultValue = "json") String formato) {
        FormatoReporte formatoReporte = FormatoReporte.desde(formato);
        RangoFechas rango = RangoFechas.de(fecha);
        return formateadores.obtener(formatoReporte).formatear(service.generar(clienteId, rango));
    }
}

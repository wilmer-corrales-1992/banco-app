package com.banco.api.service;

import com.banco.api.dto.RangoFechas;
import com.banco.api.dto.ReporteResponse;

public interface ReporteService {

    ReporteResponse generar(String clienteId, RangoFechas rango);
}

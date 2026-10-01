package com.banco.api.reporte;

import com.banco.api.dto.ReporteResponse;

public interface ReporteFormateador {

    FormatoReporte formato();

    /**
     * Devuelve el cuerpo de la respuesta HTTP para este formato.
     */
    Object formatear(ReporteResponse reporte);
}

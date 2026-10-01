package com.banco.api.reporte;

import com.banco.api.dto.ReporteResponse;
import org.springframework.stereotype.Component;

@Component
public class JsonReporteFormateador implements ReporteFormateador {

    @Override
    public FormatoReporte formato() {
        return FormatoReporte.JSON;
    }

    @Override
    public ReporteResponse formatear(ReporteResponse reporte) {
        return reporte;
    }
}

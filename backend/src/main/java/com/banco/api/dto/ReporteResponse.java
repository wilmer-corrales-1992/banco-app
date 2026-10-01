package com.banco.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Estado de cuenta de un cliente. Los totales suman todas sus cuentas; los debitos van en valor absoluto.
 */
public record ReporteResponse(
        String clienteId,
        String cliente,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDateTime fechaGeneracion,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        List<ReporteCuenta> cuentas) {
}

package com.banco.api.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@ConfigurationProperties(prefix = "banco")
public record BancoProperties(
        @NotNull(message = "El limite diario de retiro es obligatorio")
        @DecimalMin(value = "0.00", message = "El limite diario de retiro no puede ser negativo")
        BigDecimal limiteDiarioRetiro) {
}

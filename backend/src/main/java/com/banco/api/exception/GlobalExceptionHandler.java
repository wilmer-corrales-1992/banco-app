package com.banco.api.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BancoException.class)
    public ResponseEntity<RespuestaError> manejarBanco(BancoException ex, HttpServletRequest request) {
        return responder(ex.getStatus(), ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        List<ErrorCampo> errores = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> new ErrorCampo(error.getField(), error.getDefaultMessage())),
                        ex.getBindingResult().getGlobalErrors().stream()
                                .map(error -> new ErrorCampo(error.getObjectName(), error.getDefaultMessage())))
                .sorted((a, b) -> a.campo().compareTo(b.campo()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "La solicitud tiene datos invalidos", request, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                                HttpServletRequest request) {
        if (ex.getCause() instanceof InvalidFormatException formato && formato.getTargetType().isEnum()) {
            String campo = formato.getPath().isEmpty()
                    ? "valor"
                    : formato.getPath().get(formato.getPath().size() - 1).getFieldName();
            String permitidos = Arrays.stream(formato.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return responder(HttpStatus.BAD_REQUEST,
                    "Valor invalido para " + campo + ". Valores permitidos: " + permitidos,
                    request, List.of(new ErrorCampo(campo, "Valores permitidos: " + permitidos)));
        }
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es valido", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> manejarTipoParametro(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        ResolvableType tipo = ResolvableType.forMethodParameter(ex.getParameter());
        boolean esFecha = Stream.of(tipo.resolve(), tipo.resolveGeneric(0)).anyMatch(LocalDate.class::equals);
        String formato = esFecha ? " (formato esperado yyyy-MM-dd)" : "";
        return responder(HttpStatus.BAD_REQUEST,
                "El parametro " + ex.getName() + " tiene un valor invalido" + formato, request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespuestaError> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                                   HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "El parametro " + ex.getParameterName() + " es obligatorio", request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaError> manejarRutaInexistente(NoResourceFoundException ex,
                                                                 HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Recurso no encontrado", request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaError> manejarMetodo(HttpRequestMethodNotSupportedException ex,
                                                       HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "Metodo " + ex.getMethod() + " no permitido en esta ruta", request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarIntegridad(DataIntegrityViolationException ex,
                                                            HttpServletRequest request) {
        log.warn("Violacion de integridad de datos en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de datos", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarGeneral(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse respuestaSpring) {
            HttpStatusCode codigo = respuestaSpring.getStatusCode();
            HttpStatus status = HttpStatus.resolve(codigo.value());
            String mensaje = status != null ? status.getReasonPhrase() : "Error en la solicitud";
            return responder(codigo, mensaje, request, List.of());
        }
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request, List.of());
    }

    private ResponseEntity<RespuestaError> responder(HttpStatusCode codigo, String mensaje,
                                                     HttpServletRequest request, List<ErrorCampo> errores) {
        HttpStatus status = HttpStatus.resolve(codigo.value());
        RespuestaError cuerpo = new RespuestaError(
                LocalDateTime.now(),
                codigo.value(),
                status != null ? status.getReasonPhrase() : String.valueOf(codigo.value()),
                mensaje,
                request.getRequestURI(),
                errores);
        return ResponseEntity.status(codigo).body(cuerpo);
    }
}

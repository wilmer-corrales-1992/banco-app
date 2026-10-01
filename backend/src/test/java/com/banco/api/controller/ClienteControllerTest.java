package com.banco.api.controller;

import com.banco.api.dto.ClientePatchRequest;
import com.banco.api.dto.ClienteRequest;
import com.banco.api.dto.ClienteResponse;
import com.banco.api.entity.Genero;
import com.banco.api.exception.OperacionNoPermitidaException;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final ClienteResponse JOSE = new ClienteResponse(
            1L, "jlema", "Jose Lema", Genero.MASCULINO, 30, "1700000001",
            "Otavalo sn y principal", "098254785", true);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    void listarDevuelveLosClientes() throws Exception {
        when(service.listar()).thenReturn(List.of(JOSE));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clienteId").value("jlema"))
                .andExpect(jsonPath("$[0].contrasena").doesNotExist());
    }

    @Test
    void crearDevuelve201ConUbicacionYSinContrasena() throws Exception {
        when(service.crear(any(ClienteRequest.class))).thenReturn(JOSE);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Jose Lema",
                                  "genero": "MASCULINO",
                                  "edad": 30,
                                  "identificacion": "1700000001",
                                  "direccion": "Otavalo sn y principal",
                                  "telefono": "098254785",
                                  "clienteId": "jlema",
                                  "contrasena": "1234"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/clientes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void crearSinDatosObligatoriosDevuelve400ConErroresPorCampo() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "edad": 130, "clienteId": "x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("La solicitud tiene datos invalidos"))
                .andExpect(jsonPath("$.ruta").value("/clientes"))
                .andExpect(jsonPath("$.errores[*].campo", containsInAnyOrder(
                        "nombre", "genero", "edad", "identificacion", "direccion", "telefono", "contrasena")));
        verify(service, never()).crear(any());
    }

    @Test
    void actualizarConPutNoExigeContrasena() throws Exception {
        when(service.actualizar(eq(1L), any(ClienteRequest.class))).thenReturn(JOSE);

        mockMvc.perform(put("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Jose Lema",
                                  "genero": "MASCULINO",
                                  "edad": 30,
                                  "identificacion": "1700000001",
                                  "direccion": "Otavalo sn y principal",
                                  "telefono": "098254785",
                                  "clienteId": "jlema"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("jlema"));
    }

    @Test
    void actualizarParcialConGeneroInvalidoIndicaLosValoresPermitidos() throws Exception {
        mockMvc.perform(patch("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"genero": "X"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Valor invalido para genero. Valores permitidos: MASCULINO, FEMENINO, OTRO"));
        verify(service, never()).actualizarParcial(any(), any(ClientePatchRequest.class));
    }

    @Test
    void obtenerUnClienteInexistenteDevuelve404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("Cliente no encontrado con id 99"));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con id 99"))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    void obtenerConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/clientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parametro id tiene un valor invalido"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }

    @Test
    void eliminarUnClienteConCuentasDevuelve409() throws Exception {
        doThrow(new OperacionNoPermitidaException("No se puede eliminar el cliente jlema porque tiene cuentas asociadas"))
                .when(service).eliminar(1L);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("No se puede eliminar el cliente jlema porque tiene cuentas asociadas"));
    }
}

package com.banco.api.service.impl;

import com.banco.api.DatosPrueba;
import com.banco.api.dto.ClientePatchRequest;
import com.banco.api.dto.ClienteRequest;
import com.banco.api.dto.ClienteResponse;
import com.banco.api.entity.Cliente;
import com.banco.api.entity.Genero;
import com.banco.api.exception.OperacionNoPermitidaException;
import com.banco.api.exception.RecursoDuplicadoException;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.mapper.ClienteMapper;
import com.banco.api.repository.ClienteRepository;
import com.banco.api.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private ClienteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ClienteServiceImpl(clienteRepository, cuentaRepository, new ClienteMapper(), passwordEncoder);
    }

    @Test
    void crearCifraLaContrasenaConBcrypt() {
        when(clienteRepository.findByClienteId("aprueba")).thenReturn(Optional.empty());
        when(clienteRepository.findByIdentificacion("1700000009")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente cliente = inv.getArgument(0);
            cliente.setId(10L);
            return cliente;
        });

        ClienteResponse respuesta = service.crear(nuevoCliente("aprueba", "1700000009", "1234"));

        ArgumentCaptor<Cliente> guardado = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(guardado.capture());
        assertThat(guardado.getValue().getContrasena()).startsWith("$2a$").isNotEqualTo("1234");
        assertThat(passwordEncoder.matches("1234", guardado.getValue().getContrasena())).isTrue();
        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(respuesta.estado()).isTrue();
    }

    @Test
    void crearConClienteIdRepetidoLanzaDuplicado() {
        when(clienteRepository.findByClienteId("jlema")).thenReturn(Optional.of(DatosPrueba.joseLema()));

        assertThatThrownBy(() -> service.crear(nuevoCliente("jlema", "1700000009", "1234")))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("jlema");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearConIdentificacionRepetidaLanzaDuplicado() {
        when(clienteRepository.findByClienteId("nuevo")).thenReturn(Optional.empty());
        when(clienteRepository.findByIdentificacion("1700000001")).thenReturn(Optional.of(DatosPrueba.joseLema()));

        assertThatThrownBy(() -> service.crear(nuevoCliente("nuevo", "1700000001", "1234")))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("identificacion");
    }

    @Test
    void actualizarConservandoSusPropiosDatosUnicosNoEsDuplicado() {
        Cliente jose = DatosPrueba.joseLema();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(jose));
        when(clienteRepository.findByClienteId("jlema")).thenReturn(Optional.of(jose));
        when(clienteRepository.findByIdentificacion("1700000001")).thenReturn(Optional.of(jose));
        when(clienteRepository.saveAndFlush(jose)).thenReturn(jose);
        String contrasenaAnterior = jose.getContrasena();

        ClienteResponse respuesta = service.actualizar(1L, new ClienteRequest(
                "Jose Lema Editado", Genero.MASCULINO, 31, "1700000001", "Otavalo", "0999", "jlema", null, true));

        assertThat(respuesta.nombre()).isEqualTo("Jose Lema Editado");
        assertThat(respuesta.edad()).isEqualTo(31);
        assertThat(jose.getContrasena()).isEqualTo(contrasenaAnterior);
    }

    @Test
    void actualizarParcialSoloCambiaLosCamposEnviados() {
        Cliente jose = DatosPrueba.joseLema();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(jose));
        when(clienteRepository.saveAndFlush(jose)).thenReturn(jose);

        ClienteResponse respuesta = service.actualizarParcial(1L, new ClientePatchRequest(
                null, null, null, null, null, null, null, null, false));

        assertThat(respuesta.estado()).isFalse();
        assertThat(respuesta.nombre()).isEqualTo("Jose Lema");
        assertThat(respuesta.direccion()).isEqualTo("Otavalo sn y principal");
    }

    @Test
    void noSeEliminaUnClienteConCuentas() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(DatosPrueba.joseLema()));
        when(cuentaRepository.existsByCliente_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("cuentas");
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void obtenerUnClienteInexistenteLanzaNoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    private static ClienteRequest nuevoCliente(String clienteId, String identificacion, String contrasena) {
        return new ClienteRequest("Ana Prueba", Genero.FEMENINO, 40, identificacion,
                "Calle 1", "0999", clienteId, contrasena, null);
    }
}

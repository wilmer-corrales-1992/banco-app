package com.banco.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "persona_id")
public class Cliente extends Persona {

    @NotBlank(message = "El clienteId es obligatorio")
    @Size(max = 30, message = "El clienteId no puede superar 30 caracteres")
    @Column(name = "cliente_id", nullable = false, length = 30, unique = true)
    private String clienteId;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(max = 100, message = "La contrasena no puede superar 100 caracteres")
    @Column(nullable = false, length = 100)
    private String contrasena;

    @NotNull(message = "El estado es obligatorio")
    @Column(nullable = false)
    private Boolean estado = Boolean.TRUE;

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}

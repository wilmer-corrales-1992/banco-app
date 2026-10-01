package com.banco.api.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

final class UbicacionRecurso {

    private UbicacionRecurso() {
    }

    static URI de(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}

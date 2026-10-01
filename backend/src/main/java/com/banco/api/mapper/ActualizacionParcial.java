package com.banco.api.mapper;

import java.util.Optional;
import java.util.function.Consumer;

final class ActualizacionParcial {

    private ActualizacionParcial() {
    }

    static <T> void siPresente(T valor, Consumer<T> setter) {
        Optional.ofNullable(valor).ifPresent(setter);
    }
}

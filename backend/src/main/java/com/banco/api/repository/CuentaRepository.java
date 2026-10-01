package com.banco.api.repository;

import com.banco.api.entity.Cuenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByCliente_Id(Long clienteId);

    @EntityGraph(attributePaths = "cliente")
    List<Cuenta> findAllByOrderByNumeroCuentaAsc();

    @EntityGraph(attributePaths = "cliente")
    List<Cuenta> findByCliente_ClienteId(String clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> findByNumeroCuentaParaActualizar(@Param("numeroCuenta") String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.id = :id")
    Optional<Cuenta> findByIdParaActualizar(@Param("id") Long id);
}

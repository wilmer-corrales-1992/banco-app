package com.banco.api.repository;

import com.banco.api.entity.Movimiento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    Optional<Movimiento> findFirstByCuentaIdOrderByFechaDescIdDesc(Long cuentaId);

    Optional<Movimiento> findFirstByCuentaIdAndFechaLessThanOrderByFechaDescIdDesc(Long cuentaId,
                                                                                  LocalDateTime hasta);

    boolean existsByCuenta_Id(Long cuentaId);

    @Query("""
            select m
            from Movimiento m
            join fetch m.cuenta c
            join fetch c.cliente cl
            where cl.clienteId = :clienteId
              and m.fecha >= :desde
              and m.fecha < :hasta
            order by c.numeroCuenta, m.fecha, m.id
            """)
    List<Movimiento> findDelClienteEntre(@Param("clienteId") String clienteId,
                                         @Param("desde") LocalDateTime desde,
                                         @Param("hasta") LocalDateTime hasta);

    @EntityGraph(attributePaths = {"cuenta", "cuenta.cliente"})
    List<Movimiento> findAllByOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"cuenta", "cuenta.cliente"})
    List<Movimiento> findByCuenta_NumeroCuentaOrderByFechaDescIdDesc(String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Movimiento m where m.id = :id")
    Optional<Movimiento> findByIdParaActualizar(@Param("id") Long id);

    @Query("""
            select coalesce(sum(abs(m.valor)), 0)
            from Movimiento m
            where m.cuenta.id = :cuentaId
              and m.tipoMovimiento = com.banco.api.entity.TipoMovimiento.DEBITO
              and m.fecha >= :desde
              and m.fecha < :hasta
            """)
    BigDecimal totalDebitosEntre(@Param("cuentaId") Long cuentaId,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta);
}

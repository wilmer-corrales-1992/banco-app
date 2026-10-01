import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { MovimientoApi } from '../../core/api/movimiento-api';
import { ErrorApi } from '../../core/http/error-api';
import { Movimiento } from '../../core/models/movimiento';
import { Notificaciones } from '../../core/notificaciones/notificaciones';
import { DialogoConfirmacion } from '../../shared/componentes/dialogo-confirmacion';
import { EncabezadoPagina } from '../../shared/componentes/encabezado-pagina';
import { Modal } from '../../shared/componentes/modal';
import { AccionesFila, ColumnaTabla, TablaDatos } from '../../shared/componentes/tabla-datos';
import {
  claseValor,
  formatearFechaHora,
  formatearMoneda,
  nombreTipoCuenta,
  nombreTipoMovimiento,
} from '../../shared/utilidades/formato';
import { ListadoConBusqueda } from '../../shared/utilidades/listado-con-busqueda';
import { MovimientoFormulario } from './movimiento-formulario';

@Component({
  selector: 'app-movimientos-pagina',
  imports: [
    EncabezadoPagina,
    TablaDatos,
    AccionesFila,
    Modal,
    DialogoConfirmacion,
    MovimientoFormulario,
  ],
  templateUrl: './movimientos-pagina.html',
})
export class MovimientosPagina {
  private readonly api = inject(MovimientoApi);
  private readonly notificaciones = inject(Notificaciones);

  protected readonly columnas: readonly ColumnaTabla<Movimiento>[] = [
    { titulo: 'Fecha', valor: (m) => formatearFechaHora(m.fecha), clase: () => 'sin-salto' },
    { titulo: 'Número de cuenta', valor: (m) => m.numeroCuenta },
    { titulo: 'Tipo de cuenta', valor: (m) => nombreTipoCuenta(m.tipoCuenta) },
    { titulo: 'Cliente', valor: (m) => m.cliente },
    { titulo: 'Movimiento', valor: (m) => nombreTipoMovimiento(m.tipoMovimiento) },
    {
      titulo: 'Valor',
      valor: (m) => formatearMoneda(m.valor),
      alineacion: 'derecha',
      clase: (m) => claseValor(m.valor),
    },
    {
      titulo: 'Saldo disponible',
      valor: (m) => formatearMoneda(m.saldo),
      alineacion: 'derecha',
      clase: () => 'valor',
    },
  ];

  protected readonly listado = new ListadoConBusqueda<Movimiento>((m) =>
    this.columnas.map((col) => col.valor(m)),
  );
  protected readonly identificador = (movimiento: Movimiento) => movimiento.id;

  protected readonly formularioAbierto = signal(false);
  protected readonly seleccionado = signal<Movimiento | null>(null);
  protected readonly porEliminar = signal<Movimiento | null>(null);
  protected readonly eliminando = signal(false);

  protected readonly formatearMoneda = formatearMoneda;
  protected readonly nombreTipoMovimiento = nombreTipoMovimiento;

  constructor() {
    this.cargar();
  }

  protected nuevo(): void {
    this.seleccionado.set(null);
    this.formularioAbierto.set(true);
  }

  protected editar(movimiento: Movimiento): void {
    this.seleccionado.set(movimiento);
    this.formularioAbierto.set(true);
  }

  protected alGuardar(movimiento: Movimiento): void {
    const accion = this.seleccionado() ? 'actualizado' : 'registrado';
    this.formularioAbierto.set(false);
    this.notificaciones.exito(
      `${nombreTipoMovimiento(movimiento.tipoMovimiento)} ${accion} en la cuenta ${movimiento.numeroCuenta}. ` +
        `Saldo disponible: ${formatearMoneda(movimiento.saldo)}`,
    );
    this.cargar();
  }

  protected eliminar(): void {
    const movimiento = this.porEliminar();
    if (!movimiento) {
      return;
    }
    this.eliminando.set(true);
    this.api
      .eliminar(movimiento.id)
      .pipe(
        finalize(() => {
          this.eliminando.set(false);
          this.porEliminar.set(null);
        }),
      )
      .subscribe({
        next: () => {
          this.notificaciones.exito(`Movimiento de la cuenta ${movimiento.numeroCuenta} eliminado`);
          this.cargar();
        },
        error: (error: ErrorApi) => this.notificaciones.error(error.message),
      });
  }

  private cargar(): void {
    this.listado.cargar(this.api.listar(), (error) => this.notificaciones.error(error.message));
  }
}

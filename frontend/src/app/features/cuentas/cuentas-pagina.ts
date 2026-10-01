import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { CuentaApi } from '../../core/api/cuenta-api';
import { ErrorApi } from '../../core/http/error-api';
import { Cuenta } from '../../core/models/cuenta';
import { Notificaciones } from '../../core/notificaciones/notificaciones';
import { DialogoConfirmacion } from '../../shared/componentes/dialogo-confirmacion';
import { EncabezadoPagina } from '../../shared/componentes/encabezado-pagina';
import { Modal } from '../../shared/componentes/modal';
import { AccionesFila, ColumnaTabla, TablaDatos } from '../../shared/componentes/tabla-datos';
import {
  claseEstado,
  formatearMoneda,
  nombreTipoCuenta,
  textoEstado,
} from '../../shared/utilidades/formato';
import { ListadoConBusqueda } from '../../shared/utilidades/listado-con-busqueda';
import { CuentaFormulario } from './cuenta-formulario';

@Component({
  selector: 'app-cuentas-pagina',
  imports: [
    EncabezadoPagina,
    TablaDatos,
    AccionesFila,
    Modal,
    DialogoConfirmacion,
    CuentaFormulario,
  ],
  templateUrl: './cuentas-pagina.html',
})
export class CuentasPagina {
  private readonly api = inject(CuentaApi);
  private readonly notificaciones = inject(Notificaciones);

  protected readonly columnas: readonly ColumnaTabla<Cuenta>[] = [
    { titulo: 'Número de cuenta', valor: (c) => c.numeroCuenta },
    { titulo: 'Tipo', valor: (c) => nombreTipoCuenta(c.tipoCuenta) },
    {
      titulo: 'Saldo inicial',
      valor: (c) => formatearMoneda(c.saldoInicial),
      alineacion: 'derecha',
      clase: () => 'valor',
    },
    { titulo: 'Estado', valor: (c) => textoEstado(c.estado), clase: (c) => claseEstado(c.estado) },
    { titulo: 'Cliente', valor: (c) => c.cliente },
  ];

  protected readonly listado = new ListadoConBusqueda<Cuenta>((c) => [
    ...this.columnas.map((col) => col.valor(c)),
    c.clienteId,
  ]);
  protected readonly identificador = (cuenta: Cuenta) => cuenta.id;

  protected readonly formularioAbierto = signal(false);
  protected readonly seleccionada = signal<Cuenta | null>(null);
  protected readonly porEliminar = signal<Cuenta | null>(null);
  protected readonly eliminando = signal(false);

  constructor() {
    this.cargar();
  }

  protected nueva(): void {
    this.seleccionada.set(null);
    this.formularioAbierto.set(true);
  }

  protected editar(cuenta: Cuenta): void {
    this.seleccionada.set(cuenta);
    this.formularioAbierto.set(true);
  }

  protected alGuardar(cuenta: Cuenta): void {
    const accion = this.seleccionada() ? 'actualizada' : 'creada';
    this.formularioAbierto.set(false);
    this.notificaciones.exito(`Cuenta ${cuenta.numeroCuenta} ${accion}`);
    this.cargar();
  }

  protected cambiarEstado(cuenta: Cuenta): void {
    this.api.actualizarParcial(cuenta.id, { estado: !cuenta.estado }).subscribe({
      next: (actualizada) => {
        this.notificaciones.exito(
          `Cuenta ${actualizada.numeroCuenta} ${actualizada.estado ? 'activada' : 'desactivada'}`,
        );
        this.cargar();
      },
      error: (error: ErrorApi) => this.notificaciones.error(error.message),
    });
  }

  protected eliminar(): void {
    const cuenta = this.porEliminar();
    if (!cuenta) {
      return;
    }
    this.eliminando.set(true);
    this.api
      .eliminar(cuenta.id)
      .pipe(
        finalize(() => {
          this.eliminando.set(false);
          this.porEliminar.set(null);
        }),
      )
      .subscribe({
        next: () => {
          this.notificaciones.exito(`Cuenta ${cuenta.numeroCuenta} eliminada`);
          this.cargar();
        },
        error: (error: ErrorApi) => this.notificaciones.error(error.message),
      });
  }

  private cargar(): void {
    this.listado.cargar(this.api.listar(), (error) => this.notificaciones.error(error.message));
  }
}

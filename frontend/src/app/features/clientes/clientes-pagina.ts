import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ClienteApi } from '../../core/api/cliente-api';
import { ErrorApi } from '../../core/http/error-api';
import { Cliente } from '../../core/models/cliente';
import { Notificaciones } from '../../core/notificaciones/notificaciones';
import { DialogoConfirmacion } from '../../shared/componentes/dialogo-confirmacion';
import { EncabezadoPagina } from '../../shared/componentes/encabezado-pagina';
import { Modal } from '../../shared/componentes/modal';
import { AccionesFila, ColumnaTabla, TablaDatos } from '../../shared/componentes/tabla-datos';
import { claseEstado, nombreGenero, textoEstado } from '../../shared/utilidades/formato';
import { ListadoConBusqueda } from '../../shared/utilidades/listado-con-busqueda';
import { ClienteFormulario } from './cliente-formulario';

@Component({
  selector: 'app-clientes-pagina',
  imports: [
    EncabezadoPagina,
    TablaDatos,
    AccionesFila,
    Modal,
    DialogoConfirmacion,
    ClienteFormulario,
  ],
  templateUrl: './clientes-pagina.html',
})
export class ClientesPagina {
  private readonly api = inject(ClienteApi);
  private readonly notificaciones = inject(Notificaciones);

  protected readonly columnas: readonly ColumnaTabla<Cliente>[] = [
    { titulo: 'Usuario', valor: (c) => c.clienteId },
    { titulo: 'Nombre', valor: (c) => c.nombre },
    { titulo: 'Identificación', valor: (c) => c.identificacion },
    { titulo: 'Género', valor: (c) => nombreGenero(c.genero) },
    { titulo: 'Edad', valor: (c) => c.edad, alineacion: 'derecha' },
    { titulo: 'Dirección', valor: (c) => c.direccion },
    { titulo: 'Teléfono', valor: (c) => c.telefono },
    { titulo: 'Estado', valor: (c) => textoEstado(c.estado), clase: (c) => claseEstado(c.estado) },
  ];

  protected readonly listado = new ListadoConBusqueda<Cliente>((c) =>
    this.columnas.map((col) => col.valor(c)),
  );
  protected readonly identificador = (cliente: Cliente) => cliente.id;

  protected readonly formularioAbierto = signal(false);
  protected readonly seleccionado = signal<Cliente | null>(null);
  protected readonly porEliminar = signal<Cliente | null>(null);
  protected readonly eliminando = signal(false);

  constructor() {
    this.cargar();
  }

  protected nuevo(): void {
    this.seleccionado.set(null);
    this.formularioAbierto.set(true);
  }

  protected editar(cliente: Cliente): void {
    this.seleccionado.set(cliente);
    this.formularioAbierto.set(true);
  }

  protected alGuardar(cliente: Cliente): void {
    const accion = this.seleccionado() ? 'actualizado' : 'creado';
    this.formularioAbierto.set(false);
    this.notificaciones.exito(`Cliente ${cliente.nombre} ${accion}`);
    this.cargar();
  }

  protected cambiarEstado(cliente: Cliente): void {
    this.api.actualizarParcial(cliente.id, { estado: !cliente.estado }).subscribe({
      next: (actualizado) => {
        this.notificaciones.exito(
          `Cliente ${actualizado.nombre} ${actualizado.estado ? 'activado' : 'desactivado'}`,
        );
        this.cargar();
      },
      error: (error: ErrorApi) => this.notificaciones.error(error.message),
    });
  }

  protected eliminar(): void {
    const cliente = this.porEliminar();
    if (!cliente) {
      return;
    }
    this.eliminando.set(true);
    this.api
      .eliminar(cliente.id)
      .pipe(
        finalize(() => {
          this.eliminando.set(false);
          this.porEliminar.set(null);
        }),
      )
      .subscribe({
        next: () => {
          this.notificaciones.exito(`Cliente ${cliente.nombre} eliminado`);
          this.cargar();
        },
        error: (error: ErrorApi) => this.notificaciones.error(error.message),
      });
  }

  private cargar(): void {
    this.listado.cargar(this.api.listar(), (error) => this.notificaciones.error(error.message));
  }
}

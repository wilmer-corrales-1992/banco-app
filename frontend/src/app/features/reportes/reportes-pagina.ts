import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, of } from 'rxjs';
import { ClienteApi } from '../../core/api/cliente-api';
import { FiltroReporte, ReporteApi } from '../../core/api/reporte-api';
import { ErrorApi } from '../../core/http/error-api';
import { Cliente } from '../../core/models/cliente';
import { Reporte, ReporteMovimiento } from '../../core/models/reporte';
import { Notificaciones } from '../../core/notificaciones/notificaciones';
import { CampoFormulario } from '../../shared/componentes/campo-formulario';
import { EncabezadoPagina } from '../../shared/componentes/encabezado-pagina';
import { ColumnaTabla, TablaDatos } from '../../shared/componentes/tabla-datos';
import { descargarBase64 } from '../../shared/utilidades/descarga';
import { fechaIso, primerDiaDelMes } from '../../shared/utilidades/fechas';
import {
  claseEstado,
  claseValor,
  formatearFecha,
  formatearFechaHora,
  formatearMoneda,
  nombreTipoCuenta,
  textoEstado,
} from '../../shared/utilidades/formato';
import { noVacio, rangoFechas } from '../../shared/utilidades/validadores';

@Component({
  selector: 'app-reportes-pagina',
  imports: [ReactiveFormsModule, EncabezadoPagina, CampoFormulario, TablaDatos],
  templateUrl: './reportes-pagina.html',
  styleUrl: './reportes-pagina.scss',
})
export class ReportesPagina {
  private readonly api = inject(ReporteApi);
  private readonly clienteApi = inject(ClienteApi);
  private readonly notificaciones = inject(Notificaciones);

  protected readonly clientes = toSignal(
    this.clienteApi.listar().pipe(
      catchError((error: ErrorApi) => {
        this.notificaciones.error(`No se pudieron cargar los clientes: ${error.message}`);
        return of<Cliente[]>([]);
      }),
    ),
    { initialValue: [] as Cliente[] },
  );

  protected readonly formulario = new FormGroup(
    {
      clienteId: new FormControl('', { nonNullable: true, validators: [noVacio] }),
      fechaInicio: new FormControl(fechaIso(primerDiaDelMes(new Date())), {
        nonNullable: true,
        validators: [Validators.required],
      }),
      fechaFin: new FormControl(fechaIso(new Date()), {
        nonNullable: true,
        validators: [Validators.required],
      }),
    },
    { validators: rangoFechas('fechaInicio', 'fechaFin') },
  );

  protected readonly reporte = signal<Reporte | null>(null);
  protected readonly generando = signal(false);
  protected readonly descargando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);

  protected readonly columnas: readonly ColumnaTabla<ReporteMovimiento>[] = [
    { titulo: 'Fecha', valor: (m) => formatearFechaHora(m.fecha), clase: () => 'sin-salto' },
    { titulo: 'Cliente', valor: (m) => m.cliente },
    { titulo: 'Número cuenta', valor: (m) => m.numeroCuenta },
    { titulo: 'Tipo', valor: (m) => nombreTipoCuenta(m.tipo) },
    {
      titulo: 'Saldo inicial',
      valor: (m) => formatearMoneda(m.saldoInicial),
      alineacion: 'derecha',
      clase: () => 'valor',
    },
    { titulo: 'Estado', valor: (m) => textoEstado(m.estado), clase: (m) => claseEstado(m.estado) },
    {
      titulo: 'Movimiento',
      valor: (m) => formatearMoneda(m.movimiento),
      alineacion: 'derecha',
      clase: (m) => claseValor(m.movimiento),
    },
    {
      titulo: 'Saldo disponible',
      valor: (m) => formatearMoneda(m.saldoDisponible),
      alineacion: 'derecha',
      clase: () => 'valor',
    },
  ];
  protected readonly identificadorFila = (fila: ReporteMovimiento) =>
    `${fila.fecha}-${fila.movimiento}-${fila.saldoDisponible}`;

  protected readonly formatearMoneda = formatearMoneda;
  protected readonly formatearFecha = formatearFecha;
  protected readonly formatearFechaHora = formatearFechaHora;
  protected readonly nombreTipoCuenta = nombreTipoCuenta;
  protected readonly textoEstado = textoEstado;

  protected generar(): void {
    const filtro = this.filtroValido();
    if (!filtro) {
      return;
    }
    this.generando.set(true);
    this.errorGeneral.set(null);
    this.api
      .generar(filtro)
      .pipe(finalize(() => this.generando.set(false)))
      .subscribe({
        next: (reporte) => this.reporte.set(reporte),
        error: (error: ErrorApi) => {
          this.reporte.set(null);
          this.errorGeneral.set(error.message);
        },
      });
  }

  protected descargarPdf(): void {
    const filtro = this.filtroValido();
    if (!filtro) {
      return;
    }
    this.descargando.set(true);
    this.errorGeneral.set(null);
    this.api
      .generarPdf(filtro)
      .pipe(finalize(() => this.descargando.set(false)))
      .subscribe({
        next: (pdf) => {
          descargarBase64(pdf.contenidoBase64, pdf.nombreArchivo, pdf.tipoContenido);
          this.notificaciones.exito(`Se descargó ${pdf.nombreArchivo}`);
        },
        error: (error: ErrorApi) => this.errorGeneral.set(error.message),
      });
  }

  private filtroValido(): FiltroReporte | null {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return null;
    }
    return this.formulario.getRawValue();
  }
}

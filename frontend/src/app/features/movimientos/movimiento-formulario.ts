import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, of } from 'rxjs';
import { CuentaApi } from '../../core/api/cuenta-api';
import { MovimientoApi } from '../../core/api/movimiento-api';
import { ErrorApi } from '../../core/http/error-api';
import { Cuenta } from '../../core/models/cuenta';
import {
  Movimiento,
  MovimientoSolicitud,
  TipoMovimiento,
  TIPOS_MOVIMIENTO,
} from '../../core/models/movimiento';
import { CampoFormulario } from '../../shared/componentes/campo-formulario';
import { aplicarErrorDelServidor } from '../../shared/utilidades/errores-formulario';
import { nombreTipoCuenta, nombreTipoMovimiento } from '../../shared/utilidades/formato';
import { maxDecimales, noVacio } from '../../shared/utilidades/validadores';

@Component({
  selector: 'app-movimiento-formulario',
  imports: [ReactiveFormsModule, CampoFormulario],
  templateUrl: './movimiento-formulario.html',
})
export class MovimientoFormulario implements OnInit {
  /** Movimiento a editar; null para registrar uno nuevo. */
  readonly movimiento = input<Movimiento | null>(null);
  readonly guardado = output<Movimiento>();
  readonly cancelado = output<void>();

  private readonly api = inject(MovimientoApi);
  private readonly cuentaApi = inject(CuentaApi);

  protected readonly tipos = TIPOS_MOVIMIENTO;
  protected readonly nombreTipoMovimiento = nombreTipoMovimiento;
  protected readonly nombreTipoCuenta = nombreTipoCuenta;
  protected readonly guardando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);
  protected readonly esEdicion = computed(() => this.movimiento() !== null);

  protected readonly cuentas = toSignal(
    this.cuentaApi.listar().pipe(
      catchError((error: ErrorApi) => {
        this.errorGeneral.set(`No se pudieron cargar las cuentas: ${error.message}`);
        return of<Cuenta[]>([]);
      }),
    ),
    { initialValue: [] as Cuenta[] },
  );

  protected readonly formulario = new FormGroup({
    numeroCuenta: new FormControl('', { nonNullable: true, validators: [noVacio] }),
    tipoMovimiento: new FormControl<TipoMovimiento | null>(null, Validators.required),
    valor: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(0.01),
      maxDecimales(2),
    ]),
  });

  protected readonly mensajesValor = { min: 'El valor debe ser mayor que cero' };

  ngOnInit(): void {
    const movimiento = this.movimiento();
    if (movimiento) {
      this.formulario.patchValue({
        numeroCuenta: movimiento.numeroCuenta,
        tipoMovimiento: movimiento.tipoMovimiento,
        valor: Math.abs(movimiento.valor),
      });
      this.formulario.controls.numeroCuenta.disable();
    }
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    const solicitud: MovimientoSolicitud = {
      numeroCuenta: valor.numeroCuenta,
      tipoMovimiento: valor.tipoMovimiento as TipoMovimiento,
      valor: Number(valor.valor),
    };
    const movimiento = this.movimiento();
    const peticion = movimiento
      ? this.api.actualizar(movimiento.id, solicitud)
      : this.api.crear(solicitud);

    this.guardando.set(true);
    this.errorGeneral.set(null);
    peticion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: (guardado) => this.guardado.emit(guardado),
      error: (error: ErrorApi) =>
        this.errorGeneral.set(aplicarErrorDelServidor(this.formulario, error)),
    });
  }
}

import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, of } from 'rxjs';
import { ClienteApi } from '../../core/api/cliente-api';
import { CuentaApi } from '../../core/api/cuenta-api';
import { ErrorApi } from '../../core/http/error-api';
import { Cliente } from '../../core/models/cliente';
import { Cuenta, CuentaSolicitud, TipoCuenta, TIPOS_CUENTA } from '../../core/models/cuenta';
import { CampoFormulario } from '../../shared/componentes/campo-formulario';
import { aplicarErrorDelServidor } from '../../shared/utilidades/errores-formulario';
import { nombreTipoCuenta } from '../../shared/utilidades/formato';
import { maxDecimales, noVacio } from '../../shared/utilidades/validadores';

@Component({
  selector: 'app-cuenta-formulario',
  imports: [ReactiveFormsModule, CampoFormulario],
  templateUrl: './cuenta-formulario.html',
})
export class CuentaFormulario implements OnInit {
  /** Cuenta a editar; null para crear una nueva. */
  readonly cuenta = input<Cuenta | null>(null);
  readonly guardado = output<Cuenta>();
  readonly cancelado = output<void>();

  private readonly api = inject(CuentaApi);
  private readonly clienteApi = inject(ClienteApi);

  protected readonly tipos = TIPOS_CUENTA;
  protected readonly nombreTipoCuenta = nombreTipoCuenta;
  protected readonly guardando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);
  protected readonly esEdicion = computed(() => this.cuenta() !== null);

  protected readonly clientes = toSignal(
    this.clienteApi.listar().pipe(
      catchError((error: ErrorApi) => {
        this.errorGeneral.set(`No se pudieron cargar los clientes: ${error.message}`);
        return of<Cliente[]>([]);
      }),
    ),
    { initialValue: [] as Cliente[] },
  );

  protected readonly formulario = new FormGroup({
    numeroCuenta: new FormControl('', {
      nonNullable: true,
      validators: [noVacio, Validators.maxLength(20), Validators.pattern(/^\d+$/)],
    }),
    tipoCuenta: new FormControl<TipoCuenta | null>(null, Validators.required),
    saldoInicial: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(0),
      maxDecimales(2),
    ]),
    clienteId: new FormControl('', { nonNullable: true, validators: [noVacio] }),
    estado: new FormControl(true, { nonNullable: true }),
  });

  protected readonly mensajesNumero = { pattern: 'Solo se admiten dígitos' };
  protected readonly mensajesSaldo = { min: 'El saldo inicial no puede ser negativo' };

  ngOnInit(): void {
    const cuenta = this.cuenta();
    if (cuenta) {
      this.formulario.patchValue(cuenta);
    }
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    const solicitud: CuentaSolicitud = {
      numeroCuenta: valor.numeroCuenta.trim(),
      tipoCuenta: valor.tipoCuenta as TipoCuenta,
      saldoInicial: Number(valor.saldoInicial),
      estado: valor.estado,
      clienteId: valor.clienteId,
    };
    const cuenta = this.cuenta();
    const peticion = cuenta ? this.api.actualizar(cuenta.id, solicitud) : this.api.crear(solicitud);

    this.guardando.set(true);
    this.errorGeneral.set(null);
    peticion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: (guardada) => this.guardado.emit(guardada),
      error: (error: ErrorApi) =>
        this.errorGeneral.set(aplicarErrorDelServidor(this.formulario, error)),
    });
  }
}

import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ClienteApi } from '../../core/api/cliente-api';
import { ErrorApi } from '../../core/http/error-api';
import { Cliente, ClienteSolicitud, Genero, GENEROS } from '../../core/models/cliente';
import { CampoFormulario } from '../../shared/componentes/campo-formulario';
import { aplicarErrorDelServidor } from '../../shared/utilidades/errores-formulario';
import { nombreGenero } from '../../shared/utilidades/formato';
import { entero, noVacio } from '../../shared/utilidades/validadores';

@Component({
  selector: 'app-cliente-formulario',
  imports: [ReactiveFormsModule, CampoFormulario],
  templateUrl: './cliente-formulario.html',
})
export class ClienteFormulario implements OnInit {
  /** Cliente a editar; null para crear uno nuevo. */
  readonly cliente = input<Cliente | null>(null);
  readonly guardado = output<Cliente>();
  readonly cancelado = output<void>();

  private readonly api = inject(ClienteApi);

  protected readonly generos = GENEROS;
  protected readonly nombreGenero = nombreGenero;
  protected readonly guardando = signal(false);
  protected readonly errorGeneral = signal<string | null>(null);
  protected readonly esEdicion = computed(() => this.cliente() !== null);

  protected readonly formulario = new FormGroup({
    nombre: new FormControl('', {
      nonNullable: true,
      validators: [noVacio, Validators.maxLength(100)],
    }),
    genero: new FormControl<Genero | null>(null, Validators.required),
    edad: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(0),
      Validators.max(120),
      entero,
    ]),
    identificacion: new FormControl('', {
      nonNullable: true,
      validators: [noVacio, Validators.maxLength(20)],
    }),
    direccion: new FormControl('', {
      nonNullable: true,
      validators: [noVacio, Validators.maxLength(200)],
    }),
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [noVacio, Validators.maxLength(20)],
    }),
    clienteId: new FormControl('', {
      nonNullable: true,
      validators: [noVacio, Validators.maxLength(30)],
    }),
    contrasena: new FormControl('', {
      nonNullable: true,
      validators: [Validators.minLength(4), Validators.maxLength(50)],
    }),
    estado: new FormControl(true, { nonNullable: true }),
  });

  ngOnInit(): void {
    const cliente = this.cliente();
    if (cliente) {
      this.formulario.patchValue({ ...cliente, contrasena: '' });
    } else {
      this.formulario.controls.contrasena.addValidators(noVacio);
    }
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    const solicitud: ClienteSolicitud = {
      nombre: valor.nombre.trim(),
      genero: valor.genero as Genero,
      edad: Number(valor.edad),
      identificacion: valor.identificacion.trim(),
      direccion: valor.direccion.trim(),
      telefono: valor.telefono.trim(),
      clienteId: valor.clienteId.trim(),
      contrasena: valor.contrasena === '' ? null : valor.contrasena,
      estado: valor.estado,
    };
    const cliente = this.cliente();
    const peticion = cliente
      ? this.api.actualizar(cliente.id, solicitud)
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

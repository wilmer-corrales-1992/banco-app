import { Component, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl } from '@angular/forms';
import { map, startWith, switchMap } from 'rxjs';
import { mensajeDeError, MensajesValidacion } from '../utilidades/errores-formulario';

/**
 * Etiqueta, control proyectado y mensaje de validacion. El mensaje se recalcula con cada
 * evento del control (valor, estado o tocado), asi que no depende de la deteccion de cambios.
 */
@Component({
  selector: 'app-campo-formulario',
  host: { class: 'campo', '[class.campo--error]': 'mensaje() !== null' },
  template: `
    <label [for]="para()">{{ etiqueta() }}</label>
    <ng-content />
    @if (mensaje(); as texto) {
      <p class="campo__error" [id]="para() + '-error'" role="alert">{{ texto }}</p>
    }
  `,
})
export class CampoFormulario {
  readonly etiqueta = input.required<string>();
  /** Id del control, para asociar la etiqueta. */
  readonly para = input.required<string>();
  readonly control = input.required<AbstractControl>();
  readonly mensajes = input<MensajesValidacion>({});

  protected readonly mensaje = toSignal(
    toObservable(this.control).pipe(
      switchMap((control) =>
        control.events.pipe(
          startWith(null),
          map(() => mensajeDeError(control, this.mensajes())),
        ),
      ),
    ),
    { initialValue: null },
  );
}

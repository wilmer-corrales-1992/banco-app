import { Component, input, output } from '@angular/core';
import { Modal } from './modal';

@Component({
  selector: 'app-dialogo-confirmacion',
  imports: [Modal],
  template: `
    <app-modal [titulo]="titulo()" (cerrar)="cancelar.emit()">
      <p>{{ mensaje() }}</p>
      <div class="formulario__acciones">
        <button type="button" class="boton" (click)="cancelar.emit()">Cancelar</button>
        <button
          type="button"
          class="boton boton--peligro"
          [disabled]="procesando()"
          (click)="confirmar.emit()"
        >
          {{ procesando() ? 'Procesando...' : textoConfirmar() }}
        </button>
      </div>
    </app-modal>
  `,
})
export class DialogoConfirmacion {
  readonly titulo = input('Confirmar');
  readonly mensaje = input.required<string>();
  readonly textoConfirmar = input('Eliminar');
  readonly procesando = input(false);
  readonly confirmar = output<void>();
  readonly cancelar = output<void>();
}

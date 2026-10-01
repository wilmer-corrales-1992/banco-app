import { Component, input, output } from '@angular/core';

let siguienteId = 0;

@Component({
  selector: 'app-modal',
  host: { '(document:keydown.escape)': 'cerrar.emit()' },
  template: `
    <div class="modal__fondo" (click)="cerrar.emit()"></div>
    <section class="modal" role="dialog" aria-modal="true" [attr.aria-labelledby]="idTitulo">
      <header class="modal__cabecera">
        <h2 class="modal__titulo" [id]="idTitulo">{{ titulo() }}</h2>
        <button type="button" class="modal__cerrar" aria-label="Cerrar" (click)="cerrar.emit()">
          ×
        </button>
      </header>
      <div class="modal__cuerpo">
        <ng-content />
      </div>
    </section>
  `,
  styleUrl: './modal.scss',
})
export class Modal {
  readonly titulo = input.required<string>();
  readonly cerrar = output<void>();

  protected readonly idTitulo = `modal-titulo-${++siguienteId}`;
}

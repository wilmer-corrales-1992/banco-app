import { Component, inject } from '@angular/core';
import { Notificaciones } from '../../core/notificaciones/notificaciones';

@Component({
  selector: 'app-panel-notificaciones',
  template: `
    <div class="panel" aria-live="polite">
      @for (notificacion of notificaciones.activas(); track notificacion.id) {
        <div [class]="'aviso aviso--' + notificacion.tipo" role="status">
          <span>{{ notificacion.mensaje }}</span>
          <button
            type="button"
            class="aviso__cerrar"
            aria-label="Cerrar aviso"
            (click)="notificaciones.cerrar(notificacion.id)"
          >
            ×
          </button>
        </div>
      }
    </div>
  `,
  styleUrl: './panel-notificaciones.scss',
})
export class PanelNotificaciones {
  protected readonly notificaciones = inject(Notificaciones);
}

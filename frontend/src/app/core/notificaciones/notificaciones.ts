import { Injectable, signal } from '@angular/core';

export type TipoNotificacion = 'exito' | 'error';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  mensaje: string;
}

export const DURACION_NOTIFICACION_MS = 4000;

@Injectable({ providedIn: 'root' })
export class Notificaciones {
  private readonly lista = signal<readonly Notificacion[]>([]);
  private siguienteId = 1;

  readonly activas = this.lista.asReadonly();

  exito(mensaje: string): void {
    this.agregar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.agregar('error', mensaje);
  }

  cerrar(id: number): void {
    this.lista.update((lista) => lista.filter((notificacion) => notificacion.id !== id));
  }

  private agregar(tipo: TipoNotificacion, mensaje: string): void {
    const id = this.siguienteId++;
    this.lista.update((lista) => [...lista, { id, tipo, mensaje }]);
    setTimeout(() => this.cerrar(id), DURACION_NOTIFICACION_MS);
  }
}

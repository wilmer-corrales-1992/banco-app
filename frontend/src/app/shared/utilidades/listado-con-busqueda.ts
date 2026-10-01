import { computed, signal } from '@angular/core';
import { finalize, Observable } from 'rxjs';
import { ErrorApi } from '../../core/http/error-api';
import { filtrarPorTexto } from './busqueda';

/**
 * Estado de una pantalla de listado: los registros, si estan cargando y la busqueda rapida.
 * Cada pagina indica que valores de la fila participan en la busqueda.
 */
export class ListadoConBusqueda<T> {
  readonly registros = signal<readonly T[]>([]);
  readonly cargando = signal(false);
  readonly busqueda = signal('');
  readonly filtrados = computed(() =>
    filtrarPorTexto(this.registros(), this.busqueda(), this.valoresDe),
  );

  constructor(private readonly valoresDe: (fila: T) => readonly unknown[]) {}

  cargar(fuente: Observable<T[]>, alFallar: (error: ErrorApi) => void): void {
    this.cargando.set(true);
    fuente.pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: (registros) => this.registros.set(registros),
      error: alFallar,
    });
  }
}

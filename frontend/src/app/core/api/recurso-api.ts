import { HttpClient } from '@angular/common/http';
import { inject, InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';

/** Ruta base de la API. En desarrollo el proxy de Angular la envia al backend; en Docker lo hace Nginx. */
export const API_URL = new InjectionToken<string>('API_URL', {
  providedIn: 'root',
  factory: () => '/api',
});

/**
 * Operaciones REST comunes a clientes, cuentas y movimientos.
 * Cada servicio concreto solo indica el nombre del recurso.
 */
export abstract class RecursoApi<T, S> {
  protected readonly http = inject(HttpClient);
  protected readonly url: string;

  protected constructor(recurso: string) {
    this.url = `${inject(API_URL)}/${recurso}`;
  }

  listar(filtros: Record<string, string> = {}): Observable<T[]> {
    return this.http.get<T[]>(this.url, { params: filtros });
  }

  obtener(id: number): Observable<T> {
    return this.http.get<T>(`${this.url}/${id}`);
  }

  crear(solicitud: S): Observable<T> {
    return this.http.post<T>(this.url, solicitud);
  }

  actualizar(id: number, solicitud: S): Observable<T> {
    return this.http.put<T>(`${this.url}/${id}`, solicitud);
  }

  actualizarParcial(id: number, cambios: Partial<S>): Observable<T> {
    return this.http.patch<T>(`${this.url}/${id}`, cambios);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

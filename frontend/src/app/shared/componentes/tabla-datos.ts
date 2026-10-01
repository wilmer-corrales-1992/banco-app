import { NgTemplateOutlet } from '@angular/common';
import {
  Component,
  computed,
  contentChild,
  Directive,
  inject,
  input,
  TemplateRef,
} from '@angular/core';

export type Alineacion = 'izquierda' | 'derecha' | 'centro';

export interface ColumnaTabla<T> {
  titulo: string;
  valor: (fila: T) => string | number;
  alineacion?: Alineacion;
  /** Clases CSS para el valor, por ejemplo una insignia de estado o el color de un monto. */
  clase?: (fila: T) => string;
}

export interface ContextoFila<T> {
  $implicit: T;
}

/**
 * Marca la plantilla con los botones de cada fila. Recibe las filas solo para que
 * el compilador conozca el tipo de la variable de la plantilla.
 */
@Directive({ selector: 'ng-template[appAccionesFila]' })
export class AccionesFila<T> {
  readonly appAccionesFila = input.required<readonly T[]>();
  readonly plantilla = inject<TemplateRef<ContextoFila<T>>>(TemplateRef);

  static ngTemplateContextGuard<T>(
    _directiva: AccionesFila<T>,
    contexto: unknown,
  ): contexto is ContextoFila<T> {
    return true;
  }
}

@Component({
  selector: 'app-tabla-datos',
  imports: [NgTemplateOutlet],
  template: `
    <div class="tabla-contenedor">
      <table class="tabla">
        @if (descripcion(); as texto) {
          <caption class="visualmente-oculto">
            {{
              texto
            }}
          </caption>
        }
        <thead>
          <tr>
            @for (columna of columnas(); track columna.titulo) {
              <th scope="col" [class]="'alinear-' + (columna.alineacion ?? 'izquierda')">
                {{ columna.titulo }}
              </th>
            }
            @if (acciones()) {
              <th scope="col" class="alinear-derecha">Acciones</th>
            }
          </tr>
        </thead>
        <tbody>
          @if (cargando()) {
            <tr>
              <td class="tabla__mensaje" [attr.colspan]="totalColumnas()">Cargando...</td>
            </tr>
          } @else {
            @for (fila of filas(); track identificador()(fila)) {
              <tr>
                @for (columna of columnas(); track columna.titulo) {
                  <td [class]="'alinear-' + (columna.alineacion ?? 'izquierda')">
                    <span [class]="columna.clase?.(fila) ?? ''">{{ columna.valor(fila) }}</span>
                  </td>
                }
                @if (acciones(); as acciones) {
                  <td class="alinear-derecha tabla__acciones">
                    <ng-container
                      *ngTemplateOutlet="acciones.plantilla; context: { $implicit: fila }"
                    />
                  </td>
                }
              </tr>
            } @empty {
              <tr>
                <td class="tabla__mensaje" [attr.colspan]="totalColumnas()">
                  {{ mensajeVacio() }}
                </td>
              </tr>
            }
          }
        </tbody>
      </table>
    </div>
  `,
})
export class TablaDatos<T> {
  readonly columnas = input.required<readonly ColumnaTabla<T>[]>();
  readonly filas = input.required<readonly T[]>();
  readonly identificador = input.required<(fila: T) => unknown>();
  readonly cargando = input(false);
  readonly mensajeVacio = input('No hay registros para mostrar');
  readonly descripcion = input<string | null>(null);

  protected readonly acciones = contentChild<AccionesFila<T>>(AccionesFila);
  protected readonly totalColumnas = computed(
    () => this.columnas().length + (this.acciones() ? 1 : 0),
  );
}

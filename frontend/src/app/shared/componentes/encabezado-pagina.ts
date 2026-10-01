import { Component, input, model, output } from '@angular/core';

let siguienteId = 0;

/** Titulo de la pantalla, buscador a la izquierda y boton amarillo a la derecha, como en el diseno. */
@Component({
  selector: 'app-encabezado-pagina',
  template: `
    <h1 class="encabezado__titulo">{{ titulo() }}</h1>
    @if (conBuscador() || textoBoton()) {
      <div class="encabezado__barra">
        @if (conBuscador()) {
          <label class="visualmente-oculto" [for]="idBuscador">Buscar</label>
          <input
            [id]="idBuscador"
            class="control encabezado__buscador"
            type="search"
            placeholder="Buscar"
            autocomplete="off"
            [value]="busqueda()"
            (input)="busqueda.set($any($event.target).value)"
          />
        }
        @if (textoBoton(); as texto) {
          <button type="button" class="boton boton--primario" (click)="accion.emit()">
            {{ texto }}
          </button>
        }
      </div>
    }
  `,
  styleUrl: './encabezado-pagina.scss',
})
export class EncabezadoPagina {
  readonly titulo = input.required<string>();
  readonly conBuscador = input(true);
  readonly textoBoton = input<string | null>('Nuevo');
  readonly busqueda = model('');
  readonly accion = output<void>();

  protected readonly idBuscador = `buscador-${++siguienteId}`;
}

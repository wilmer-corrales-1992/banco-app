import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { buscarTodos, clic, textos } from '../../../testing/dom';
import { AccionesFila, ColumnaTabla, TablaDatos } from './tabla-datos';

interface Persona {
  id: number;
  nombre: string;
  saldo: number;
}

@Component({
  imports: [TablaDatos, AccionesFila],
  template: `
    <app-tabla-datos
      [columnas]="columnas"
      [filas]="filas()"
      [identificador]="identificador"
      [cargando]="cargando()"
      mensajeVacio="Sin personas"
    >
      @if (conAcciones()) {
        <ng-template [appAccionesFila]="filas()" let-persona>
          <button type="button" (click)="elegida.set(persona.nombre)">Ver</button>
        </ng-template>
      }
    </app-tabla-datos>
  `,
})
class Anfitrion {
  readonly columnas: ColumnaTabla<Persona>[] = [
    { titulo: 'Nombre', valor: (p) => p.nombre },
    {
      titulo: 'Saldo',
      valor: (p) => p.saldo.toFixed(2),
      alineacion: 'derecha',
      clase: (p) => (p.saldo < 0 ? 'negativo' : ''),
    },
  ];
  readonly filas = signal<Persona[]>([
    { id: 1, nombre: 'José', saldo: 10 },
    { id: 2, nombre: 'Juan', saldo: -5 },
  ]);
  readonly cargando = signal(false);
  readonly conAcciones = signal(true);
  readonly elegida = signal<string | null>(null);
  readonly identificador = (p: Persona) => p.id;
}

describe('TablaDatos', () => {
  async function montar() {
    const fixture = TestBed.createComponent(Anfitrion);
    await fixture.whenStable();
    return fixture;
  }

  it('dibuja encabezados y una fila por registro con los valores de cada columna', async () => {
    const fixture = await montar();

    expect(textos(fixture, 'th')).toEqual(['Nombre', 'Saldo', 'Acciones']);
    expect(textos(fixture, 'tbody tr td:first-child')).toEqual(['José', 'Juan']);
    expect(textos(fixture, 'tbody tr td:nth-child(2)')).toEqual(['10.00', '-5.00']);
  });

  it('aplica la alineacion y las clases por fila', async () => {
    const fixture = await montar();
    const saldos = buscarTodos(fixture, 'tbody td:nth-child(2)');

    expect(saldos[0].classList).toContain('alinear-derecha');
    expect(saldos[1].querySelector('span')?.className).toBe('negativo');
  });

  it('la plantilla de acciones recibe la fila correspondiente', async () => {
    const fixture = await montar();

    await clic(fixture, buscarTodos<HTMLButtonElement>(fixture, 'tbody button')[1]);

    expect(fixture.componentInstance.elegida()).toBe('Juan');
  });

  it('sin plantilla de acciones no agrega la columna', async () => {
    const fixture = await montar();
    fixture.componentInstance.conAcciones.set(false);
    await fixture.whenStable();

    expect(textos(fixture, 'th')).toEqual(['Nombre', 'Saldo']);
  });

  it('muestra el mensaje vacio ocupando todas las columnas', async () => {
    const fixture = await montar();
    fixture.componentInstance.filas.set([]);
    await fixture.whenStable();

    const celda = buscarTodos(fixture, 'tbody td');
    expect(celda).toHaveLength(1);
    expect(celda[0].textContent?.trim()).toBe('Sin personas');
    expect(celda[0].getAttribute('colspan')).toBe('3');
  });

  it('mientras carga muestra el aviso en lugar de las filas', async () => {
    const fixture = await montar();
    fixture.componentInstance.cargando.set(true);
    await fixture.whenStable();

    expect(textos(fixture, 'tbody td')).toEqual(['Cargando...']);
  });
});

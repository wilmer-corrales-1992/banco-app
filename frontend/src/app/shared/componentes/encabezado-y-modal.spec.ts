import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { boton, buscar, buscarTodos, clic, escribir } from '../../../testing/dom';
import { DialogoConfirmacion } from './dialogo-confirmacion';
import { EncabezadoPagina } from './encabezado-pagina';
import { Modal } from './modal';

@Component({
  imports: [EncabezadoPagina],
  template: `
    <app-encabezado-pagina
      titulo="Clientes"
      [conBuscador]="conBuscador()"
      [textoBoton]="textoBoton()"
      [(busqueda)]="busqueda"
      (accion)="clics = clics + 1"
    />
  `,
})
class AnfitrionEncabezado {
  readonly busqueda = signal('');
  readonly conBuscador = signal(true);
  readonly textoBoton = signal<string | null>('Nuevo');
  clics = 0;
}

describe('EncabezadoPagina', () => {
  it('muestra titulo, buscador y boton Nuevo como en el diseno', async () => {
    const fixture = TestBed.createComponent(AnfitrionEncabezado);
    await fixture.whenStable();

    expect(buscar(fixture, 'h1').textContent?.trim()).toBe('Clientes');
    expect(buscar<HTMLInputElement>(fixture, 'input[type=search]').placeholder).toBe('Buscar');
    expect(boton(fixture, 'Nuevo').classList).toContain('boton--primario');
  });

  it('la busqueda es de doble via y el boton emite la accion', async () => {
    const fixture = TestBed.createComponent(AnfitrionEncabezado);
    await fixture.whenStable();

    await escribir(fixture, 'input[type=search]', 'jose');
    await clic(fixture, boton(fixture, 'Nuevo'));

    expect(fixture.componentInstance.busqueda()).toBe('jose');
    expect(fixture.componentInstance.clics).toBe(1);
  });

  it('puede ocultar el buscador y el boton', async () => {
    const fixture = TestBed.createComponent(AnfitrionEncabezado);
    fixture.componentInstance.conBuscador.set(false);
    fixture.componentInstance.textoBoton.set(null);
    await fixture.whenStable();

    expect(buscarTodos(fixture, 'input')).toHaveLength(0);
    expect(buscarTodos(fixture, 'button')).toHaveLength(0);
  });
});

@Component({
  imports: [Modal, DialogoConfirmacion],
  template: `
    @if (abierto()) {
      <app-modal titulo="Nuevo cliente" (cerrar)="abierto.set(false)"
        ><p class="contenido">Formulario</p></app-modal
      >
    }
    @if (confirmando()) {
      <app-dialogo-confirmacion
        mensaje="¿Eliminar al cliente?"
        (confirmar)="resultado.set('confirmado')"
        (cancelar)="resultado.set('cancelado')"
      />
    }
  `,
})
class AnfitrionModal {
  readonly abierto = signal(true);
  readonly confirmando = signal(false);
  readonly resultado = signal<string | null>(null);
}

describe('Modal y DialogoConfirmacion', () => {
  it('el modal es un dialogo accesible con titulo y contenido proyectado', async () => {
    const fixture = TestBed.createComponent(AnfitrionModal);
    await fixture.whenStable();
    const dialogo = buscar(fixture, '[role=dialog]');
    const titulo = buscar(fixture, 'h2');

    expect(dialogo.getAttribute('aria-modal')).toBe('true');
    expect(dialogo.getAttribute('aria-labelledby')).toBe(titulo.id);
    expect(titulo.textContent?.trim()).toBe('Nuevo cliente');
    expect(buscar(fixture, '.contenido').textContent).toBe('Formulario');
  });

  it.each([
    [
      'la tecla Escape',
      (f: { nativeElement: HTMLElement }) =>
        document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' })),
    ],
    [
      'el boton cerrar',
      (f: { nativeElement: HTMLElement }) =>
        f.nativeElement.querySelector<HTMLButtonElement>('.modal__cerrar')?.click(),
    ],
    [
      'un clic en el fondo',
      (f: { nativeElement: HTMLElement }) =>
        f.nativeElement.querySelector<HTMLElement>('.modal__fondo')?.click(),
    ],
  ])('se cierra con %s', async (_nombre, cerrar) => {
    const fixture = TestBed.createComponent(AnfitrionModal);
    await fixture.whenStable();

    cerrar(fixture);
    await fixture.whenStable();

    expect(fixture.componentInstance.abierto()).toBe(false);
    expect(buscarTodos(fixture, '[role=dialog]')).toHaveLength(0);
  });

  it('la confirmacion emite confirmar o cancelar segun el boton', async () => {
    const fixture = TestBed.createComponent(AnfitrionModal);
    fixture.componentInstance.abierto.set(false);
    fixture.componentInstance.confirmando.set(true);
    await fixture.whenStable();

    expect(buscar(fixture, '[role=dialog] p').textContent).toBe('¿Eliminar al cliente?');
    await clic(fixture, boton(fixture, 'Eliminar', '[role=dialog]'));
    expect(fixture.componentInstance.resultado()).toBe('confirmado');

    await clic(fixture, boton(fixture, 'Cancelar'));
    expect(fixture.componentInstance.resultado()).toBe('cancelado');
  });
});

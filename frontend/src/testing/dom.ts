import { ComponentFixture } from '@angular/core/testing';

/** Ayudantes para manipular la vista como lo haria un usuario y esperar a que Angular la actualice. */

export function buscar<T extends Element = HTMLElement>(
  fixture: ComponentFixture<unknown>,
  selector: string,
): T {
  const elemento = (fixture.nativeElement as HTMLElement).querySelector<T>(selector);
  if (!elemento) {
    throw new Error(`No se encontro ${selector}`);
  }
  return elemento;
}

export function buscarTodos<T extends Element = HTMLElement>(
  fixture: ComponentFixture<unknown>,
  selector: string,
): T[] {
  return Array.from((fixture.nativeElement as HTMLElement).querySelectorAll<T>(selector));
}

export function textos(fixture: ComponentFixture<unknown>, selector: string): string[] {
  return buscarTodos(fixture, selector).map((elemento) => elemento.textContent?.trim() ?? '');
}

/** Boton por su texto visible, opcionalmente dentro de un contenedor (por ejemplo '[role=dialog]'). */
export function boton(
  fixture: ComponentFixture<unknown>,
  texto: string,
  dentroDe = '',
): HTMLButtonElement {
  const selector = dentroDe ? `${dentroDe} button` : 'button';
  const encontrado = buscarTodos<HTMLButtonElement>(fixture, selector).find(
    (b) => b.textContent?.trim() === texto,
  );
  if (!encontrado) {
    throw new Error(
      `No se encontro el boton "${texto}"${dentroDe ? ` dentro de ${dentroDe}` : ''}`,
    );
  }
  return encontrado;
}

export async function clic(
  fixture: ComponentFixture<unknown>,
  elemento: HTMLElement,
): Promise<void> {
  elemento.click();
  await fixture.whenStable();
}

export async function escribir(
  fixture: ComponentFixture<unknown>,
  selector: string,
  valor: string,
): Promise<void> {
  const control = buscar<HTMLInputElement>(fixture, selector);
  control.value = valor;
  control.dispatchEvent(new Event('input'));
  control.dispatchEvent(new Event('blur'));
  await fixture.whenStable();
}

/** Elige la opcion de un select por su texto visible. */
export async function elegir(
  fixture: ComponentFixture<unknown>,
  selector: string,
  texto: string,
): Promise<void> {
  const select = buscar<HTMLSelectElement>(fixture, selector);
  const opcion = Array.from(select.options).find((o) => o.textContent?.trim() === texto);
  if (!opcion) {
    throw new Error(`El select ${selector} no tiene la opcion "${texto}"`);
  }
  select.value = opcion.value;
  select.dispatchEvent(new Event('change'));
  await fixture.whenStable();
}

export async function enviar(fixture: ComponentFixture<unknown>): Promise<void> {
  buscar<HTMLFormElement>(fixture, 'form').dispatchEvent(new Event('submit'));
  await fixture.whenStable();
}

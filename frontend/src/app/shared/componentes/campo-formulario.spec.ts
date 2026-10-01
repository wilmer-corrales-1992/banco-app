import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { buscar, escribir, textos } from '../../../testing/dom';
import { CampoFormulario } from './campo-formulario';

@Component({
  imports: [ReactiveFormsModule, CampoFormulario],
  template: `
    <app-campo-formulario etiqueta="Número" para="numero" [control]="control" [mensajes]="mensajes">
      <input id="numero" class="control" [formControl]="control" />
    </app-campo-formulario>
  `,
})
class Anfitrion {
  readonly control = new FormControl('', [Validators.required, Validators.pattern(/^\d+$/)]);
  readonly mensajes = { pattern: 'Solo se admiten dígitos' };
}

describe('CampoFormulario', () => {
  async function montar() {
    const fixture = TestBed.createComponent(Anfitrion);
    await fixture.whenStable();
    return fixture;
  }

  it('asocia la etiqueta con el control', async () => {
    const fixture = await montar();
    const etiqueta = buscar<HTMLLabelElement>(fixture, 'label');

    expect(etiqueta.textContent?.trim()).toBe('Número');
    expect(etiqueta.htmlFor).toBe('numero');
  });

  it('no muestra errores antes de que el usuario toque el campo', async () => {
    const fixture = await montar();
    expect(textos(fixture, '.campo__error')).toEqual([]);
  });

  it('muestra el mensaje personalizado al escribir un valor invalido', async () => {
    const fixture = await montar();

    await escribir(fixture, '#numero', '12a');

    expect(textos(fixture, '.campo__error')).toEqual(['Solo se admiten dígitos']);
    expect(buscar(fixture, 'app-campo-formulario').classList).toContain('campo--error');
  });

  it('se actualiza cuando el formulario marca todo como tocado, sin interaccion del usuario', async () => {
    const fixture = await montar();

    fixture.componentInstance.control.markAsTouched();
    await fixture.whenStable();

    expect(textos(fixture, '.campo__error')).toEqual(['Este campo es obligatorio']);
  });

  it('muestra el error del servidor y lo quita cuando el valor cambia', async () => {
    const fixture = await montar();
    const control = fixture.componentInstance.control;

    control.setValue('478758');
    control.setErrors({ servidor: 'Ya existe la cuenta 478758' });
    control.markAsTouched();
    await fixture.whenStable();
    expect(textos(fixture, '.campo__error')).toEqual(['Ya existe la cuenta 478758']);

    await escribir(fixture, '#numero', '478759');
    expect(textos(fixture, '.campo__error')).toEqual([]);
  });
});

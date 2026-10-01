import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ErrorApi } from '../../core/http/error-api';
import { aplicarErrorDelServidor, mensajeDeError } from './errores-formulario';

describe('mensajeDeError', () => {
  it('no muestra nada hasta que el usuario toca el control', () => {
    const control = new FormControl('', Validators.required);
    expect(mensajeDeError(control)).toBeNull();

    control.markAsTouched();
    expect(mensajeDeError(control)).toBe('Este campo es obligatorio');
  });

  it('usa los datos del error en el mensaje por defecto', () => {
    const control = new FormControl('ab', Validators.minLength(4));
    control.markAsDirty();
    expect(mensajeDeError(control)).toBe('Debe tener al menos 4 caracteres');
  });

  it('prefiere el mensaje personalizado del campo', () => {
    const control = new FormControl('12a', Validators.pattern(/^\d+$/));
    control.markAsTouched();
    expect(mensajeDeError(control, { pattern: 'Solo se admiten dígitos' })).toBe(
      'Solo se admiten dígitos',
    );
  });

  it('muestra el texto del error del servidor tal cual', () => {
    const control = new FormControl('jlema');
    control.setErrors({ servidor: 'Ya existe un cliente con clienteId jlema' });
    control.markAsTouched();
    expect(mensajeDeError(control)).toBe('Ya existe un cliente con clienteId jlema');
  });

  it('un control valido no tiene mensaje', () => {
    const control = new FormControl('ok', Validators.required);
    control.markAsTouched();
    expect(mensajeDeError(control)).toBeNull();
  });
});

describe('aplicarErrorDelServidor', () => {
  const formulario = () =>
    new FormGroup({ clienteId: new FormControl('jlema'), nombre: new FormControl('José') });

  it('marca los campos que el backend rechazo y pide revisarlos', () => {
    const form = formulario();
    const error = new ErrorApi(400, 'La solicitud tiene datos invalidos', {
      clienteId: 'El clienteId es obligatorio',
      campoInexistente: 'ignorado',
    });

    expect(aplicarErrorDelServidor(form, error)).toBe('Revisa los campos marcados.');
    expect(form.controls.clienteId.errors).toEqual({ servidor: 'El clienteId es obligatorio' });
    expect(form.controls.clienteId.touched).toBe(true);
    expect(form.controls.nombre.valid).toBe(true);
  });

  it('sin errores por campo devuelve el mensaje del backend', () => {
    expect(aplicarErrorDelServidor(formulario(), new ErrorApi(422, 'Saldo no disponible'))).toBe(
      'Saldo no disponible',
    );
  });

  it('el error del servidor desaparece cuando el usuario cambia el valor', () => {
    const form = formulario();
    aplicarErrorDelServidor(form, new ErrorApi(409, 'x', { clienteId: 'Duplicado' }));

    form.controls.clienteId.setValue('otro');
    expect(form.controls.clienteId.errors).toBeNull();
  });
});

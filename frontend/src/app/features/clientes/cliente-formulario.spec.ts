import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JOSE, respuestaError } from '../../../testing/datos';
import { boton, buscar, elegir, enviar, escribir, textos } from '../../../testing/dom';
import { errorApiInterceptor } from '../../core/http/error-api.interceptor';
import { Cliente } from '../../core/models/cliente';
import { ClienteFormulario } from './cliente-formulario';

describe('ClienteFormulario', () => {
  let servidor: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorApiInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    servidor = TestBed.inject(HttpTestingController);
  });

  afterEach(() => servidor.verify());

  async function montar(cliente: Cliente | null = null) {
    const fixture = TestBed.createComponent(ClienteFormulario);
    fixture.componentRef.setInput('cliente', cliente);
    const guardado = jest.fn();
    fixture.componentInstance.guardado.subscribe(guardado);
    await fixture.whenStable();
    return { fixture, guardado };
  }

  async function llenarNuevo(fixture: ComponentFixture<ClienteFormulario>) {
    await escribir(fixture, '#cliente-nombre', '  Pedro Prueba ');
    await escribir(fixture, '#cliente-identificacion', '1700000099');
    await elegir(fixture, '#cliente-genero', 'Masculino');
    await escribir(fixture, '#cliente-edad', '33');
    await escribir(fixture, '#cliente-direccion', 'Av. de prueba 123');
    await escribir(fixture, '#cliente-telefono', '0991234567');
    await escribir(fixture, '#cliente-clienteId', 'pprueba');
    await escribir(fixture, '#cliente-contrasena', '9999');
  }

  it('al guardar vacio muestra un mensaje en cada campo obligatorio y no llama a la API', async () => {
    const { fixture } = await montar();

    await enviar(fixture);

    expect(textos(fixture, '.campo__error')).toEqual(Array(8).fill('Este campo es obligatorio'));
    servidor.expectNone('/api/clientes');
  });

  it('valida los limites del modelo, como la edad maxima', async () => {
    const { fixture } = await montar();

    await escribir(fixture, '#cliente-edad', '130');
    await escribir(fixture, '#cliente-contrasena', '12');

    expect(textos(fixture, '.campo__error')).toEqual([
      'El valor máximo es 120',
      'Debe tener al menos 4 caracteres',
    ]);
  });

  it('crea el cliente con POST, recortando espacios, y emite el guardado', async () => {
    const { fixture, guardado } = await montar();
    await llenarNuevo(fixture);

    await enviar(fixture);
    const peticion = servidor.expectOne({ method: 'POST', url: '/api/clientes' });
    expect(boton(fixture, 'Guardando...').disabled).toBe(true);
    expect(peticion.request.body).toEqual({
      nombre: 'Pedro Prueba',
      genero: 'MASCULINO',
      edad: 33,
      identificacion: '1700000099',
      direccion: 'Av. de prueba 123',
      telefono: '0991234567',
      clienteId: 'pprueba',
      contrasena: '9999',
      estado: true,
    });

    const creado = { ...JOSE, id: 4, clienteId: 'pprueba', nombre: 'Pedro Prueba' };
    peticion.flush(creado);
    await fixture.whenStable();

    expect(guardado).toHaveBeenCalledWith(creado);
  });

  it('muestra en el campo el error por campo que devuelve el backend', async () => {
    const { fixture, guardado } = await montar();
    await llenarNuevo(fixture);
    await enviar(fixture);

    servidor
      .expectOne('/api/clientes')
      .flush(
        respuestaError(400, 'La solicitud tiene datos invalidos', [
          { campo: 'identificacion', mensaje: 'La identificacion no puede superar 20 caracteres' },
        ]),
        { status: 400, statusText: 'Bad Request' },
      );
    await fixture.whenStable();

    expect(buscar(fixture, '.alerta--error').textContent?.trim()).toBe(
      'Revisa los campos marcados.',
    );
    expect(textos(fixture, '.campo__error')).toEqual([
      'La identificacion no puede superar 20 caracteres',
    ]);
    expect(guardado).not.toHaveBeenCalled();
    expect(boton(fixture, 'Guardar').disabled).toBe(false);
  });

  it('muestra como alerta un conflicto sin errores por campo', async () => {
    const { fixture } = await montar();
    await llenarNuevo(fixture);
    await enviar(fixture);

    servidor
      .expectOne('/api/clientes')
      .flush(respuestaError(409, 'Ya existe un cliente con clienteId pprueba'), {
        status: 409,
        statusText: 'Conflict',
      });
    await fixture.whenStable();

    expect(buscar(fixture, '.alerta--error').textContent?.trim()).toBe(
      'Ya existe un cliente con clienteId pprueba',
    );
  });

  it('al editar precarga los datos, la contrasena es opcional y usa PUT', async () => {
    const { fixture, guardado } = await montar(JOSE);

    expect(buscar<HTMLInputElement>(fixture, '#cliente-nombre').value).toBe('José Lema');
    expect(buscar(fixture, 'label[for=cliente-contrasena]').textContent).toContain(
      'vacía para conservarla',
    );

    await escribir(fixture, '#cliente-telefono', '0999999999');
    await enviar(fixture);

    const peticion = servidor.expectOne({ method: 'PUT', url: '/api/clientes/1' });
    expect(peticion.request.body).toEqual(
      expect.objectContaining({ telefono: '0999999999', contrasena: null }),
    );
    peticion.flush({ ...JOSE, telefono: '0999999999' });
    await fixture.whenStable();
    expect(guardado).toHaveBeenCalled();
  });

  it('Cancelar emite cancelado', async () => {
    const { fixture } = await montar();
    const cancelado = jest.fn();
    fixture.componentInstance.cancelado.subscribe(cancelado);

    boton(fixture, 'Cancelar').click();

    expect(cancelado).toHaveBeenCalled();
  });
});

import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { JOSE, MARIANELA, respuestaError } from '../../../testing/datos';
import { boton, buscarTodos, clic, escribir, textos } from '../../../testing/dom';
import { errorApiInterceptor } from '../../core/http/error-api.interceptor';
import { Notificaciones } from '../../core/notificaciones/notificaciones';
import { ClientesPagina } from './clientes-pagina';

describe('ClientesPagina', () => {
  let servidor: HttpTestingController;
  let notificaciones: Notificaciones;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorApiInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    servidor = TestBed.inject(HttpTestingController);
    notificaciones = TestBed.inject(Notificaciones);
  });

  afterEach(() => servidor.verify());

  async function montar() {
    const fixture = TestBed.createComponent(ClientesPagina);
    await fixture.whenStable();
    servidor.expectOne({ method: 'GET', url: '/api/clientes' }).flush([JOSE, MARIANELA]);
    await fixture.whenStable();
    return fixture;
  }

  const nombres = (fixture: Parameters<typeof textos>[0]) =>
    textos(fixture, 'tbody tr td:nth-child(2)');

  it('lista los clientes con sus datos legibles', async () => {
    const fixture = await montar();

    expect(nombres(fixture)).toEqual(['José Lema', 'Marianela Montalvo']);
    expect(textos(fixture, 'tbody tr:first-child td')).toEqual(
      expect.arrayContaining(['jlema', 'Masculino', '30', 'Activo']),
    );
  });

  it('la busqueda rapida filtra sin importar tildes y avisa cuando no hay coincidencias', async () => {
    const fixture = await montar();

    await escribir(fixture, 'input[type=search]', 'jose');
    expect(nombres(fixture)).toEqual(['José Lema']);

    await escribir(fixture, 'input[type=search]', 'zzz');
    expect(textos(fixture, 'tbody td')).toEqual(['Ningún cliente coincide con la búsqueda']);
  });

  it('Nuevo abre el formulario en un modal y Cancelar lo cierra', async () => {
    const fixture = await montar();

    await clic(fixture, boton(fixture, 'Nuevo'));
    expect(textos(fixture, '[role=dialog] h2')).toEqual(['Nuevo cliente']);

    await clic(fixture, boton(fixture, 'Cancelar'));
    expect(buscarTodos(fixture, '[role=dialog]')).toHaveLength(0);
  });

  it('Editar abre el formulario con el cliente elegido', async () => {
    const fixture = await montar();

    await clic(
      fixture,
      buscarTodos<HTMLButtonElement>(fixture, 'tbody tr')[1].querySelector('button')!,
    );

    expect(textos(fixture, '[role=dialog] h2')).toEqual(['Editar cliente']);
    expect(fixture.nativeElement.querySelector('#cliente-nombre').value).toBe('Marianela Montalvo');
  });

  it('Desactivar envia PATCH con el estado contrario, avisa y recarga la lista', async () => {
    const fixture = await montar();

    await clic(fixture, boton(fixture, 'Desactivar') as HTMLButtonElement);
    const peticion = servidor.expectOne({ method: 'PATCH', url: '/api/clientes/1' });
    expect(peticion.request.body).toEqual({ estado: false });
    peticion.flush({ ...JOSE, estado: false });
    servidor.expectOne('/api/clientes').flush([{ ...JOSE, estado: false }, MARIANELA]);
    await fixture.whenStable();

    expect(notificaciones.activas().map((n) => n.mensaje)).toEqual([
      'Cliente José Lema desactivado',
    ]);
    expect(textos(fixture, 'tbody tr:first-child .insignia')).toEqual(['Inactivo']);
  });

  it('eliminar pide confirmacion, envia DELETE y recarga', async () => {
    const fixture = await montar();

    await clic(fixture, buscarTodos<HTMLButtonElement>(fixture, 'tbody .accion--peligro')[1]);
    expect(textos(fixture, '[role=dialog] p')[0]).toContain('Marianela Montalvo (mmontalvo)');

    await clic(fixture, boton(fixture, 'Eliminar', '[role=dialog]'));
    servidor
      .expectOne({ method: 'DELETE', url: '/api/clientes/2' })
      .flush(null, { status: 204, statusText: 'No Content' });
    servidor.expectOne('/api/clientes').flush([JOSE]);
    await fixture.whenStable();

    expect(buscarTodos(fixture, '[role=dialog]')).toHaveLength(0);
    expect(nombres(fixture)).toEqual(['José Lema']);
    expect(notificaciones.activas()[0].tipo).toBe('exito');
  });

  it('si el backend no permite eliminar, cierra la confirmacion y muestra su mensaje', async () => {
    const fixture = await montar();
    const mensaje = 'No se puede eliminar el cliente jlema porque tiene cuentas asociadas';

    await clic(fixture, buscarTodos<HTMLButtonElement>(fixture, 'tbody .accion--peligro')[0]);
    await clic(fixture, boton(fixture, 'Eliminar', '[role=dialog]'));
    servidor
      .expectOne('/api/clientes/1')
      .flush(respuestaError(409, mensaje), { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(buscarTodos(fixture, '[role=dialog]')).toHaveLength(0);
    expect(notificaciones.activas()).toEqual([expect.objectContaining({ tipo: 'error', mensaje })]);
    expect(nombres(fixture)).toHaveLength(2);
  });

  it('avisa si no puede conectar con la API al cargar', async () => {
    const fixture = TestBed.createComponent(ClientesPagina);
    await fixture.whenStable();

    servidor.expectOne('/api/clientes').error(new ProgressEvent('error'), { status: 0 });
    await fixture.whenStable();

    expect(notificaciones.activas()[0].mensaje).toContain('No se pudo conectar con el servidor');
    expect(textos(fixture, 'tbody td')).toEqual(['No hay clientes registrados']);
  });
});

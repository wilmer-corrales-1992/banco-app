import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { respuestaError } from '../../../testing/datos';
import { ErrorApi } from './error-api';
import { errorApiInterceptor } from './error-api.interceptor';

describe('ErrorApi.desde', () => {
  it('toma el mensaje y los errores por campo del JSON del backend', () => {
    const error = ErrorApi.desde(
      new HttpErrorResponse({
        status: 400,
        error: respuestaError(400, 'La solicitud tiene datos invalidos', [
          { campo: 'nombre', mensaje: 'El nombre es obligatorio' },
          { campo: 'edad', mensaje: 'La edad no puede superar 120' },
        ]),
      }),
    );

    expect(error).toBeInstanceOf(ErrorApi);
    expect(error.estado).toBe(400);
    expect(error.message).toBe('La solicitud tiene datos invalidos');
    expect(error.errores).toEqual({
      nombre: 'El nombre es obligatorio',
      edad: 'La edad no puede superar 120',
    });
  });

  it('explica que no hay conexion cuando el estado es 0', () => {
    const error = ErrorApi.desde(new HttpErrorResponse({ status: 0 }));
    expect(error.message).toContain('No se pudo conectar con el servidor');
  });

  it('usa un mensaje generico si el cuerpo no tiene la forma esperada', () => {
    const error = ErrorApi.desde(
      new HttpErrorResponse({ status: 502, error: '<html>Bad gateway</html>' }),
    );
    expect(error.message).toBe('Ocurrió un error inesperado (502).');
    expect(error.errores).toEqual({});
  });
});

describe('errorApiInterceptor', () => {
  let http: HttpClient;
  let servidor: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorApiInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    servidor = TestBed.inject(HttpTestingController);
  });

  afterEach(() => servidor.verify());

  it('entrega a los componentes un ErrorApi con el mensaje exacto del backend', async () => {
    const respuesta = firstValueFrom(http.post('/api/movimientos', {}));
    servidor
      .expectOne('/api/movimientos')
      .flush(respuestaError(422, 'Saldo no disponible'), {
        status: 422,
        statusText: 'Unprocessable Entity',
      });

    await expect(respuesta).rejects.toEqual(
      expect.objectContaining({ estado: 422, message: 'Saldo no disponible' }),
    );
    await respuesta.catch((error) => expect(error).toBeInstanceOf(ErrorApi));
  });

  it('no altera las respuestas correctas', async () => {
    const respuesta = firstValueFrom(http.get('/api/clientes'));
    servidor.expectOne('/api/clientes').flush([]);
    await expect(respuesta).resolves.toEqual([]);
  });
});

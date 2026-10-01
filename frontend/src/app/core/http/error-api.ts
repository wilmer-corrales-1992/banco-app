import { HttpErrorResponse } from '@angular/common/http';

/** Forma del JSON de error que devuelve el backend en todos los casos. */
interface RespuestaError {
  mensaje: string;
  errores?: { campo: string; mensaje: string }[];
}

/**
 * Error de la API ya normalizado: un mensaje para mostrar y, si los hay, los errores por campo.
 */
export class ErrorApi extends Error {
  constructor(
    readonly estado: number,
    mensaje: string,
    readonly errores: Readonly<Record<string, string>> = {},
  ) {
    super(mensaje);
    this.name = 'ErrorApi';
  }

  static desde(respuesta: HttpErrorResponse): ErrorApi {
    if (respuesta.status === 0) {
      return new ErrorApi(
        0,
        'No se pudo conectar con el servidor. Verifica que la API esté en ejecución.',
      );
    }
    const cuerpo = respuesta.error as Partial<RespuestaError> | null;
    if (cuerpo && typeof cuerpo.mensaje === 'string') {
      const errores = Object.fromEntries((cuerpo.errores ?? []).map((e) => [e.campo, e.mensaje]));
      return new ErrorApi(respuesta.status, cuerpo.mensaje, errores);
    }
    return new ErrorApi(respuesta.status, `Ocurrió un error inesperado (${respuesta.status}).`);
  }
}

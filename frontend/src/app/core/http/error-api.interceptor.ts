import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ErrorApi } from './error-api';

/** Convierte cualquier error HTTP en ErrorApi para que los componentes manejen un solo tipo. */
export const errorApiInterceptor: HttpInterceptorFn = (peticion, siguiente) =>
  siguiente(peticion).pipe(
    catchError((error: unknown) =>
      throwError(() => (error instanceof HttpErrorResponse ? ErrorApi.desde(error) : error)),
    ),
  );

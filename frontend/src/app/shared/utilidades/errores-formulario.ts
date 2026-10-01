import { AbstractControl, FormGroup, ValidationErrors } from '@angular/forms';
import { ErrorApi } from '../../core/http/error-api';

export type MensajesValidacion = Partial<Record<string, string>>;

const MENSAJES_POR_DEFECTO: Record<string, (detalle: Record<string, unknown>) => string> = {
  required: () => 'Este campo es obligatorio',
  minlength: (d) => `Debe tener al menos ${d['requiredLength']} caracteres`,
  maxlength: (d) => `No puede superar ${d['requiredLength']} caracteres`,
  min: (d) => `El valor mínimo es ${d['min']}`,
  max: (d) => `El valor máximo es ${d['max']}`,
  pattern: () => 'El formato no es válido',
  entero: () => 'Debe ser un número entero',
  maxDecimales: (d) => `Admite hasta ${d['maximo']} decimales`,
};

export const ERROR_SERVIDOR = 'servidor';

/**
 * Mensaje del primer error de un control, solo cuando el usuario ya interactuo con el.
 * Los mensajes personalizados tienen prioridad sobre los por defecto.
 */
export function mensajeDeError(
  control: AbstractControl,
  personalizados: MensajesValidacion = {},
): string | null {
  if (!control.invalid || !(control.touched || control.dirty)) {
    return null;
  }
  const errores = control.errors as ValidationErrors;
  const [clave] = Object.keys(errores);
  if (clave === ERROR_SERVIDOR) {
    return String(errores[ERROR_SERVIDOR]);
  }
  const detalle =
    typeof errores[clave] === 'object' ? (errores[clave] as Record<string, unknown>) : {};
  return personalizados[clave] ?? MENSAJES_POR_DEFECTO[clave]?.(detalle) ?? 'El valor no es válido';
}

/**
 * Pinta en cada control los errores por campo que devolvio el backend y
 * devuelve el mensaje general que debe mostrar el formulario.
 */
export function aplicarErrorDelServidor(formulario: FormGroup, error: ErrorApi): string {
  const conControl = Object.entries(error.errores)
    .map(([campo, mensaje]) => ({ control: formulario.get(campo), mensaje }))
    .filter((par): par is { control: AbstractControl; mensaje: string } => par.control !== null);

  conControl.forEach(({ control, mensaje }) => {
    control.setErrors({ [ERROR_SERVIDOR]: mensaje });
    control.markAsTouched();
  });
  return conControl.length > 0 ? 'Revisa los campos marcados.' : error.message;
}

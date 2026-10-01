import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Como Validators.required, pero tambien rechaza textos con solo espacios. */
export const noVacio: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;
  const vacio =
    valor === null || valor === undefined || (typeof valor === 'string' && valor.trim() === '');
  return vacio ? { required: true } : null;
};

export const entero: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;
  return valor === null || valor === '' || Number.isInteger(Number(valor))
    ? null
    : { entero: true };
};

export function maxDecimales(maximo: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value;
    if (valor === null || valor === '' || valor === undefined) {
      return null;
    }
    const decimales = String(valor).split('.')[1]?.length ?? 0;
    return decimales > maximo ? { maxDecimales: { maximo } } : null;
  };
}

/** Validador de grupo: la fecha inicial no puede ser posterior a la final (formato yyyy-MM-dd). */
export function rangoFechas(campoInicio: string, campoFin: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const inicio = grupo.get(campoInicio)?.value as string | null;
    const fin = grupo.get(campoFin)?.value as string | null;
    return inicio && fin && inicio > fin ? { rangoFechas: true } : null;
  };
}

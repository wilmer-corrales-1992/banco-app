import { Injectable } from '@angular/core';
import { Cuenta, CuentaSolicitud } from '../models/cuenta';
import { RecursoApi } from './recurso-api';

@Injectable({ providedIn: 'root' })
export class CuentaApi extends RecursoApi<Cuenta, CuentaSolicitud> {
  constructor() {
    super('cuentas');
  }
}

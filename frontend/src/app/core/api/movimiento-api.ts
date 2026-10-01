import { Injectable } from '@angular/core';
import { Movimiento, MovimientoSolicitud } from '../models/movimiento';
import { RecursoApi } from './recurso-api';

@Injectable({ providedIn: 'root' })
export class MovimientoApi extends RecursoApi<Movimiento, MovimientoSolicitud> {
  constructor() {
    super('movimientos');
  }
}

import { Injectable } from '@angular/core';
import { Cliente, ClienteSolicitud } from '../models/cliente';
import { RecursoApi } from './recurso-api';

@Injectable({ providedIn: 'root' })
export class ClienteApi extends RecursoApi<Cliente, ClienteSolicitud> {
  constructor() {
    super('clientes');
  }
}

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Reporte, ReportePdf } from '../models/reporte';
import { API_URL } from './recurso-api';

export interface FiltroReporte {
  clienteId: string;
  /** Formato yyyy-MM-dd. */
  fechaInicio: string;
  /** Formato yyyy-MM-dd. */
  fechaFin: string;
}

@Injectable({ providedIn: 'root' })
export class ReporteApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(API_URL)}/reportes`;

  generar(filtro: FiltroReporte): Observable<Reporte> {
    return this.http.get<Reporte>(this.url, { params: this.parametros(filtro, 'json') });
  }

  generarPdf(filtro: FiltroReporte): Observable<ReportePdf> {
    return this.http.get<ReportePdf>(this.url, { params: this.parametros(filtro, 'pdf') });
  }

  private parametros(filtro: FiltroReporte, formato: 'json' | 'pdf'): Record<string, string> {
    return {
      fecha: `${filtro.fechaInicio},${filtro.fechaFin}`,
      clienteId: filtro.clienteId,
      formato,
    };
  }
}

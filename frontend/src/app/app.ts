import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { PanelNotificaciones } from './shared/componentes/panel-notificaciones';

interface OpcionMenu {
  ruta: string;
  etiqueta: string;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, PanelNotificaciones],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly menu: readonly OpcionMenu[] = [
    { ruta: '/clientes', etiqueta: 'Clientes' },
    { ruta: '/cuentas', etiqueta: 'Cuentas' },
    { ruta: '/movimientos', etiqueta: 'Movimientos' },
    { ruta: '/reportes', etiqueta: 'Reportes' },
  ];
}

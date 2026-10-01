import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clientes' },
  {
    path: 'clientes',
    title: 'Clientes | Banco',
    loadComponent: () =>
      import('./features/clientes/clientes-pagina').then((m) => m.ClientesPagina),
  },
  {
    path: 'cuentas',
    title: 'Cuentas | Banco',
    loadComponent: () => import('./features/cuentas/cuentas-pagina').then((m) => m.CuentasPagina),
  },
  {
    path: 'movimientos',
    title: 'Movimientos | Banco',
    loadComponent: () =>
      import('./features/movimientos/movimientos-pagina').then((m) => m.MovimientosPagina),
  },
  {
    path: 'reportes',
    title: 'Reportes | Banco',
    loadComponent: () =>
      import('./features/reportes/reportes-pagina').then((m) => m.ReportesPagina),
  },
  { path: '**', redirectTo: 'clientes' },
];

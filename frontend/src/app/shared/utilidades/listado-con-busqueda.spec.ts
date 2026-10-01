import { of, Subject, throwError } from 'rxjs';
import { ErrorApi } from '../../core/http/error-api';
import { ListadoConBusqueda } from './listado-con-busqueda';

interface Fila {
  id: number;
  nombre: string;
}

describe('ListadoConBusqueda', () => {
  const crear = () => new ListadoConBusqueda<Fila>((fila) => [fila.nombre]);

  it('carga los registros y apaga el indicador de carga al terminar', () => {
    const listado = crear();
    const fuente = new Subject<Fila[]>();
    const alFallar = jest.fn();

    listado.cargar(fuente, alFallar);
    expect(listado.cargando()).toBe(true);

    fuente.next([{ id: 1, nombre: 'José' }]);
    fuente.complete();
    expect(listado.cargando()).toBe(false);
    expect(listado.registros()).toHaveLength(1);
    expect(alFallar).not.toHaveBeenCalled();
  });

  it('filtra en vivo segun la busqueda', () => {
    const listado = crear();
    listado.cargar(
      of([
        { id: 1, nombre: 'José Lema' },
        { id: 2, nombre: 'Juan Osorio' },
      ]),
      () => undefined,
    );

    listado.busqueda.set('osorio');
    expect(listado.filtrados().map((f) => f.id)).toEqual([2]);

    listado.busqueda.set('');
    expect(listado.filtrados()).toHaveLength(2);
  });

  it('avisa el error y deja de cargar', () => {
    const listado = crear();
    const alFallar = jest.fn();

    listado.cargar(
      throwError(() => new ErrorApi(0, 'Sin conexión')),
      alFallar,
    );

    expect(alFallar).toHaveBeenCalledWith(expect.objectContaining({ message: 'Sin conexión' }));
    expect(listado.cargando()).toBe(false);
  });
});

export type Genero = 'MASCULINO' | 'FEMENINO' | 'OTRO';

export const GENEROS: readonly Genero[] = ['MASCULINO', 'FEMENINO', 'OTRO'];

export interface Cliente {
  id: number;
  clienteId: string;
  nombre: string;
  genero: Genero;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono: string;
  estado: boolean;
}

/** Cuerpo de POST y PUT. En PUT la contrasena es opcional: si llega vacia se conserva la actual. */
export interface ClienteSolicitud {
  nombre: string;
  genero: Genero;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono: string;
  clienteId: string;
  contrasena: string | null;
  estado: boolean;
}

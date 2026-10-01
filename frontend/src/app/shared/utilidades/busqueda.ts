/** Minusculas y sin tildes, para que "jose" encuentre "José". */
export function normalizarTexto(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim();
}

/**
 * Busqueda rapida: una fila coincide si contiene todas las palabras buscadas,
 * en cualquier orden y en cualquiera de sus valores.
 */
export function filtrarPorTexto<T>(
  filas: readonly T[],
  texto: string,
  valoresDe: (fila: T) => readonly unknown[],
): T[] {
  const palabras = normalizarTexto(texto).split(/\s+/).filter(Boolean);
  if (palabras.length === 0) {
    return [...filas];
  }
  return filas.filter((fila) => {
    const contenido = normalizarTexto(
      valoresDe(fila)
        .map((valor) => String(valor ?? ''))
        .join(' '),
    );
    return palabras.every((palabra) => contenido.includes(palabra));
  });
}

export function base64ABlob(contenidoBase64: string, tipoContenido: string): Blob {
  const binario = atob(contenidoBase64);
  const bytes = Uint8Array.from(binario, (caracter) => caracter.charCodeAt(0));
  return new Blob([bytes], { type: tipoContenido });
}

/** Guarda en el equipo un archivo que la API entrega como base64. */
export function descargarBase64(
  contenidoBase64: string,
  nombreArchivo: string,
  tipoContenido: string,
): void {
  const url = URL.createObjectURL(base64ABlob(contenidoBase64, tipoContenido));
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombreArchivo;
  enlace.click();
  URL.revokeObjectURL(url);
}

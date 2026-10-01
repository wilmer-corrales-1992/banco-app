import { base64ABlob, descargarBase64 } from './descarga';

describe('descarga', () => {
  // "%PDF-" en base64
  const PDF_BASE64 = 'JVBERi0=';

  it('convierte base64 en un Blob con los bytes y el tipo correctos', async () => {
    const blob = base64ABlob(PDF_BASE64, 'application/pdf');

    expect(blob.type).toBe('application/pdf');
    expect(blob.size).toBe(5);
    expect(await blob.text()).toBe('%PDF-');
  });

  it('descarga el archivo con su nombre y libera la URL temporal', () => {
    const crear = jest.fn(() => 'blob:prueba');
    const liberar = jest.fn();
    Object.assign(URL, { createObjectURL: crear, revokeObjectURL: liberar });
    const clic = jest.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (
      this: HTMLAnchorElement,
    ) {
      expect(this.download).toBe('estado-cuenta.pdf');
      expect(this.href).toBe('blob:prueba');
    });

    descargarBase64(PDF_BASE64, 'estado-cuenta.pdf', 'application/pdf');

    expect(crear).toHaveBeenCalledWith(expect.any(Blob));
    expect(clic).toHaveBeenCalledTimes(1);
    expect(liberar).toHaveBeenCalledWith('blob:prueba');
    clic.mockRestore();
  });
});

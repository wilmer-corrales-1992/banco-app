package com.banco.api.reporte;

import com.banco.api.dto.ReporteCuenta;
import com.banco.api.dto.ReporteMovimiento;
import com.banco.api.dto.ReportePdfResponse;
import com.banco.api.dto.ReporteResponse;
import com.banco.api.entity.TipoCuenta;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Dibuja el estado de cuenta en PDF (A4 horizontal, fondo blanco) y lo entrega como base64.
 */
@Component
public class PdfReporteFormateador implements ReporteFormateador {

    private static final String TIPO_CONTENIDO = "application/pdf";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA_ARCHIVO = DateTimeFormatter.BASIC_ISO_DATE;

    private static final List<String> COLUMNAS = List.of(
            "Fecha", "Cliente", "Numero cuenta", "Tipo", "Saldo inicial", "Estado", "Movimiento", "Saldo disponible");
    private static final float[] ANCHOS = {13, 20, 11, 9, 11, 8, 11, 12};

    private static final Color NEGRO = new Color(0x22, 0x22, 0x22);
    private static final Color GRIS = new Color(0x66, 0x66, 0x66);
    private static final Color GRIS_CABECERA = new Color(0xEE, 0xEE, 0xEE);
    private static final Color BORDE = new Color(0xCC, 0xCC, 0xCC);
    private static final Color CREDITO = new Color(0x1B, 0x6E, 0x3A);
    private static final Color DEBITO = new Color(0xB0, 0x2A, 0x2A);

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL, NEGRO);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.NORMAL, NEGRO);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, NEGRO);
    private static final Font TEXTO_SUAVE = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, GRIS);
    private static final Font CABECERA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, NEGRO);
    private static final Font CELDA = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, NEGRO);
    private static final Font CELDA_CREDITO = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, CREDITO);
    private static final Font CELDA_DEBITO = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, DEBITO);
    private static final Font TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, NEGRO);

    @Override
    public FormatoReporte formato() {
        return FormatoReporte.PDF;
    }

    @Override
    public ReportePdfResponse formatear(ReporteResponse reporte) {
        return new ReportePdfResponse(
                nombreArchivo(reporte),
                TIPO_CONTENIDO,
                Base64.getEncoder().encodeToString(generarPdf(reporte)));
    }

    byte[] generarPdf(ReporteResponse reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.addTitle("Estado de cuenta " + reporte.clienteId());
            documento.open();
            agregarEncabezado(documento, reporte);
            agregarResumen(documento, reporte);
            if (reporte.cuentas().isEmpty()) {
                documento.add(new Paragraph("El cliente no tiene cuentas registradas.", TEXTO));
            }
            reporte.cuentas().forEach(cuenta -> agregarCuenta(documento, cuenta));
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return salida.toByteArray();
    }

    private void agregarEncabezado(Document documento, ReporteResponse reporte) {
        documento.add(new Paragraph("BANCO", TEXTO_SUAVE));
        Paragraph titulo = new Paragraph("Estado de cuenta", TITULO);
        titulo.setSpacingAfter(6);
        documento.add(titulo);
        documento.add(new Paragraph("Cliente: " + reporte.cliente() + " (" + reporte.clienteId() + ")", TEXTO));
        documento.add(new Paragraph(
                "Periodo: " + reporte.fechaInicio().format(FECHA) + " al " + reporte.fechaFin().format(FECHA), TEXTO));
        Paragraph generado = new Paragraph("Generado: " + reporte.fechaGeneracion().format(FECHA_HORA), TEXTO_SUAVE);
        generado.setSpacingAfter(10);
        documento.add(generado);
    }

    private void agregarResumen(Document documento, ReporteResponse reporte) {
        PdfPTable resumen = new PdfPTable(new float[]{1, 1, 1});
        resumen.setWidthPercentage(60);
        resumen.setHorizontalAlignment(Element.ALIGN_LEFT);
        resumen.setSpacingAfter(14);
        Stream.of("Cuentas", "Total creditos", "Total debitos")
                .map(texto -> celda(texto, CABECERA, Element.ALIGN_CENTER, GRIS_CABECERA))
                .forEach(resumen::addCell);
        resumen.addCell(celda(String.valueOf(reporte.cuentas().size()), TOTAL, Element.ALIGN_CENTER, null));
        resumen.addCell(celda(dinero(reporte.totalCreditos()), TOTAL, Element.ALIGN_CENTER, null));
        resumen.addCell(celda(dinero(reporte.totalDebitos()), TOTAL, Element.ALIGN_CENTER, null));
        documento.add(resumen);
    }

    private void agregarCuenta(Document documento, ReporteCuenta cuenta) {
        Paragraph titulo = new Paragraph(
                "Cuenta " + cuenta.numeroCuenta() + "  " + tipo(cuenta.tipoCuenta()), SUBTITULO);
        titulo.setSpacingBefore(6);
        documento.add(titulo);
        Paragraph detalle = new Paragraph(
                "Saldo inicial " + dinero(cuenta.saldoInicial())
                        + "    Saldo disponible " + dinero(cuenta.saldoDisponible())
                        + "    Estado " + estado(cuenta.estado()),
                TEXTO_SUAVE);
        detalle.setSpacingAfter(4);
        documento.add(detalle);

        PdfPTable tabla = new PdfPTable(ANCHOS);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        tabla.setSpacingAfter(10);
        COLUMNAS.stream()
                .map(columna -> celda(columna, CABECERA, Element.ALIGN_CENTER, GRIS_CABECERA))
                .forEach(tabla::addCell);

        if (cuenta.movimientos().isEmpty()) {
            PdfPCell vacia = celda("Sin movimientos en el periodo", TEXTO_SUAVE, Element.ALIGN_CENTER, null);
            vacia.setColspan(COLUMNAS.size());
            tabla.addCell(vacia);
        }
        cuenta.movimientos().forEach(movimiento -> agregarFila(tabla, movimiento));

        PdfPCell etiquetaTotales = celda("Totales del periodo", TOTAL, Element.ALIGN_RIGHT, GRIS_CABECERA);
        etiquetaTotales.setColspan(5);
        tabla.addCell(etiquetaTotales);
        PdfPCell totales = celda(
                "Creditos " + dinero(cuenta.totalCreditos()) + "    Debitos " + dinero(cuenta.totalDebitos()),
                TOTAL, Element.ALIGN_LEFT, GRIS_CABECERA);
        totales.setColspan(3);
        tabla.addCell(totales);

        documento.add(tabla);
    }

    private void agregarFila(PdfPTable tabla, ReporteMovimiento fila) {
        Font fuenteMovimiento = fila.movimiento().signum() < 0 ? CELDA_DEBITO : CELDA_CREDITO;
        tabla.addCell(celda(fila.fecha().format(FECHA_HORA), CELDA, Element.ALIGN_LEFT, null));
        tabla.addCell(celda(fila.cliente(), CELDA, Element.ALIGN_LEFT, null));
        tabla.addCell(celda(fila.numeroCuenta(), CELDA, Element.ALIGN_LEFT, null));
        tabla.addCell(celda(tipo(fila.tipo()), CELDA, Element.ALIGN_LEFT, null));
        tabla.addCell(celda(dinero(fila.saldoInicial()), CELDA, Element.ALIGN_RIGHT, null));
        tabla.addCell(celda(estado(fila.estado()), CELDA, Element.ALIGN_CENTER, null));
        tabla.addCell(celda(dinero(fila.movimiento()), fuenteMovimiento, Element.ALIGN_RIGHT, null));
        tabla.addCell(celda(dinero(fila.saldoDisponible()), CELDA, Element.ALIGN_RIGHT, null));
    }

    private static PdfPCell celda(String texto, Font fuente, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(5);
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(BORDE);
        celda.setBorderWidth(0.5f);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    private static String dinero(BigDecimal valor) {
        return String.format(Locale.US, "%,.2f", valor);
    }

    private static String estado(Boolean activo) {
        return Boolean.TRUE.equals(activo) ? "Activa" : "Inactiva";
    }

    private static String tipo(TipoCuenta tipoCuenta) {
        return tipoCuenta == TipoCuenta.AHORRO ? "Ahorro" : "Corriente";
    }

    private static String nombreArchivo(ReporteResponse reporte) {
        return "estado-cuenta-" + reporte.clienteId()
                + "-" + reporte.fechaInicio().format(FECHA_ARCHIVO)
                + "-" + reporte.fechaFin().format(FECHA_ARCHIVO) + ".pdf";
    }
}

// PdfCertificadoGenerator.java — componente dedicado, responsabilidad única

package com.proyecto.fhce.library.services.loads;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Component
public class PdfCertificadoGenerator {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DeviceRgb COLOR_AZUL_INSTITUCIONAL = new DeviceRgb(0, 51, 102);
    private static final DeviceRgb COLOR_VERDE_SELLO = new DeviceRgb(0, 120, 60);

    @Value("${app.certificados.ruta-base:/certificados}")
    private String rutaBase;

    /**
     * Genera el PDF del certificado y retorna la ruta donde fue guardado.
     */
    public String generar(CertificadoNoDeuda certificado) throws IOException {
        Path directorio = Paths.get(rutaBase);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        String nombreArchivo = "certificado-" + certificado.getIdCertificado() + ".pdf";
        Path rutaArchivo = directorio.resolve(nombreArchivo);

        try (PdfWriter writer = new PdfWriter(rutaArchivo.toString());
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc, PageSize.A4)) {

            document.setMargins(60, 60, 60, 60);

            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            agregarEncabezadoInstitucional(document, fontBold, certificado);
            agregarTitulo(document, fontBold);
            agregarCuerpoLegal(document, fontNormal, fontBold, fontItalic, certificado);
            agregarTablaDetalles(document, fontNormal, fontBold, certificado);
            agregarSelloVigencia(document, fontBold, certificado);
            agregarCodigoVerificacion(document, fontNormal, fontItalic, certificado);
            agregarPieDePagina(document, fontItalic, certificado);
        }

        // return rutaArchivo.toString();
        return nombreArchivo;
    }

    private void agregarEncabezadoInstitucional(Document doc, PdfFont fontBold,
            CertificadoNoDeuda certificado) throws IOException {
        // Nombre de la institución / facultad
        doc.add(new Paragraph("FACULTAD DE HUMANIDADES Y CIENCIAS DE LA EDUCACIÓN")
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(COLOR_AZUL_INSTITUCIONAL)
                .setTextAlignment(TextAlignment.CENTER));

        // Nombre de la biblioteca
        String nombreBiblioteca = certificado.getBiblioteca() != null
                ? certificado.getBiblioteca().getNombre().toUpperCase()
                : "BIBLIOTECA";

        doc.add(new Paragraph(nombreBiblioteca)
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(COLOR_AZUL_INSTITUCIONAL)
                .setTextAlignment(TextAlignment.CENTER));

        // Línea separadora
        doc.add(new Paragraph("─".repeat(80))
                .setFontColor(COLOR_AZUL_INSTITUCIONAL)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(4)
                .setMarginBottom(4));
    }

    private void agregarTitulo(Document doc, PdfFont fontBold) throws IOException {
        doc.add(new Paragraph("CERTIFICADO DE NO DEUDA")
                .setFont(fontBold)
                .setFontSize(18)
                .setFontColor(COLOR_AZUL_INSTITUCIONAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setMarginBottom(20));
    }

    private void agregarCuerpoLegal(Document doc, PdfFont fontNormal, PdfFont fontBold,
            PdfFont fontItalic, CertificadoNoDeuda certificado) throws IOException {

        String nombreCompleto = construirNombreCompleto(certificado);
        String ci = certificado.getUsuario().getPersona() != null
                ? certificado.getUsuario().getPersona().getCi() + ""
                : "N/D";
        String nombreBiblioteca = certificado.getBiblioteca() != null
                ? certificado.getBiblioteca().getNombre()
                : "la biblioteca";
        String fechaEmision = certificado.getFechaEmision().format(FORMATO_FECHA);

        // Párrafo principal — texto legal del certificado
        Paragraph cuerpo = new Paragraph()
                .setFont(fontNormal)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginBottom(16)
                // .setLeading(16);
                .setMultipliedLeading(1.2f);

        cuerpo.add("El/La suscrito/a, responsable de la ");
        cuerpo.add(new com.itextpdf.layout.element.Text(nombreBiblioteca)
                .setFont(fontBold));
        cuerpo.add(", certifica que el/la estudiante ");
        cuerpo.add(new com.itextpdf.layout.element.Text(nombreCompleto)
                .setFont(fontBold));
        cuerpo.add(", con Cédula de Identidad N° ");
        cuerpo.add(new com.itextpdf.layout.element.Text(ci)
                .setFont(fontBold));
        cuerpo.add(", ");
        cuerpo.add(new com.itextpdf.layout.element.Text(
                "NO REGISTRA NINGÚN TIPO DE DEUDA, PRÉSTAMO ACTIVO, VENCIDO NI RENOVACIÓN PENDIENTE")
                .setFont(fontBold)
                .setFontColor(COLOR_VERDE_SELLO));
        cuerpo.add(" en esta unidad de información a la fecha de emisión del presente documento: ");
        cuerpo.add(new com.itextpdf.layout.element.Text(fechaEmision + ".")
                .setFont(fontBold));

        doc.add(cuerpo);

        doc.add(new Paragraph(
                "El presente certificado es válido únicamente para los trámites institucionales " +
                        "que lo requieran y puede ser verificado mediante el código de verificación " +
                        "que figura al pie del documento.")
                .setFont(fontItalic)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginBottom(20));
    }

    private void agregarTablaDetalles(Document doc, PdfFont fontNormal, PdfFont fontBold,
            CertificadoNoDeuda certificado) throws IOException {

        Table tabla = new Table(UnitValue.createPercentArray(new float[] { 35, 65 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        DeviceRgb colorFondo = new DeviceRgb(240, 245, 255);

        // Helper para agregar filas
        agregarFilaTabla(tabla, "Estudiante:", construirNombreCompleto(certificado), fontBold, fontNormal, colorFondo,
                true);
        agregarFilaTabla(tabla, "C.I.:", obtenerCi(certificado), fontBold, fontNormal, colorFondo, false);
        agregarFilaTabla(tabla, "Biblioteca:", obtenerNombreBiblioteca(certificado), fontBold, fontNormal, colorFondo,
                true);
        agregarFilaTabla(tabla, "Fecha de Emisión:", certificado.getFechaEmision().format(FORMATO_FECHA), fontBold,
                fontNormal, colorFondo, false);
        agregarFilaTabla(tabla, "Fecha de Vencimiento:",
                certificado.getFechaVencimiento() != null
                        ? certificado.getFechaVencimiento().format(FORMATO_FECHA)
                        : "Sin vencimiento",
                fontBold, fontNormal, colorFondo, true);
        agregarFilaTabla(tabla, "Emitido por:", obtenerNombreBibliotecario(certificado), fontBold, fontNormal,
                colorFondo,
                false);

        doc.add(tabla);
    }

    private void agregarFilaTabla(Table tabla, String etiqueta, String valor,
            PdfFont fontBold, PdfFont fontNormal, DeviceRgb colorFondo, boolean fondoAlterno) {

        Cell celdaEtiqueta = new Cell()
                .add(new Paragraph(etiqueta).setFont(fontBold).setFontSize(10))
                .setBackgroundColor(fondoAlterno ? colorFondo : ColorConstants.WHITE)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(6);

        Cell celdaValor = new Cell()
                .add(new Paragraph(valor).setFont(fontNormal).setFontSize(10))
                .setBackgroundColor(fondoAlterno ? colorFondo : ColorConstants.WHITE)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(6);

        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    private void agregarSelloVigencia(Document doc, PdfFont fontBold,
            CertificadoNoDeuda certificado) throws IOException {

        String textoSello = "✓  CERTIFICADO " + certificado.getEstadoCertificado().name();

        doc.add(new Paragraph(textoSello)
                .setFont(fontBold)
                .setFontSize(13)
                .setFontColor(COLOR_VERDE_SELLO)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(COLOR_VERDE_SELLO, 1.5f))
                .setPadding(10)
                .setMarginBottom(20));
    }

    private void agregarCodigoVerificacion(Document doc, PdfFont fontNormal, PdfFont fontItalic,
            CertificadoNoDeuda certificado) throws IOException {

        doc.add(new Paragraph("CÓDIGO DE VERIFICACIÓN")
                .setFont(fontNormal)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(certificado.getCodigoVerificacion())
                .setFont(fontNormal)
                .setFontSize(12)
                .setFontColor(COLOR_AZUL_INSTITUCIONAL)
                .setTextAlignment(TextAlignment.CENTER)
                // .setLetterSpacing(3)
                .setCharacterSpacing(3)
                .setMarginBottom(4));

        doc.add(new Paragraph("Verifique la autenticidad de este documento en: /api/certificados/validar/" +
                certificado.getCodigoVerificacion())
                .setFont(fontItalic)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    private void agregarPieDePagina(Document doc, PdfFont fontItalic,
            CertificadoNoDeuda certificado) throws IOException {

        doc.add(new Paragraph("─".repeat(80))
                .setFontColor(ColorConstants.LIGHT_GRAY)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(
                "Documento generado electrónicamente el " +
                        certificado.getFechaEmision().format(FORMATO_FECHA) +
                        " — SIGEB · Sistema de Gestión de Bibliotecas")
                .setFont(fontItalic)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // ── Helpers de datos ──────────────────────────────────────────────────────

    private String construirNombreCompleto(CertificadoNoDeuda certificado) {
        if (certificado.getUsuario().getPersona() == null)
            return "N/D";
        var p = certificado.getUsuario().getPersona();
        return (p.getNombre() + " " + p.getApellido_pat() + " " + p.getApellido_mat()).trim();
    }

    private String obtenerCi(CertificadoNoDeuda certificado) {
        return certificado.getUsuario().getPersona() != null
                ? certificado.getUsuario().getPersona().getCi() + ""
                : "N/D";
    }

    private String obtenerNombreBiblioteca(CertificadoNoDeuda certificado) {
        return certificado.getBiblioteca() != null
                ? certificado.getBiblioteca().getNombre()
                : "N/D";
    }

    private String obtenerNombreBibliotecario(CertificadoNoDeuda certificado) {
        if (certificado.getBibliotecario() == null)
            return "N/D";
        if (certificado.getBibliotecario().getPersona() == null)
            return certificado.getBibliotecario().getUsername();
        var p = certificado.getBibliotecario().getPersona();
        return (p.getNombre() + " " + p.getApellido_pat()).trim();
    }
}
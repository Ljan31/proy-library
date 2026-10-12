package com.proyecto.fhce.library.services.library;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

import org.docx4j.relationships.Relationship;

import org.docx4j.wml.Body;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

@Service
public class DocxPlaceholderService {

  /**
   * Recibe la plantilla como bytes, reemplaza los placeholders y retorna
   * el documento resultante como bytes. La plantilla original NO se toca.
   *
   * Maneja correctamente el caso en que Word fragmenta el texto del
   * placeholder en múltiples runs (ej: {{nom + bre}} en w:r separados).
   */
  public byte[] reemplazar(byte[] plantillaBytes, Map<String, String> variables) throws Exception {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(plantillaBytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

      WordprocessingMLPackage wordPackage = WordprocessingMLPackage.load(bis);
      MainDocumentPart mainPart = wordPackage.getMainDocumentPart();

      // 1. Normalizar runs para que cada párrafo tenga sus placeholders
      // en un solo run (soluciona la fragmentación de Word)
      normalizarParagrafos(mainPart.getContent());

      // 2. Reemplazar en el cuerpo principal del documento
      reemplazarEnParte(mainPart, variables);

      // 3. Reemplazar en encabezados y pies de página
      RelationshipsPart rel = mainPart.getRelationshipsPart();
      if (rel != null) {
        for (Relationship r : rel.getRelationships().getRelationship()) {
          if (r.getType().endsWith("/header") || r.getType().endsWith("/footer")) {
            // JaxbXmlPart parte = (JaxbXmlPart) mainPart.getPart(r);
            // JaxbXmlPart parte = (JaxbXmlPart) wordPackage.getParts().get(r);
            // JaxbXmlPart parte = (JaxbXmlPart) wordPackage.getParts().get(new
            // PartName(r.getTarget()));
            PartName nombreParte = new PartName("/word/" + r.getTarget());
            JaxbXmlPart<?> parte = (JaxbXmlPart<?>) wordPackage.getParts().get(nombreParte);

            if (parte != null)
              reemplazarEnParte(parte, variables);
          }
        }
      }

      wordPackage.save(bos);
      return bos.toByteArray();
    }
  }

  /**
   * Normaliza los runs de cada párrafo para que el texto de un placeholder
   * que Word fragmentó quede en un único run consecutivo.
   *
   * Estrategia: concatenar el texto de todos los runs de un párrafo en el
   * primero y vaciar los demás. Esto preserva el estilo del primer run.
   */
  private void normalizarParagrafos(List<Object> contenido) {
    for (Object elemento : contenido) {
      Object desenmascarado = XmlUtils.unwrap(elemento);

      if (desenmascarado instanceof P parrafo) {
        normalizarRunsDeParrafo(parrafo);
      } else if (desenmascarado instanceof Body body) {
        normalizarParagrafos(body.getContent());
      } else if (desenmascarado instanceof Tbl tabla) {
        for (Object fila : tabla.getContent()) {
          Object f = XmlUtils.unwrap(fila);
          if (f instanceof Tr tr) {
            for (Object celda : tr.getContent()) {
              Object c = XmlUtils.unwrap(celda);
              if (c instanceof Tc tc)
                normalizarParagrafos(tc.getContent());
            }
          }
        }
      }
    }
  }

  private void normalizarRunsDeParrafo(P parrafo) {
    // Recopilar todos los runs del párrafo
    List<R> runs = parrafo.getContent().stream()
        .map(XmlUtils::unwrap)
        .filter(o -> o instanceof R)
        .map(o -> (R) o)
        .toList();

    if (runs.size() < 2)
      return;

    // Construir el texto completo del párrafo
    StringBuilder textoTotal = new StringBuilder();
    for (R run : runs) {
      textoTotal.append(extraerTextoDeRun(run));
    }

    String texto = textoTotal.toString();

    // Solo normalizar si contiene al menos un placeholder (evita trabajo
    // innecesario)
    if (!texto.contains("{{"))
      return;

    // Poner todo el texto en el primer run y vaciar el resto
    R primerRun = runs.get(0);
    establecerTextoEnRun(primerRun, texto);
    for (int i = 1; i < runs.size(); i++) {
      establecerTextoEnRun(runs.get(i), "");
    }
  }

  private String extraerTextoDeRun(R run) {
    StringBuilder sb = new StringBuilder();
    for (Object contenido : run.getContent()) {
      Object d = XmlUtils.unwrap(contenido);
      if (d instanceof Text t)
        sb.append(t.getValue());
    }
    return sb.toString();
  }

  private void establecerTextoEnRun(R run, String texto) {
    // Limpiar contenido de texto existente
    run.getContent().removeIf(c -> XmlUtils.unwrap(c) instanceof Text);

    // Agregar nuevo nodo Text
    Text nuevoTexto = new Text();
    nuevoTexto.setValue(texto);
    nuevoTexto.setSpace("preserve"); // respetar espacios
    run.getContent().add(nuevoTexto);
  }

  private void reemplazarEnParte(JaxbXmlPart<?> parte, Map<String, String> variables) throws Exception {
    // Serializar a XML, reemplazar con regex y deserializar
    // Es el método más robusto para partes ya normalizadas
    String xml = parte.getXML();

    for (Map.Entry<String, String> entry : variables.entrySet()) {
      String placeholder = "\\{\\{" + Pattern.quote(entry.getKey()) + "\\}\\}";
      String valor = entry.getValue() != null
          ? Matcher.quoteReplacement(entry.getValue())
          : "";
      xml = xml.replaceAll(placeholder, valor);
    }

    // parte.setXML(xml);
    // parte.setJaxbElement(XmlUtils.unmarshalString(xml));
    // Deserializar el XML modificado
    Object objDeserializado = XmlUtils.unmarshalString(xml);

    @SuppressWarnings("rawtypes")
    JaxbXmlPart parteRaw = parte;
    parteRaw.setJaxbElement(objDeserializado);
  }

  private void reemplazarImagenes(
      WordprocessingMLPackage wordPackage,
      MainDocumentPart mainPart,
      Map<String, byte[]> imagenes) throws Exception {
    List<Object> contenido = mainPart.getContent();
    reemplazarImagenesEnLista(wordPackage, mainPart, contenido, imagenes);
  }

  private void reemplazarImagenesEnLista(
      WordprocessingMLPackage wordPackage,
      MainDocumentPart mainPart,
      List<Object> contenido,
      Map<String, byte[]> imagenes) throws Exception {
    Pattern patron = Pattern.compile("\\+\\+\\+IMAGE\\(([^)]+)\\)\\+\\+\\+");
    for (Object elemento : contenido) {
      Object obj = XmlUtils.unwrap(elemento);
      if (obj instanceof P parrafo) {
        String textoParrafo = extraerTextoDeParrafo(parrafo);
        Matcher m = patron.matcher(textoParrafo);
        if (m.find()) {
          String clave = m.group(1).trim().toLowerCase(); // "firma", "sello", "qr"
          byte[] imgBytes = imagenes.get(clave);
          if (imgBytes != null) {
            // Reemplazar el párrafo completo con la imagen
            P parrafoConImagen = crearParrafoConImangen(
                wordPackage, mainPart, imgBytes, clave);

            // Sustituir in-place en la lista
            int idx = contenido.indexOf(elemento);
            if (idx >= 0)
              contenido.set(idx, parrafoConImagen);
          } else {
            limpiarTextoDeParrafo(parrafo);
          }
        }
      } else if (obj instanceof Tbl tabla) {
        for (Object fila : tabla.getContent()) {
          Object f = XmlUtils.unwrap(fila);
          if (f instanceof Tr tr) {
            for (Object celda : tr.getContent()) {
              Object c = XmlUtils.unwrap(celda);
              if (c instanceof Tc tc)
                reemplazarImagenesEnLista(wordPackage, mainPart, tc.getContent(), imagenes);
            }
          }
        }
      }
    }
  }

  private P crearParrafoConImangen(
      WordprocessingMLPackage wordPackage,
      MainDocumentPart mainPart,
      byte[] imgBytes,
      String nombre) throws Exception {
    // Detectar tipo MIME por magic bytes
    String contentType = detectarContentType(imgBytes);

    // Agregar imagen al package y obtener el part
    BinaryPartAbstractImage imagePart = BinaryPartAbstractImage
        .createImagePart(wordPackage, imgBytes);

    // Crear el inline con dimensiones razonables para firma/sello (4cm x 2cm)
    long cx = 3_600_000L; // EMU: 1 cm = 914400 EMU; 4 cm ≈ 3_657_600
    long cy = 1_800_000L; // 2 cm

    Inline inline = imagePart.createImageInline(
        nombre, // filename hint
        nombre, // alt text
        1, // id de la imagen (único en el doc)
        2,
        cx, cy,
        false);

    // Construir párrafo con el run que contiene la imagen
    P p = new P();
    R r = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(inline);
    r.getContent().add(drawing);
    p.getContent().add(r);
    return p;
  }

  private String extraerTextoDeParrafo(P parrafo) {
    StringBuilder sb = new StringBuilder();
    for (Object c : parrafo.getContent()) {
      Object obj = XmlUtils.unwrap(c);
      if (obj instanceof R run) {
        for (Object rc : run.getContent()) {
          Object ro = XmlUtils.unwrap(rc);
          if (ro instanceof Text t)
            sb.append(t.getValue());
        }
      }
    }
    return sb.toString();
  }

  private void limpiarTextoDeParrafo(P parrafo) {
    parrafo.getContent().removeIf(c -> XmlUtils.unwrap(c) instanceof R);
  }

  private String detectarContentType(byte[] bytes) {
    // PNG: magic bytes 89 50 4E 47
    if (bytes.length >= 4 &&
        bytes[0] == (byte) 0x89 && bytes[1] == 0x50 &&
        bytes[2] == 0x4E && bytes[3] == 0x47) {
      return "image/png";
    }
    // JPEG: FF D8
    if (bytes.length >= 2 &&
        bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
      return "image/jpeg";
    }
    return "image/png"; // default
  }
}
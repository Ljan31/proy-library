package com.proyecto.fhce.library.services.library;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.proyecto.fhce.library.config.ImagenConfigProvider;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;

import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocxPlaceholderService {
  private final ImagenConfigProvider imagenConfig;

  public DocxPlaceholderService(
      ImagenConfigProvider imagenConfig) {
    this.imagenConfig = imagenConfig;
  }

  private static final Logger log = LoggerFactory.getLogger(FormatoCertificadoService.class);
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)\\}\\}");
  private static final Pattern IMAGE_MARKER = Pattern.compile("\\+\\+\\+IMAGE\\(([^)]+)\\)\\+\\+\\+");

  /**
   * Recibe la plantilla como bytes, reemplaza los placeholders y retorna
   * el documento resultante como bytes. La plantilla original NO se toca.
   *
   * Maneja correctamente el caso en que Word fragmenta el texto del
   * placeholder en múltiples runs (ej: {{nom + bre}} en w:r separados).
   */
  public byte[] reemplazar(byte[] plantillaBytes, Map<String, String> variables, Map<String, byte[]> imagenes)
      throws Exception {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(plantillaBytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

      WordprocessingMLPackage pkg = WordprocessingMLPackage.load(bis);
      MainDocumentPart main = pkg.getMainDocumentPart();

      // 1. Procesar cuerpo del documento
      procesarContenido(pkg, main.getContent(), variables, imagenes);

      // 2. Procesar encabezados y pies de página
      procesarHeadersFooters(pkg, variables, imagenes);

      pkg.save(bos);
      return bos.toByteArray();

    }
  }

  /**
   * Recorre la lista de contenido (Body, celdas de tabla, etc.)
   * y procesa cada párrafo o tabla que encuentre.
   */
  private void procesarContenido(
      WordprocessingMLPackage pkg,
      List<Object> contenido,
      Map<String, String> variables,
      Map<String, byte[]> imagenes) throws Exception {

    Pattern imgPattern = IMAGE_MARKER;

    for (int i = 0; i < contenido.size(); i++) {
      Object elem = XmlUtils.unwrap(contenido.get(i));

      if (elem instanceof P parrafo) {
        // ¿Es un marcador de imagen?
        String textoParagrafo = extraerTextoCompleto(parrafo);
        Matcher mImg = imgPattern.matcher(textoParagrafo);

        if (mImg.find() && imagenes != null) {
          String clave = mImg.group(1).trim().toLowerCase();
          byte[] imgBytes = imagenes.get(clave);
          if (imgBytes != null) {
            P reemplazo = crearParrafoConImagen(pkg, imgBytes, clave, parrafo);
            contenido.set(i, reemplazo);
          } else {
            vaciarParrafo(parrafo);
          }
        } else {
          // Reemplazar variables de texto preservando formato
          procesarParrafo(parrafo, variables);
        }

      } else if (elem instanceof Tbl tabla) {
        // Recorrer celdas de la tabla recursivamente
        for (Object filaObj : tabla.getContent()) {
          Object fila = XmlUtils.unwrap(filaObj);
          if (fila instanceof Tr tr) {
            for (Object celdaObj : tr.getContent()) {
              Object celda = XmlUtils.unwrap(celdaObj);
              if (celda instanceof Tc tc) {
                procesarContenido(pkg, tc.getContent(), variables, imagenes);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Estrategia:
   * 1. Fusionar runs consecutivos que tengan el MISMO rPr (Word fragmenta por
   * rsid/spellcheck).
   * 2. Por cada run fusionado, reemplazar las variables que quepan completamente
   * dentro.
   * 3. Si un placeholder queda partido entre runs con DISTINTO rPr, usar el rPr
   * del primer
   * fragmento (el que contiene "{{") para todo el valor reemplazado.
   */
  private void procesarParrafo(P parrafo, Map<String, String> variables) {
    // Paso 1: fusionar runs con mismo rPr
    fusionarRunsSimilares(parrafo);

    // Paso 2: manejar placeholders que puedan estar partidos entre runs distintos
    resolverPlaceholdersCortados(parrafo, variables);

    // Paso 3: reemplazar variables dentro de cada run individual
    for (Object elem : parrafo.getContent()) {
      Object obj = XmlUtils.unwrap(elem);
      if (obj instanceof R run) {
        reemplazarEnRun(run, variables);
      }
    }
  }

  /**
   * Fusiona runs CONSECUTIVOS que tienen exactamente el mismo rPr serializado.
   * Esto resuelve la fragmentación por rsid/spellcheck de Word sin tocar el
   * formato.
   */
  private void fusionarRunsSimilares(P parrafo) {
    List<Object> contenido = parrafo.getContent();
    int i = 0;
    while (i < contenido.size() - 1) {
      Object actual = XmlUtils.unwrap(contenido.get(i));
      Object siguiente = XmlUtils.unwrap(contenido.get(i + 1));

      if (actual instanceof R rActual && siguiente instanceof R rSiguiente) {
        if (mismoRpr(rActual, rSiguiente)) {
          // Concatenar texto del siguiente en el actual
          String textoActual = extraerTextoDeRun(rActual);
          String textoSiguiente = extraerTextoDeRun(rSiguiente);
          establecerTextoEnRun(rActual, textoActual + textoSiguiente);
          contenido.remove(i + 1);
          // No incrementar i — revisar el nuevo siguiente
          continue;
        }
      }
      i++;
    }
  }

  /**
   * Detecta placeholders que quedaron PARTIDOS entre runs con distinto rPr.
   * Ejemplo: [run_bold "{{nom"] [run_normal "bre}}"]
   *
   * Estrategia: construir el texto del párrafo con un mapa de posición→run,
   * detectar el placeholder, y reconstruir los runs afectados preservando
   * el rPr del run que contiene el inicio del placeholder.
   */
  private void resolverPlaceholdersCortados(P parrafo, Map<String, String> variables) {
    // Construir índice: posición en cadena → run
    List<RunSegmento> segmentos = construirSegmentos(parrafo);
    if (segmentos.isEmpty())
      return;

    StringBuilder textoTotal = new StringBuilder();
    for (RunSegmento seg : segmentos)
      textoTotal.append(seg.texto);

    String texto = textoTotal.toString();
    if (!texto.contains("{{"))
      return;

    Matcher m = PLACEHOLDER.matcher(texto);
    boolean hayPartidos = false;

    while (m.find()) {
      int inicio = m.start();
      int fin = m.end();

      // ¿Qué runs cubren este placeholder?
      int runInicio = runEnPosicion(segmentos, inicio);
      int runFin = runEnPosicion(segmentos, fin - 1);

      if (runInicio != runFin) {
        hayPartidos = true;
        break;
      }
    }

    if (!hayPartidos)
      return;

    // Reconstruir el párrafo: un run por "segmento lógico" separado por
    // placeholders
    // El rPr del run que contiene {{ se aplica al valor de reemplazo
    List<Object> nuevaLista = new ArrayList<>();
    int posActual = 0;
    m.reset();

    while (m.find()) {
      int inicio = m.start();
      int fin = m.end();
      String clave = m.group(1).trim();
      String valor = variables.getOrDefault(clave, m.group(0)); // si no existe, dejar el placeholder

      // Texto antes del placeholder
      if (posActual < inicio) {
        String antes = texto.substring(posActual, inicio);
        RPr rprAntes = rprEnPosicion(segmentos, posActual);
        nuevaLista.add(crearRun(antes, rprAntes));
      }

      // El placeholder mismo — usar rPr del run donde empieza {{
      RPr rprPlaceholder = rprEnPosicion(segmentos, inicio);
      nuevaLista.add(crearRun(valor, rprPlaceholder));

      posActual = fin;
    }

    // Texto restante después del último placeholder
    if (posActual < texto.length()) {
      String resto = texto.substring(posActual);
      RPr rprResto = rprEnPosicion(segmentos, posActual < texto.length() ? posActual : posActual - 1);
      nuevaLista.add(crearRun(resto, rprResto));
    }

    // Reemplazar runs en el párrafo (preservar elementos no-R como pPr, bookmarks,
    // etc.)
    parrafo.getContent().removeIf(e -> XmlUtils.unwrap(e) instanceof R);
    // Encontrar posición del primer run para insertar ahí
    int insertPos = 0;
    for (int i = 0; i < parrafo.getContent().size(); i++) {
      Object e = XmlUtils.unwrap(parrafo.getContent().get(i));
      if (!(e instanceof PPr)) {
        insertPos = i;
        break;
      }
      insertPos = i + 1;
    }
    parrafo.getContent().addAll(insertPos, nuevaLista);
  }

  /**
   * Reemplaza variables dentro de un único run.
   * El rPr del run NO se toca — solo el contenido de w:t.
   */
  private void reemplazarEnRun(R run, Map<String, String> variables) {
    String texto = extraerTextoDeRun(run);
    if (!texto.contains("{{"))
      return;

    Matcher m = PLACEHOLDER.matcher(texto);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String clave = m.group(1).trim();
      String valor = variables.getOrDefault(clave, m.group(0));
      m.appendReplacement(sb, Matcher.quoteReplacement(valor));
    }
    m.appendTail(sb);

    if (!sb.toString().equals(texto)) {
      establecerTextoEnRun(run, sb.toString());
    }
  }

  private String extraerTextoCompleto(P parrafo) {
    StringBuilder sb = new StringBuilder();
    for (Object c : parrafo.getContent()) {
      Object obj = XmlUtils.unwrap(c);
      if (obj instanceof R run)
        sb.append(extraerTextoDeRun(run));
    }
    return sb.toString();
  }

  /**
   * Compara dos runs por su rPr serializado para detectar si son "iguales en
   * formato".
   */
  private boolean mismoRpr(R a, R b) {
    try {
      String rprA = a.getRPr() == null ? "" : XmlUtils.marshaltoString(a.getRPr(), true);
      String rprB = b.getRPr() == null ? "" : XmlUtils.marshaltoString(b.getRPr(), true);
      return rprA.equals(rprB);
    } catch (Exception e) {
      return false;
    }
  }

  private R crearRun(String texto, RPr rpr) {
    R r = new R();
    if (rpr != null) {
      try {
        // Clonar el rPr para no compartir referencias
        r.setRPr(XmlUtils.deepCopy(rpr));
      } catch (Exception e) {
        r.setRPr(rpr);
      }
    }
    Text t = new Text();
    t.setValue(texto);
    t.setSpace("preserve");
    r.getContent().add(t);
    return r;
  }

  private void vaciarParrafo(P parrafo) {
    parrafo.getContent().removeIf(c -> XmlUtils.unwrap(c) instanceof R);
  }

  // ── Segmentos para resolver placeholders partidos ─────────────────────────

  private record RunSegmento(int inicio, int fin, R run, String texto, RPr rpr) {
  }

  private List<RunSegmento> construirSegmentos(P parrafo) {
    List<RunSegmento> lista = new ArrayList<>();
    int pos = 0;
    for (Object elem : parrafo.getContent()) {
      Object obj = XmlUtils.unwrap(elem);
      if (obj instanceof R run) {
        String texto = extraerTextoDeRun(run);
        lista.add(new RunSegmento(pos, pos + texto.length(), run, texto, run.getRPr()));
        pos += texto.length();
      }
    }
    return lista;
  }

  private int runEnPosicion(List<RunSegmento> segmentos, int posicion) {
    for (int i = 0; i < segmentos.size(); i++) {
      RunSegmento s = segmentos.get(i);
      if (posicion >= s.inicio() && posicion < s.fin())
        return i;
    }
    return segmentos.size() - 1;
  }

  private RPr rprEnPosicion(List<RunSegmento> segmentos, int posicion) {
    int idx = runEnPosicion(segmentos, posicion);
    return segmentos.get(idx).rpr();
  }

  // ── Encabezados y pies de página ─────────────────────────────────────────

  private void procesarHeadersFooters(
      WordprocessingMLPackage pkg,
      Map<String, String> variables,
      Map<String, byte[]> imagenes) throws Exception {

    List<SectionWrapper> sections = pkg.getDocumentModel().getSections();
    for (SectionWrapper section : sections) {
      HeaderFooterPolicy hfp = section.getHeaderFooterPolicy();
      if (hfp == null)
        continue;

      if (hfp.getDefaultHeader() != null)
        procesarContenido(pkg, hfp.getDefaultHeader().getContent(), variables, imagenes);
      if (hfp.getDefaultFooter() != null)
        procesarContenido(pkg, hfp.getDefaultFooter().getContent(), variables, imagenes);
      if (hfp.getFirstHeader() != null)
        procesarContenido(pkg, hfp.getFirstHeader().getContent(), variables, imagenes);
      if (hfp.getFirstFooter() != null)
        procesarContenido(pkg, hfp.getFirstFooter().getContent(), variables, imagenes);
    }
  }

  // ── Imágenes ──────────────────────────────────────────────────────────────

  private P crearParrafoConImagen(
      WordprocessingMLPackage pkg,
      byte[] imgBytes,
      String nombre,
      P parrafoOriginal) throws Exception {
    // ── Preprocesar la imagen antes de insertar ──────────────────
    // 1. Limpiar el falso fondo de cuadros de la imagen en memoria
    byte[] imagenLimpiaBytes = removerFalsoFondoPng(imgBytes);
    BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(pkg, imagenLimpiaBytes);

    // BinaryPartAbstractImage.createImagePart(pkg, imgBytes);
    // Obtener dimensiones según el tipo de imagen
    long[] dims = imagenConfig.dimensiones(nombre);

    // ID único por imagen — usar hashCode de la clave para evitar colisiones
    int idImagen = Math.abs(nombre.hashCode()) % 10000 + 1;
    Inline inline = imagePart.createImageInline(
        nombre,
        nombre,
        idImagen,
        idImagen + 1,
        dims[0], // ancho
        dims[1], // alto
        false);

    // Preservar el pPr (alineación, etc.) del párrafo original
    P p = new P();
    if (parrafoOriginal.getPPr() != null) {
      p.setPPr(XmlUtils.deepCopy(parrafoOriginal.getPPr()));
    }
    R r = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(inline);
    r.getContent().add(drawing);
    p.getContent().add(r);
    return p;
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

  /**
   * Método interno para evaluar píxel por píxel y volver transparentes los
   * cuadros blancos/grises
   */

  private byte[] removerFalsoFondoPng(byte[] archivoBytes) throws IOException {
    BufferedImage imagenOriginal = ImageIO.read(new ByteArrayInputStream(archivoBytes));
    if (imagenOriginal == null) {
      throw new IOException("No se pudo decodificar el arreglo de bytes como una imagen válida.");
    }

    BufferedImage imagenLimpia = new BufferedImage(
        imagenOriginal.getWidth(), imagenOriginal.getHeight(), BufferedImage.TYPE_INT_ARGB);

    for (int x = 0; x < imagenOriginal.getWidth(); x++) {
      for (int y = 0; y < imagenOriginal.getHeight(); y++) {
        int rgb = imagenOriginal.getRGB(x, y);

        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        // 1. Calcular la diferencia máxima entre los canales (Saturación de color)
        // Los cuadros grises y blancos tienen componentes R, G y B casi idénticos.
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        int diferenciaColor = max - min;

        // 2. Evaluamos si el píxel es parte del fondo "sin color"
        // Si la diferencia es menor a 25, significa que es un tono gris, blanco o negro
        // del fondo.
        // Excluimos tonos muy oscuros (menores a 50) para evitar borrar trazos negros
        // si los hubiera.
        boolean esFondoCuadros = (diferenciaColor < 25) && (max > 50);

        if (esFondoCuadros) {
          // Forzar transparencia absoluta en la cuadrícula
          int rgbTransparente = (0x00 << 24) | (0xFF << 16) | (0xFF << 8) | 0xFF;
          imagenLimpia.setRGB(x, y, rgbTransparente);
        } else {
          // Preservar los píxeles que sí tienen color (como el azul del sello y la firma)
          imagenLimpia.setRGB(x, y, rgb);
        }
      }
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(imagenLimpia, "png", baos);
    return baos.toByteArray();
  }
}
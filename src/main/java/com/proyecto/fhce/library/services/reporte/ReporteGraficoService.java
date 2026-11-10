package com.proyecto.fhce.library.services.reporte;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.grafico.DashboardGraficosDTO;
import com.proyecto.fhce.library.dto.reportes.grafico.GraficoDTO;
import com.proyecto.fhce.library.dto.reportes.grafico.SerieGraficoDTO;
import com.proyecto.fhce.library.dto.reportes.grafico.TipoGrafico;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.repositories.ReporteGraficoRepository;

/**
 * Arma los gráficos del módulo de reportes. La base de datos agrega; este
 * service solo completa huecos (meses sin préstamos, estados en cero), da
 * etiquetas legibles y elige el tipo de gráfico.
 */
@Service
@Transactional(readOnly = true)
public class ReporteGraficoService {

  private static final int MAX_MESES = 120;
  private static final int MESES_DASHBOARD = 6;
  private static final int TOP_DASHBOARD = 5;

  private static final String SIN_CLASIFICAR = "Sin clasificar";
  private static final String[] DEWEY = {
      "Generalidades", "Filosofía y psicología", "Religión", "Ciencias sociales",
      "Lenguas", "Ciencias puras", "Tecnología", "Artes y recreación",
      "Literatura", "Historia y geografía" };
  private static final String[] TRAMOS = {
      "1–3 días", "4–7 días", "8–15 días", "16–30 días", "Más de 30 días" };

  private final ReporteGraficoRepository repo;
  private final Clock clock;

  public ReporteGraficoService(ReporteGraficoRepository repo, Clock clock) {
    this.repo = repo;
    this.clock = clock;
  }

  // ======================= PRÉSTAMOS =======================

  /**
   * Línea: préstamos por mes, con meses en cero para que la línea sea continua.
   */
  public GraficoDTO prestamosPorMes(
      Long bibliotecaId, EstadoPrestamo estado, TipoPrestamo tipo,
      LocalDate fechaInicio, LocalDate fechaFin) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    List<Object[]> rows = repo.prestamosPorMes(
        bibliotecaId, estado, tipo,
        ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin));

    String titulo = "Préstamos por mes";
    if (rows.isEmpty()) {
      return GraficoDTO.of("prestamos-por-mes", titulo, TipoGrafico.LINEA,
          "préstamos", List.of(), List.of());
    }

    Map<YearMonth, Long> porMes = new LinkedHashMap<>();
    for (Object[] r : rows) {
      porMes.put(YearMonth.of(((Number) r[0]).intValue(), ((Number) r[1]).intValue()), n(r[2]));
    }
    YearMonth primero = porMes.keySet().iterator().next();
    YearMonth ultimo = primero;
    for (YearMonth ym : porMes.keySet()) {
      ultimo = ym;
    }

    // Si el usuario eligió fechas se muestra TODO su rango (con ceros);
    // si el rango es absurdamente largo se acota a donde hay datos.
    YearMonth inicio = fechaInicio != null ? YearMonth.from(fechaInicio) : primero;
    YearMonth fin = fechaFin != null ? YearMonth.from(fechaFin) : ultimo;
    if (ChronoUnit.MONTHS.between(inicio, fin) > MAX_MESES) {
      inicio = primero;
      fin = ultimo;
    }

    List<String> etiquetas = new ArrayList<>();
    List<Number> valores = new ArrayList<>();
    for (YearMonth ym = inicio; !ym.isAfter(fin); ym = ym.plusMonths(1)) {
      etiquetas.add(ym.toString()); // "2026-03"
      valores.add(porMes.getOrDefault(ym, 0L));
    }
    return GraficoDTO.of("prestamos-por-mes", titulo, TipoGrafico.LINEA, "préstamos",
        etiquetas, List.of(new SerieGraficoDTO("Préstamos", valores)));
  }

  /** Dona: préstamos por estado (ACTIVO, VENCIDO, DEVUELTO, ...). */
  public GraficoDTO prestamosPorEstado(
      Long bibliotecaId, TipoPrestamo tipo, LocalDate fechaInicio, LocalDate fechaFin) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    List<Object[]> rows = repo.prestamosPorEstado(
        bibliotecaId, tipo,
        ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin));
    return distribucion("prestamos-por-estado", "Préstamos por estado",
        TipoGrafico.DONA, "préstamos", EstadoPrestamo.values(), rows);
  }

  /** Dona: préstamos en sala vs a domicilio. */
  public GraficoDTO prestamosPorTipo(
      Long bibliotecaId, EstadoPrestamo estado, LocalDate fechaInicio, LocalDate fechaFin) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    List<Object[]> rows = repo.prestamosPorTipo(
        bibliotecaId, estado,
        ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin));
    return distribucion("prestamos-por-tipo", "Préstamos por tipo",
        TipoGrafico.DONA, "préstamos", TipoPrestamo.values(), rows);
  }

  /** Barras horizontales: top N libros más prestados. */
  public GraficoDTO librosMasPrestados(
      Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin, int limite) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    List<Object[]> rows = repo.librosMasPrestados(
        bibliotecaId, ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin),
        PageRequest.of(0, limite));
    return ranking("libros-mas-prestados", "Libros más prestados (top " + limite + ")", rows);
  }

  /**
   * Barras horizontales: top N libros menos prestados (con al menos 1 préstamo).
   */
  public GraficoDTO librosMenosPrestados(
      Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin, int limite) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    List<Object[]> rows = repo.librosMenosPrestados(
        bibliotecaId, ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin),
        PageRequest.of(0, limite));
    return ranking("libros-menos-prestados", "Libros menos prestados (top " + limite + ")", rows);
  }

  /**
   * Barras horizontales: préstamos por área temática (clase Dewey, primer
   * dígito).
   */
  public GraficoDTO prestamosPorArea(
      Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    List<Object[]> rows = repo.prestamosPorClasificacion(
        bibliotecaId, ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin));

    Map<String, Long> porArea = new HashMap<>();
    for (Object[] r : rows) {
      String codigo = r[0] != null ? r[0].toString().trim() : "";
      String etiqueta = SIN_CLASIFICAR;
      if (!codigo.isEmpty() && Character.isDigit(codigo.charAt(0))) {
        int clase = Character.digit(codigo.charAt(0), 10);
        etiqueta = clase + "00 · " + DEWEY[clase];
      }
      porArea.merge(etiqueta, n(r[1]), Long::sum);
    }

    List<Map.Entry<String, Long>> ordenado = new ArrayList<>(porArea.entrySet());
    ordenado.sort((a, b) -> {
      int cmp = Long.compare(b.getValue(), a.getValue());
      return cmp != 0 ? cmp : a.getKey().compareTo(b.getKey());
    });

    List<String> etiquetas = new ArrayList<>();
    List<Number> valores = new ArrayList<>();
    for (Map.Entry<String, Long> e : ordenado) {
      etiquetas.add(e.getKey());
      valores.add(e.getValue());
    }
    return GraficoDTO.of("prestamos-por-area", "Préstamos por área temática",
        TipoGrafico.BARRAS_HORIZONTALES, "préstamos",
        etiquetas, List.of(new SerieGraficoDTO("Préstamos", valores)));
  }

  /**
   * Barras: préstamos VENCIDOS hoy, repartidos por días de retraso.
   * Es una foto de hoy; no recibe fechas.
   */
  public GraficoDTO vencidosPorTramo(Long bibliotecaId) {
    LocalDate hoy = LocalDate.now(clock);
    long[] conteo = new long[TRAMOS.length];

    for (Object[] r : repo.vencidosPorFechaEstimada(bibliotecaId)) {
      LocalDate estimada = (LocalDate) r[0];
      // Un VENCIDO con fecha de hoy (el CRON lo marcó antes) cuenta como 1 día.
      long dias = Math.max(1, ChronoUnit.DAYS.between(estimada, hoy));
      int tramo = dias <= 3 ? 0 : dias <= 7 ? 1 : dias <= 15 ? 2 : dias <= 30 ? 3 : 4;
      conteo[tramo] += n(r[1]);
    }

    List<Number> valores = new ArrayList<>();
    for (long c : conteo) {
      valores.add(c);
    }
    return GraficoDTO.of("vencidos-por-tramo", "Devoluciones vencidas por días de retraso",
        TipoGrafico.BARRAS, "préstamos",
        List.of(TRAMOS), List.of(new SerieGraficoDTO("Vencidos", valores)));
  }

  // ======================= INVENTARIO =======================

  /**
   * Varias bibliotecas → barras apiladas (una barra por biblioteca, un color por
   * estado). Una sola biblioteca → dona por estado (una barra apilada sola no
   * comunica nada).
   */
  public GraficoDTO estadoEjemplares(Long bibliotecaId) {
    Map<String, Map<EstadoEjemplar, Long>> porBiblioteca = new LinkedHashMap<>();
    for (Object[] r : repo.ejemplaresPorBibliotecaYEstado(bibliotecaId)) {
      if (r[1] == null) {
        continue;
      }
      porBiblioteca
          .computeIfAbsent((String) r[0], k -> new EnumMap<>(EstadoEjemplar.class))
          .merge((EstadoEjemplar) r[1], n(r[2]), Long::sum);
    }

    String titulo = "Estado de ejemplares";
    if (porBiblioteca.isEmpty()) {
      return GraficoDTO.of("estado-ejemplares", titulo, TipoGrafico.BARRAS_APILADAS,
          "ejemplares", List.of(), List.of());
    }

    if (porBiblioteca.size() == 1) {
      Map<EstadoEjemplar, Long> estados = porBiblioteca.values().iterator().next();
      List<String> etiquetas = new ArrayList<>();
      List<Number> valores = new ArrayList<>();
      for (EstadoEjemplar est : EstadoEjemplar.values()) {
        long cantidad = estados.getOrDefault(est, 0L);
        if (cantidad > 0) {
          etiquetas.add(est.name());
          valores.add(cantidad);
        }
      }
      return GraficoDTO.of("estado-ejemplares", titulo, TipoGrafico.DONA, "ejemplares",
          etiquetas, List.of(new SerieGraficoDTO("Ejemplares", valores)));
    }

    List<String> bibliotecas = new ArrayList<>(porBiblioteca.keySet());
    List<SerieGraficoDTO> series = new ArrayList<>();
    for (EstadoEjemplar est : EstadoEjemplar.values()) {
      List<Number> valores = new ArrayList<>();
      long suma = 0;
      for (String b : bibliotecas) {
        long cantidad = porBiblioteca.get(b).getOrDefault(est, 0L);
        valores.add(cantidad);
        suma += cantidad;
      }
      if (suma > 0) { // no ensuciar la leyenda con estados que nadie tiene
        series.add(new SerieGraficoDTO(est.name(), valores));
      }
    }
    return GraficoDTO.of("estado-ejemplares", titulo, TipoGrafico.BARRAS_APILADAS,
        "ejemplares", bibliotecas, series);
  }

  // ======================= SANCIONES =======================

  /**
   * Barras: monto de las sanciones por estado (ACTIVA = pendiente, PAGADA =
   * cobrado).
   */
  public GraficoDTO sancionesMontos(
      Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    Map<EstadoSancion, Double> monto = new EnumMap<>(EstadoSancion.class);
    for (Object[] r : repo.sancionesPorEstado(
        bibliotecaId, ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin))) {
      BigDecimal suma = r[2] instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) r[2]).doubleValue());
      monto.merge((EstadoSancion) r[0], suma.doubleValue(), Double::sum);
    }

    List<String> etiquetas = new ArrayList<>();
    List<Number> valores = new ArrayList<>();
    for (EstadoSancion est : EstadoSancion.values()) {
      etiquetas.add(est.name());
      valores.add(monto.getOrDefault(est, 0.0));
    }
    return GraficoDTO.of("sanciones-montos", "Monto de sanciones por estado",
        TipoGrafico.BARRAS, "monto", etiquetas, List.of(new SerieGraficoDTO("Monto", valores)));
  }

  /** Dona: sanciones por tipo (multa, suspensión, ambas). */
  public GraficoDTO sancionesPorTipo(
      Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin) {

    ReporteUtil.validarRango(fechaInicio, fechaFin);
    List<Object[]> rows = repo.sancionesPorTipo(
        bibliotecaId, ReporteUtil.desde(fechaInicio), ReporteUtil.hastaExclusivo(fechaFin));
    return distribucion("sanciones-por-tipo", "Sanciones por tipo",
        TipoGrafico.DONA, "sanciones", TipoSancion.values(), rows);
  }

  // ======================= DASHBOARD =======================

  /** Lo más importante para el bibliotecario, en una sola petición. */
  public DashboardGraficosDTO dashboard(Long bibliotecaId) {
    LocalDate hoy = LocalDate.now(clock);
    LocalDate desde = hoy.minusMonths(MESES_DASHBOARD - 1L).withDayOfMonth(1);

    return new DashboardGraficosDTO(
        prestamosPorMes(bibliotecaId, null, null, desde, hoy),
        librosMasPrestados(bibliotecaId, desde, hoy, TOP_DASHBOARD),
        prestamosPorEstado(bibliotecaId, null, desde, hoy),
        vencidosPorTramo(bibliotecaId),
        estadoEjemplares(bibliotecaId));
  }

  // ======================= helpers =======================

  private static long n(Object o) {
    return ((Number) o).longValue();
  }

  /**
   * Todas las constantes del enum aparecen (en cero si no hay datos): orden y
   * colores estables.
   */
  private static <E extends Enum<E>> GraficoDTO distribucion(
      String id, String titulo, TipoGrafico tipo, String unidad, E[] valores, List<Object[]> rows) {

    Map<Object, Long> conteo = new HashMap<>();
    for (Object[] r : rows) {
      if (r[0] != null) {
        conteo.merge(r[0], n(r[1]), Long::sum);
      }
    }
    List<String> etiquetas = new ArrayList<>();
    List<Number> serie = new ArrayList<>();
    for (E v : valores) {
      etiquetas.add(v.name());
      serie.add(conteo.getOrDefault(v, 0L));
    }
    String nombreSerie = unidad.substring(0, 1).toUpperCase(Locale.ROOT) + unidad.substring(1);
    return GraficoDTO.of(id, titulo, tipo, unidad, etiquetas,
        List.of(new SerieGraficoDTO(nombreSerie, serie)));
  }

  private static GraficoDTO ranking(String id, String titulo, List<Object[]> rows) {
    List<String> etiquetas = new ArrayList<>();
    List<Number> valores = new ArrayList<>();
    for (Object[] r : rows) {
      etiquetas.add((String) r[0]);
      valores.add(n(r[1]));
    }
    return GraficoDTO.of(id, titulo, TipoGrafico.BARRAS_HORIZONTALES, "préstamos",
        etiquetas, List.of(new SerieGraficoDTO("Préstamos", valores)));
  }
}
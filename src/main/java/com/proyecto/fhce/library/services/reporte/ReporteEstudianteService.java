package com.proyecto.fhce.library.services.reporte;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.estudiante.EstudianteDashboardDTO;
import com.proyecto.fhce.library.dto.reportes.estudiante.EstudianteDashboardDTO.CertificadoEstado;
import com.proyecto.fhce.library.dto.reportes.estudiante.EstudianteDashboardDTO.PrestamoItem;
import com.proyecto.fhce.library.dto.reportes.estudiante.EstudianteDashboardDTO.ReservaItem;
import com.proyecto.fhce.library.dto.reportes.estudiante.EstudianteDashboardDTO.SancionItem;
import com.proyecto.fhce.library.dto.reportes.grafico.GraficoDTO;
import com.proyecto.fhce.library.dto.reportes.grafico.SerieGraficoDTO;
import com.proyecto.fhce.library.dto.reportes.grafico.TipoGrafico;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Reserva;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.repositories.ReporteEstudianteRepository;
import com.proyecto.fhce.library.repositories.SancionRepository;

/**
 * Dashboard del estudiante. Recibe el id del usuario ya resuelto desde el JWT:
 * este service nunca lee ids de la petición.
 */
@Service
@Transactional(readOnly = true)
public class ReporteEstudianteService {

  private static final int MESES_GRAFICO = 6;

  private final ReporteEstudianteRepository repo;
  private final SancionRepository sancionRepository;
  private final Clock clock;

  public ReporteEstudianteService(
      ReporteEstudianteRepository repo, SancionRepository sancionRepository, Clock clock) {
    this.repo = repo;
    this.sancionRepository = sancionRepository;
    this.clock = clock;
  }

  public EstudianteDashboardDTO obtenerDashboard(Long usuarioId) {
    LocalDate hoy = LocalDate.now(clock);

    List<Prestamo> vigentes = repo.findPrestamosVigentes(usuarioId);
    List<Reserva> reservas = repo.findReservasVigentes(usuarioId);
    List<Sancion> sanciones = sancionRepository.findActivasByUsuario(usuarioId);

    long activos = vigentes.stream()
        .filter(p -> p.getEstadoPrestamo() == EstadoPrestamo.ACTIVO
            || p.getEstadoPrestamo() == EstadoPrestamo.RENOVADO)
        .count();
    long vencidos = vigentes.stream()
        .filter(p -> p.getEstadoPrestamo() == EstadoPrestamo.VENCIDO)
        .count();

    return new EstudianteDashboardDTO(
        activos,
        vencidos,
        repo.countPrestamosDeUsuario(usuarioId),
        (long) reservas.size(),
        (long) sanciones.size(),
        evaluarCertificado(vigentes, sanciones, hoy),
        vigentes.stream().map(p -> mapPrestamo(p, hoy)).toList(),
        reservas.stream().map(this::mapReserva).toList(),
        sanciones.stream().map(this::mapSancion).toList(),
        prestamosPorMes(usuarioId, hoy));
  }

  // ======================= CERTIFICADO DE NO DEUDA =======================

  /**
   * Misma regla que CertificadoNoDeudaServiceImpl.validarDeudas (préstamos
   * ACTIVO, RENOVADO y VENCIDO bloquean), aplicada a TODAS las bibliotecas
   * porque el dashboard no es de una biblioteca en particular, MÁS las
   * sanciones sin resolver (criterios de SancionService.obtenerEstadoSanciones):
   * multa sin pagar, suspensión vigente o reposición pendiente.
   */
  private CertificadoEstado evaluarCertificado(
      List<Prestamo> vigentes, List<Sancion> sanciones, LocalDate hoy) {

    List<String> motivos = new ArrayList<>();

    if (!vigentes.isEmpty()) {
      long vencidos = vigentes.stream()
          .filter(p -> p.getEstadoPrestamo() == EstadoPrestamo.VENCIDO).count();
      String detalleVencidos = vencidos > 0
          ? " (" + cantidad(vencidos, "vencido", "vencidos") + ")"
          : "";
      String bibliotecas = String.join(", ", vigentes.stream()
          .map(p -> p.getBiblioteca().getNombre())
          .distinct()
          .toList());
      motivos.add("Tienes "
          + cantidad(vigentes.size(), "préstamo pendiente de devolución", "préstamos pendientes de devolución")
          + detalleVencidos + " en: " + bibliotecas + ".");
    }

    long conMulta = sanciones.stream().filter(ReporteEstudianteService::tieneMultaPendiente).count();
    if (conMulta > 0) {
      motivos.add("Tienes "
          + cantidad(conMulta, "sanción con multa pendiente de pago", "sanciones con multa pendientes de pago")
          + ".");
    }

    LocalDate finSuspension = sanciones.stream()
        .filter(s -> suspensionVigente(s, hoy))
        .map(Sancion::getFechaFinSuspension)
        .min(LocalDate::compareTo)
        .orElse(null);
    if (finSuspension != null) {
      motivos.add("Tienes una suspensión vigente hasta el " + finSuspension + ".");
    }

    long reposiciones = sanciones.stream()
        .filter(s -> s.getTipoSancion() == TipoSancion.REPOSICION).count();
    if (reposiciones > 0) {
      motivos.add("Tienes "
          + cantidad(reposiciones, "reposición de ejemplar pendiente", "reposiciones de ejemplar pendientes")
          + ".");
    }

    return motivos.isEmpty()
        ? new CertificadoEstado(true, null)
        : new CertificadoEstado(false, String.join(" ", motivos));
  }

  private static boolean tieneMultaPendiente(Sancion s) {
    return s.getMontoMulta() != null
        && s.getMontoMulta().compareTo(BigDecimal.ZERO) > 0
        && s.getFechaPago() == null;
  }

  private static boolean suspensionVigente(Sancion s, LocalDate hoy) {
    return s.implicaSuspension()
        && s.getFechaFinSuspension() != null
        && !s.getFechaFinSuspension().isBefore(hoy);
  }

  private static String cantidad(long n, String singular, String plural) {
    return n + " " + (n == 1 ? singular : plural);
  }

  // ======================= PRÉSTAMOS POR MES =======================

  /** Últimos 6 meses (el actual incluido), con los meses sin actividad en 0. */
  private GraficoDTO prestamosPorMes(Long usuarioId, LocalDate hoy) {
    YearMonth actual = YearMonth.from(hoy);
    YearMonth inicio = actual.minusMonths(MESES_GRAFICO - 1L);
    LocalDateTime desde = inicio.atDay(1).atStartOfDay();
    LocalDateTime hasta = actual.plusMonths(1).atDay(1).atStartOfDay();

    Map<YearMonth, Long> porMes = new HashMap<>();
    for (Object[] r : repo.prestamosPorMes(usuarioId, desde, hasta)) {
      porMes.put(
          YearMonth.of(((Number) r[0]).intValue(), ((Number) r[1]).intValue()),
          ((Number) r[2]).longValue());
    }

    List<String> etiquetas = new ArrayList<>();
    List<Number> valores = new ArrayList<>();
    for (YearMonth ym = inicio; !ym.isAfter(actual); ym = ym.plusMonths(1)) {
      etiquetas.add(ym.toString()); // "2026-11"
      valores.add(porMes.getOrDefault(ym, 0L));
    }
    return GraficoDTO.of("prestamos-por-mes", "Mis préstamos por mes", TipoGrafico.LINEA,
        "préstamos", etiquetas, List.of(new SerieGraficoDTO("Préstamos", valores)));
  }

  // ======================= MAPPERS =======================

  private PrestamoItem mapPrestamo(Prestamo p, LocalDate hoy) {
    var libro = p.getEjemplar().getEdicion().getLibro();
    return new PrestamoItem(
        p.getIdPrestamo(),
        libro.getIdLibro(),
        libro.getTitulo(),
        p.getTipoPrestamo().name(),
        p.getEstadoPrestamo().name(),
        p.getFechaPrestamo().toLocalDate(),
        p.getFechaDevolucionEstimada(),
        (int) ChronoUnit.DAYS.between(hoy, p.getFechaDevolucionEstimada()),
        p.getBiblioteca().getIdBiblioteca(),
        p.getBiblioteca().getNombre()); // negativo = retraso
  }

  private ReservaItem mapReserva(Reserva r) {
    return new ReservaItem(
        r.getIdReserva(),
        r.getLibro().getIdLibro(),
        r.getLibro().getTitulo(),
        r.getEstadoReserva().name(),
        r.getFechaReserva().toLocalDate(),
        r.getBiblioteca().getIdBiblioteca(),
        r.getBiblioteca().getNombre());
  }

  private SancionItem mapSancion(Sancion s) {
    LocalDate inicio = s.getFechaInicioSuspension() != null
        ? s.getFechaInicioSuspension()
        : s.getFechaGeneracion().toLocalDate();
    Prestamo p = s.getPrestamo();

    return new SancionItem(
        s.getIdSancion(),
        s.getMotivo() != null ? s.getMotivo().name() : null,
        inicio,
        s.getFechaFinSuspension(),
        p != null ? p.getIdPrestamo() : null,
        s.getBiblioteca().getIdBiblioteca(),
        s.getBiblioteca().getNombre(),
        p != null
            ? p.getEjemplar().getEdicion().getLibro().getTitulo()
            : null);
  }
}
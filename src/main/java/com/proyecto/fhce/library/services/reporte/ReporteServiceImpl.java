package com.proyecto.fhce.library.services.reporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.DashboardDTO;
import com.proyecto.fhce.library.dto.reportes.InventarioBibliotecaDTO;
import com.proyecto.fhce.library.dto.reportes.ReportePrestamoDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.DeterioroItemDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.DeterioroReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.DeterioroResumenDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EjemplarNuncaPrestadoDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EjemplarNuncaPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EstadoEjemplarBibliotecaDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EstadoEjemplarBibliotecaResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioItemDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioResumenDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.ResumenEstadoEjemplarDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

  @Autowired
  private LibroRepository libroRepository;

  @Autowired
  private PrestamoRepository prestamoRepository;

  @Autowired
  private EjemplarRepository ejemplarRepository;

  @Autowired
  private UserRepository usuarioRepository;
  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  @Override
  public DashboardDTO obtenerDashboard(Long bibliotecaId) {

    DashboardDTO dto = new DashboardDTO();
    if (bibliotecaId == null) {

      // ADMIN

      dto.setTotalLibros(
          libroRepository.countTotalLibros());

      dto.setTotalEjemplares(
          ejemplarRepository.countTotalEjemplares());

      dto.setTotalPrestamosActivos(
          prestamoRepository.countPrestamosActivos());

      dto.setTotalPrestamosVencidos(
          prestamoRepository.countPrestamosVencidos());

      dto.setTotalUsuarios(
          usuarioRepository.countTotalUsuarios());

    } else {

      Biblioteca biblioteca = bibliotecaRepository.findById(bibliotecaId)
          .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));
      dto.setNombreBiblioteca(
          biblioteca.getNombre());

      dto.setTotalLibros(
          libroRepository
              .countLibrosByBiblioteca(
                  bibliotecaId));

      dto.setTotalEjemplares(
          ejemplarRepository
              .countByBiblioteca_IdBiblioteca(
                  bibliotecaId));

      dto.setTotalPrestamosActivos(
          prestamoRepository
              .countPrestamosActivosByBiblioteca(
                  bibliotecaId));

      dto.setTotalPrestamosVencidos(
          prestamoRepository
              .countPrestamosVencidosByBiblioteca(
                  bibliotecaId));
    }

    return dto;
  }

  @Override
  public List<ReportePrestamoDTO> reportePrestamos(Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'reportePrestamos'");
  }

  @Override
  public InventarioBibliotecaDTO inventarioBiblioteca(Long bibliotecaId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'inventarioBiblioteca'");
  }

  @Override
  public InventarioReporteDTO generarInventario(
      InventarioReporteFiltroDTO filtro) {

    List<Ejemplar> ejemplares = ejemplarRepository.obtenerInventario(
        filtro.getBibliotecaId(),
        filtro.getEstado(),
        filtro.getClasificacionDecimal(),
        filtro.getCategoriaId());

    InventarioReporteDTO reporte = new InventarioReporteDTO();

    InventarioResumenDTO resumen = construirResumen(filtro.getBibliotecaId());

    reporte.setResumen(resumen);

    reporte.setDetalle(
        ejemplares.stream()
            .map(this::mapToItemDTO)
            .toList());

    return reporte;
  }

  private InventarioResumenDTO construirResumen(
      Long bibliotecaId) {

    InventarioResumenDTO resumen = new InventarioResumenDTO();

    if (bibliotecaId == null) {

      resumen.setTotalEjemplares(
          (long) ejemplarRepository.findAll().size());

    } else {

      resumen.setTotalEjemplares(
          ejemplarRepository
              .countByBiblioteca_IdBiblioteca(
                  bibliotecaId));
    }

    resumen.setDisponibles(
        ejemplarRepository.countByBibliotecaAndEstado(
            bibliotecaId,
            EstadoEjemplar.DISPONIBLE));

    resumen.setPrestados(
        ejemplarRepository.countByBibliotecaAndEstado(
            bibliotecaId,
            EstadoEjemplar.PRESTADO));

    resumen.setReservados(
        ejemplarRepository.countByBibliotecaAndEstado(
            bibliotecaId,
            EstadoEjemplar.RESERVADO));

    resumen.setDeteriorados(
        ejemplarRepository.countByBibliotecaAndEstado(
            bibliotecaId,
            EstadoEjemplar.DETERIORADO));

    resumen.setPerdidos(
        ejemplarRepository.countByBibliotecaAndEstado(
            bibliotecaId,
            EstadoEjemplar.PERDIDO));

    return resumen;
  }

  @Override
  public EstadoEjemplarBibliotecaResponseDTO obtenerEstadoEjemplaresPorBiblioteca(
      Long bibliotecaId) {
    List<EstadoEjemplarBibliotecaDTO> detalle = ejemplarRepository
        .obtenerEstadoEjemplaresPorBiblioteca(
            bibliotecaId);

    Long totalEjemplares = detalle.stream()
        .mapToLong(d -> d.getDisponibles()
            + d.getPrestados()
            + d.getReservados()
            + d.getReparacion()
            + d.getPerdidos()
            + d.getDanados()
            + d.getDeteriorados()
            + d.getBajas())
        .sum();

    ResumenEstadoEjemplarDTO resumen = new ResumenEstadoEjemplarDTO();

    resumen.setTotalBibliotecas(
        (long) detalle.size());

    resumen.setTotalEjemplares(
        totalEjemplares);

    EstadoEjemplarBibliotecaResponseDTO response = new EstadoEjemplarBibliotecaResponseDTO();

    response.setResumen(resumen);
    response.setBibliotecas(detalle);

    return response;
  }

  private InventarioItemDTO mapToItemDTO(Ejemplar ejemplar) {

    InventarioItemDTO dto = new InventarioItemDTO();

    dto.setIdEjemplar(
        ejemplar.getIdEjemplar());

    dto.setCodigoEjemplar(
        ejemplar.getCodigoEjemplar());

    dto.setTituloLibro(
        ejemplar.getEdicion()
            .getLibro()
            .getTitulo());

    dto.setBiblioteca(
        ejemplar.getBiblioteca()
            .getNombre());

    dto.setEstado(
        ejemplar.getEstadoEjemplar());

    dto.setClasificacionDecimal(
        ejemplar.getClasificacionDecimal());

    dto.setUbicacionFisica(
        ejemplar.getUbicacionFisica());

    return dto;
  }

  public DeterioroReporteResponseDTO reporteDeterioro(
      Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin) {

    LocalDateTime inicio = fechaInicio != null
        ? fechaInicio.atStartOfDay()
        : null;
    LocalDateTime fin = fechaFin != null
        ? fechaFin.atTime(23, 59, 59)
        : null;

    List<Prestamo> devoluciones = prestamoRepository
        .obtenerDevolucionesConCondicion(bibliotecaId, inicio, fin);

    List<DeterioroItemDTO> detalle = devoluciones.stream()
        .map(this::mapDeterioro)
        .toList();

    DeterioroResumenDTO resumen = new DeterioroResumenDTO();
    resumen.setTotalDevoluciones((long) detalle.size());
    resumen.setTotalConDeterioro(detalle.stream()
        .filter(d -> d.getDiferenciaNivel() > 0).count());
    resumen.setSinCambio(detalle.stream()
        .filter(d -> d.getDiferenciaNivel() == 0).count());
    resumen.setMejorados(detalle.stream()
        .filter(d -> d.getDiferenciaNivel() < 0).count());

    if (resumen.getTotalDevoluciones() > 0) {
      resumen.setPorcentajeDeterioro(
          (double) resumen.getTotalConDeterioro()
              / resumen.getTotalDevoluciones() * 100);
    }

    DeterioroReporteResponseDTO response = new DeterioroReporteResponseDTO();
    response.setResumen(resumen);
    response.setDetalle(detalle);
    return response;
  }

  public EjemplarNuncaPrestadoResponseDTO reporteEjemplaresNuncaPrestados(
      Long bibliotecaId) {

    List<Ejemplar> ejemplares = ejemplarRepository
        .findEjemplaresNuncaPrestados(bibliotecaId);

    Long totalGeneral = bibliotecaId != null
        ? ejemplarRepository.countByBiblioteca_IdBiblioteca(bibliotecaId)
        : ejemplarRepository.countTotalEjemplares();

    List<EjemplarNuncaPrestadoDTO> detalle = ejemplares.stream()
        .map(this::mapEjemplarNuncaPrestado)
        .toList();

    double porcentaje = totalGeneral > 0
        ? (double) detalle.size() / totalGeneral * 100
        : 0;

    EjemplarNuncaPrestadoResponseDTO response = new EjemplarNuncaPrestadoResponseDTO();
    response.setTotalEjemplaresNuncaPrestados((long) detalle.size());
    response.setTotalEjemplares(totalGeneral);
    response.setPorcentajeSinCirculacion(porcentaje);
    response.setEjemplares(detalle);
    return response;
  }

  // ── mappers privados ──────────────────────────────────────────────────────

  private DeterioroItemDTO mapDeterioro(Prestamo p) {
    DeterioroItemDTO dto = new DeterioroItemDTO();
    dto.setIdPrestamo(p.getIdPrestamo());
    dto.setIdEjemplar(p.getEjemplar().getIdEjemplar());
    dto.setCodigoEjemplar(p.getEjemplar().getCodigoEjemplar());
    dto.setLibro(p.getEjemplar().getEdicion().getLibro().getTitulo());
    dto.setBiblioteca(p.getBiblioteca().getNombre());
    dto.setUsuario(p.getUsuario().getPersona().getNombre() + " "
        + p.getUsuario().getPersona().getApellido_pat());
    dto.setCi(p.getUsuario().getPersona().getCi());
    if (p.getCondicionEntrega() != null) {
      dto.setCondicionEntrega(p.getCondicionEntrega().name());
    }
    if (p.getCondicionDevolucion() != null) {
      dto.setCondicionDevolucion(p.getCondicionDevolucion().name());
      int diferencia = p.getCondicionDevolucion().ordinal()
          - (p.getCondicionEntrega() != null
              ? p.getCondicionEntrega().ordinal()
              : 0);
      dto.setDiferenciaNivel(diferencia); // positivo = deterioro
    }
    dto.setFechaDevolucion(p.getFechaDevolucionReal());
    return dto;
  }

  private EjemplarNuncaPrestadoDTO mapEjemplarNuncaPrestado(Ejemplar e) {
    EjemplarNuncaPrestadoDTO dto = new EjemplarNuncaPrestadoDTO();
    dto.setIdEjemplar(e.getIdEjemplar());
    dto.setCodigoEjemplar(e.getCodigoEjemplar());
    dto.setLibro(e.getEdicion().getLibro().getTitulo());
    dto.setEditorial(e.getEdicion().getEditorial());
    dto.setBiblioteca(e.getBiblioteca().getNombre());
    dto.setClasificacionDecimal(e.getClasificacionDecimal());
    dto.setUbicacionFisica(e.getUbicacionFisica());
    dto.setEstadoEjemplar(e.getEstadoEjemplar().name());
    return dto;
  }

}

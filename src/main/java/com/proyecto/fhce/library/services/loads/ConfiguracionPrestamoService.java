package com.proyecto.fhce.library.services.loads;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.ReglasPrestamoDTO;
import com.proyecto.fhce.library.dto.request.loads.ConfiguracionPrestamoRequestDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionResueltaDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.mapper.ConfiguracionPrestamoMapper;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.ConfiguracionPrestamoRepository;

@Service
@Transactional(readOnly = true)
public class ConfiguracionPrestamoService {

  private final ConfiguracionPrestamoRepository configuracionRepository;
  private final BibliotecaRepository bibliotecaRepository;
  private final ConfiguracionPrestamoMapper mapper;

  public ConfiguracionPrestamoService(
      ConfiguracionPrestamoRepository configuracionRepository,
      BibliotecaRepository bibliotecaRepository,
      ConfiguracionPrestamoMapper mapper) {
    this.configuracionRepository = configuracionRepository;
    this.bibliotecaRepository = bibliotecaRepository;
    this.mapper = mapper;
  }

  // ─────────────────────────────────────────────
  // CRUD
  // ─────────────────────────────────────────────

  @Transactional
  public ConfiguracionPrestamoResponseDTO crear(ConfiguracionPrestamoRequestDTO request) {
    validarNoExisteDuplicado(request.getBibliotecaId(), null);

    Biblioteca biblioteca = buscarBibliotecaOLanzarError(request.getBibliotecaId());
    ConfiguracionPrestamo nueva = mapper.toEntity(request, biblioteca);

    return mapper.toResponseDTO(configuracionRepository.save(nueva));
  }

  @Transactional
  public ConfiguracionPrestamoResponseDTO actualizar(Long id,
      ConfiguracionPrestamoRequestDTO request) {
    ConfiguracionPrestamo existente = buscarConfiguracionOLanzarError(id);
    validarNoExisteDuplicado(request.getBibliotecaId(), id);

    Biblioteca biblioteca = buscarBibliotecaOLanzarError(request.getBibliotecaId());
    mapper.actualizarEntidad(existente, request, biblioteca);

    return mapper.toResponseDTO(existente);
  }

  @Transactional
  public void eliminar(Long id) {
    configuracionRepository.delete(buscarConfiguracionOLanzarError(id));
  }

  public ConfiguracionPrestamoResponseDTO buscarPorId(Long id) {
    return mapper.toResponseDTO(buscarConfiguracionOLanzarError(id));
  }

  public ConfiguracionPrestamoResponseDTO buscarPorBiblioteca(Long bibliotecaId) {
    ConfiguracionPrestamo config = configuracionRepository
        .findByBibliotecaId(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe configuración para la biblioteca con id: " + bibliotecaId));
    return mapper.toResponseDTO(config);
  }

  // ─────────────────────────────────────────────
  // LÓGICA DE NEGOCIO — usada por otros módulos
  // ─────────────────────────────────────────────

  /**
   * Devuelve las reglas aplicables según el tipo de préstamo.
   * Este es el método central que consumen Préstamos, Reservas y Sanciones.
   */
  public ReglasPrestamoDTO obtenerReglas(Long bibliotecaId, TipoPrestamo tipo) {
    ConfiguracionPrestamo config = configuracionRepository
        .findByBibliotecaId(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe configuración para la biblioteca con id: " + bibliotecaId));

    return tipo == TipoPrestamo.DOMICILIO
        ? mapper.toReglasDTO(config, config.getEjemplaresMaxDomicilio())
        : mapper.toReglasDTO(config, config.getEjemplaresMaxSala());
  }

  /**
   * Calcula la multa acumulada respetando el tope configurado.
   * Retorna 0 si la configuración no tiene multa definida.
   */
  public BigDecimal calcularMulta(Long bibliotecaId, int diasRetraso) {
    ConfiguracionPrestamo config = configuracionRepository
        .findByBibliotecaId(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe configuración para la biblioteca con id: " + bibliotecaId));

    if (config.getMultaPorDia() == null || config.getMultaMaxDias() == null) {
      return BigDecimal.ZERO;
    }

    int diasEfectivos = Math.min(diasRetraso, config.getMultaMaxDias());
    return config.getMultaPorDia().multiply(BigDecimal.valueOf(diasEfectivos));
  }

  /**
   * Indica si un retraso activa la suspensión del usuario.
   */
  public boolean debeSuspender(Long bibliotecaId, int diasRetraso) {
    ConfiguracionPrestamo config = configuracionRepository
        .findByBibliotecaId(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe configuración para la biblioteca con id: " + bibliotecaId));

    return config.getMultaMaxDias() != null && diasRetraso > config.getMultaMaxDias();
  }

  // ─────────────────────────────────────────────
  // AUXILIARES PRIVADOS
  // ─────────────────────────────────────────────

  private void validarNoExisteDuplicado(Long bibliotecaId, Long excludeId) {
    if (configuracionRepository.existeConfiguracionParaBiblioteca(
        bibliotecaId, excludeId)) {
      throw new BusinessException(
          "Ya existe una configuración para la biblioteca con id: " + bibliotecaId);
    }
  }

  private ConfiguracionPrestamo buscarConfiguracionOLanzarError(Long id) {
    return configuracionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Configuración no encontrada con id: " + id));
  }

  private Biblioteca buscarBibliotecaOLanzarError(Long bibliotecaId) {
    return bibliotecaRepository.findById(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Biblioteca no encontrada con id: " + bibliotecaId));
  }
}

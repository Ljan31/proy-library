package com.proyecto.fhce.library.services.loads;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.proyecto.fhce.library.repositories.RoleRepository;

@Service
@Transactional(readOnly = true)
public class ConfiguracionPrestamoService {

  private final ConfiguracionPrestamoRepository configuracionRepository;
  private final BibliotecaRepository bibliotecaRepository;
  private final RoleRepository roleRepository;
  private final ConfiguracionPrestamoMapper mapper;

  public ConfiguracionPrestamoService(
      ConfiguracionPrestamoRepository configuracionRepository,
      BibliotecaRepository bibliotecaRepository,
      RoleRepository roleRepository,
      ConfiguracionPrestamoMapper mapper) {
    this.configuracionRepository = configuracionRepository;
    this.bibliotecaRepository = bibliotecaRepository;
    this.roleRepository = roleRepository;
    this.mapper = mapper;
  }

  @Transactional
  public ConfiguracionPrestamoResponseDTO crear(ConfiguracionPrestamoRequestDTO request) {
    validarNoExisteDuplicado(request.getBibliotecaId(), request.getRolId(),
        request.getTipoPrestamo(), null);

    Biblioteca biblioteca = resolverBiblioteca(request.getBibliotecaId());
    Role rol = resolverRol(request.getRolId());

    ConfiguracionPrestamo nueva = mapper.toEntity(request, biblioteca, rol);
    ConfiguracionPrestamo guardada = configuracionRepository.save(nueva);

    return mapper.toResponseDTO(guardada);
  }

  @Transactional
  public ConfiguracionPrestamoResponseDTO actualizar(Long id,
      ConfiguracionPrestamoRequestDTO request) {
    ConfiguracionPrestamo existente = buscarPorIdOLanzarError(id);

    validarNoExisteDuplicado(request.getBibliotecaId(), request.getRolId(),
        request.getTipoPrestamo(), id);

    Biblioteca biblioteca = resolverBiblioteca(request.getBibliotecaId());
    Role rol = resolverRol(request.getRolId());

    mapper.actualizarEntidad(existente, request, biblioteca, rol);
    return mapper.toResponseDTO(existente);
  }

  @Transactional
  public void eliminar(Long id) {
    ConfiguracionPrestamo configuracion = buscarPorIdOLanzarError(id);
    configuracionRepository.delete(configuracion);
  }

  public ConfiguracionPrestamoResponseDTO buscarPorId(Long id) {
    return mapper.toResponseDTO(buscarPorIdOLanzarError(id));
  }

  public List<ConfiguracionPrestamoResponseDTO> listarPorBiblioteca(Long bibliotecaId) {
    return configuracionRepository.findAllByBibliotecaId(bibliotecaId)
        .stream()
        .map(mapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  /**
   * Punto central del módulo: resuelve qué configuración aplica
   * para un usuario con un rol dado, en una biblioteca y tipo de préstamo.
   * Prioridad: ROL+BIBLIOTECA > BIBLIOTECA > GLOBAL
   */
  public ConfiguracionResueltaDTO resolverConfiguracionAplicable(
      Long bibliotecaId, Long rolId, TipoPrestamo tipoPrestamo) {

    List<ConfiguracionPrestamo> candidatas = configuracionRepository
        .findConfiguracionesAplicables(bibliotecaId, rolId, tipoPrestamo);

    ConfiguracionPrestamo aplicable = candidatas.stream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe configuración de préstamo para el contexto solicitado"));

    return mapper.toResueltaDTO(aplicable);
  }

  /**
   * Calcula la multa total acumulada por días de retraso,
   * respetando el tope configurado antes de activar suspensión.
   */
  public BigDecimal calcularMulta(Long idConfig, int diasRetraso) {
    ConfiguracionPrestamo config = buscarPorIdOLanzarError(idConfig);

    if (config.getMultaPorDia() == null || config.getMultaMaxDias() == null) {
      return BigDecimal.ZERO;
    }

    int diasEfectivos = Math.min(diasRetraso, config.getMultaMaxDias());
    return config.getMultaPorDia().multiply(BigDecimal.valueOf(diasEfectivos));
  }

  /**
   * Determina si un retraso debe generar suspensión según la política
   * configurada.
   */
  public boolean debeSuspender(Long idConfig, int diasRetraso) {
    ConfiguracionPrestamo config = buscarPorIdOLanzarError(idConfig);
    return config.getMultaMaxDias() != null && diasRetraso > config.getMultaMaxDias();
  }

  // --- Métodos privados auxiliares ---

  private void validarNoExisteDuplicado(Long bibliotecaId, Long rolId,
      TipoPrestamo tipo, Long excludeId) {
    if (configuracionRepository.existeConfiguracionDuplicada(
        bibliotecaId, rolId, tipo, excludeId)) {
      throw new BusinessException(
          "Ya existe una configuración para esta combinación de biblioteca, " +
              "rol y tipo de préstamo");
    }
  }

  private Biblioteca resolverBiblioteca(Long bibliotecaId) {
    if (bibliotecaId == null)
      return null;
    return bibliotecaRepository.findById(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Biblioteca no encontrada con id: " + bibliotecaId));
  }

  private Role resolverRol(Long rolId) {
    if (rolId == null)
      return null;
    return roleRepository.findById(rolId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Rol no encontrado con id: " + rolId));
  }

  private ConfiguracionPrestamo buscarPorIdOLanzarError(Long id) {
    return configuracionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Configuración de préstamo no encontrada con id: " + id));
  }
}

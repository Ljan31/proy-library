package com.proyecto.fhce.library.iteracion3.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.fhce.library.dto.ReglasPrestamoDTO;
import com.proyecto.fhce.library.dto.request.loads.ConfiguracionPrestamoRequestDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.mapper.ConfiguracionPrestamoMapper;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.ConfiguracionPrestamoRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.loads.ConfiguracionPrestamoService;
import com.proyecto.fhce.library.support.TestData;

/** Iteración 3 · Reglas de configuración de préstamos (por biblioteca). */
@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 3 · ConfiguracionPrestamoService (unitaria)")
class ConfiguracionPrestamoServiceTest {

  @Mock
  ConfiguracionPrestamoRepository repo;
  @Mock
  BibliotecaRepository bibliotecaRepository;
  @Mock
  AuditoriaService auditoriaService;

  ConfiguracionPrestamoService service;
  Biblioteca biblioteca;

  @BeforeEach
  void init() {
    // el mapper es lógica pura: se usa el real
    service = new ConfiguracionPrestamoService(repo, bibliotecaRepository,
        new ConfiguracionPrestamoMapper(), auditoriaService);
    biblioteca = TestData.biblioteca(1L, "Central", TipoBiblioteca.FACULTATIVA);
  }

  private ConfiguracionPrestamoRequestDTO request() {
    ConfiguracionPrestamoRequestDTO r = new ConfiguracionPrestamoRequestDTO();
    r.setBibliotecaId(1L);
    r.setDiasPrestamoMax(7);
    r.setRenovacionesMax(2);
    r.setEjemplaresMaxDomicilio(3);
    r.setEjemplaresMaxSala(5);
    r.setMultaPorDia(new BigDecimal("3.00"));
    r.setMultaMaxDias(7);
    r.setDiasSuspension(15);
    r.setDiasReserva(2);
    return r;
  }

  private ConfiguracionPrestamo entidad() {
    ConfiguracionPrestamo c = new ConfiguracionPrestamo();
    c.setIdConfig(10L);
    c.setBiblioteca(biblioteca);
    c.setDiasPrestamoMax(7);
    c.setRenovacionesMax(2);
    c.setEjemplaresMaxDomicilio(3);
    c.setEjemplaresMaxSala(5);
    c.setMultaPorDia(new BigDecimal("3.00"));
    c.setMultaMaxDias(7);
    c.setDiasSuspension(15);
    c.setDiasReserva(2);
    return c;
  }

  @Test
  @DisplayName("IT3-U-CFG-01: crear una configuración válida la guarda y audita")
  void crear_ok() {
    when(repo.existeConfiguracionParaBiblioteca(1L, null)).thenReturn(false);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(repo.save(any(ConfiguracionPrestamo.class))).thenAnswer(i -> {
      ConfiguracionPrestamo c = i.getArgument(0);
      c.setIdConfig(10L);
      return c;
    });

    ConfiguracionPrestamoResponseDTO r = service.crear(request());

    assertThat(r.getIdConfig()).isEqualTo(10L);
    assertThat(r.getDiasPrestamoMax()).isEqualTo(7);
    assertThat(r.getEjemplaresMaxDomicilio()).isEqualTo(3);
    assertThat(r.getNombreBiblioteca()).isEqualTo("Central");
    verify(auditoriaService).registrar(org.mockito.ArgumentMatchers.eq("CREAR"),
        org.mockito.ArgumentMatchers.eq("configuracion_prestamo"),
        org.mockito.ArgumentMatchers.eq(10L), isNull(), any());
  }

  @Test
  @DisplayName("IT3-U-CFG-02: una biblioteca no puede tener dos configuraciones")
  void crear_duplicada() {
    when(repo.existeConfiguracionParaBiblioteca(1L, null)).thenReturn(true);

    assertThatThrownBy(() -> service.crear(request()))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Ya existe una configuración");
    verify(repo, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-CFG-03: crear para una biblioteca inexistente lanza 'no encontrada'")
  void crear_bibliotecaInexistente() {
    when(repo.existeConfiguracionParaBiblioteca(1L, null)).thenReturn(false);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.crear(request()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-CFG-04: actualizar modifica los valores de la configuración existente")
  void actualizar_ok() {
    ConfiguracionPrestamo existente = entidad();
    when(repo.findById(10L)).thenReturn(Optional.of(existente));
    when(repo.existeConfiguracionParaBiblioteca(1L, 10L)).thenReturn(false);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    ConfiguracionPrestamoRequestDTO req = request();
    req.setDiasPrestamoMax(14);
    req.setRenovacionesMax(0);

    ConfiguracionPrestamoResponseDTO r = service.actualizar(10L, req);

    assertThat(r.getDiasPrestamoMax()).isEqualTo(14);
    assertThat(existente.getRenovacionesMax()).isZero();
  }

  @Test
  @DisplayName("IT3-U-CFG-05: actualizar una configuración inexistente lanza 'no encontrada'")
  void actualizar_inexistente() {
    when(repo.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.actualizar(99L, request()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-CFG-06: actualizar hacia una biblioteca que ya tiene configuración es rechazado")
  void actualizar_duplicado() {
    when(repo.findById(10L)).thenReturn(Optional.of(entidad()));
    when(repo.existeConfiguracionParaBiblioteca(1L, 10L)).thenReturn(true);

    assertThatThrownBy(() -> service.actualizar(10L, request()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("IT3-U-CFG-07: eliminar borra la configuración y audita")
  void eliminar_ok() {
    ConfiguracionPrestamo c = entidad();
    when(repo.findById(10L)).thenReturn(Optional.of(c));

    service.eliminar(10L);

    verify(repo).delete(c);
    verify(auditoriaService).registrar(org.mockito.ArgumentMatchers.eq("ELIMINAR"), any(), anyLong(), any(),
        isNull());
  }

  @Test
  @DisplayName("IT3-U-CFG-08: buscar por id inexistente lanza 'no encontrada'")
  void buscarPorId_inexistente() {
    when(repo.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.buscarPorId(5L)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-CFG-09: buscar por biblioteca sin configuración lanza 'no existe configuración'")
  void buscarPorBiblioteca_sinConfig() {
    when(repo.findByBibliotecaId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.buscarPorBiblioteca(1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("No existe configuración");
  }

  @Test
  @DisplayName("IT3-U-CFG-10: las reglas de DOMICILIO usan el máximo de ejemplares a domicilio")
  void reglas_domicilio() {
    when(repo.findByBibliotecaId(1L)).thenReturn(Optional.of(entidad()));

    ReglasPrestamoDTO r = service.obtenerReglas(1L, TipoPrestamo.DOMICILIO);

    assertThat(r.getEjemplaresPermitidos()).isEqualTo(3);
    assertThat(r.getDiasPrestamoMax()).isEqualTo(7);
    assertThat(r.getRenovacionesMax()).isEqualTo(2);
    assertThat(r.getIdConfig()).isEqualTo(10L);
  }

  @Test
  @DisplayName("IT3-U-CFG-11: las reglas de SALA usan el máximo de ejemplares en sala")
  void reglas_sala() {
    when(repo.findByBibliotecaId(1L)).thenReturn(Optional.of(entidad()));

    assertThat(service.obtenerReglas(1L, TipoPrestamo.SALA).getEjemplaresPermitidos()).isEqualTo(5);
  }

  @Test
  @DisplayName("IT3-U-CFG-12: reglas de una biblioteca sin configuración lanza 'no existe configuración'")
  void reglas_sinConfig() {
    when(repo.findByBibliotecaId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obtenerReglas(1L, TipoPrestamo.SALA))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-CFG-13: calcularMulta respeta el tope de días")
  void calcularMulta_conTope() {
    when(repo.findByBibliotecaId(1L)).thenReturn(Optional.of(entidad()));

    assertThat(service.calcularMulta(1L, 3)).isEqualByComparingTo("9");
    assertThat(service.calcularMulta(1L, 30)).isEqualByComparingTo("21");
  }

  @Test
  @DisplayName("IT3-U-CFG-14: calcularMulta devuelve 0 si no hay multa configurada")
  void calcularMulta_sinMulta() {
    ConfiguracionPrestamo c = entidad();
    c.setMultaPorDia(null);
    when(repo.findByBibliotecaId(1L)).thenReturn(Optional.of(c));

    assertThat(service.calcularMulta(1L, 5)).isEqualByComparingTo("0");
  }

  @Test
  @DisplayName("IT3-U-CFG-15: debeSuspender es true solo si el retraso supera multaMaxDias")
  void debeSuspender() {
    when(repo.findByBibliotecaId(1L)).thenReturn(Optional.of(entidad()));

    assertThat(service.debeSuspender(1L, 7)).isFalse();
    assertThat(service.debeSuspender(1L, 8)).isTrue();
  }
}

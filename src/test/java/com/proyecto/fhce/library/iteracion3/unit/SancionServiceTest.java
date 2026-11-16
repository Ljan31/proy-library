package com.proyecto.fhce.library.iteracion3.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.fhce.library.dto.SancionDTO.CondonacionRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.EstadoSancionUsuarioDTO;
import com.proyecto.fhce.library.dto.SancionDTO.PagoMultaRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.SancionManualReposicionRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.SancionManualRequestDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.FormaResolucion;
import com.proyecto.fhce.library.enums.MotivoSancion;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.exception.SancionException.PrestamoNoVencidoException;
import com.proyecto.fhce.library.exception.SancionException.SancionDuplicadaException;
import com.proyecto.fhce.library.exception.SancionException.SancionNoActivaException;
import com.proyecto.fhce.library.exception.SancionException.SancionNotFoundException;
import com.proyecto.fhce.library.mapper.SancionMapper;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.SancionRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.library.EjemplarService;
import com.proyecto.fhce.library.services.loads.ConfiguracionPrestamoService;
import com.proyecto.fhce.library.services.loads.SancionService;
import com.proyecto.fhce.library.services.notificaciones.NotificacionService;
import com.proyecto.fhce.library.support.TestData;

/**
 * Iteración 3 · Sanciones: generación por retraso, manuales, reposición,
 * pago, condonación, estado consolidado y tareas programadas.
 * Config de referencia: Bs 2 por día, tope 7 días, suspensión de 15 días.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 3 · SancionService (unitaria)")
class SancionServiceTest {

  @Mock
  SancionRepository sancionRepository;
  @Mock
  PrestamoRepository prestamoRepository;
  @Mock
  UserRepository usuarioRepository;
  @Mock
  BibliotecaRepository bibliotecaRepository;
  @Mock
  ConfiguracionPrestamoService configuracionService;
  @Mock
  NotificacionService notificacionService;
  @Mock
  SancionMapper mapper;
  @Mock
  AuditoriaService auditoriaService;
  @Mock
  EjemplarService ejemplarService;

  @InjectMocks
  SancionService service;

  Usuario lector;
  Usuario bibliotecario;
  Biblioteca biblioteca;
  Ejemplar ejemplar;
  ConfiguracionPrestamo config;

  @BeforeEach
  void init() {
    lector = TestData.usuario(1L, "lector");
    bibliotecario = TestData.usuario(2L, "biblio");
    biblioteca = TestData.biblioteca(1L, "Central", TipoBiblioteca.FACULTATIVA);
    ejemplar = TestData.ejemplar(5L, TestData.edicion(1L, TestData.libro(1L, "Rayuela")), biblioteca,
        EstadoEjemplar.PRESTADO);
    config = new ConfiguracionPrestamo();
    config.setIdConfig(77L);
    config.setBiblioteca(biblioteca);
    config.setMultaPorDia(new BigDecimal("2.00"));
    config.setMultaMaxDias(7);
    config.setDiasSuspension(15);
    lenient().when(sancionRepository.save(any(Sancion.class))).thenAnswer(i -> i.getArgument(0));
  }

  // ---------------- helpers ----------------
  private Prestamo prestamoConRetraso(int diasRetraso) {
    Prestamo p = new Prestamo();
    p.setIdPrestamo(100L);
    p.setUsuario(lector);
    p.setBiblioteca(biblioteca);
    p.setEjemplar(ejemplar);
    p.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
    p.setFechaPrestamo(LocalDateTime.now().minusDays(diasRetraso + 7L));
    p.setFechaDevolucionEstimada(LocalDate.now().minusDays(diasRetraso));
    p.setIdConfigUsado(77L);
    return p;
  }

  private Sancion sancion(Long id, TipoSancion tipo, EstadoSancion estado, String monto) {
    Sancion s = new Sancion();
    s.setIdSancion(id);
    s.setUsuario(lector);
    s.setBiblioteca(biblioteca);
    s.setTipoSancion(tipo);
    s.setMotivo(MotivoSancion.RETRASO_DEVOLUCION);
    s.setEstado(estado);
    s.setMontoMulta(monto == null ? null : new BigDecimal(monto));
    return s;
  }

  private void prestamoEncontrado(Prestamo p) {
    when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(p));
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivoAndEstado(100L, MotivoSancion.RETRASO_DEVOLUCION,
        EstadoSancion.ACTIVA)).thenReturn(false);
  }

  private Sancion sancionGuardada() {
    ArgumentCaptor<Sancion> cap = ArgumentCaptor.forClass(Sancion.class);
    verify(sancionRepository).save(cap.capture());
    return cap.getValue();
  }

  // ===================== SANCIÓN POR RETRASO =====================

  @Test
  @DisplayName("IT3-U-SAN-01: 3 días de retraso generan una MULTA de Bs 6 sin suspensión")
  void retraso_soloMulta() {
    prestamoEncontrado(prestamoConRetraso(3));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.procesarDevolucionTardia(100L);

    Sancion s = sancionGuardada();
    assertThat(s.getTipoSancion()).isEqualTo(TipoSancion.MULTA);
    assertThat(s.getMotivo()).isEqualTo(MotivoSancion.RETRASO_DEVOLUCION);
    assertThat(s.getDiasRetraso()).isEqualTo(3);
    assertThat(s.getMontoMulta()).isEqualByComparingTo("6");
    assertThat(s.getEstado()).isEqualTo(EstadoSancion.ACTIVA);
    assertThat(s.getIdConfigUsado()).isEqualTo(77L);
    assertThat(s.getFechaFinSuspension()).isNull();
  }

  @Test
  @DisplayName("IT3-U-SAN-02: un retraso que supera el tope genera MULTA_Y_SUSPENSION (multa topada)")
  void retraso_multaYSuspension() {
    prestamoEncontrado(prestamoConRetraso(10));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.procesarDevolucionTardia(100L);

    Sancion s = sancionGuardada();
    assertThat(s.getTipoSancion()).isEqualTo(TipoSancion.MULTA_Y_SUSPENSION);
    assertThat(s.getMontoMulta()).isEqualByComparingTo("14"); // 7 días x Bs 2
    assertThat(s.getDiasSuspension()).isEqualTo(15);
    assertThat(s.getFechaInicioSuspension()).isEqualTo(LocalDate.now());
    assertThat(s.getFechaFinSuspension()).isEqualTo(LocalDate.now().plusDays(15));
  }

  @Test
  @DisplayName("IT3-U-SAN-03: exactamente en el tope (7 días) solo hay multa, no suspensión")
  void retraso_enElTope() {
    prestamoEncontrado(prestamoConRetraso(7));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.procesarDevolucionTardia(100L);

    Sancion s = sancionGuardada();
    assertThat(s.getTipoSancion()).isEqualTo(TipoSancion.MULTA);
    assertThat(s.getMontoMulta()).isEqualByComparingTo("14");
  }

  @Test
  @DisplayName("IT3-U-SAN-04: si el préstamo ya fue devuelto, el retraso se mide con la fecha real de devolución")
  void retraso_conFechaRealDeDevolucion() {
    Prestamo p = prestamoConRetraso(5);
    p.setFechaDevolucionReal(LocalDate.now().minusDays(2).atTime(10, 0)); // 3 días tarde
    prestamoEncontrado(p);
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.procesarDevolucionTardia(100L);

    assertThat(sancionGuardada().getDiasRetraso()).isEqualTo(3);
  }

  @Test
  @DisplayName("IT3-U-SAN-05: un préstamo sin retraso no genera sanción")
  void retraso_prestamoNoVencido() {
    prestamoEncontrado(prestamoConRetraso(-2)); // vence en 2 días

    assertThatThrownBy(() -> service.procesarDevolucionTardia(100L))
        .isInstanceOf(PrestamoNoVencidoException.class);
    verify(sancionRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-SAN-06: no se duplica la sanción por retraso si ya hay una ACTIVA")
  void retraso_duplicada() {
    when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(prestamoConRetraso(3)));
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivoAndEstado(100L, MotivoSancion.RETRASO_DEVOLUCION,
        EstadoSancion.ACTIVA)).thenReturn(true);

    assertThatThrownBy(() -> service.procesarDevolucionTardia(100L))
        .isInstanceOf(SancionDuplicadaException.class);
    verify(sancionRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-SAN-07: préstamo inexistente lanza error")
  void retraso_prestamoInexistente() {
    when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.procesarDevolucionTardia(100L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("Préstamo no encontrado");
  }

  @Test
  @DisplayName("IT3-U-SAN-08: un préstamo sin configuración histórica guardada no se puede sancionar")
  void retraso_sinConfigHistorica() {
    Prestamo p = prestamoConRetraso(3);
    p.setIdConfigUsado(null);
    prestamoEncontrado(p);

    assertThatThrownBy(() -> service.procesarDevolucionTardia(100L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("idConfigUsado");
  }

  @Test
  @DisplayName("IT3-U-SAN-09: un fallo al notificar NO revierte la sanción")
  void retraso_falloNotificacionNoRevierte() {
    prestamoEncontrado(prestamoConRetraso(3));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);
    doThrow(new RuntimeException("SMTP caído")).when(notificacionService).crear(any());

    service.procesarDevolucionTardia(100L);

    verify(sancionRepository).save(any(Sancion.class));
  }

  // ===================== SANCIÓN MANUAL =====================

  private void escenarioManual() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
  }

  @Test
  @DisplayName("IT3-U-MAN-01: sanción manual por daño con monto fijo (sin préstamo asociado)")
  void manual_ok() {
    escenarioManual();

    service.registrarSancionManual(new SancionManualRequestDTO(1L, 1L, null, MotivoSancion.DANIO_EJEMPLAR,
        new BigDecimal("50.00"), "Páginas rotas"), 2L);

    Sancion s = sancionGuardada();
    assertThat(s.getTipoSancion()).isEqualTo(TipoSancion.MULTA);
    assertThat(s.getMotivo()).isEqualTo(MotivoSancion.DANIO_EJEMPLAR);
    assertThat(s.getMontoMulta()).isEqualByComparingTo("50");
    assertThat(s.getBibliotecario()).isSameAs(bibliotecario);
    assertThat(s.getPrestamo()).isNull();
    assertThat(s.getObservaciones()).isEqualTo("Páginas rotas");
    verify(auditoriaService).registrar(eq("REGISTRAR_SANCION_MANUAL"), eq("sanciones"), any(), any(), any());
  }

  @Test
  @DisplayName("IT3-U-MAN-02: sin monto fijo la multa manual queda en 0")
  void manual_sinMonto() {
    escenarioManual();

    service.registrarSancionManual(
        new SancionManualRequestDTO(1L, 1L, null, MotivoSancion.REINCIDENCIA, null, null), 2L);

    assertThat(sancionGuardada().getMontoMulta()).isEqualByComparingTo("0");
  }

  @Test
  @DisplayName("IT3-U-MAN-03: no se repite una sanción manual activa con el mismo motivo para un préstamo")
  void manual_duplicada() {
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivoAndEstado(100L, MotivoSancion.DANIO_EJEMPLAR,
        EstadoSancion.ACTIVA)).thenReturn(true);

    assertThatThrownBy(() -> service.registrarSancionManual(
        new SancionManualRequestDTO(1L, 1L, 100L, MotivoSancion.DANIO_EJEMPLAR, BigDecimal.TEN, null), 2L))
        .isInstanceOf(SancionDuplicadaException.class);
    verify(sancionRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-MAN-04: usuario inexistente lanza error")
  void manual_usuarioInexistente() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.registrarSancionManual(
        new SancionManualRequestDTO(1L, 1L, null, MotivoSancion.DANIO_EJEMPLAR, BigDecimal.TEN, null), 2L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("Usuario no encontrado");
  }

  // ===================== PÉRDIDA / REPOSICIÓN =====================

  @Test
  @DisplayName("IT3-U-REP-01: reportar pérdida crea sanción REPOSICION pendiente y marca el ejemplar perdido")
  void perdida_ok() {
    when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(prestamoConRetraso(0)));
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivo(100L, MotivoSancion.PERDIDA_EJEMPLAR))
        .thenReturn(false);
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));

    service.registrarPerdidaEjemplar(
        new SancionManualReposicionRequestDTO(100L, 1L, new BigDecimal("80.00"), "Extravío"), 2L);

    verify(ejemplarService).reportarPerdido(eq(5L), anyString());
    Sancion s = sancionGuardada();
    assertThat(s.getTipoSancion()).isEqualTo(TipoSancion.REPOSICION);
    assertThat(s.getMotivo()).isEqualTo(MotivoSancion.PERDIDA_EJEMPLAR);
    assertThat(s.getFormaResolucion()).isEqualTo(FormaResolucion.PENDIENTE);
    assertThat(s.getMontoReposicion()).isEqualByComparingTo("80");
  }

  @Test
  @DisplayName("IT3-U-REP-02: no se puede reportar dos veces la pérdida del mismo préstamo")
  void perdida_duplicada() {
    when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(prestamoConRetraso(0)));
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivo(100L, MotivoSancion.PERDIDA_EJEMPLAR))
        .thenReturn(true);

    assertThatThrownBy(() -> service.registrarPerdidaEjemplar(
        new SancionManualReposicionRequestDTO(100L, 1L, BigDecimal.TEN, null), 2L))
        .isInstanceOf(SancionDuplicadaException.class);
    verify(ejemplarService, never()).reportarPerdido(any(), any());
  }

  private Sancion reposicionPendiente() {
    Sancion s = sancion(30L, TipoSancion.REPOSICION, EstadoSancion.ACTIVA, null);
    s.setMotivo(MotivoSancion.PERDIDA_EJEMPLAR);
    s.setFormaResolucion(FormaResolucion.PENDIENTE);
    s.setPrestamo(prestamoConRetraso(0));
    return s;
  }

  @Test
  @DisplayName("IT3-U-REP-03: la reposición física cierra la sanción (PAGADA) y habilita el ejemplar")
  void reposicionFisica_ok() {
    Sancion s = reposicionPendiente();
    when(sancionRepository.findById(30L)).thenReturn(Optional.of(s));

    service.registrarReposicionFisica(30L, "Trajo un ejemplar nuevo");

    verify(ejemplarService).habilitarPorReposicion(eq(5L), anyString());
    assertThat(s.getEstado()).isEqualTo(EstadoSancion.PAGADA);
    assertThat(s.getFormaResolucion()).isEqualTo(FormaResolucion.REPOSICION_FISICA);
    assertThat(s.getFechaResolucion()).isNotNull();
    assertThat(s.getObservaciones()).isEqualTo("Trajo un ejemplar nuevo");
  }

  @Test
  @DisplayName("IT3-U-REP-04: reposición sobre una sanción no activa es rechazada")
  void reposicionFisica_noActiva() {
    Sancion s = reposicionPendiente();
    s.setEstado(EstadoSancion.CONDONADA);
    when(sancionRepository.findById(30L)).thenReturn(Optional.of(s));

    assertThatThrownBy(() -> service.registrarReposicionFisica(30L, null))
        .isInstanceOf(SancionNoActivaException.class);
    verify(ejemplarService, never()).habilitarPorReposicion(any(), any());
  }

  @Test
  @DisplayName("IT3-U-REP-05: reposición sobre una sanción que no es de tipo REPOSICION es rechazada")
  void reposicionFisica_tipoIncorrecto() {
    Sancion s = sancion(31L, TipoSancion.MULTA, EstadoSancion.ACTIVA, "10");
    when(sancionRepository.findById(31L)).thenReturn(Optional.of(s));

    assertThatThrownBy(() -> service.registrarReposicionFisica(31L, null))
        .isInstanceOf(IllegalStateException.class).hasMessageContaining("no corresponde a una reposición");
  }

  @Test
  @DisplayName("IT3-U-REP-06: una reposición ya resuelta no se puede resolver de nuevo")
  void reposicionFisica_yaResuelta() {
    Sancion s = reposicionPendiente();
    s.setFormaResolucion(FormaResolucion.REPOSICION_FISICA);
    when(sancionRepository.findById(30L)).thenReturn(Optional.of(s));

    assertThatThrownBy(() -> service.registrarReposicionFisica(30L, null))
        .isInstanceOf(IllegalStateException.class).hasMessageContaining("ya fue resuelta");
  }

  // ===================== PAGO =====================

  @Test
  @DisplayName("IT3-U-PAG-01: pagar una MULTA la pasa a PAGADA con fecha y método de pago")
  void pago_multa() {
    Sancion s = sancion(40L, TipoSancion.MULTA, EstadoSancion.ACTIVA, "10");
    when(sancionRepository.findById(40L)).thenReturn(Optional.of(s));

    service.registrarPago(40L, new PagoMultaRequestDTO("EFECTIVO", "Pagó en ventanilla"));

    assertThat(s.getEstado()).isEqualTo(EstadoSancion.PAGADA);
    assertThat(s.getFechaPago()).isNotNull();
    assertThat(s.getMetodoPago()).isEqualTo("EFECTIVO");
    assertThat(s.getObservaciones()).isEqualTo("Pagó en ventanilla");
    verify(auditoriaService).registrar(eq("REGISTRAR_PAGO_SANCION"), eq("sanciones"), any(), any(), any());
  }

  @Test
  @DisplayName("IT3-U-PAG-02: pagar la multa de una sanción con suspensión vigente deja la sanción ACTIVA (hasta que termine la suspensión)")
  void pago_multaConSuspensionVigente() {
    Sancion s = sancion(41L, TipoSancion.MULTA_Y_SUSPENSION, EstadoSancion.ACTIVA, "14");
    s.setFechaFinSuspension(LocalDate.now().plusDays(5));
    when(sancionRepository.findById(41L)).thenReturn(Optional.of(s));

    service.registrarPago(41L, new PagoMultaRequestDTO("QR", null));

    assertThat(s.getEstado()).isEqualTo(EstadoSancion.ACTIVA);
    assertThat(s.getFechaPago()).isNotNull();
  }

  @Test
  @DisplayName("IT3-U-PAG-03: no se puede pagar dos veces la misma multa")
  void pago_doble() {
    Sancion s = sancion(41L, TipoSancion.MULTA_Y_SUSPENSION, EstadoSancion.ACTIVA, "14");
    s.setFechaFinSuspension(LocalDate.now().plusDays(5));
    s.setFechaPago(LocalDateTime.now());
    when(sancionRepository.findById(41L)).thenReturn(Optional.of(s));

    assertThatThrownBy(() -> service.registrarPago(41L, new PagoMultaRequestDTO("QR", null)))
        .isInstanceOf(IllegalStateException.class).hasMessageContaining("ya fue pagada");
  }

  @Test
  @DisplayName("IT3-U-PAG-04: pagar una sanción que ya no está activa es rechazado")
  void pago_noActiva() {
    Sancion s = sancion(42L, TipoSancion.MULTA, EstadoSancion.PAGADA, "10");
    when(sancionRepository.findById(42L)).thenReturn(Optional.of(s));

    assertThatThrownBy(() -> service.registrarPago(42L, new PagoMultaRequestDTO("EFECTIVO", null)))
        .isInstanceOf(SancionNoActivaException.class);
  }

  @Test
  @DisplayName("IT3-U-PAG-05: pagar una sanción inexistente lanza 'no encontrada'")
  void pago_inexistente() {
    when(sancionRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.registrarPago(99L, new PagoMultaRequestDTO("EFECTIVO", null)))
        .isInstanceOf(SancionNotFoundException.class);
  }

  // ===================== CONDONACIÓN =====================

  @Test
  @DisplayName("IT3-U-CON-01: condonar una sanción activa la pasa a CONDONADA con fecha y motivo")
  void condonar_ok() {
    Sancion s = sancion(50L, TipoSancion.MULTA, EstadoSancion.ACTIVA, "10");
    when(sancionRepository.findById(50L)).thenReturn(Optional.of(s));

    service.condonar(50L, new CondonacionRequestDTO("Caso de fuerza mayor"));

    assertThat(s.getEstado()).isEqualTo(EstadoSancion.CONDONADA);
    assertThat(s.getFechaCondonacion()).isNotNull();
    assertThat(s.getObservaciones()).isEqualTo("Caso de fuerza mayor");
    verify(auditoriaService).registrar(eq("CONDONAR_SANCION"), eq("sanciones"), any(), any(), any());
  }

  @Test
  @DisplayName("IT3-U-CON-02: no se puede condonar una sanción ya pagada o condonada")
  void condonar_noActiva() {
    for (EstadoSancion estado : new EstadoSancion[] { EstadoSancion.PAGADA, EstadoSancion.CONDONADA }) {
      Sancion s = sancion(51L, TipoSancion.MULTA, estado, "10");
      when(sancionRepository.findById(51L)).thenReturn(Optional.of(s));

      assertThatThrownBy(() -> service.condonar(51L, new CondonacionRequestDTO("x")))
          .as(estado.name()).isInstanceOf(SancionNoActivaException.class);
    }
  }

  // ===================== ESTADO CONSOLIDADO =====================

  @Test
  @DisplayName("IT3-U-EST-01: el estado consolida suspensión vigente, deuda total y total de sanciones activas")
  void estado_consolidado() {
    Sancion multa = sancion(1L, TipoSancion.MULTA, EstadoSancion.ACTIVA, "6");
    Sancion multaSusp = sancion(2L, TipoSancion.MULTA_Y_SUSPENSION, EstadoSancion.ACTIVA, "10");
    multaSusp.setFechaFinSuspension(LocalDate.now().plusDays(3));
    Sancion suspVencida = sancion(3L, TipoSancion.SUSPENSION, EstadoSancion.ACTIVA, null);
    suspVencida.setFechaFinSuspension(LocalDate.now().minusDays(1));
    when(sancionRepository.findActivasByUsuarioAndBiblioteca(1L, 1L, EstadoSancion.ACTIVA))
        .thenReturn(List.of(multa, multaSusp, suspVencida));

    EstadoSancionUsuarioDTO e = service.obtenerEstadoSanciones(1L, 1L);

    assertThat(e.tieneSuspensionVigente()).isTrue();
    assertThat(e.tieneDeudaPendiente()).isTrue();
    assertThat(e.tieneReposicionPendiente()).isFalse();
    assertThat(e.totalSancionesActivas()).isEqualTo(3);
    assertThat(e.montoTotalDeuda()).isEqualByComparingTo("16");
    assertThat(e.fechaFinSuspensionMasProxima()).isEqualTo(LocalDate.now().plusDays(3));
    assertThat(e.sanciones()).hasSize(3);
  }

  @Test
  @DisplayName("IT3-U-EST-02: una suspensión ya vencida no cuenta como vigente")
  void estado_suspensionVencida() {
    Sancion vencida = sancion(3L, TipoSancion.SUSPENSION, EstadoSancion.ACTIVA, null);
    vencida.setFechaFinSuspension(LocalDate.now().minusDays(1));
    when(sancionRepository.findActivasByUsuarioAndBiblioteca(1L, 1L, EstadoSancion.ACTIVA))
        .thenReturn(List.of(vencida));

    EstadoSancionUsuarioDTO e = service.obtenerEstadoSanciones(1L, 1L);

    assertThat(e.tieneSuspensionVigente()).isFalse();
    assertThat(e.fechaFinSuspensionMasProxima()).isNull();
  }

  @Test
  @DisplayName("IT3-U-EST-03: un usuario sin sanciones queda limpio")
  void estado_sinSanciones() {
    when(sancionRepository.findActivasByUsuarioAndBiblioteca(1L, 1L, EstadoSancion.ACTIVA))
        .thenReturn(List.of());

    EstadoSancionUsuarioDTO e = service.obtenerEstadoSanciones(1L, 1L);

    assertThat(e.tieneSuspensionVigente()).isFalse();
    assertThat(e.tieneDeudaPendiente()).isFalse();
    assertThat(e.totalSancionesActivas()).isZero();
    assertThat(e.montoTotalDeuda()).isEqualByComparingTo("0");
  }

  @Test
  @DisplayName("IT3-U-EST-04: una sanción de reposición activa se marca como reposición pendiente")
  void estado_reposicionPendiente() {
    when(sancionRepository.findActivasByUsuarioAndBiblioteca(1L, 1L, EstadoSancion.ACTIVA))
        .thenReturn(List.of(reposicionPendiente()));

    assertThat(service.obtenerEstadoSanciones(1L, 1L).tieneReposicionPendiente()).isTrue();
  }

  @Test
  @DisplayName("IT3-U-EST-05: el estado por CI de un usuario inexistente es neutro (sin sanciones)")
  void estadoPorCi_inexistente() {
    when(usuarioRepository.findByPersona_Ci("000")).thenReturn(Optional.empty());

    EstadoSancionUsuarioDTO e = service.obtenerEstadoSancionesPorCi("000", 1L);

    assertThat(e.usuarioId()).isNull();
    assertThat(e.tieneSuspensionVigente()).isFalse();
    assertThat(e.totalSancionesActivas()).isZero();
  }

  @Test
  @DisplayName("IT3-U-EST-06: buscarPorId inexistente lanza 'no encontrada'")
  void buscarPorId_inexistente() {
    when(sancionRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.buscarPorId(99L)).isInstanceOf(SancionNotFoundException.class);
  }

  // ===================== MONTO ACTUAL =====================

  @Test
  @DisplayName("IT3-U-MON-01: el monto actual de una sanción activa se recalcula con la config histórica")
  void montoActual_recalcula() {
    Sancion s = sancion(60L, TipoSancion.MULTA, EstadoSancion.ACTIVA, "2");
    s.setIdConfigUsado(77L);
    s.setPrestamo(prestamoConRetraso(2));
    when(sancionRepository.findById(60L)).thenReturn(Optional.of(s));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    assertThat(service.calcularMontoActual(60L)).isEqualByComparingTo("4");
  }

  @Test
  @DisplayName("IT3-U-MON-02: si la sanción no está activa devuelve el monto guardado")
  void montoActual_noActiva() {
    Sancion s = sancion(61L, TipoSancion.MULTA, EstadoSancion.PAGADA, "9");
    when(sancionRepository.findById(61L)).thenReturn(Optional.of(s));

    assertThat(service.calcularMontoActual(61L)).isEqualByComparingTo("9");
  }

  @Test
  @DisplayName("IT3-U-MON-03: una sanción manual (sin config ni préstamo) devuelve su monto fijo")
  void montoActual_manual() {
    Sancion s = sancion(62L, TipoSancion.MULTA, EstadoSancion.ACTIVA, "50");
    when(sancionRepository.findById(62L)).thenReturn(Optional.of(s));

    assertThat(service.calcularMontoActual(62L)).isEqualByComparingTo("50");
  }

  // ===================== TAREAS PROGRAMADAS =====================

  private Sancion sancionPorRetraso(int diasRetraso, String montoGuardado) {
    Sancion s = sancion(70L, TipoSancion.MULTA, EstadoSancion.ACTIVA, montoGuardado);
    s.setIdConfigUsado(77L);
    s.setPrestamo(prestamoConRetraso(diasRetraso));
    return s;
  }

  @Test
  @DisplayName("IT3-U-JOB-01: la tarea de multas incrementa el monto de una sanción activa por retraso")
  void job_actualizaMonto() {
    Sancion s = sancionPorRetraso(3, "4"); // guardado 2 días
    when(sancionRepository.findActivasPorRetrasoConConfig()).thenReturn(List.of(s));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.actualizarMultasSanciones();

    assertThat(s.getMontoMulta()).isEqualByComparingTo("6");
    assertThat(s.getDiasRetraso()).isEqualTo(3);
    assertThat(s.getObservaciones()).contains("Multa actualizada automáticamente");
    verify(sancionRepository).save(s);
  }

  @Test
  @DisplayName("IT3-U-JOB-02: si el monto no cambió, la tarea no guarda nada")
  void job_sinCambios() {
    Sancion s = sancionPorRetraso(3, "6");
    when(sancionRepository.findActivasPorRetrasoConConfig()).thenReturn(List.of(s));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.actualizarMultasSanciones();

    verify(sancionRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-JOB-03: al alcanzar el tope, la multa queda congelada en el máximo (7 x Bs 2 = Bs 14)")
  void job_congelaEnElTope() {
    Sancion s = sancionPorRetraso(7, "12");
    when(sancionRepository.findActivasPorRetrasoConConfig()).thenReturn(List.of(s));
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.actualizarMultasSanciones();

    assertThat(s.getMontoMulta()).isEqualByComparingTo("14");
    assertThat(s.getTipoSancion()).isEqualTo(TipoSancion.MULTA);
  }

  @Test
  @DisplayName("IT3-U-JOB-05: la tarea de suspensiones cierra (PAGADA) las suspensiones vencidas sin deuda")
  void job_cierraSuspensiones() {
    Sancion s = sancion(80L, TipoSancion.SUSPENSION, EstadoSancion.ACTIVA, null);
    s.setFechaFinSuspension(LocalDate.now().minusDays(1));
    when(sancionRepository.findSuspensionesVencidasSinDeuda()).thenReturn(List.of(s));

    service.cerrarSuspensionesVencidas();

    assertThat(s.getEstado()).isEqualTo(EstadoSancion.PAGADA);
    verify(sancionRepository).save(s);
  }

  @Test
  @DisplayName("IT3-U-JOB-06: la tarea de generación crea sanciones y sigue aunque una falle o esté duplicada")
  void job_generaSanciones_toleraErrores() {
    Prestamo ok = prestamoConRetraso(3);
    ok.setIdPrestamo(100L);
    Prestamo duplicado = prestamoConRetraso(4);
    duplicado.setIdPrestamo(101L);
    Prestamo roto = prestamoConRetraso(5);
    roto.setIdPrestamo(102L);
    roto.setIdConfigUsado(null);
    when(prestamoRepository.findPrestamosVencidosSinSancion()).thenReturn(List.of(duplicado, roto, ok));
    when(prestamoRepository.findByIdWithRelations(101L)).thenReturn(Optional.of(duplicado));
    when(prestamoRepository.findByIdWithRelations(102L)).thenReturn(Optional.of(roto));
    when(prestamoRepository.findByIdWithRelations(100L)).thenReturn(Optional.of(ok));
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivoAndEstado(eq(101L), any(), any())).thenReturn(true);
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivoAndEstado(eq(102L), any(), any())).thenReturn(false);
    when(sancionRepository.existsByPrestamo_IdPrestamoAndMotivoAndEstado(eq(100L), any(), any())).thenReturn(false);
    when(configuracionService.obtenerEntidadPorId(77L)).thenReturn(config);

    service.procesarPrestamosVencidos();

    // solo el préstamo 100 llegó a generar sanción
    verify(sancionRepository).save(any(Sancion.class));
  }
}

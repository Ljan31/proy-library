package com.proyecto.fhce.library.iteracion3.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.proyecto.fhce.library.dto.ReglasPrestamoDTO;
import com.proyecto.fhce.library.dto.SancionDTO.EstadoSancionUsuarioDTO;
import com.proyecto.fhce.library.dto.request.loads.DevolucionRequest;
import com.proyecto.fhce.library.dto.request.loads.FiltroPrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.PrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.RenovacionRequest;
import com.proyecto.fhce.library.dto.response.loads.EstadoPrestamoUsuarioDTO;
import com.proyecto.fhce.library.dto.response.loads.PrestamoResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.HistorialEstadoEjemplar;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.CondicionFisicaLibro;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.EstadoReserva;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.enums.TipoDocumentoGarantia;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.mapper.PrestamoMapper;
import com.proyecto.fhce.library.repositories.BibliotecaEncargadoRepository;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.HistorialEstadoEjemplarRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.ReservaRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.loads.AsignacionReservaService;
import com.proyecto.fhce.library.services.loads.ConfiguracionPrestamoService;
import com.proyecto.fhce.library.services.loads.PrestamoServiceImpl;
import com.proyecto.fhce.library.services.loads.SancionService;
import com.proyecto.fhce.library.support.TestData;

/**
 * Iteración 3 · Lógica de préstamos, devoluciones y renovaciones
 * (repositorios y servicios colaboradores simulados con Mockito).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 3 · PrestamoServiceImpl (unitaria)")
class PrestamoServiceImplTest {

  @Mock
  PrestamoRepository prestamoRepository;
  @Mock
  EjemplarRepository ejemplarRepository;
  @Mock
  UserRepository usuarioRepository;
  @Mock
  BibliotecaRepository bibliotecaRepository;
  @Mock
  HistorialEstadoEjemplarRepository historialRepository;
  @Mock
  BibliotecaEncargadoRepository encargadoRepository;
  @Mock
  PrestamoMapper prestamoMapper;
  @Mock
  ConfiguracionPrestamoService configuracionService;
  @Mock
  ReservaRepository reservaRepository;
  @Mock
  AsignacionReservaService asignacionReservaService;
  @Mock
  SancionService sancionService;
  @Mock
  AuditoriaService auditoriaService;

  @InjectMocks
  PrestamoServiceImpl service;

  Usuario lector;
  Usuario bibliotecario;
  Biblioteca biblioteca;
  Ejemplar ejemplar;

  @BeforeEach
  void init() {
    lector = TestData.usuario(1L, "lector");
    bibliotecario = TestData.usuario(2L, "biblio");
    biblioteca = TestData.biblioteca(1L, "Central", TipoBiblioteca.FACULTATIVA);
    ejemplar = TestData.ejemplar(5L, TestData.edicion(1L, TestData.libro(1L, "Rayuela")), biblioteca,
        EstadoEjemplar.DISPONIBLE);
    // El mapper devuelve una respuesta con el estado del préstamo
    lenient().when(prestamoMapper.toResponse(any(Prestamo.class))).thenAnswer(i -> {
      Prestamo p = i.getArgument(0);
      PrestamoResponse r = new PrestamoResponse();
      r.setEstadoPrestamo(p.getEstadoPrestamo());
      r.setRenovaciones(p.getRenovaciones());
      r.setFechaDevolucionEstimada(p.getFechaDevolucionEstimada());
      return r;
    });
    lenient().when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));
  }

  // ---------------- helpers ----------------
  private EstadoSancionUsuarioDTO sinSanciones() {
    return new EstadoSancionUsuarioDTO(1L, false, false, false, 0, BigDecimal.ZERO, null, List.of());
  }

  private EstadoSancionUsuarioDTO conSuspension() {
    return new EstadoSancionUsuarioDTO(1L, true, false, false, 1, BigDecimal.ZERO,
        LocalDate.now().plusDays(5), List.of());
  }

  private ReglasPrestamoDTO reglas(int dias, int renovaciones, int ejemplares) {
    ReglasPrestamoDTO r = new ReglasPrestamoDTO();
    r.setIdConfig(77L);
    r.setDiasPrestamoMax(dias);
    r.setRenovacionesMax(renovaciones);
    r.setEjemplaresPermitidos(ejemplares);
    return r;
  }

  private PrestamoRequest requestPrestamo() {
    PrestamoRequest r = new PrestamoRequest();
    r.setUsuarioId(1L);
    r.setEjemplarId(5L);
    r.setBibliotecaId(1L);
    r.setTipoPrestamo(TipoPrestamo.DOMICILIO);
    r.setTipoDocumentoGarantia(TipoDocumentoGarantia.CI);
    r.setCondicionEntrega(CondicionFisicaLibro.BUENO);
    return r;
  }

  /** Stubs comunes para que realizarPrestamo llegue hasta el final. */
  private void escenarioPrestamoValido() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);
    when(sancionService.obtenerEstadoSanciones(1L, 2L)).thenReturn(sinSanciones());
    when(prestamoRepository.findPrestamoActivoByEjemplar(5L)).thenReturn(Optional.empty());
    when(configuracionService.obtenerReglas(1L, TipoPrestamo.DOMICILIO)).thenReturn(reglas(7, 2, 3));
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(0L);
  }

  private Prestamo prestamoActivo(EstadoPrestamo estado, LocalDate estimada) {
    Prestamo p = new Prestamo();
    p.setIdPrestamo(100L);
    p.setUsuario(lector);
    p.setEjemplar(ejemplar);
    p.setBiblioteca(biblioteca);
    p.setEstadoPrestamo(estado);
    p.setFechaPrestamo(LocalDateTime.now().minusDays(3));
    p.setFechaDevolucionEstimada(estimada);
    p.setRenovaciones(0);
    p.setTipoPrestamo(TipoPrestamo.DOMICILIO);
    p.setCondicionEntrega(CondicionFisicaLibro.BUENO);
    return p;
  }

  private DevolucionRequest requestDevolucion(CondicionFisicaLibro condicion) {
    DevolucionRequest r = new DevolucionRequest();
    r.setPrestamoId(100L);
    r.setCondicionDevolucion(condicion);
    return r;
  }

  private List<GrantedAuthority> roles(String... nombres) {
    return java.util.Arrays.stream(nombres).map(n -> (GrantedAuthority) new SimpleGrantedAuthority(n)).toList();
  }

  // ===================== REALIZAR PRÉSTAMO =====================

  @Test
  @DisplayName("IT3-U-PRE-01: un préstamo válido queda ACTIVO, con 0 renovaciones y el ejemplar pasa a PRESTADO")
  void prestamo_ok() {
    escenarioPrestamoValido();

    PrestamoResponse r = service.realizarPrestamo(requestPrestamo(), 2L);

    ArgumentCaptor<Prestamo> cap = ArgumentCaptor.forClass(Prestamo.class);
    verify(prestamoRepository).save(cap.capture());
    Prestamo p = cap.getValue();
    assertThat(p.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.ACTIVO);
    assertThat(p.getRenovaciones()).isZero();
    assertThat(p.getUsuario()).isSameAs(lector);
    assertThat(p.getBibliotecarioPrestamo()).isSameAs(bibliotecario);
    assertThat(p.getIdConfigUsado()).isEqualTo(77L);
    assertThat(p.getTipoPrestamo()).isEqualTo(TipoPrestamo.DOMICILIO);
    assertThat(ejemplar.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.PRESTADO);
    assertThat(r.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.ACTIVO);
    verify(auditoriaService).registrar(eq("CREAR_PRESTAMO"), eq("Prestamos"), any(), any(), any());
  }

  @Test
  @DisplayName("IT3-U-PRE-02: sin fecha pedida, la devolución estimada es hoy + días máximos de la configuración")
  void prestamo_fechaPorDefecto() {
    escenarioPrestamoValido();

    service.realizarPrestamo(requestPrestamo(), 2L);

    ArgumentCaptor<Prestamo> cap = ArgumentCaptor.forClass(Prestamo.class);
    verify(prestamoRepository).save(cap.capture());
    assertThat(cap.getValue().getFechaDevolucionEstimada()).isEqualTo(LocalDate.now().plusDays(7));
  }

  @Test
  @DisplayName("IT3-U-PRE-03: si se pide una fecha de devolución, se respeta")
  void prestamo_fechaPedida() {
    escenarioPrestamoValido();
    PrestamoRequest req = requestPrestamo();
    req.setFechaDevolucionEstimada(LocalDate.now().plusDays(3));

    service.realizarPrestamo(req, 2L);

    ArgumentCaptor<Prestamo> cap = ArgumentCaptor.forClass(Prestamo.class);
    verify(prestamoRepository).save(cap.capture());
    assertThat(cap.getValue().getFechaDevolucionEstimada()).isEqualTo(LocalDate.now().plusDays(3));
  }

  @Test
  @DisplayName("IT3-U-PRE-04: usuario inexistente lanza 'no encontrado' y no guarda nada")
  void prestamo_usuarioInexistente() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Usuario");
    verify(prestamoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-PRE-05: ejemplar inexistente lanza 'no encontrado'")
  void prestamo_ejemplarInexistente() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Ejemplar");
  }

  @Test
  @DisplayName("IT3-U-PRE-06: biblioteca inexistente lanza 'no encontrada'")
  void prestamo_bibliotecaInexistente() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Biblioteca");
  }

  @Test
  @DisplayName("IT3-U-PRE-07: un bibliotecario que no es encargado de la biblioteca no puede prestar")
  void prestamo_bibliotecarioNoEncargado() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(false);

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(BusinessException.class).hasMessageContaining("no está autorizado");
    verify(prestamoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-PRE-08: un usuario con suspensión vigente no puede recibir préstamos")
  void prestamo_usuarioSuspendido() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);
    when(sancionService.obtenerEstadoSanciones(1L, 2L)).thenReturn(conSuspension());

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(BusinessException.class).hasMessageContaining("suspensión activa");
    verify(prestamoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT3-U-PRE-09: un ejemplar PRESTADO no puede prestarse")
  void prestamo_ejemplarNoDisponible() {
    ejemplar.setEstadoEjemplar(EstadoEjemplar.PRESTADO);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);
    when(sancionService.obtenerEstadoSanciones(1L, 2L)).thenReturn(sinSanciones());

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(BusinessException.class).hasMessageContaining("no está disponible");
  }

  @Test
  @DisplayName("IT3-U-PRE-10: un ejemplar RESERVADO con reserva NOTIFICADA del mismo usuario sí puede prestarse")
  void prestamo_reservadoParaElUsuario() {
    ejemplar.setEstadoEjemplar(EstadoEjemplar.RESERVADO);
    escenarioPrestamoValido();
    when(reservaRepository.existsByEjemplarIdAndUsuarioIdAndEstadoReserva(5L, 1L, EstadoReserva.NOTIFICADA))
        .thenReturn(true);

    service.realizarPrestamo(requestPrestamo(), 2L);

    assertThat(ejemplar.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.PRESTADO);
  }

  @Test
  @DisplayName("IT3-U-PRE-11: un ejemplar RESERVADO para otro usuario no puede prestarse")
  void prestamo_reservadoParaOtro() {
    ejemplar.setEstadoEjemplar(EstadoEjemplar.RESERVADO);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);
    when(sancionService.obtenerEstadoSanciones(1L, 2L)).thenReturn(sinSanciones());
    when(reservaRepository.existsByEjemplarIdAndUsuarioIdAndEstadoReserva(5L, 1L, EstadoReserva.NOTIFICADA))
        .thenReturn(false);

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(BusinessException.class).hasMessageContaining("no está disponible");
  }

  @Test
  @DisplayName("IT3-U-PRE-12: un ejemplar que ya tiene un préstamo activo es rechazado")
  void prestamo_ejemplarConPrestamoActivo() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);
    when(sancionService.obtenerEstadoSanciones(1L, 2L)).thenReturn(sinSanciones());
    when(prestamoRepository.findPrestamoActivoByEjemplar(5L))
        .thenReturn(Optional.of(prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2))));

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(BusinessException.class).hasMessageContaining("préstamo activo");
  }

  @Test
  @DisplayName("IT3-U-PRE-13: al alcanzar el límite de préstamos simultáneos no se permite otro")
  void prestamo_limiteSimultaneos() {
    escenarioPrestamoValido();
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(3L);

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(BusinessException.class).hasMessageContaining("límite de préstamos simultáneos");
    verify(prestamoRepository, never()).save(any());
    assertThat(ejemplar.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.DISPONIBLE);
  }

  @Test
  @DisplayName("IT3-U-PRE-14: una biblioteca sin configuración no puede realizar préstamos")
  void prestamo_sinConfiguracion() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplar));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);
    when(sancionService.obtenerEstadoSanciones(1L, 2L)).thenReturn(sinSanciones());
    when(prestamoRepository.findPrestamoActivoByEjemplar(5L)).thenReturn(Optional.empty());
    when(configuracionService.obtenerReglas(1L, TipoPrestamo.DOMICILIO))
        .thenThrow(new ResourceNotFoundException("No existe configuración para la biblioteca con id: 1"));

    assertThatThrownBy(() -> service.realizarPrestamo(requestPrestamo(), 2L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // ===================== DEVOLUCIÓN =====================

  private void escenarioDevolucion(Prestamo p) {
    p.getEjemplar().setEstadoEjemplar(EstadoEjemplar.PRESTADO);
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);
  }

  @Test
  @DisplayName("IT3-U-DEV-01: devolver un préstamo ACTIVO lo marca DEVUELTO y deja el ejemplar DISPONIBLE")
  void devolucion_ok() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    escenarioDevolucion(p);

    PrestamoResponse r = service.realizarDevolucion(requestDevolucion(CondicionFisicaLibro.BUENO), 2L);

    assertThat(r.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.DEVUELTO);
    assertThat(p.getFechaDevolucionReal()).isNotNull();
    assertThat(p.getBibliotecarioDevolucion()).isSameAs(bibliotecario);
    assertThat(p.getCondicionDevolucion()).isEqualTo(CondicionFisicaLibro.BUENO);
    assertThat(ejemplar.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.DISPONIBLE);
    verify(ejemplarRepository).save(ejemplar);
    verify(historialRepository).save(any(HistorialEstadoEjemplar.class));
    verify(asignacionReservaService).detectarYAsignarDisponibles();
    verify(auditoriaService).registrar(eq("DEVOLUCION_PRESTAMO"), eq("Prestamos"), any(), any(), any());
  }

  @Test
  @DisplayName("IT3-U-DEV-02: se puede devolver un préstamo VENCIDO o RENOVADO")
  void devolucion_vencidoYRenovado() {
    for (EstadoPrestamo estado : new EstadoPrestamo[] { EstadoPrestamo.VENCIDO, EstadoPrestamo.RENOVADO }) {
      Prestamo p = prestamoActivo(estado, LocalDate.now().minusDays(1));
      escenarioDevolucion(p);
      PrestamoResponse r = service.realizarDevolucion(requestDevolucion(CondicionFisicaLibro.BUENO), 2L);
      assertThat(r.getEstadoPrestamo()).as(estado.name()).isEqualTo(EstadoPrestamo.DEVUELTO);
    }
  }

  @Test
  @DisplayName("IT3-U-DEV-03: un préstamo ya DEVUELTO no puede devolverse otra vez")
  void devolucion_yaDevuelto() {
    Prestamo p = prestamoActivo(EstadoPrestamo.DEVUELTO, LocalDate.now());
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));

    assertThatThrownBy(() -> service.realizarDevolucion(requestDevolucion(CondicionFisicaLibro.BUENO), 2L))
        .isInstanceOf(BusinessException.class).hasMessageContaining("no está en un estado válido");
  }

  @Test
  @DisplayName("IT3-U-DEV-04: devolver un préstamo inexistente lanza 'no encontrado'")
  void devolucion_inexistente() {
    when(prestamoRepository.findById(100L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.realizarDevolucion(requestDevolucion(CondicionFisicaLibro.BUENO), 2L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-DEV-05: un bibliotecario ajeno a la biblioteca no puede registrar la devolución")
  void devolucion_bibliotecarioNoEncargado() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(false);

    assertThatThrownBy(() -> service.realizarDevolucion(requestDevolucion(CondicionFisicaLibro.BUENO), 2L))
        .isInstanceOf(BusinessException.class);
    assertThat(p.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.ACTIVO);
  }

  @Test
  @DisplayName("IT3-U-DEV-06: si el ejemplar vuelve en peor condición se agrega una alerta a las observaciones")
  void devolucion_deterioro() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    escenarioDevolucion(p);

    service.realizarDevolucion(requestDevolucion(CondicionFisicaLibro.DETERIORADO), 2L);

    assertThat(p.getObservaciones()).contains("[ALERTA]").contains("BUENO").contains("DETERIORADO");
  }

  @Test
  @DisplayName("IT3-U-DEV-07: con la misma condición no se agrega alerta")
  void devolucion_sinDeterioro() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    escenarioDevolucion(p);

    service.realizarDevolucion(requestDevolucion(CondicionFisicaLibro.BUENO), 2L);

    assertThat(p.getObservaciones()).isNull();
  }

  @Test
  @DisplayName("IT3-U-DEV-08: si se indica un estado para el ejemplar (p. ej. DAÑADO) se aplica en vez de DISPONIBLE")
  void devolucion_estadoEjemplarIndicado() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    escenarioDevolucion(p);
    DevolucionRequest req = requestDevolucion(CondicionFisicaLibro.DAÑADO);
    req.setEstadoEjemplar(EstadoEjemplar.DAÑADO);
    req.setObservaciones("Tapa rota");

    service.realizarDevolucion(req, 2L);

    assertThat(ejemplar.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.DAÑADO);
    assertThat(p.getObservaciones()).contains("Tapa rota");
  }

  @Test
  @DisplayName("IT3-U-DEV-09: las observaciones de la devolución se concatenan a las del préstamo")
  void devolucion_concatenaObservaciones() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    p.setObservaciones("Entrega con marcapáginas");
    escenarioDevolucion(p);
    DevolucionRequest req = requestDevolucion(CondicionFisicaLibro.BUENO);
    req.setObservaciones("Sin novedad");

    service.realizarDevolucion(req, 2L);

    assertThat(p.getObservaciones()).isEqualTo("Entrega con marcapáginas. Sin novedad");
  }

  // ===================== RENOVACIÓN =====================

  private RenovacionRequest requestRenovacion() {
    RenovacionRequest r = new RenovacionRequest();
    r.setPrestamoId(100L);
    r.setMotivo("Necesito más tiempo");
    return r;
  }

  private void escenarioRenovacion(Prestamo p) {
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));
    when(sancionService.obtenerEstadoSanciones(1L, 1L)).thenReturn(sinSanciones());
    when(configuracionService.obtenerReglas(1L, TipoPrestamo.DOMICILIO)).thenReturn(reglas(7, 2, 3));
  }

  @Test
  @DisplayName("IT3-U-REN-01: el dueño del préstamo puede renovarlo: +1 renovación, +días y estado RENOVADO")
  void renovar_propietario() {
    LocalDate estimada = LocalDate.now().plusDays(2);
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, estimada);
    escenarioRenovacion(p);

    PrestamoResponse r = service.renovarPrestamo(requestRenovacion(), 1L, roles("ROLE_ESTUDIANTE"));

    assertThat(r.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.RENOVADO);
    assertThat(p.getRenovaciones()).isEqualTo(1);
    assertThat(p.getFechaDevolucionEstimada()).isEqualTo(estimada.plusDays(7));
    verify(auditoriaService).registrar(eq("RENOVAR_PRESTAMO"), eq("Prestamos"), any(), any(), any());
  }

  @Test
  @DisplayName("IT3-U-REN-02: un tercero sin rol de bibliotecario no puede renovar el préstamo ajeno")
  void renovar_terceroNoAutorizado() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));

    assertThatThrownBy(() -> service.renovarPrestamo(requestRenovacion(), 99L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("No está autorizado");
    assertThat(p.getRenovaciones()).isZero();
  }

  @Test
  @DisplayName("IT3-U-REN-03: un bibliotecario encargado de la biblioteca puede renovar préstamos de otros")
  void renovar_bibliotecarioEncargado() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    escenarioRenovacion(p);
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);

    service.renovarPrestamo(requestRenovacion(), 2L, roles("ROLE_BIBLIOTECARIO"));

    assertThat(p.getRenovaciones()).isEqualTo(1);
  }

  @Test
  @DisplayName("IT3-U-REN-04: un bibliotecario de otra biblioteca no puede renovar")
  void renovar_bibliotecarioAjeno() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(false);

    assertThatThrownBy(() -> service.renovarPrestamo(requestRenovacion(), 2L, roles("ROLE_BIBLIOTECARIO")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("IT3-U-REN-05: no se puede renovar un préstamo DEVUELTO o VENCIDO")
  void renovar_estadoInvalido() {
    for (EstadoPrestamo estado : new EstadoPrestamo[] { EstadoPrestamo.DEVUELTO, EstadoPrestamo.VENCIDO }) {
      Prestamo p = prestamoActivo(estado, LocalDate.now().plusDays(2));
      when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));

      assertThatThrownBy(() -> service.renovarPrestamo(requestRenovacion(), 1L, roles("ROLE_ESTUDIANTE")))
          .as(estado.name()).isInstanceOf(BusinessException.class).hasMessageContaining("no puede renovarse");
    }
  }

  @Test
  @DisplayName("IT3-U-REN-06: no se puede renovar un préstamo cuya fecha ya pasó")
  void renovar_fechaVencida() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().minusDays(1));
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));

    assertThatThrownBy(() -> service.renovarPrestamo(requestRenovacion(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("vencido");
  }

  @Test
  @DisplayName("IT3-U-REN-07: no se puede renovar con una suspensión vigente")
  void renovar_conSuspension() {
    Prestamo p = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(2));
    when(prestamoRepository.findById(100L)).thenReturn(Optional.of(p));
    when(sancionService.obtenerEstadoSanciones(1L, 1L)).thenReturn(conSuspension());

    assertThatThrownBy(() -> service.renovarPrestamo(requestRenovacion(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("suspensión");
  }

  @Test
  @DisplayName("IT3-U-REN-08: al llegar al máximo de renovaciones se rechaza otra")
  void renovar_limiteRenovaciones() {
    Prestamo p = prestamoActivo(EstadoPrestamo.RENOVADO, LocalDate.now().plusDays(2));
    p.setRenovaciones(2);
    escenarioRenovacion(p);

    assertThatThrownBy(() -> service.renovarPrestamo(requestRenovacion(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("límite de renovaciones");
    assertThat(p.getRenovaciones()).isEqualTo(2);
  }

  @Test
  @DisplayName("IT3-U-REN-09: renovar un préstamo ya RENOVADO (con cupo) sigue funcionando")
  void renovar_segundaRenovacion() {
    Prestamo p = prestamoActivo(EstadoPrestamo.RENOVADO, LocalDate.now().plusDays(2));
    p.setRenovaciones(1);
    escenarioRenovacion(p);

    service.renovarPrestamo(requestRenovacion(), 1L, roles("ROLE_ESTUDIANTE"));

    assertThat(p.getRenovaciones()).isEqualTo(2);
  }

  @Test
  @DisplayName("IT3-U-REN-10: renovar un préstamo inexistente lanza 'no encontrado'")
  void renovar_inexistente() {
    when(prestamoRepository.findById(100L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.renovarPrestamo(requestRenovacion(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // ===================== TAREAS PROGRAMADAS =====================

  @Test
  @DisplayName("IT3-U-JOB-01: la tarea de vencidos marca como VENCIDO los préstamos ACTIVO/RENOVADO atrasados")
  void job_marcaVencidos() {
    Prestamo a = prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().minusDays(2));
    Prestamo b = prestamoActivo(EstadoPrestamo.RENOVADO, LocalDate.now().minusDays(1));
    when(prestamoRepository.findPrestamosVencidos(any(LocalDate.class))).thenReturn(List.of(a, b));

    service.procesarPrestamosVencidos();

    assertThat(a.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.VENCIDO);
    assertThat(b.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.VENCIDO);
    verify(prestamoRepository, org.mockito.Mockito.times(2)).save(any(Prestamo.class));
  }

  @Test
  @DisplayName("IT3-U-JOB-02: la tarea de vencidos ignora préstamos que ya no están activos")
  void job_ignoraOtrosEstados() {
    Prestamo devuelto = prestamoActivo(EstadoPrestamo.DEVUELTO, LocalDate.now().minusDays(2));
    when(prestamoRepository.findPrestamosVencidos(any(LocalDate.class))).thenReturn(List.of(devuelto));

    service.procesarPrestamosVencidos();

    assertThat(devuelto.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.DEVUELTO);
    verify(prestamoRepository, never()).save(any());
  }

  // ===================== CONSULTAS =====================

  @Test
  @DisplayName("IT3-U-QRY-01: findById inexistente lanza 'no encontrado'")
  void findById_inexistente() {
    when(prestamoRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(9L)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-QRY-02: findByUsuario valida que el usuario exista")
  void findByUsuario_inexistente() {
    when(usuarioRepository.existsById(9L)).thenReturn(false);

    assertThatThrownBy(() -> service.findByUsuario(9L)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-QRY-03: findByBiblioteca valida que la biblioteca exista")
  void findByBiblioteca_inexistente() {
    when(bibliotecaRepository.existsById(9L)).thenReturn(false);

    assertThatThrownBy(() -> service.findByBiblioteca(9L, null)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT3-U-QRY-04: el estado por CI de un usuario inexistente es 'sin pendientes' con ceros")
  void estadoPorCi_usuarioInexistente() {
    when(usuarioRepository.findByPersona_Ci("999")).thenReturn(Optional.empty());

    EstadoPrestamoUsuarioDTO dto = service.obtenerEstadoPrestamosPorCi("999", 1L);

    assertThat(dto.usuarioId()).isNull();
    assertThat(dto.tienePrestamosPendientes()).isFalse();
    assertThat(dto.prestamosActivos()).isZero();
    assertThat(dto.prestamosVencidos()).isZero();
  }

  @Test
  @DisplayName("IT3-U-QRY-05: el estado por CI refleja préstamos activos, vencidos y renovados")
  void estadoPorCi_conPendientes() {
    when(usuarioRepository.findByPersona_Ci("1000001")).thenReturn(Optional.of(lector));
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(2L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(1L, 1L, EstadoPrestamo.VENCIDO))
        .thenReturn(1L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(1L, 1L, EstadoPrestamo.RENOVADO))
        .thenReturn(0L);

    EstadoPrestamoUsuarioDTO dto = service.obtenerEstadoPrestamosPorCi("1000001", 1L);

    assertThat(dto.usuarioId()).isEqualTo(1L);
    assertThat(dto.tienePrestamosPendientes()).isTrue();
    assertThat(dto.prestamosActivos()).isEqualTo(2);
    assertThat(dto.prestamosVencidos()).isEqualTo(1);
  }

  @Test
  @DisplayName("IT3-U-QRY-06: el estado por CI sin préstamos no tiene pendientes")
  void estadoPorCi_sinPendientes() {
    when(usuarioRepository.findByPersona_Ci("1000001")).thenReturn(Optional.of(lector));
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(0L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(eq(1L), eq(1L), any()))
        .thenReturn(0L);

    assertThat(service.obtenerEstadoPrestamosPorCi("1000001", 1L).tienePrestamosPendientes()).isFalse();
  }

  @Test
  @DisplayName("IT3-U-QRY-07: filtrar por usuario y estado usa la consulta combinada")
  void filtrar_usuarioYEstado() {
    FiltroPrestamoRequest f = new FiltroPrestamoRequest();
    f.setUsuarioId(1L);
    f.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
    when(prestamoRepository.findByUsuarioAndEstado(1L, EstadoPrestamo.ACTIVO))
        .thenReturn(List.of(prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(1))));

    assertThat(service.filtrar(f)).hasSize(1);
    verify(prestamoRepository, never()).findAll();
  }

  @Test
  @DisplayName("IT3-U-QRY-08: filtrar sin criterios devuelve todos los préstamos")
  void filtrar_sinCriterios() {
    when(prestamoRepository.findAll()).thenReturn(List.of(
        prestamoActivo(EstadoPrestamo.ACTIVO, LocalDate.now().plusDays(1)),
        prestamoActivo(EstadoPrestamo.DEVUELTO, LocalDate.now())));

    assertThat(service.filtrar(new FiltroPrestamoRequest())).hasSize(2);
  }

  @Test
  @DisplayName("IT3-U-QRY-09: filtrar por biblioteca y rango de fechas cubre el día completo")
  void filtrar_bibliotecaYFechas() {
    FiltroPrestamoRequest f = new FiltroPrestamoRequest();
    f.setBibliotecaId(1L);
    f.setFechaDesde(LocalDate.of(2026, 1, 1));
    f.setFechaHasta(LocalDate.of(2026, 1, 31));
    when(prestamoRepository.findByBibliotecaAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of());

    service.filtrar(f);

    verify(prestamoRepository).findByBibliotecaAndFechaBetween(1L,
        LocalDate.of(2026, 1, 1).atStartOfDay(),
        LocalDate.of(2026, 1, 31).atTime(23, 59, 59));
  }
}

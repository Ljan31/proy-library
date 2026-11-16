package com.proyecto.fhce.library.iteracion4.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.request.library.RazonCertificadoRequestDTO;
import com.proyecto.fhce.library.dto.request.library.SolicitudCertificadoRequest;
import com.proyecto.fhce.library.dto.response.library.RazonCertificadoResponseDTO;
import com.proyecto.fhce.library.dto.response.library.SolicitudCertificadoResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoSolicitud;
import com.proyecto.fhce.library.enums.RolEncargado;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.RazonCertificadoRepository;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.library.RazonCertificadoServiceImpl;
import com.proyecto.fhce.library.services.library.SolicitudCertificadoService;
import com.proyecto.fhce.library.services.notificaciones.NotificacionService;
import com.proyecto.fhce.library.support.TestData;

/** Iteración 4 · Solicitudes de certificado y razones de certificado. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 4 · Solicitudes y razones de certificado (unitaria)")
class SolicitudYRazonServiceTest {

  @Mock
  SolicitudCertificadoRepository solicitudRepository;
  @Mock
  BibliotecaRepository bibliotecaRepository;
  @Mock
  UserRepository usuarioRepository;
  @Mock
  RazonCertificadoRepository razonRepository;
  @Mock
  NotificacionService notificacionService;
  @Mock
  AuditoriaService auditoriaService;

  @InjectMocks
  SolicitudCertificadoService service;

  Biblioteca biblioteca;
  RazonCertificado razon;
  Usuario estudiante;
  Usuario bibliotecario;

  @BeforeEach
  void init() {
    biblioteca = TestData.biblioteca(1L, "Central", TipoBiblioteca.FACULTATIVA);
    razon = new RazonCertificado();
    razon.setIdRazon(3L);
    razon.setNombre("Titulación");
    razon.setBiblioteca(biblioteca);
    razon.setActivo(true);
    estudiante = TestData.usuario(1L, "estud");
    bibliotecario = TestData.usuario(2L, "biblio");
    lenient().when(solicitudRepository.save(any(SolicitudCertificado.class))).thenAnswer(i -> {
      SolicitudCertificado s = i.getArgument(0);
      if (s.getId() == null) {
        s.setId(20L);
      }
      return s;
    });
  }

  // ---------------- helpers ----------------
  private Collection<GrantedAuthority> roles(String... r) {
    return java.util.Arrays.stream(r).map(x -> (GrantedAuthority) new SimpleGrantedAuthority(x)).toList();
  }

  private SolicitudCertificadoRequest request() {
    SolicitudCertificadoRequest r = new SolicitudCertificadoRequest();
    r.setBibliotecaId(1L);
    r.setRazonCertificadoId(3L);
    r.setNombres("Juan");
    r.setApellidos("Perez Gomez");
    r.setCi("1000001");
    r.setMatricula("2020-123");
    r.setEmail("juan@correo.com");
    r.setTelefono("71234567");
    r.setDescripcion("Para titulación");
    return r;
  }

  private void escenarioCrear() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(solicitudRepository.existsByCiAndBiblioteca_IdBibliotecaAndRazonCertificado_IdRazonAndEstado("1000001",
        1L, 3L, EstadoSolicitud.PENDIENTE)).thenReturn(false);
    when(usuarioRepository.findByPersona_Ci("1000001")).thenReturn(Optional.of(estudiante));
  }

  private BibliotecaEncargado encargado(Usuario u, RolEncargado rol, boolean activo, LocalDateTime fin) {
    BibliotecaEncargado e = new BibliotecaEncargado();
    e.setUsuario(u);
    e.setBiblioteca(biblioteca);
    e.setRolEncargado(rol);
    e.setActivo(activo);
    e.setFechaFin(fin);
    return e;
  }

  private SolicitudCertificado solicitud(EstadoSolicitud estado) {
    SolicitudCertificado s = new SolicitudCertificado();
    s.setId(20L);
    s.setBiblioteca(biblioteca);
    s.setUsuario(estudiante);
    s.setCi("1000001");
    s.setNombres("Juan");
    s.setApellidos("Perez Gomez");
    s.setRazonCertificado(razon);
    s.setEstado(estado);
    s.setFechaSolicitud(LocalDateTime.now());
    return s;
  }

  // ===================== CREAR SOLICITUD =====================

  @Test
  @DisplayName("IT4-U-SOL-01: una solicitud nueva queda PENDIENTE y se vincula al usuario registrado por CI")
  void crear_ok() {
    escenarioCrear();

    SolicitudCertificadoResponse r = service.crearSolicitud(request(), 1L, roles("ROLE_ESTUDIANTE"));

    assertThat(r.getEstado()).isEqualTo("PENDIENTE");
    assertThat(r.getUsuarioId()).isEqualTo(1L);
    assertThat(r.getRazonNombre()).isEqualTo("Titulación");
    assertThat(r.getBibliotecaNombre()).isEqualTo("Central");
    assertThat(r.getCi()).isEqualTo("1000001");
    verify(auditoriaService).registrar(eq("CREAR_SOLICITUD_CERTIFICADO"), any(), any(), any(), any());
  }

  @Test
  @DisplayName("IT4-U-SOL-02: una persona sin cuenta también puede solicitar (usuario queda nulo)")
  void crear_sinCuenta() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(solicitudRepository.existsByCiAndBiblioteca_IdBibliotecaAndRazonCertificado_IdRazonAndEstado(any(), any(),
        any(), any())).thenReturn(false);
    when(usuarioRepository.findByPersona_Ci("1000001")).thenReturn(Optional.empty());

    SolicitudCertificadoResponse r = service.crearSolicitud(request(), null, null);

    assertThat(r.getUsuarioId()).isNull();
    assertThat(r.getEstado()).isEqualTo("PENDIENTE");
  }

  @Test
  @DisplayName("IT4-U-SOL-03: no se permiten dos solicitudes PENDIENTES del mismo CI, biblioteca y razón")
  void crear_duplicadaPendiente() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(solicitudRepository.existsByCiAndBiblioteca_IdBibliotecaAndRazonCertificado_IdRazonAndEstado("1000001",
        1L, 3L, EstadoSolicitud.PENDIENTE)).thenReturn(true);

    assertThatThrownBy(() -> service.crearSolicitud(request(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("solicitud pendiente");
    verify(solicitudRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT4-U-SOL-04: una razón de otra biblioteca no se puede usar")
  void crear_razonDeOtraBiblioteca() {
    razon.setBiblioteca(TestData.biblioteca(2L, "Otra", TipoBiblioteca.FACULTATIVA));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));

    assertThatThrownBy(() -> service.crearSolicitud(request(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("no está disponible");
  }

  @Test
  @DisplayName("IT4-U-SOL-05: biblioteca o razón inexistente lanza 'no encontrada'")
  void crear_inexistentes() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.crearSolicitud(request(), 1L, null))
        .isInstanceOf(ResourceNotFoundException.class);

    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(razonRepository.findById(3L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.crearSolicitud(request(), 1L, null))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT4-U-SOL-06: se notifica solo a los encargados activos (PRINCIPAL/AUXILIAR) y vigentes")
  void crear_notificaSoloEncargadosActivos() {
    escenarioCrear();
    Usuario u3 = TestData.usuario(3L, "aux");
    Usuario u4 = TestData.usuario(4L, "inactivo");
    Usuario u5 = TestData.usuario(5L, "terminado");
    biblioteca.setEncargados(List.of(
        encargado(bibliotecario, RolEncargado.PRINCIPAL, true, null),
        encargado(u3, RolEncargado.AUXILIAR, true, null),
        encargado(u4, RolEncargado.PRINCIPAL, false, null),
        encargado(u5, RolEncargado.PRINCIPAL, true, LocalDateTime.now().minusDays(1))));

    service.crearSolicitud(request(), 1L, roles("ROLE_ESTUDIANTE"));

    ArgumentCaptor<CrearNotificacionRequest> cap = ArgumentCaptor.forClass(CrearNotificacionRequest.class);
    verify(notificacionService, times(2)).crear(cap.capture());
    assertThat(cap.getAllValues()).extracting(CrearNotificacionRequest::idUsuario).containsExactlyInAnyOrder(2L, 3L);
    assertThat(cap.getAllValues().get(0).asunto()).contains("Certificado No Deuda");
  }

  @Test
  @DisplayName("IT4-U-SOL-07: un fallo al notificar no impide registrar la solicitud")
  void crear_falloNotificacion() {
    escenarioCrear();
    biblioteca.setEncargados(List.of(encargado(bibliotecario, RolEncargado.PRINCIPAL, true, null)));
    org.mockito.Mockito.doThrow(new RuntimeException("SMTP caído")).when(notificacionService).crear(any());

    SolicitudCertificadoResponse r = service.crearSolicitud(request(), 1L, roles("ROLE_ESTUDIANTE"));

    assertThat(r.getEstado()).isEqualTo("PENDIENTE");
  }

  /**
   * DEF-18 (defecto detectado): en {@code enviarNotificacionesEncargados} el
   * {@code String.format} recibe los argumentos en distinto orden que los
   * marcadores: "Razón" muestra el id del solicitante, "Biblioteca" muestra la
   * razón y "Descripción" muestra el nombre de la biblioteca.
   */
  @Test
  // @Disabled("DEF-18: el mensaje de notificación a encargados mezcla
  // Razón/Biblioteca/Descripción")
  @DisplayName("IT4-U-SOL-08: el mensaje a los encargados muestra razón, biblioteca y descripción correctas")
  void crear_mensajeNotificacionCorrecto() {
    escenarioCrear();
    biblioteca.setEncargados(List.of(encargado(bibliotecario, RolEncargado.PRINCIPAL, true, null)));

    service.crearSolicitud(request(), 1L, roles("ROLE_ESTUDIANTE"));

    ArgumentCaptor<CrearNotificacionRequest> cap = ArgumentCaptor.forClass(CrearNotificacionRequest.class);
    verify(notificacionService).crear(cap.capture());
    String msg = cap.getValue().mensaje();
    assertThat(msg).contains("Razón: Titulación").contains("Biblioteca: Central")
        .contains("Descripción: Para titulación");
  }

  /**
   * DEF-17 (defecto detectado): {@code crearSolicitud} no comprueba que la
   * razón esté ACTIVA; se pueden crear solicitudes con razones desactivadas.
   */
  @Test
  // @Disabled("DEF-17: se aceptan solicitudes con razones de certificado
  // inactivas")
  @DisplayName("IT4-U-SOL-09: una razón inactiva no puede usarse en una solicitud")
  void crear_razonInactiva() {
    razon.setActivo(false);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));

    assertThatThrownBy(() -> service.crearSolicitud(request(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class);
  }

  // ===================== APROBAR / RECHAZAR =====================

  @Test
  @DisplayName("IT4-U-SOL-10: aprobar deja la solicitud APROBADA con responsable, fecha y observación")
  void aprobar_ok() {
    SolicitudCertificado s = solicitud(EstadoSolicitud.PENDIENTE);
    when(solicitudRepository.findById(20L)).thenReturn(Optional.of(s));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));

    SolicitudCertificadoResponse r = service.aprobarSolicitud(20L, "Sin deudas", 2L, roles("ROLE_BIBLIOTECARIO"));

    assertThat(r.getEstado()).isEqualTo("APROBADA");
    assertThat(s.getAtendidoPor()).isSameAs(bibliotecario);
    assertThat(s.getFechaRespuesta()).isNotNull();
    assertThat(s.getObservacionRespuesta()).isEqualTo("Sin deudas");
    assertThat(r.getAtendidoPorId()).isEqualTo(2L);
    verify(auditoriaService).registrar(eq("APROBAR_SOLICITUD_CERTIFICADO"), any(), any(), any(), any());
  }

  @Test
  @DisplayName("IT4-U-SOL-11: rechazar deja la solicitud RECHAZADA con la observación")
  void rechazar_ok() {
    SolicitudCertificado s = solicitud(EstadoSolicitud.PENDIENTE);
    when(solicitudRepository.findById(20L)).thenReturn(Optional.of(s));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));

    SolicitudCertificadoResponse r = service.rechazarSolicitud(20L, "Tiene deuda", 2L, roles("ROLE_BIBLIOTECARIO"));

    assertThat(r.getEstado()).isEqualTo("RECHAZADA");
    assertThat(s.getObservacionRespuesta()).isEqualTo("Tiene deuda");
    verify(auditoriaService).registrar(eq("RECHAZAR_SOLICITUD_CERTIFICADO"), any(), any(), any(), any());
  }

  @Test
  @DisplayName("IT4-U-SOL-12: aprobar/rechazar una solicitud o usuario inexistente lanza 'no encontrado'")
  void aprobar_inexistentes() {
    when(solicitudRepository.findById(20L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.aprobarSolicitud(20L, null, 2L, roles("ROLE_ADMIN")))
        .isInstanceOf(ResourceNotFoundException.class);

    when(solicitudRepository.findById(20L)).thenReturn(Optional.of(solicitud(EstadoSolicitud.PENDIENTE)));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.rechazarSolicitud(20L, null, 2L, roles("ROLE_ADMIN")))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  /**
   * DEF-14 (defecto detectado): aprobar/rechazar no comprueban el estado actual.
   * Una solicitud ya RECHAZADA puede aprobarse y una APROBADA (que quizá ya
   * tiene certificado) puede rechazarse, dejando datos inconsistentes.
   * Solo una solicitud PENDIENTE debería poder resolverse.
   */
  @Test
  // @Disabled("DEF-14: se puede aprobar/rechazar una solicitud que ya fue
  // resuelta")
  @DisplayName("IT4-U-SOL-13: solo una solicitud PENDIENTE puede aprobarse o rechazarse")
  void resolver_soloPendientes() {
    SolicitudCertificado rechazada = solicitud(EstadoSolicitud.RECHAZADA);
    when(solicitudRepository.findById(20L)).thenReturn(Optional.of(rechazada));

    assertThatThrownBy(() -> service.aprobarSolicitud(20L, null, 2L, roles("ROLE_BIBLIOTECARIO")))
        .isInstanceOf(BusinessException.class);
    assertThat(rechazada.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADA);
  }

  // ===================== CONSULTAS =====================

  @Test
  @DisplayName("IT4-U-SOL-14: listar por biblioteca con y sin filtro de estado")
  void listarPorBiblioteca() {
    when(solicitudRepository.findByBiblioteca_IdBibliotecaAndEstado(1L, EstadoSolicitud.PENDIENTE))
        .thenReturn(List.of(solicitud(EstadoSolicitud.PENDIENTE)));
    when(solicitudRepository.findByBiblioteca_IdBiblioteca(1L)).thenReturn(
        List.of(solicitud(EstadoSolicitud.PENDIENTE), solicitud(EstadoSolicitud.APROBADA)));

    assertThat(service.listarPorBiblioteca(1L, EstadoSolicitud.PENDIENTE, 2L, roles("ROLE_ADMIN"))).hasSize(1);
    assertThat(service.listarPorBiblioteca(1L, null, 2L, roles("ROLE_ADMIN"))).hasSize(2);
  }

  @Test
  @DisplayName("IT4-U-SOL-15: obtener por id inexistente lanza 'no encontrada'")
  void obtenerPorId_inexistente() {
    when(solicitudRepository.findById(20L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.obtenerPorId(20L, 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT4-U-SOL-16: 'mis solicitudes' exige un usuario autenticado y pagina los resultados")
  void misSolicitudes() {
    assertThatThrownBy(() -> service.obtenerMisSolicitudes(null, PageRequest.of(0, 10)))
        .isInstanceOf(BusinessException.class);

    when(solicitudRepository.findByUsuario_IdUsuario(eq(1L), any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(solicitud(EstadoSolicitud.PENDIENTE))));
    assertThat(service.obtenerMisSolicitudes(1L, PageRequest.of(0, 10)).getContent()).hasSize(1);
  }

  // ===================== RAZONES DE CERTIFICADO =====================

  @Test
  @DisplayName("IT4-U-RAZ-01: crear una razón guarda sus requisitos como JSON y audita")
  void razon_crear() {
    RazonCertificadoServiceImpl razones = new RazonCertificadoServiceImpl(razonRepository, bibliotecaRepository,
        auditoriaService);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(razonRepository.save(any(RazonCertificado.class))).thenAnswer(i -> {
      RazonCertificado r = i.getArgument(0);
      r.setIdRazon(8L);
      return r;
    });
    RazonCertificadoRequestDTO dto = new RazonCertificadoRequestDTO();
    dto.setBibliotecaId(1L);
    dto.setNombre("Beca");
    dto.setDescripcion("Para trámite de beca");
    dto.setRequisitos(List.of("CI", "Matrícula"));
    dto.setActivo(true);

    RazonCertificadoResponseDTO r = razones.crear(dto);

    assertThat(r.getIdRazon()).isEqualTo(8L);
    assertThat(r.getNombre()).isEqualTo("Beca");
    assertThat(r.getBibliotecaId()).isEqualTo(1L);
    assertThat(r.getActivo()).isTrue();
    ArgumentCaptor<RazonCertificado> cap = ArgumentCaptor.forClass(RazonCertificado.class);
    verify(razonRepository).save(cap.capture());
    assertThat(cap.getValue().getRequisitos()).isEqualTo("[\"CI\",\"Matrícula\"]");
    verify(auditoriaService).registrar(eq("CREAR_RAZON_CERTIFICADO"), any(), eq(8L), any(), any());
  }

  @Test
  @DisplayName("IT4-U-RAZ-02: actualizar modifica nombre, descripción, estado y requisitos")
  void razon_actualizar() {
    RazonCertificadoServiceImpl razones = new RazonCertificadoServiceImpl(razonRepository, bibliotecaRepository,
        auditoriaService);
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(razonRepository.save(any(RazonCertificado.class))).thenAnswer(i -> i.getArgument(0));
    RazonCertificadoRequestDTO dto = new RazonCertificadoRequestDTO();
    dto.setNombre("Titulación (nuevo)");
    dto.setDescripcion("desc");
    dto.setRequisitos(List.of("CI"));
    dto.setActivo(false);

    RazonCertificadoResponseDTO r = razones.actualizar(3L, dto);

    assertThat(r.getNombre()).isEqualTo("Titulación (nuevo)");
    assertThat(r.getActivo()).isFalse();
    assertThat(razon.getRequisitos()).isEqualTo("[\"CI\"]");
  }

  @Test
  @DisplayName("IT4-U-RAZ-03: eliminar borra la razón y audita")
  void razon_eliminar() {
    RazonCertificadoServiceImpl razones = new RazonCertificadoServiceImpl(razonRepository, bibliotecaRepository,
        auditoriaService);
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));

    razones.eliminar(3L);

    verify(razonRepository).delete(razon);
    verify(auditoriaService).registrar(eq("ELIMINAR_RAZON_CERTIFICADO"), any(), eq(3L), any(), any());
  }

  @Test
  @DisplayName("IT4-U-RAZ-04: listar por biblioteca devuelve solo las de esa biblioteca")
  void razon_listarPorBiblioteca() {
    RazonCertificadoServiceImpl razones = new RazonCertificadoServiceImpl(razonRepository, bibliotecaRepository,
        auditoriaService);
    when(razonRepository.findByBiblioteca_IdBiblioteca(1L)).thenReturn(List.of(razon));

    assertThat(razones.listarPorBiblioteca(1L)).hasSize(1);
  }

  /**
   * DEF-13 (defecto detectado): el servicio de razones lanza
   * {@code RuntimeException} genérica cuando no encuentra el registro; el
   * manejador global la convierte en HTTP 500. Debe lanzar
   * {@code ResourceNotFoundException} (404), como el resto de los servicios.
   */
  @Test
  // @Disabled("DEF-13: RazonCertificadoServiceImpl lanza RuntimeException (500)
  // en vez de ResourceNotFoundException (404)")
  @DisplayName("IT4-U-RAZ-05: razón o biblioteca inexistente lanza ResourceNotFoundException")
  void razon_inexistente() {
    RazonCertificadoServiceImpl razones = new RazonCertificadoServiceImpl(razonRepository, bibliotecaRepository,
        auditoriaService);
    when(razonRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> razones.obtenerPorId(99L)).isInstanceOf(ResourceNotFoundException.class);
  }
}

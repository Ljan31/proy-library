package com.proyecto.fhce.library.iteracion4.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
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

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.EstadoSolicitud;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.CertificadoNoDeudaRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.RazonCertificadoRepository;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.DocxToPdfConverter;
import com.proyecto.fhce.library.services.library.CertificadoDocxService;
import com.proyecto.fhce.library.services.library.FormatoCertificadoService;
import com.proyecto.fhce.library.services.loads.CertificadoNoDeudaServiceImpl;
import com.proyecto.fhce.library.services.notificaciones.NotificacionService;
import com.proyecto.fhce.library.support.TestData;

/**
 * Iteración 4 · Certificados de No Deuda: emisión, validación pública,
 * anulación, vencimiento y reglas de acceso por rol/biblioteca.
 * (La generación del PDF/DOCX queda fuera: depende de plantillas y LibreOffice.)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 4 · CertificadoNoDeudaServiceImpl (unitaria)")
class CertificadoNoDeudaServiceImplTest {

  @Mock
  CertificadoNoDeudaRepository certificadoRepository;
  @Mock
  UserRepository usuarioRepository;
  @Mock
  PrestamoRepository prestamoRepository;
  @Mock
  BibliotecaRepository bibliotecaRepository;
  @Mock
  NotificacionService notificacionService;
  @Mock
  CertificadoDocxService certificadoDocxService;
  @Mock
  DocxToPdfConverter pdfConverter;
  @Mock
  SolicitudCertificadoRepository solicitudRepository;
  @Mock
  RazonCertificadoRepository razonRepository;
  @Mock
  FormatoCertificadoService formatoService;
  @Mock
  AuditoriaService auditoriaService;

  @InjectMocks
  CertificadoNoDeudaServiceImpl service;

  Biblioteca biblioteca;
  Usuario admin;
  Usuario bibliotecario;
  Usuario estudiante;
  RazonCertificado razon;

  @BeforeEach
  void init() {
    biblioteca = TestData.biblioteca(1L, "Central", TipoBiblioteca.FACULTATIVA);
    admin = TestData.usuario(9L, "admin");
    bibliotecario = TestData.usuario(2L, "biblio");
    estudiante = TestData.usuario(1L, "estud");
    razon = new RazonCertificado();
    razon.setIdRazon(3L);
    razon.setNombre("Titulación");
    razon.setBiblioteca(biblioteca);
    lenient().when(certificadoRepository.save(any(CertificadoNoDeuda.class))).thenAnswer(i -> i.getArgument(0));
    lenient().when(solicitudRepository.save(any(SolicitudCertificado.class))).thenAnswer(i -> i.getArgument(0));
  }

  // ---------------- helpers ----------------
  private Collection<GrantedAuthority> roles(String... r) {
    return java.util.Arrays.stream(r).map(x -> (GrantedAuthority) new SimpleGrantedAuthority(x)).toList();
  }

  private void encargado(Usuario u) {
    BibliotecaEncargado e = new BibliotecaEncargado();
    e.setUsuario(u);
    e.setBiblioteca(biblioteca);
    e.setActivo(true);
    biblioteca.setEncargados(List.of(e));
  }

  private CertificadoRequest requestDirecto() {
    CertificadoRequest r = new CertificadoRequest();
    r.setBibliotecaId(1L);
    r.setUsuarioId(1L);
    r.setRazonCertificadoId(3L);
    return r;
  }

  private void sinDeudas() {
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(0L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(1L, 1L, EstadoPrestamo.VENCIDO))
        .thenReturn(0L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(1L, 1L, EstadoPrestamo.RENOVADO))
        .thenReturn(0L);
  }

  /** Stubs para una generación directa (sin solicitud) válida de un usuario registrado. */
  private void escenarioDirectoValido() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    sinDeudas();
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

  private CertificadoNoDeuda certificado(EstadoCertificado estado) {
    CertificadoNoDeuda c = new CertificadoNoDeuda();
    c.setIdCertificado(50L);
    c.setSolicitud(solicitud(EstadoSolicitud.APROBADA));
    c.setBiblioteca(biblioteca);
    c.setEstadoCertificado(estado);
    c.setFechaEmision(LocalDateTime.now().minusDays(1));
    c.setCodigoVerificacion("COD-123");
    return c;
  }

  // ===================== GENERAR =====================

  @Test
  @DisplayName("IT4-U-CER-01: el admin genera un certificado directo: solicitud APROBADA + certificado VIGENTE")
  void generar_directoAdmin() {
    escenarioDirectoValido();
    CertificadoRequest req = requestDirecto();
    req.setDiasValidez(30);

    CertificadoResponse r = service.generar(req, 9L, roles("ROLE_ADMIN"));

    assertThat(r.getEstadoCertificado()).isEqualTo(EstadoCertificado.VIGENTE);
    assertThat(r.getBibliotecaId()).isEqualTo(1L);
    assertThat(r.getFechaVencimiento()).isAfter(LocalDateTime.now().plusDays(29));
    assertThat(r.getFechaVencimiento()).isBefore(LocalDateTime.now().plusDays(31));
    ArgumentCaptor<SolicitudCertificado> cap = ArgumentCaptor.forClass(SolicitudCertificado.class);
    verify(solicitudRepository).save(cap.capture());
    assertThat(cap.getValue().getEstado()).isEqualTo(EstadoSolicitud.APROBADA);
    assertThat(cap.getValue().getCi()).isEqualTo("1000001");
    assertThat(cap.getValue().getAtendidoPor()).isSameAs(admin);
    verify(auditoriaService).registrar(eq("GENERAR_CERTIFICADO"), eq("certificados_sin_deuda"), any(), any(),
        any());
  }

  @Test
  @DisplayName("IT4-U-CER-02: sin días de validez el certificado no vence")
  void generar_sinVencimiento() {
    escenarioDirectoValido();

    CertificadoResponse r = service.generar(requestDirecto(), 9L, roles("ROLE_ADMIN"));

    assertThat(r.getFechaVencimiento()).isNull();
  }

  @Test
  @DisplayName("IT4-U-CER-03: a partir de una solicitud APROBADA se usa esa solicitud (no se crea otra)")
  void generar_desdeSolicitud() {
    SolicitudCertificado s = solicitud(EstadoSolicitud.APROBADA);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(solicitudRepository.findById(20L)).thenReturn(Optional.of(s));
    when(certificadoRepository.existsBySolicitud_Id(20L)).thenReturn(false);
    sinDeudas();
    CertificadoRequest req = new CertificadoRequest();
    req.setBibliotecaId(1L);
    req.setSolicitudId(20L);

    CertificadoResponse r = service.generar(req, 9L, roles("ROLE_ADMIN"));

    assertThat(r.getSolicitudId()).isEqualTo(20L);
    verify(solicitudRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT4-U-CER-04: una solicitud PENDIENTE o RECHAZADA no genera certificado")
  void generar_solicitudNoAprobada() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    CertificadoRequest req = new CertificadoRequest();
    req.setBibliotecaId(1L);
    req.setSolicitudId(20L);
    for (EstadoSolicitud e : new EstadoSolicitud[] { EstadoSolicitud.PENDIENTE, EstadoSolicitud.RECHAZADA }) {
      when(solicitudRepository.findById(20L)).thenReturn(Optional.of(solicitud(e)));

      assertThatThrownBy(() -> service.generar(req, 9L, roles("ROLE_ADMIN")))
          .as(e.name()).isInstanceOf(BusinessException.class).hasMessageContaining("aprobada");
    }
    verify(certificadoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT4-U-CER-05: la solicitud debe pertenecer a la biblioteca indicada")
  void generar_solicitudOtraBiblioteca() {
    SolicitudCertificado s = solicitud(EstadoSolicitud.APROBADA);
    s.setBiblioteca(TestData.biblioteca(2L, "Otra", TipoBiblioteca.FACULTATIVA));
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(solicitudRepository.findById(20L)).thenReturn(Optional.of(s));
    CertificadoRequest req = new CertificadoRequest();
    req.setBibliotecaId(1L);
    req.setSolicitudId(20L);

    assertThatThrownBy(() -> service.generar(req, 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("no pertenece");
  }

  @Test
  @DisplayName("IT4-U-CER-06: una solicitud solo puede tener un certificado")
  void generar_solicitudYaTieneCertificado() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(solicitudRepository.findById(20L)).thenReturn(Optional.of(solicitud(EstadoSolicitud.APROBADA)));
    when(certificadoRepository.existsBySolicitud_Id(20L)).thenReturn(true);
    CertificadoRequest req = new CertificadoRequest();
    req.setBibliotecaId(1L);
    req.setSolicitudId(20L);

    assertThatThrownBy(() -> service.generar(req, 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("ya tiene un certificado");
  }

  @Test
  @DisplayName("IT4-U-CER-07: solicitud o biblioteca inexistente lanza 'no encontrada'")
  void generar_inexistentes() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.empty());
    CertificadoRequest req = requestDirecto();

    assertThatThrownBy(() -> service.generar(req, 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Biblioteca");

    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(solicitudRepository.findById(20L)).thenReturn(Optional.empty());
    req.setSolicitudId(20L);

    assertThatThrownBy(() -> service.generar(req, 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Solicitud");
  }

  @Test
  @DisplayName("IT4-U-CER-08: un bibliotecario que no es encargado de la biblioteca no puede generar")
  void generar_bibliotecarioNoEncargado() {
    biblioteca.setEncargados(List.of());
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));

    assertThatThrownBy(() -> service.generar(requestDirecto(), 2L, roles("ROLE_BIBLIOTECARIO")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("a su cargo");
    verify(certificadoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT4-U-CER-09: el bibliotecario encargado sí puede generar")
  void generar_bibliotecarioEncargado() {
    encargado(bibliotecario);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    sinDeudas();

    CertificadoResponse r = service.generar(requestDirecto(), 2L, roles("ROLE_BIBLIOTECARIO"));

    assertThat(r.getEstadoCertificado()).isEqualTo(EstadoCertificado.VIGENTE);
    assertThat(r.getBibliotecario().getId_usuario()).isEqualTo(2L);
  }

  @Test
  @DisplayName("IT4-U-CER-10: un estudiante no puede generar sin una solicitud previa")
  void generar_estudianteSinSolicitud() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));

    assertThatThrownBy(() -> service.generar(requestDirecto(), 1L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("No tiene permisos");
  }

  @Test
  @DisplayName("IT4-U-CER-11: sin usuario registrado se exigen nombres, apellidos y CI")
  void generar_sinDatosDePersona() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    CertificadoRequest req = new CertificadoRequest();
    req.setBibliotecaId(1L);
    req.setRazonCertificadoId(3L);
    req.setNombres("Ana");

    assertThatThrownBy(() -> service.generar(req, 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("nombres, apellidos y CI");
  }

  @Test
  @DisplayName("IT4-U-CER-12: una razón de certificado inexistente lanza 'no encontrada'")
  void generar_razonInexistente() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
    when(razonRepository.findById(3L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.generar(requestDirecto(), 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Razón");
  }

  // ---- no deuda: préstamos ----

  @Test
  @DisplayName("IT4-U-CER-13: un usuario con préstamos ACTIVOS no obtiene certificado de no deuda")
  void generar_conPrestamosActivos() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(2L);

    assertThatThrownBy(() -> service.generar(requestDirecto(), 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("2 préstamo(s) activo(s)");
    verify(certificadoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT4-U-CER-14: tampoco con préstamos VENCIDOS")
  void generar_conPrestamosVencidos() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(0L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(1L, 1L, EstadoPrestamo.VENCIDO))
        .thenReturn(1L);

    assertThatThrownBy(() -> service.generar(requestDirecto(), 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("vencido");
  }

  @Test
  @DisplayName("IT4-U-CER-15: tampoco con préstamos RENOVADOS (aún sin devolver)")
  void generar_conPrestamosRenovados() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(prestamoRepository.countPrestamosActivosByUsuarioAndBiblioteca(1L, 1L)).thenReturn(0L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(1L, 1L, EstadoPrestamo.VENCIDO))
        .thenReturn(0L);
    when(prestamoRepository.countPrestamosConEstadoByUsuarioAndBiblioteca(1L, 1L, EstadoPrestamo.RENOVADO))
        .thenReturn(1L);

    assertThatThrownBy(() -> service.generar(requestDirecto(), 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("renovación pendiente");
  }

  @Test
  @DisplayName("IT4-U-CER-16: para una persona sin usuario la verificación de deudas se hace por CI")
  void generar_porCi_conDeuda() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(prestamoRepository.countPrestamosConEstadoByCiAndBiblioteca("555", 1L, EstadoPrestamo.ACTIVO))
        .thenReturn(0L);
    when(prestamoRepository.countPrestamosConEstadoByCiAndBiblioteca("555", 1L, EstadoPrestamo.VENCIDO))
        .thenReturn(2L);
    CertificadoRequest req = new CertificadoRequest();
    req.setBibliotecaId(1L);
    req.setRazonCertificadoId(3L);
    req.setNombres("Ana");
    req.setApellidos("Lopez");
    req.setCi("555");

    assertThatThrownBy(() -> service.generar(req, 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("La persona tiene 2");
  }

  @Test
  @DisplayName("IT4-U-CER-17: una persona sin usuario y sin deudas obtiene el certificado")
  void generar_porCi_sinDeuda() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    when(usuarioRepository.findById(9L)).thenReturn(Optional.of(admin));
    when(razonRepository.findById(3L)).thenReturn(Optional.of(razon));
    when(prestamoRepository.countPrestamosConEstadoByCiAndBiblioteca(eq("555"), eq(1L), any())).thenReturn(0L);
    CertificadoRequest req = new CertificadoRequest();
    req.setBibliotecaId(1L);
    req.setRazonCertificadoId(3L);
    req.setNombres("Ana");
    req.setApellidos("Lopez");
    req.setCi("555");

    CertificadoResponse r = service.generar(req, 9L, roles("ROLE_ADMIN"));

    assertThat(r.getUsuario()).isNull();
    assertThat(r.getEstadoCertificado()).isEqualTo(EstadoCertificado.VIGENTE);
  }

  // ===================== VALIDAR (público) =====================

  @Test
  @DisplayName("IT4-U-VAL-01: un código inexistente no es válido")
  void validar_noExiste() {
    when(certificadoRepository.findByCodigoVerificacion("X")).thenReturn(Optional.empty());

    ValidacionCertificadoResponse r = service.validar("X");

    assertThat(r.getValido()).isFalse();
    assertThat(r.getMensaje()).contains("no encontrado");
    assertThat(r.getCertificado()).isNull();
  }

  @Test
  @DisplayName("IT4-U-VAL-02: un certificado VIGENTE sin fecha de vencimiento es válido")
  void validar_vigente() {
    when(certificadoRepository.findByCodigoVerificacion("COD-123"))
        .thenReturn(Optional.of(certificado(EstadoCertificado.VIGENTE)));

    ValidacionCertificadoResponse r = service.validar("COD-123");

    assertThat(r.getValido()).isTrue();
    assertThat(r.getCertificado().getCodigo_verificacion()).isEqualTo("COD-123");
  }

  @Test
  @DisplayName("IT4-U-VAL-03: un certificado ANULADO o VENCIDO no es válido")
  void validar_noVigente() {
    for (EstadoCertificado e : new EstadoCertificado[] { EstadoCertificado.ANULADO, EstadoCertificado.VENCIDO }) {
      when(certificadoRepository.findByCodigoVerificacion("COD-123")).thenReturn(Optional.of(certificado(e)));

      ValidacionCertificadoResponse r = service.validar("COD-123");

      assertThat(r.getValido()).as(e.name()).isFalse();
      assertThat(r.getMensaje()).contains("no está vigente");
    }
  }

  @Test
  @DisplayName("IT4-U-VAL-04: un certificado VIGENTE con fecha de vencimiento pasada se reporta vencido")
  void validar_vencidoPorFecha() {
    CertificadoNoDeuda c = certificado(EstadoCertificado.VIGENTE);
    c.setFechaVencimiento(LocalDateTime.now().minusMinutes(1));
    when(certificadoRepository.findByCodigoVerificacion("COD-123")).thenReturn(Optional.of(c));

    ValidacionCertificadoResponse r = service.validar("COD-123");

    assertThat(r.getValido()).isFalse();
    assertThat(r.getMensaje()).contains("vencido");
  }

  @Test
  @DisplayName("IT4-U-VAL-05: un certificado VIGENTE con fecha de vencimiento futura es válido")
  void validar_vigenteConFecha() {
    CertificadoNoDeuda c = certificado(EstadoCertificado.VIGENTE);
    c.setFechaVencimiento(LocalDateTime.now().plusDays(5));
    when(certificadoRepository.findByCodigoVerificacion("COD-123")).thenReturn(Optional.of(c));

    assertThat(service.validar("COD-123").getValido()).isTrue();
  }

  // ===================== VENCIMIENTO AUTOMÁTICO =====================

  @Test
  @DisplayName("IT4-U-VEN-01: la tarea de vencidos marca VENCIDO cada certificado vencido")
  void job_vencidos() {
    CertificadoNoDeuda a = certificado(EstadoCertificado.VIGENTE);
    CertificadoNoDeuda b = certificado(EstadoCertificado.VIGENTE);
    when(certificadoRepository.findCertificadosVencidos(any(LocalDateTime.class))).thenReturn(List.of(a, b));

    service.actualizarCertificadosVencidos();

    assertThat(a.getEstadoCertificado()).isEqualTo(EstadoCertificado.VENCIDO);
    assertThat(b.getEstadoCertificado()).isEqualTo(EstadoCertificado.VENCIDO);
    verify(certificadoRepository, org.mockito.Mockito.times(2)).save(any(CertificadoNoDeuda.class));
  }

  // ===================== ANULAR =====================

  @Test
  @DisplayName("IT4-U-ANU-01: el admin anula un certificado VIGENTE")
  void anular_admin() {
    CertificadoNoDeuda c = certificado(EstadoCertificado.VIGENTE);
    when(certificadoRepository.findById(50L)).thenReturn(Optional.of(c));

    CertificadoResponse r = service.anular(50L, 9L, roles("ROLE_ADMIN"));

    assertThat(r.getEstadoCertificado()).isEqualTo(EstadoCertificado.ANULADO);
    verify(auditoriaService).registrar(eq("ANULAR_CERTIFICADO"), eq("certificados_sin_deuda"), eq(50L), any(),
        any());
  }

  @Test
  @DisplayName("IT4-U-ANU-02: el bibliotecario encargado puede anular; uno ajeno no")
  void anular_encargadoVsAjeno() {
    CertificadoNoDeuda c = certificado(EstadoCertificado.VIGENTE);
    when(certificadoRepository.findById(50L)).thenReturn(Optional.of(c));
    biblioteca.setEncargados(List.of());

    assertThatThrownBy(() -> service.anular(50L, 2L, roles("ROLE_BIBLIOTECARIO")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("No autorizado");
    assertThat(c.getEstadoCertificado()).isEqualTo(EstadoCertificado.VIGENTE);

    encargado(bibliotecario);
    assertThat(service.anular(50L, 2L, roles("ROLE_BIBLIOTECARIO")).getEstadoCertificado())
        .isEqualTo(EstadoCertificado.ANULADO);
  }

  @Test
  @DisplayName("IT4-U-ANU-03: solo se anulan certificados VIGENTES (no VENCIDO ni ANULADO)")
  void anular_estadoInvalido() {
    for (EstadoCertificado e : new EstadoCertificado[] { EstadoCertificado.VENCIDO, EstadoCertificado.ANULADO }) {
      when(certificadoRepository.findById(50L)).thenReturn(Optional.of(certificado(e)));

      assertThatThrownBy(() -> service.anular(50L, 9L, roles("ROLE_ADMIN")))
          .as(e.name()).isInstanceOf(BusinessException.class).hasMessageContaining("VIGENTE");
    }
  }

  @Test
  @DisplayName("IT4-U-ANU-04: anular un certificado inexistente lanza 'no encontrado'")
  void anular_inexistente() {
    when(certificadoRepository.findById(50L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.anular(50L, 9L, roles("ROLE_ADMIN")))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // ===================== CONSULTAS Y ACCESO =====================

  @Test
  @DisplayName("IT4-U-QRY-01: un estudiante solo ve sus propios certificados")
  void findByUsuario_estudiante() {
    assertThatThrownBy(() -> service.findByUsuario(1L, 77L, null, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("No autorizado");

    when(certificadoRepository.findBySolicitud_Usuario_IdUsuario(1L))
        .thenReturn(List.of(certificado(EstadoCertificado.VIGENTE)));
    assertThat(service.findByUsuario(1L, 1L, null, roles("ROLE_ESTUDIANTE"))).hasSize(1);
  }

  @Test
  @DisplayName("IT4-U-QRY-02: el personal puede filtrar los certificados de un usuario por biblioteca")
  void findByUsuario_personalConFiltro() {
    when(certificadoRepository.findBySolicitud_Usuario_IdUsuarioAndBiblioteca_IdBiblioteca(1L, 1L))
        .thenReturn(List.of(certificado(EstadoCertificado.VIGENTE)));

    assertThat(service.findByUsuario(1L, 2L, 1L, roles("ROLE_BIBLIOTECARIO"))).hasSize(1);
    verify(certificadoRepository, never()).findBySolicitud_Usuario_IdUsuario(any());
  }

  @Test
  @DisplayName("IT4-U-QRY-03: acceso a un certificado: el dueño sí, otro estudiante no")
  void acceso_duenoVsTercero() {
    when(certificadoRepository.findById(50L)).thenReturn(Optional.of(certificado(EstadoCertificado.VIGENTE)));

    assertThat(service.findByIdConAutorizacion(50L, 1L, roles("ROLE_ESTUDIANTE")).getId_certificado())
        .isEqualTo(50L);
    assertThatThrownBy(() -> service.findByIdConAutorizacion(50L, 99L, roles("ROLE_ESTUDIANTE")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("No autorizado");
  }

  @Test
  @DisplayName("IT4-U-QRY-04: un bibliotecario de otra biblioteca no accede al certificado")
  void acceso_bibliotecarioAjeno() {
    when(certificadoRepository.findById(50L)).thenReturn(Optional.of(certificado(EstadoCertificado.VIGENTE)));
    biblioteca.setEncargados(List.of());

    assertThatThrownBy(() -> service.findByIdConAutorizacion(50L, 2L, roles("ROLE_BIBLIOTECARIO")))
        .isInstanceOf(BusinessException.class).hasMessageContaining("esta biblioteca");
  }

  @Test
  @DisplayName("IT4-U-QRY-05: certificado inexistente lanza 'no encontrado'")
  void acceso_inexistente() {
    when(certificadoRepository.findById(50L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(50L)).isInstanceOf(ResourceNotFoundException.class);
    assertThatThrownBy(() -> service.findByIdConAutorizacion(50L, 1L, roles("ROLE_ADMIN")))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("IT4-U-QRY-06: listar por biblioteca: el admin no necesita ser encargado; otro rol sí")
  void findByBiblioteca_permisos() {
    when(certificadoRepository.findByBibliotecaConFiltroEstado(1L, EstadoCertificado.VIGENTE))
        .thenReturn(List.of(certificado(EstadoCertificado.VIGENTE)));
    assertThat(service.findByBiblioteca(1L, EstadoCertificado.VIGENTE, 9L, roles("ROLE_ADMIN"))).hasSize(1);

    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(biblioteca));
    biblioteca.setEncargados(List.of());
    assertThatThrownBy(() -> service.findByBiblioteca(1L, null, 2L, roles("ROLE_BIBLIOTECARIO")))
        .isInstanceOf(BusinessException.class);

    encargado(bibliotecario);
    assertThat(service.findByBiblioteca(1L, EstadoCertificado.VIGENTE, 2L, roles("ROLE_BIBLIOTECARIO")))
        .hasSize(1);
  }
}

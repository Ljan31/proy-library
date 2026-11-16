package com.proyecto.fhce.library.iteracion3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.RolEncargado;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.repositories.BibliotecaEncargadoRepository;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.ConfiguracionPrestamoRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.SancionRepository;
import com.proyecto.fhce.library.support.IntegrationTestBase;

/**
 * Escenario común de la iteración 3: una biblioteca con configuración
 * (7 días, 2 renovaciones, 2 ejemplares a domicilio, multa Bs 2/día con tope
 * de 7 días, suspensión de 15 días), un bibliotecario encargado, dos
 * estudiantes y un libro del que se crean ejemplares por la API.
 */
public abstract class PrestamoIntegrationBase extends IntegrationTestBase {

  @Autowired
  protected BibliotecaRepository bibliotecaRepository;
  @Autowired
  protected BibliotecaEncargadoRepository encargadoRepository;
  @Autowired
  protected ConfiguracionPrestamoRepository configuracionRepository;
  @Autowired
  protected PrestamoRepository prestamoRepository;
  @Autowired
  protected EjemplarRepository ejemplarRepository;
  @Autowired
  protected SancionRepository sancionRepository;

  protected String tokenBiblio;
  protected String tokenOtroBiblio;
  protected String tokenEstudiante1;
  protected String tokenEstudiante2;

  protected Usuario biblio;
  protected Usuario otroBiblio;
  protected Usuario estudiante1;
  protected Usuario estudiante2;
  protected Long idBiblioteca;
  protected Long idEdicion;
  protected Long idConfig;
  private int contadorEjemplares = 0;

  @BeforeEach
  protected void escenarioBase() throws Exception {
    biblio = crearUsuario("biblio01", rolBibliotecario);
    otroBiblio = crearUsuario("biblio02", rolBibliotecario);
    estudiante1 = crearUsuario("estud01", rolEstudiante);
    estudiante2 = crearUsuario("estud02", rolEstudiante);

    Biblioteca b = nuevaBiblioteca("Biblioteca Central");
    idBiblioteca = b.getIdBiblioteca();

    BibliotecaEncargado enc = new BibliotecaEncargado();
    enc.setBiblioteca(b);
    enc.setUsuario(biblio);
    enc.setRolEncargado(RolEncargado.PRINCIPAL);
    enc.setFechaAsignacion(LocalDateTime.now());
    enc.setActivo(true);
    encargadoRepository.saveAndFlush(enc);

    ConfiguracionPrestamo c = new ConfiguracionPrestamo();
    c.setBiblioteca(b);
    c.setDiasPrestamoMax(7);
    c.setRenovacionesMax(2);
    c.setEjemplaresMaxDomicilio(2);
    c.setEjemplaresMaxSala(3);
    c.setMultaPorDia(new BigDecimal("2.00"));
    c.setMultaMaxDias(7);
    c.setDiasSuspension(15);
    c.setDiasReserva(2);
    idConfig = configuracionRepository.saveAndFlush(c).getIdConfig();
    flushYLimpiar();

    tokenBiblio = bearer("biblio01");
    tokenOtroBiblio = bearer("biblio02");
    tokenEstudiante1 = bearer("estud01");
    tokenEstudiante2 = bearer("estud02");

    // catálogo mínimo por la API
    long cat = idDe(mockMvc.perform(post("/api/categorias").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("nombre_categoria", "Literatura", "codigo_dewey", "800")))).andReturn(),
        "id_categoria");
    long autor = idDe(mockMvc.perform(post("/api/autores").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("nombre", "Julio Cortázar"))))
        .andReturn(), "idAutor");
    Map<String, Object> libro = new HashMap<>();
    libro.put("titulo", "Rayuela");
    libro.put("idioma", "Español");
    libro.put("categoriaId", cat);
    libro.put("autorIds", List.of(autor));
    long idLibro = idDe(mockMvc.perform(post("/api/libros").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(libro))).andReturn(), "idLibro");
    String datos = json(Map.of("editorial", "Sudamericana", "anoPublicacion", 1963, "edicion", "1ra",
        "libroId", idLibro));
    idEdicion = idDe(mockMvc.perform(multipart("/api/ediciones")
        .file(new MockMultipartFile("datos", "", "application/json", datos.getBytes()))
        .header("Authorization", tokenBiblio)).andReturn(), "idEdicion");
  }

  // ---------------- fixtures ----------------

  protected Biblioteca nuevaBiblioteca(String nombre) {
    Biblioteca b = new Biblioteca();
    b.setNombre(nombre);
    b.setTipoBiblioteca(TipoBiblioteca.FACULTATIVA);
    b.setEstado(EstadoBiblioteca.ACTIVA);
    return bibliotecaRepository.saveAndFlush(b);
  }

  protected long idDe(MvcResult r, String campo) throws Exception {
    assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isBetween(200, 299);
    return cuerpo(r).path("data").path(campo).asLong();
  }

  /** Crea un ejemplar DISPONIBLE de la edición base y devuelve su id. */
  protected long crearEjemplar() throws Exception {
    contadorEjemplares++;
    return idDe(mockMvc.perform(post("/api/ejemplares").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("edicionId", idEdicion, "bibliotecaId", idBiblioteca,
            "codigoEjemplar", "EJ-IT3-" + contadorEjemplares))))
        .andReturn(), "id_ejemplar");
  }

  protected Map<String, Object> cuerpoPrestamo(long usuarioId, long ejemplarId, String tipo) {
    Map<String, Object> m = new HashMap<>();
    m.put("usuarioId", usuarioId);
    m.put("ejemplarId", ejemplarId);
    m.put("bibliotecaId", idBiblioteca);
    m.put("tipoPrestamo", tipo);
    m.put("tipoDocumentoGarantia", "CI");
    m.put("condicionEntrega", "BUENO");
    return m;
  }

  protected MvcResult postPrestamo(String token, Map<String, Object> cuerpo) throws Exception {
    return mockMvc.perform(post("/api/prestamos").header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpo))).andReturn();
  }

  /** Crea un préstamo a domicilio por la API (bibliotecario encargado) y devuelve su id. */
  protected long crearPrestamo(Usuario lector, long ejemplarId) throws Exception {
    MvcResult r = postPrestamo(tokenBiblio, cuerpoPrestamo(lector.getId_usuario(), ejemplarId, "DOMICILIO"));
    assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isEqualTo(201);
    return cuerpo(r).path("data").path("id_prestamo").asLong();
  }

  protected long crearPrestamoNuevo(Usuario lector) throws Exception {
    return crearPrestamo(lector, crearEjemplar());
  }

  /** Simula el paso del tiempo: la fecha estimada pasa a "hoy - diasAtras". */
  protected void forzarFechaEstimada(long idPrestamo, int diasAtras) {
    em.flush();
    Prestamo p = prestamoRepository.findById(idPrestamo).orElseThrow();
    p.setFechaDevolucionEstimada(LocalDate.now().minusDays(diasAtras));
    prestamoRepository.saveAndFlush(p);
    em.clear();
  }

  protected void forzarEstado(long idPrestamo, EstadoPrestamo estado) {
    em.flush();
    Prestamo p = prestamoRepository.findById(idPrestamo).orElseThrow();
    p.setEstadoPrestamo(estado);
    prestamoRepository.saveAndFlush(p);
    em.clear();
  }

  protected Prestamo recargarPrestamo(long id) {
    em.flush();
    em.clear();
    return prestamoRepository.findById(id).orElseThrow();
  }
}

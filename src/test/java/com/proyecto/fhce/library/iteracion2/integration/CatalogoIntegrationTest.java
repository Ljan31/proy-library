package com.proyecto.fhce.library.iteracion2.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.support.IntegrationTestBase;

/**
 * Iteración 2 · Integración del catálogo bibliográfico:
 * categorías, autores, libros, ediciones y ejemplares, con seguridad por rol
 * y persistencia real (H2). Cada prueba arma su propio escenario vía la API.
 */
@DisplayName("Iteración 2 · Catálogo bibliográfico (integración)")
class CatalogoIntegrationTest extends IntegrationTestBase {

  @Autowired
  BibliotecaRepository bibliotecaRepository;

  private String admin;
  private String bibliotecario;
  private String estudiante;
  private Long idBiblioteca;

  @BeforeEach
  void datos() throws Exception {
    crearUsuario("admin01", rolAdmin);
    crearUsuario("biblio01", rolBibliotecario);
    crearUsuario("estud01", rolEstudiante);

    Biblioteca b = new Biblioteca();
    b.setNombre("Biblioteca Central");
    b.setTipoBiblioteca(TipoBiblioteca.FACULTATIVA);
    b.setEstado(EstadoBiblioteca.ACTIVA);
    idBiblioteca = bibliotecaRepository.saveAndFlush(b).getIdBiblioteca();
    flushYLimpiar();

    admin = bearer("admin01");
    bibliotecario = bearer("biblio01");
    estudiante = bearer("estud01");
  }

  // ------------------------------------------------------------------
  // helpers: crean datos por la API y devuelven el id generado
  // ------------------------------------------------------------------

  private long idDe(MvcResult r, String campo) throws Exception {
    return cuerpo(r).path("data").path(campo).asLong();
  }

  private long crearCategoria(String nombre, String dewey) throws Exception {
    return idDe(mockMvc.perform(post("/api/categorias").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("nombre_categoria", nombre, "codigo_dewey", dewey))))
        .andExpect(status().isCreated()).andReturn(), "id_categoria");
  }

  private long crearAutor(String nombre) throws Exception {
    return idDe(mockMvc.perform(post("/api/autores").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("nombre", nombre))))
        .andExpect(status().isCreated()).andReturn(), "idAutor");
  }

  private long crearLibro(String titulo, Long categoriaId, List<Long> autorIds) throws Exception {
    Map<String, Object> body = new java.util.HashMap<>();
    body.put("titulo", titulo);
    body.put("idioma", "Español");
    body.put("categoriaId", categoriaId);
    body.put("autorIds", autorIds);
    return idDe(mockMvc.perform(post("/api/libros").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isCreated()).andReturn(), "idLibro");
  }

  private MvcResult postEdicion(String token, Long libroId) throws Exception {
    String datos = json(Map.of("editorial", "Sudamericana", "anoPublicacion", 1967,
        "edicion", "1ra", "libroId", libroId));
    return mockMvc.perform(multipart("/api/ediciones")
        .file(new MockMultipartFile("datos", "", "application/json", datos.getBytes()))
        .header("Authorization", token)).andReturn();
  }

  private long crearEdicion(Long libroId) throws Exception {
    MvcResult r = postEdicion(bibliotecario, libroId);
    assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isEqualTo(201);
    return idDe(r, "idEdicion");
  }

  private long crearEjemplar(Long edicionId, String codigo) throws Exception {
    return idDe(mockMvc.perform(post("/api/ejemplares").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("edicionId", edicionId, "bibliotecaId", idBiblioteca, "codigoEjemplar", codigo))))
        .andExpect(status().isCreated()).andReturn(), "id_ejemplar");
  }

  private String estadoReq(String nuevoEstado, String motivo) throws Exception {
    return json(Map.of("nuevoEstado", nuevoEstado, "motivo", motivo));
  }

  // ===================== CATEGORÍAS =====================

  @Test
  @DisplayName("IT2-I-CAT-01: el BIBLIOTECARIO crea una categoría con código Dewey")
  void crearCategoria_ok() throws Exception {
    mockMvc.perform(post("/api/categorias").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("nombre_categoria", "Literatura", "codigo_dewey", "800"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.nombre_categoria").value("Literatura"))
        .andExpect(jsonPath("$.data.codigo_dewey").value("800"));
  }

  @Test
  @DisplayName("IT2-I-CAT-02: crear una categoría con Dewey repetido devuelve 409")
  void crearCategoria_deweyRepetido() throws Exception {
    crearCategoria("Literatura", "800");

    mockMvc.perform(post("/api/categorias").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("nombre_categoria", "Otra", "codigo_dewey", "800"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Ya existe una categoría con código Dewey: 800"));
  }

  @Test
  @DisplayName("IT2-I-CAT-03: crear una categoría sin nombre devuelve 400")
  void crearCategoria_sinNombre() throws Exception {
    mockMvc.perform(post("/api/categorias").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre_categoria\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT2-I-CAT-04: un ESTUDIANTE no puede crear categorías (403)")
  void crearCategoria_estudiante() throws Exception {
    mockMvc.perform(post("/api/categorias").header("Authorization", estudiante)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("nombre_categoria", "X"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT2-I-CAT-05: no se elimina una categoría que tiene libros (409)")
  void eliminarCategoria_conLibros() throws Exception {
    long cat = crearCategoria("Literatura", "800");
    crearLibro("Cien años de soledad", cat, null);

    mockMvc.perform(delete("/api/categorias/" + cat).header("Authorization", admin))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message").value("No se puede eliminar la categoría porque tiene 1 libro(s) asociado(s)"));
  }

  @Test
  @DisplayName("IT2-I-CAT-06: solo el ADMIN elimina categorías; el BIBLIOTECARIO recibe 403")
  void eliminarCategoria_soloAdmin() throws Exception {
    long cat = crearCategoria("Historia", "900");

    mockMvc.perform(delete("/api/categorias/" + cat).header("Authorization", bibliotecario))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete("/api/categorias/" + cat).header("Authorization", admin))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("IT2-I-CAT-07: buscar por código Dewey inexistente devuelve 404")
  void categoriaPorDewey_noExiste() throws Exception {
    mockMvc.perform(get("/api/categorias/dewey/999").header("Authorization", estudiante))
        .andExpect(status().isNotFound());
  }

  // ===================== AUTORES =====================

  @Test
  @DisplayName("IT2-I-AUTOR-01: crear un autor con nombre repetido (otra capitalización) devuelve 409")
  void crearAutor_duplicado() throws Exception {
    crearAutor("Gabriel García Márquez");

    mockMvc.perform(post("/api/autores").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("nombre", "GABRIEL GARCÍA MÁRQUEZ"))))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("IT2-I-AUTOR-02: un ESTUDIANTE no puede crear autores (403)")
  void crearAutor_estudiante() throws Exception {
    mockMvc.perform(post("/api/autores").header("Authorization", estudiante)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("nombre", "Borges"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT2-I-AUTOR-03: el ADMIN edita el nombre de un autor")
  void admin_actualizaAutor() throws Exception {
    long id = crearAutor("Borjes");

    mockMvc.perform(put("/api/autores/" + id).header("Authorization", admin)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("nombre", "Borges"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nombre").value("Borges"));
  }

  /**
   * DEFECTO DETECTADO (DEF-04): AutorController.update usa
   * {@code hasRole('ADMIN') or hasRole('BIBLIOTECARIO', 'AUXILIAR')}; hasRole
   * acepta UN
   * solo argumento, así que para un no-ADMIN la expresión SpEL falla (500).
   * Debe ser hasAnyRole('BIBLIOTECARIO','AUXILIAR').
   */
  @Test
  // @Disabled("DEF-04: expresión @PreAuthorize inválida en
  // AutorController.update. Habilitar tras corregir.")
  @DisplayName("IT2-I-AUTOR-04: el BIBLIOTECARIO puede editar un autor (200)")
  void bibliotecario_actualizaAutor() throws Exception {
    long id = crearAutor("Borjes");

    mockMvc.perform(put("/api/autores/" + id).header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("nombre", "Borges"))))
        .andExpect(status().isOk());
  }

  // ===================== LIBROS =====================

  @Test
  @DisplayName("IT2-I-LIB-01: crear un libro con categoría y dos autores devuelve el libro completo")
  void crearLibro_completo() throws Exception {
    long cat = crearCategoria("Literatura", "800");
    long a1 = crearAutor("Autor Uno");
    long a2 = crearAutor("Autor Dos");

    mockMvc.perform(get("/api/libros/" + crearLibro("Novela de prueba", cat, List.of(a1, a2)))
        .header("Authorization", estudiante))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.titulo").value("Novela de prueba"))
        .andExpect(jsonPath("$.data.categoria.nombre_categoria").value("Literatura"))
        .andExpect(jsonPath("$.data.autores.length()").value(2))
        .andExpect(jsonPath("$.data.ejemplaresTotal").value(0));
  }

  @Test
  @DisplayName("IT2-I-LIB-02: crear un libro con categoría inexistente devuelve 404")
  void crearLibro_categoriaInexistente() throws Exception {
    mockMvc.perform(post("/api/libros").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("titulo", "Libro", "categoriaId", 99999))))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("IT2-I-LIB-03: crear un libro sin título devuelve 400")
  void crearLibro_sinTitulo() throws Exception {
    mockMvc.perform(post("/api/libros").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT2-I-LIB-04: un ESTUDIANTE no puede crear libros (403)")
  void crearLibro_estudiante() throws Exception {
    mockMvc.perform(post("/api/libros").header("Authorization", estudiante)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("titulo", "Libro"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT2-I-LIB-05: consultar un libro inexistente devuelve 404")
  void libro_noExiste() throws Exception {
    mockMvc.perform(get("/api/libros/999999").header("Authorization", estudiante))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("IT2-I-LIB-06: el libro refleja el total y los ejemplares disponibles por biblioteca")
  void libro_conteoDeEjemplares() throws Exception {
    long libro = crearLibro("Con ejemplares", null, null);
    long edicion = crearEdicion(libro);
    crearEjemplar(edicion, "EJ-001");
    long e2 = crearEjemplar(edicion, "EJ-002");
    mockMvc.perform(put("/api/ejemplares/" + e2 + "/estado").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content(estadoReq("EN_REPARACION", "Lomo roto")))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/libros/" + libro).header("Authorization", estudiante))
        .andExpect(jsonPath("$.data.ejemplaresTotal").value(2))
        .andExpect(jsonPath("$.data.ejemplaresDisponibles").value(1))
        .andExpect(jsonPath("$.data.nombreBiblioteca").value("Biblioteca Central"));
  }

  @Test
  @DisplayName("IT2-I-LIB-07: la búsqueda simple encuentra libros por título y por autor")
  void buscarLibros_porTituloYAutor() throws Exception {
    long a = crearAutor("Julio Cortázar");
    crearLibro("Rayuela", null, List.of(a));
    crearLibro("Cien años de soledad", null, null);

    mockMvc.perform(get("/api/libros/search").param("q", "rayu").header("Authorization", estudiante))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].titulo").value("Rayuela"));

    mockMvc.perform(get("/api/libros/search").param("q", "cortázar").header("Authorization", estudiante))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].titulo").value("Rayuela"));
  }

  @Test
  @DisplayName("IT2-I-LIB-08: la búsqueda avanzada filtra por título (sin distinguir mayúsculas)")
  void busquedaAvanzada_porTitulo() throws Exception {
    crearLibro("Rayuela", null, null);
    crearLibro("Cien años de soledad", null, null);

    mockMvc.perform(post("/api/libros/busqueda-avanzada").header("Authorization", estudiante)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("busqueda", "SOLEDAD"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.content[0].titulo").value("Cien años de soledad"));
  }

  @Test
  @DisplayName("IT2-I-LIB-09: la búsqueda avanzada filtra por categoría")
  void busquedaAvanzada_porCategoria() throws Exception {
    long lit = crearCategoria("Literatura", "800");
    long his = crearCategoria("Historia", "900");
    crearLibro("Rayuela", lit, null);
    crearLibro("Historia de Bolivia", his, null);

    mockMvc.perform(post("/api/libros/busqueda-avanzada").header("Authorization", estudiante)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("categoriaId", his))))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.content[0].titulo").value("Historia de Bolivia"));
  }

  @Test
  @DisplayName("IT2-I-LIB-10: la búsqueda avanzada filtra por año de publicación de la edición")
  void busquedaAvanzada_porAno() throws Exception {
    long l1 = crearLibro("Libro de 1967", null, null);
    crearEdicion(l1); // anoPublicacion = 1967
    crearLibro("Libro sin edición", null, null);

    mockMvc.perform(post("/api/libros/busqueda-avanzada").header("Authorization", estudiante)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("anoPublicacion", 1967))))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.content[0].titulo").value("Libro de 1967"));
  }

  /**
   * DEFECTO DETECTADO (DEF-05): en LibroServiceImpl.busquedaAvanzada los filtros
   * título y
   * autor se combinan con {@code cb.or(...)}; al enviar ambos devuelve libros que
   * cumplan
   * SOLO UNO. Una búsqueda avanzada debe usar AND (cb.and).
   */
  // @Test
  // // @Disabled("DEF-05: título y autor se combinan con OR en vez de AND.
  // Habilitar
  // // tras corregir.")
  // @DisplayName("IT2-I-LIB-11: la búsqueda avanzada con título Y autor exige
  // ambos criterios")
  // void busquedaAvanzada_tituloYAutor() throws Exception {
  // long cortazar = crearAutor("Julio Cortázar");
  // long borges = crearAutor("Jorge Borges");
  // crearLibro("Rayuela", null, List.of(cortazar));
  // crearLibro("Rayuela ilustrada", null, List.of(borges));

  // mockMvc.perform(post("/api/libros/busqueda-avanzada").header("Authorization",
  // estudiante)
  // .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("titulo",
  // "rayuela", "autor", "cortázar"))))
  // .andExpect(jsonPath("$.data.totalElements").value(1))
  // .andExpect(jsonPath("$.data.content[0].titulo").value("Rayuela"));
  // }

  @Test
  @DisplayName("IT2-I-LIB-12: se elimina un libro sin ediciones ni ejemplares")
  void eliminarLibro_ok() throws Exception {
    long libro = crearLibro("Para borrar", null, null);

    mockMvc.perform(delete("/api/libros/" + libro).header("Authorization", bibliotecario))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/libros/" + libro).header("Authorization", estudiante))
        .andExpect(status().isNotFound());
  }

  // ===================== EDICIONES =====================

  @Test
  @DisplayName("IT2-I-EDI-01: crear una edición (multipart) para un libro existente")
  void crearEdicion_ok() throws Exception {
    long libro = crearLibro("Libro", null, null);

    MvcResult r = postEdicion(bibliotecario, libro);

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    assertThat(cuerpo(r).path("data").path("editorial").asText()).isEqualTo("Sudamericana");
    assertThat(cuerpo(r).path("data").path("libro").path("titulo").asText()).isEqualTo("Libro");
  }

  @Test
  @DisplayName("IT2-I-EDI-02: crear una edición de un libro inexistente devuelve 404")
  void crearEdicion_libroInexistente() throws Exception {
    assertThat(postEdicion(bibliotecario, 99999L).getResponse().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("IT2-I-EDI-03: un ESTUDIANTE no puede crear ediciones (403)")
  void crearEdicion_estudiante() throws Exception {
    long libro = crearLibro("Libro", null, null);

    assertThat(postEdicion(estudiante, libro).getResponse().getStatus()).isEqualTo(403);
  }

  @Test
  @DisplayName("IT2-I-EDI-04: no se elimina una edición con ejemplares (409)")
  void eliminarEdicion_conEjemplares() throws Exception {
    long edicion = crearEdicion(crearLibro("Libro", null, null));
    crearEjemplar(edicion, "EJ-001");

    mockMvc.perform(delete("/api/ediciones/" + edicion).header("Authorization", admin))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("No se puede eliminar una edición que tiene ejemplares registrados"));
  }

  // ===================== EJEMPLARES =====================

  @Test
  @DisplayName("IT2-I-EJE-01: un ejemplar nuevo queda DISPONIBLE y en la biblioteca indicada")
  void crearEjemplar_disponible() throws Exception {
    long edicion = crearEdicion(crearLibro("Libro", null, null));

    mockMvc.perform(post("/api/ejemplares").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("edicionId", edicion, "bibliotecaId", idBiblioteca, "codigoEjemplar", "EJ-100"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.estadoEjemplar").value("DISPONIBLE"))
        .andExpect(jsonPath("$.data.codigoEjemplar").value("EJ-100"));
  }

  @Test
  @DisplayName("IT2-I-EJE-02: crear un ejemplar sin edición ni biblioteca devuelve 400")
  void crearEjemplar_sinReferencias() throws Exception {
    mockMvc.perform(post("/api/ejemplares").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content("{\"codigoEjemplar\":\"EJ-1\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT2-I-EJE-03: cambiar el estado a EN_REPARACION queda registrado en el historial")
  void cambiarEstado_registraHistorial() throws Exception {
    long ej = crearEjemplar(crearEdicion(crearLibro("Libro", null, null)), "EJ-001");

    mockMvc.perform(put("/api/ejemplares/" + ej + "/estado").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content(estadoReq("EN_REPARACION", "Tapa suelta")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.estadoEjemplar").value("EN_REPARACION"));

    mockMvc.perform(get("/api/ejemplares/" + ej + "/historial").header("Authorization", bibliotecario))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("IT2-I-EJE-04: cambiar a un estado igual al actual devuelve 409")
  void cambiarEstado_mismoEstado() throws Exception {
    long ej = crearEjemplar(crearEdicion(crearLibro("Libro", null, null)), "EJ-001");

    mockMvc.perform(put("/api/ejemplares/" + ej + "/estado").header("Authorization", bibliotecario)
        .contentType(MediaType.APPLICATION_JSON).content(estadoReq("DISPONIBLE", "sin cambio")))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("IT2-I-EJE-05: solo el ADMIN da de baja; luego el ejemplar ya no puede cambiar de estado")
  void darDeBaja_yEstadoFinal() throws Exception {
    long ej = crearEjemplar(crearEdicion(crearLibro("Libro", null, null)), "EJ-001");

    mockMvc.perform(put("/api/ejemplares/" + ej + "/baja").param("motivo", "obsoleto")
        .header("Authorization", bibliotecario))
        .andExpect(status().isForbidden());

    mockMvc.perform(put("/api/ejemplares/" + ej + "/baja").param("motivo", "obsoleto")
        .header("Authorization", admin))
        .andExpect(status().isOk());

    mockMvc.perform(put("/api/ejemplares/" + ej + "/estado").header("Authorization", admin)
        .contentType(MediaType.APPLICATION_JSON).content(estadoReq("DISPONIBLE", "reactivar")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Un ejemplar dado de baja no puede cambiar de estado"));
  }

  @Test
  @DisplayName("IT2-I-EJE-06: el ADMIN transfiere un ejemplar a otra biblioteca")
  void transferirEjemplar_ok() throws Exception {
    long ej = crearEjemplar(crearEdicion(crearLibro("Libro", null, null)), "EJ-001");
    Biblioteca destino = new Biblioteca();
    destino.setNombre("Biblioteca de Carrera");
    destino.setTipoBiblioteca(TipoBiblioteca.FACULTATIVA);
    destino.setEstado(EstadoBiblioteca.ACTIVA);
    Long idDestino = bibliotecaRepository.saveAndFlush(destino).getIdBiblioteca();

    mockMvc.perform(put("/api/ejemplares/" + ej + "/transferir/" + idDestino)
        .param("motivo", "traslado").header("Authorization", admin))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/ejemplares/" + ej).header("Authorization", estudiante))
        .andExpect(jsonPath("$.data.biblioteca.nombre").value("Biblioteca de Carrera"));
  }

  @Test
  @DisplayName("IT2-I-EJE-07: se elimina un ejemplar disponible sin préstamos")
  void eliminarEjemplar_ok() throws Exception {
    long ej = crearEjemplar(crearEdicion(crearLibro("Libro", null, null)), "EJ-001");

    mockMvc.perform(delete("/api/ejemplares/" + ej).header("Authorization", bibliotecario))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/ejemplares/" + ej).header("Authorization", estudiante))
        .andExpect(status().isNotFound());
  }
}

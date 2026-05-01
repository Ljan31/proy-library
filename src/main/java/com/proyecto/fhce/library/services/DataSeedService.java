package com.proyecto.fhce.library.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Carrera;
import com.proyecto.fhce.library.entities.CategoriaLibro;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.entities.Edicion;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.HistorialEstadoEjemplar;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.entities.Permiso;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.CarreraRepository;
import com.proyecto.fhce.library.repositories.CategoriaLibroRepository;
import com.proyecto.fhce.library.repositories.ConfiguracionPrestamoRepository;
import com.proyecto.fhce.library.repositories.EdicionRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.HistorialEstadoEjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;
import com.proyecto.fhce.library.repositories.PermisoRepository;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.security.RoleName;

@Service
public class DataSeedService {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PersonaRepository personaRepository;
  private final PermisoRepository permisoRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  private CarreraRepository carreraRepository;
  @Autowired
  private BibliotecaRepository bibliotecaRepository;
  @Autowired
  private LibroRepository libroRepository;
  @Autowired
  private EdicionRepository edicionRepository;
  @Autowired
  private EjemplarRepository ejemplarRepository;
  @Autowired
  private HistorialEstadoEjemplarRepository historialEstadoEjemplarRepository;
  @Autowired
  private CategoriaLibroRepository categoriaLibroRepository;
  @Autowired
  private ConfiguracionPrestamoRepository configuracionPrestamoRepository;

  public DataSeedService(RoleRepository roleRepository,
      UserRepository userRepository, PersonaRepository personaRepository, PermisoRepository permisoRepository,
      PasswordEncoder passwordEncoder) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.personaRepository = personaRepository;
    this.permisoRepository = permisoRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public String seed() {
    // 1️⃣ Crear roles
    for (RoleName roleName : RoleName.values()) {
      roleRepository.findByName(roleName.name())
          .orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName.name());
            return roleRepository.save(role);
          });
    }
    // ==================== 2️⃣ PERMISOS ====================
    if (permisoRepository.count() == 0) {
      String[] modulos = { "USUARIOS", "LIBROS", "PRESTAMOS", "RESERVAS", "SANCIONES", "CERTIFICADOS", "REPORTES" };
      String[] acciones = { "VER", "CREAR", "EDITAR", "ELIMINAR" };

      for (String modulo : modulos) {
        for (String accion : acciones) {
          Permiso permiso = new Permiso();
          permiso.setNombre_permiso(modulo + "_" + accion);
          permiso.setDescripcion(accion + " " + modulo.toLowerCase());
          permiso.setModulo(modulo);
          permisoRepository.save(permiso);
        }
      }

      System.out.println("Permisos por defecto creados");
    }
    // ==================== 3️⃣ CREAR USUARIOS ====================
    createUserIfNotExists("admin", "Admin", "System", "n/n", 1111,
        "admin@localhost", "11111", "Admin123!", RoleName.ROLE_ADMIN);

    createUserIfNotExists("crali", "Juan", "Perez", "Gomez", 1234567,
        "bibliotecario@localhost", "22222", "123456", RoleName.ROLE_BIBLIOTECARIO);

    createUserIfNotExists("maria", "Maria", "Lopez", "Diaz", 7654321,
        "estudiante@localhost", "33333", "123456", RoleName.ROLE_ESTUDIANTE);

    // Bibliotecarios
    createUserIfNotExists("historia", "Luis", "Mamani", "Quispe", 2233445,
        "biblio2@localhost", "700001", "123456", RoleName.ROLE_BIBLIOTECARIO);

    createUserIfNotExists("bibliotecario3", "Ana", "Choque", "Rojas", 3344556,
        "biblio3@localhost", "700002", "123456", RoleName.ROLE_BIBLIOTECARIO);

    // Estudiantes (10)
    createUserIfNotExists("carlos", "Carlos", "Perez", "Lopez", 12,
        "est2@localhost", "710002", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("lucia", "Lucia", "Gomez", "Ramos", 13,
        "est3@localhost", "710003", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("diego", "Diego", "Fernandez", "Torrez", 14,
        "est4@localhost", "710004", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("sofia", "Sofia", "Vargas", "Mendoza", 15,
        "est5@localhost", "710005", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("jorge", "Jorge", "Castro", "Quisbert", 16,
        "est6@localhost", "710006", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("elena", "Elena", "Flores", "Condori", 17,
        "est7@localhost", "710007", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("miguel", "Miguel", "Huanca", "Perez", 18,
        "est8@localhost", "710008", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("alejandra", "alejandra", "Rojas", "Loza", 19,
        "est9@localhost", "710009", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("andres", "Andres", "Gutierrez", "Silva", 21,
        "est10@localhost", "710010", "123456", RoleName.ROLE_ESTUDIANTE);

    createUserIfNotExists("paola", "Paola", "Navarro", "Ortiz", 22,
        "est11@localhost", "710011", "123456", RoleName.ROLE_ESTUDIANTE);
    // ==================== 4️⃣ ASIGNAR PERMISOS A ROLES ====================
    assignPermisos();
    seedCarrerasYBibliotecas();
    seedLibrosEjemplares();
    seedCategorias();
    seedConfiguracionPrestamo();
    return "Data seed completed successfully ✅";
  }

  // ==================== MÉTODO AUXILIAR: CREAR USUARIOS ====================
  private void createUserIfNotExists(String username,
      String nombre, String apellidoPat, String apellidoMat, int ci,
      String email, String celular, String password,
      RoleName roleName) {
    if (!userRepository.existsByUsername(username)) {
      Role role = roleRepository.findByName(roleName.name())
          .orElseThrow(() -> new RuntimeException(roleName.name() + " no existe"));

      Persona persona = new Persona();
      persona.setNombre(nombre);
      persona.setApellido_pat(apellidoPat);
      persona.setApellido_mat(apellidoMat);
      persona.setCi(ci);
      persona.setEmail(email);
      persona.setCelular(celular);
      persona = personaRepository.save(persona);

      Usuario usuario = new Usuario();
      usuario.setUsername(username);
      usuario.setPassword(passwordEncoder.encode(password));
      usuario.setEnabled(true);
      usuario.setRoles(Set.of(role));
      usuario.setPersona(persona);

      userRepository.save(usuario);

      System.out.println("Usuario creado: " + username + " con rol " + roleName.name());
    }
  }

  // ==================== MÉTODO AUXILIAR: ASIGNAR PERMISOS ====================
  private void assignPermisos() {
    // Obtener todos los permisos
    List<Permiso> todosPermisos = permisoRepository.findAll();

    // ROLE_ADMIN = todos los permisos
    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN.name()).orElseThrow();
    adminRole.setPermisos(new HashSet<>(todosPermisos));
    roleRepository.save(adminRole);

    // ROLE_BIBLIOTECARIO = solo prestamos, devoluciones, historial
    Role biblioRole = roleRepository.findByName(RoleName.ROLE_BIBLIOTECARIO.name()).orElseThrow();
    Set<Permiso> biblioPermisos = todosPermisos.stream()
        .filter(p -> p.getModulo().equals("PRESTAMOS") || p.getModulo().equals("RESERVAS"))
        .collect(Collectors.toSet());
    biblioRole.setPermisos(biblioPermisos);
    roleRepository.save(biblioRole);

    // ROLE_ESTUDIANTE = ver info propia, consultar recursos
    Role estudianteRole = roleRepository.findByName(RoleName.ROLE_ESTUDIANTE.name()).orElseThrow();
    Set<Permiso> estudiantePermisos = todosPermisos.stream()
        .filter(p -> p.getModulo().equals("LIBROS") && p.getNombre_permiso().startsWith("VER"))
        .collect(Collectors.toSet());
    estudianteRole.setPermisos(estudiantePermisos);
    roleRepository.save(estudianteRole);

    System.out.println("Permisos asignados a los roles ✅");
  }

  private void seedCarrerasYBibliotecas() {

    // ==================== CARRERAS ====================
    if (carreraRepository.count() == 0) {

      Carrera historia = new Carrera();
      historia.setNombre_carrera("Historia");
      historia.setCodigo_carrera("HIS");
      historia = carreraRepository.save(historia);

      Carrera linguistica = new Carrera();
      linguistica.setNombre_carrera("Lingüística e Idiomas");
      linguistica.setCodigo_carrera("LIN");
      linguistica = carreraRepository.save(linguistica);

      Carrera filosofia = new Carrera();
      filosofia.setNombre_carrera("Filosofía");
      filosofia.setCodigo_carrera("FIL");
      filosofia = carreraRepository.save(filosofia);

      Carrera cienciasInfo = new Carrera();
      cienciasInfo.setNombre_carrera("Ciencias de la Información");
      cienciasInfo.setCodigo_carrera("CIN");
      cienciasInfo = carreraRepository.save(cienciasInfo);

      System.out.println("Carreras FHCE creadas ✅");

      // ==================== BIBLIOTECA FACULTATIVA ====================
      if (bibliotecaRepository.findBibliotecaFacultativa().isEmpty()) {

        Biblioteca central = new Biblioteca();
        central.setNombre("Biblioteca Central FHCE");
        central.setTipoBiblioteca(TipoBiblioteca.FACULTATIVA);
        central.setDireccion("Campus Universitario");
        central.setTelefono("2222222");
        central.setEmail("central@fhce.edu");
        central.setHorario_atencion("08:00 - 20:00");
        central.setEstado(EstadoBiblioteca.ACTIVA);

        bibliotecaRepository.save(central);
      }

      // ==================== BIBLIOTECAS POR CARRERA ====================
      crearBibliotecaCarrera(historia, "Biblioteca Historia");
      crearBibliotecaCarrera(linguistica, "Biblioteca Lingüística");
      crearBibliotecaCarrera(filosofia, "Biblioteca Filosofía");
      crearBibliotecaCarrera(cienciasInfo, "Biblioteca Ciencias de la Información");

      System.out.println("Bibliotecas FHCE creadas ✅");
    }
  }

  private void crearBibliotecaCarrera(Carrera carrera, String nombreBiblioteca) {

    Optional<Biblioteca> existente = bibliotecaRepository
        .findByCarrera_IdCarreraAndTipoBiblioteca(
            carrera.getId_carrera(),
            TipoBiblioteca.CARRERA);

    if (existente.isEmpty()) {
      Biblioteca biblioteca = new Biblioteca();
      biblioteca.setNombre(nombreBiblioteca);
      biblioteca.setTipoBiblioteca(TipoBiblioteca.CARRERA);
      biblioteca.setDireccion("Dirección de " + nombreBiblioteca);
      biblioteca.setTelefono("7777777");
      biblioteca.setEmail(nombreBiblioteca.toLowerCase().replace(" ", "") + "@mail.com");
      biblioteca.setHorario_atencion("08:00 - 18:00");
      biblioteca.setEstado(EstadoBiblioteca.ACTIVA);
      biblioteca.setCarrera(carrera);

      bibliotecaRepository.save(biblioteca);
    }
  }

  private void seedLibrosEjemplares() {

    // Obtener bibliotecas
    Biblioteca historia = bibliotecaRepository.findByNombre("Biblioteca Historia")
        .orElseThrow();

    Biblioteca linguistica = bibliotecaRepository.findByNombre("Biblioteca Lingüística")
        .orElseThrow();

    // ==================== LIBROS HISTORIA ====================

    Libro libro1 = crearLibro("Historia de Bolivia", "Español", "Historia general del país");
    Edicion ed1 = crearEdicion(
        libro1,
        "ISBN-HIS-001",
        "Editorial Andina",
        2010,
        "https://covers.openlibrary.org/b/id/8231996-L.jpg");

    crearEjemplares(ed1, historia, "HIS-EJ-", 3);
    // Segunda edición del libro Historia de Bolivia
    Edicion ed1_v2 = crearEdicion(
        libro1,
        "ISBN-HIS-001-V2",
        "Editorial Andina",
        2022,
        "https://covers.openlibrary.org/b/id/10523338-L.jpg");

    // Crear ejemplar para nueva edición
    crearEjemplares(ed1_v2, historia, "HIS-EJ-V2-", 1);

    Libro libro2 = crearLibro("Historia Universal", "Español", "Historia del mundo");
    Edicion ed2 = crearEdicion(
        libro2,
        "ISBN-HIS-002",
        "Santillana",
        2015,
        "https://covers.openlibrary.org/b/id/8228691-L.jpg");

    crearEjemplares(ed2, historia, "HIS-EJ-", 2);

    // ==================== LIBROS LINGÜÍSTICA ====================

    Libro libro3 = crearLibro("Introducción a la Lingüística", "Español", "Fundamentos lingüísticos");
    Edicion ed3 = crearEdicion(
        libro3,
        "ISBN-LIN-001",
        "McGraw-Hill",
        2018,
        "https://covers.openlibrary.org/b/id/8235083-L.jpg");

    crearEjemplares(ed3, linguistica, "LIN-EJ-", 3);
    // Segunda edición de Introducción a la Lingüística
    Edicion ed3_v2 = crearEdicion(
        libro3,
        "ISBN-LIN-001-V2",
        "McGraw-Hill",
        2021,
        "https://covers.openlibrary.org/b/id/10909258-L.jpg");

    // Crear ejemplar para nueva edición
    crearEjemplares(ed3_v2, linguistica, "LIN-EJ-V2-", 1);
    Libro libro4 = crearLibro("Gramática Española", "Español", "Reglas gramaticales");
    Edicion ed4 = crearEdicion(
        libro4,
        "ISBN-LIN-002",
        "Planeta",
        2020,
        "https://covers.openlibrary.org/b/id/8244151-L.jpg");

    crearEjemplares(ed4, linguistica, "LIN-EJ-", 2);

    System.out.println("Libros y ejemplares creados ✅");
  }

  private void seedCategorias() {

    if (categoriaLibroRepository.count() > 0) {
      return; // evitar duplicados
    }

    List<CategoriaLibro> categorias = List.of(

        crearCategoria("Filosofía", "Pensamiento filosófico", "100"),
        crearCategoria("Religión", "Estudios religiosos", "200"),
        crearCategoria("Ciencias Sociales", "Sociedad y cultura", "300"),
        crearCategoria("Lenguas", "Idiomas y lingüística", "400"),
        crearCategoria("Ciencias Naturales", "Matemáticas, física, química", "500"),
        crearCategoria("Tecnología", "Ingeniería y aplicaciones", "600"),
        crearCategoria("Artes", "Arte, música y recreación", "700"),
        crearCategoria("Literatura", "Obras literarias", "800"),
        crearCategoria("Historia", "Historia universal", "900"),
        crearCategoria("Geografía", "Estudio de la tierra", "910")

    );

    categoriaLibroRepository.saveAll(categorias);

    System.out.println("Categorías creadas ✅");
  }

  private void seedConfiguracionPrestamo() {

    if (configuracionPrestamoRepository.count() > 0) {
      return; // evitar duplicados
    }

    Biblioteca biblioteca2 = bibliotecaRepository.findById(2L)
        .orElseThrow(() -> new RuntimeException("Biblioteca 2 no existe"));

    Biblioteca biblioteca3 = bibliotecaRepository.findById(3L)
        .orElseThrow(() -> new RuntimeException("Biblioteca 3 no existe"));

    // Configuración Biblioteca 2
    ConfiguracionPrestamo config2 = new ConfiguracionPrestamo();
    config2.setBiblioteca(biblioteca2);
    config2.setDiasPrestamoMax(7);
    config2.setRenovacionesMax(2);
    config2.setEjemplaresMaxDomicilio(3);
    config2.setEjemplaresMaxSala(2);
    config2.setMultaPorDia(new BigDecimal("2.50"));
    config2.setMultaMaxDias(10);
    config2.setDiasSuspension(3);
    config2.setDiasReserva(2);

    configuracionPrestamoRepository.save(config2);

    // Configuración Biblioteca 3
    ConfiguracionPrestamo config3 = new ConfiguracionPrestamo();
    config3.setBiblioteca(biblioteca3);
    config3.setDiasPrestamoMax(5);
    config3.setRenovacionesMax(1);
    config3.setEjemplaresMaxDomicilio(2);
    config3.setEjemplaresMaxSala(1);
    config3.setMultaPorDia(new BigDecimal("3.00"));
    config3.setMultaMaxDias(7);
    config3.setDiasSuspension(5);
    config3.setDiasReserva(1);

    configuracionPrestamoRepository.save(config3);

    System.out.println("Configuraciones de préstamo creadas ✅");
  }

  private CategoriaLibro crearCategoria(String nombre, String descripcion, String dewey) {
    CategoriaLibro c = new CategoriaLibro();
    c.setNombre_categoria(nombre);
    c.setDescripcion(descripcion);
    c.setCodigo_dewey(dewey);
    return c;
  }

  private Libro crearLibro(String titulo, String idioma, String descripcion) {
    Libro libro = new Libro();
    libro.setTitulo(titulo);
    libro.setIdioma(idioma);
    libro.setDescripcion(descripcion);
    return libroRepository.save(libro);
  }

  private Edicion crearEdicion(Libro libro, String isbn, String editorial, int anio, String imagen) {

    if (edicionRepository.existsByIsbn(isbn)) {
      return edicionRepository.findByIsbn(isbn).orElseThrow();
    }

    Edicion ed = new Edicion();
    ed.setLibro(libro);
    ed.setIsbn(isbn);
    ed.setEditorial(editorial);
    ed.setAnoPublicacion(anio);
    ed.setEdicion("1ra");
    ed.setImagenPortada(imagen);

    return edicionRepository.save(ed);
  }

  private void crearEjemplares(Edicion edicion, Biblioteca biblioteca,
      String prefijo, int cantidad) {

    for (int i = 1; i <= cantidad; i++) {

      String codigo = prefijo + edicion.getIdEdicion() + "-" + i;

      if (ejemplarRepository.existsByCodigoEjemplar(codigo))
        continue;

      Ejemplar ej = new Ejemplar();
      ej.setEdicion(edicion);
      ej.setBiblioteca(biblioteca);
      ej.setCodigoEjemplar(codigo);
      ej.setCodigoTopografico("TOP-" + codigo);
      ej.setUbicacionFisica("Estante " + i);
      ej.setEstadoEjemplar(EstadoEjemplar.DISPONIBLE);
      ej.setFechaAdquisicion(LocalDate.now());

      Ejemplar saved = ejemplarRepository.save(ej);

      // historial inicial (igual que tu service)
      registrarCambioEstadoSeed(saved);
    }
  }

  private void registrarCambioEstadoSeed(Ejemplar ejemplar) {
    HistorialEstadoEjemplar h = new HistorialEstadoEjemplar();
    h.setEjemplar(ejemplar);
    h.setEstadoAnterior(null);
    h.setEstadoNuevo(EstadoEjemplar.DISPONIBLE);
    h.setMotivo("Seed inicial");

    // ⚠️ evitar problema de seguridad (puede no haber usuario logueado)
    Usuario admin = userRepository.findByUsername("admin").orElseThrow();
    h.setUsuarioCambio(admin);

    historialEstadoEjemplarRepository.save(h);
  }

}
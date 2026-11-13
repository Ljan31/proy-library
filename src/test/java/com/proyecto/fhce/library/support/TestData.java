package com.proyecto.fhce.library.support;

import java.util.HashSet;
import java.util.Set;

import com.proyecto.fhce.library.entities.Autor;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.CategoriaLibro;
import com.proyecto.fhce.library.entities.Edicion;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

/**
 * Fábrica de objetos de dominio para las pruebas UNITARIAS (sin base de datos).
 * Los ids se asignan a mano porque no hay JPA que los genere.
 */
public final class TestData {

  private TestData() {
  }

  public static Persona persona(Long id, String ci) {
    Persona p = new Persona();
    p.setId_persona(id);
    p.setNombre("Juan");
    p.setApellido_pat("Perez");
    p.setApellido_mat("Gomez");
    p.setCi(ci);
    p.setCelular("71234567");
    p.setEmail("persona" + id + "@correo.com");
    return p;
  }

  public static Role role(Long id, String nombre) {
    Role r = new Role();
    r.setId_role(id);
    r.setName(nombre);
    return r;
  }

  /** Usuario habilitado con los roles indicados (set mutable). */
  public static Usuario usuario(Long id, String username, Role... roles) {
    Usuario u = new Usuario();
    u.setId_usuario(id);
    u.setUsername(username);
    u.setPassword("HASH_BCRYPT_DE_PRUEBA");
    u.setEnabled(true);
    u.setPersona(persona(id, "100000" + id));
    u.setRoles(new HashSet<>(Set.of(roles)));
    return u;
  }

  public static Biblioteca biblioteca(Long id, String nombre, TipoBiblioteca tipo) {
    Biblioteca b = new Biblioteca();
    b.setIdBiblioteca(id);
    b.setNombre(nombre);
    b.setTipoBiblioteca(tipo);
    b.setEstado(EstadoBiblioteca.ACTIVA);
    return b;
  }

  public static CategoriaLibro categoria(Long id, String nombre, String dewey) {
    CategoriaLibro c = new CategoriaLibro();
    c.setId_categoria(id);
    c.setNombre_categoria(nombre);
    c.setCodigo_dewey(dewey);
    return c;
  }

  public static Autor autor(Long id, String nombre) {
    Autor a = new Autor();
    a.setIdAutor(id);
    a.setNombre(nombre);
    return a;
  }

  public static Libro libro(Long id, String titulo) {
    Libro l = new Libro();
    l.setIdLibro(id);
    l.setTitulo(titulo);
    l.setIdioma("Español");
    return l;
  }

  public static Edicion edicion(Long id, Libro libro) {
    Edicion e = new Edicion();
    e.setIdEdicion(id);
    e.setEditorial("Editorial Test");
    e.setAnoPublicacion(2020);
    e.setEdicion("1ra");
    e.setLibro(libro);
    return e;
  }

  public static Ejemplar ejemplar(Long id, Edicion edicion, Biblioteca biblioteca, EstadoEjemplar estado) {
    Ejemplar e = new Ejemplar();
    e.setIdEjemplar(id);
    e.setEdicion(edicion);
    e.setBiblioteca(biblioteca);
    e.setCodigoEjemplar("EJ-" + id);
    e.setEstadoEjemplar(estado);
    return e;
  }
}

package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Libro;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

  Optional<Libro> findByIsbn(String isbn);

  List<Libro> findByTituloContainingIgnoreCase(String titulo);

  List<Libro> findByCategoria_IdCategoria(Long categoriaId);

  List<Libro> findByEditorialContainingIgnoreCase(String editorial);

  List<Libro> findByAnoPublicacion(Integer anoPublicacion);

  boolean existsByIsbn(String isbn);

  // @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.autores WHERE l.id_libro =
  // :id")
  // Optional<Libro> findByIdWithAutores(@Param("id") Long id);

  // @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.ejemplares e " +
  // "WHERE l.id_libro = :id")
  // Optional<Libro> findByIdWithEjemplares(@Param("id") Long id);

  // @Query("SELECT DISTINCT l FROM Libro l JOIN l.autores a " +
  // "WHERE a.nombre LIKE %:nombre% OR a.apellido LIKE %:apellido%")
  // List<Libro> findByAutorNombreOrApellido(
  // @Param("nombre") String nombre,
  // @Param("apellido") String apellido);

  @Query("SELECT l FROM Libro l WHERE " +
      "LOWER(l.titulo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
      "LOWER(l.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
      "LOWER(l.editorial) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  List<Libro> searchLibros(@Param("searchTerm") String searchTerm);

  // @Query("SELECT l FROM Libro l JOIN l.ejemplares e " +
  // "WHERE e.biblioteca.id_biblioteca = :bibliotecaId " +
  // "GROUP BY l ORDER BY l.titulo")
  // List<Libro> findByBiblioteca(@Param("bibliotecaId") Long bibliotecaId);

  @Query("SELECT COUNT(l) FROM Libro l WHERE l.categoria.idCategoria = :categoriaId")
  Long countByCategoria_IdCategoria(@Param("categoriaId") Long categoriaId);

  List<Libro> findTop10ByCategoria_IdCategoriaOrderByAnoPublicacionDesc(Long categoriaId);
}

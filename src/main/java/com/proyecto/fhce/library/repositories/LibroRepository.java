package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Libro;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long>, JpaSpecificationExecutor<Libro> {

  List<Libro> findByTituloContainingIgnoreCase(String titulo);

  List<Libro> findByCategoria_IdCategoria(Long categoriaId);

  // @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.ejemplares e " +
  // "WHERE l.id_libro = :id")
  // Optional<Libro> findByIdWithEjemplares(@Param("id") Long id);

  // @Query("SELECT DISTINCT l FROM Libro l JOIN l.autores a " +
  // "WHERE a.nombre LIKE %:nombre% OR a.apellido LIKE %:apellido%")
  // List<Libro> findByAutorNombreOrApellido(
  // @Param("nombre") String nombre,
  // @Param("apellido") String apellido);

  @Query("SELECT DISTINCT l FROM Libro l " +
      "LEFT JOIN l.ediciones ed " +
      "WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%')) " +
      "OR LOWER(ed.isbn) LIKE LOWER(CONCAT('%', :q, '%')) " +
      "OR LOWER(ed.editorial) LIKE LOWER(CONCAT('%', :q, '%'))")
  List<Libro> searchLibros(@Param("q") String q);

  // @Query("SELECT l FROM Libro l JOIN l.ejemplares e " +
  // "WHERE e.biblioteca.id_biblioteca = :bibliotecaId " +
  // "GROUP BY l ORDER BY l.titulo")
  // List<Libro> findByBiblioteca(@Param("bibliotecaId") Long bibliotecaId);

  @Query("SELECT COUNT(l) FROM Libro l WHERE l.categoria.idCategoria = :categoriaId")
  Long countByCategoria_IdCategoria(@Param("categoriaId") Long categoriaId);

  @Query("SELECT COUNT(l) FROM Libro l WHERE l.categoria.idCategoria = :categoriaId")
  Long countByCategoria(@Param("categoriaId") Long categoriaId);

  List<Libro> findTop10ByCategoria_IdCategoriaOrderByIdLibroDesc(Long categoriaId);
}

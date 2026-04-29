package com.proyecto.fhce.library.repositories;

import com.proyecto.fhce.library.entities.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {

  List<Autor> findByNombreContainingIgnoreCase(String nombre);

  Optional<Autor> findByNombreIgnoreCase(String nombre);

  boolean existsByNombreIgnoreCase(String nombre);

  @Query("SELECT a FROM Autor a JOIN a.libros l WHERE l.idLibro = :libroId")
  List<Autor> findByLibroId(@Param("libroId") Long libroId);
}
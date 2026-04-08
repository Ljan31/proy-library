package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Edicion;

@Repository
public interface EdicionRepository extends JpaRepository<Edicion, Long> {

  Optional<Edicion> findByIsbn(String isbn);

  boolean existsByIsbn(String isbn);

  List<Edicion> findByLibro_IdLibro(Long libroId);

  List<Edicion> findByEditorialContainingIgnoreCase(String editorial);

  @Query("SELECT e FROM Edicion e WHERE e.libro.idLibro = :libroId " +
      "ORDER BY e.anoPublicacion DESC")
  List<Edicion> findByLibroOrderByAnoDesc(@Param("libroId") Long libroId);
}
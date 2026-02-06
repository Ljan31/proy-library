package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.CategoriaLibro;

@Repository
public interface CategoriaLibroRepository extends JpaRepository<CategoriaLibro, Long> {

  Optional<CategoriaLibro> findByCodigoDewey(String codigoDewey);

  List<CategoriaLibro> findByNombreCategoriaContainingIgnoreCase(String nombreCategoria);

  boolean existsByCodigoDewey(String codigoDewey);

  @Query("SELECT c FROM CategoriaLibro c LEFT JOIN c.libros l " +
      "GROUP BY c ORDER BY COUNT(l) DESC")
  List<CategoriaLibro> findCategoriasOrderByLibrosCount();
}
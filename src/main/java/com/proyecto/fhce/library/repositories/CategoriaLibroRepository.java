package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.dto.response.library.CategoriaConConteoResponse;
import com.proyecto.fhce.library.entities.CategoriaLibro;

@Repository
public interface CategoriaLibroRepository extends JpaRepository<CategoriaLibro, Long> {

    Optional<CategoriaLibro> findByCodigoDewey(String codigoDewey);

    List<CategoriaLibro> findByNombreCategoriaContainingIgnoreCase(String nombreCategoria);

    List<CategoriaLibro> findAllByOrderByNombreCategoriaAsc();

    boolean existsByCodigoDewey(String codigoDewey);

    @Query("SELECT c FROM CategoriaLibro c LEFT JOIN c.libros l " +
            "GROUP BY c ORDER BY COUNT(l) DESC")
    List<CategoriaLibro> findCategoriasOrderByLibrosCount();

    @Query("SELECT c.idCategoria, c.nombreCategoria, COUNT(l) FROM CategoriaLibro c " +
            "LEFT JOIN c.libros l GROUP BY c.idCategoria, c.nombreCategoria " +
            "ORDER BY COUNT(l) DESC")
    List<CategoriaConConteoResponse> findTopCategoriasConMasLibros();

    @Query("SELECT COUNT(c) FROM CategoriaLibro c WHERE c.libros IS EMPTY")
    Long countCategoriasSinLibros();
}
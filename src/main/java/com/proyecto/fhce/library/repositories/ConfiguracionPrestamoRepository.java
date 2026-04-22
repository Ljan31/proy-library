package com.proyecto.fhce.library.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.enums.TipoPrestamo;

@Repository
public interface ConfiguracionPrestamoRepository
    extends JpaRepository<ConfiguracionPrestamo, Long> {

  // Busca la configuración más específica según prioridad de negocio
  @Query("""
      SELECT c FROM ConfiguracionPrestamo c
      LEFT JOIN FETCH c.biblioteca
      LEFT JOIN FETCH c.rol
      WHERE (:bibliotecaId IS NULL OR c.biblioteca.idBiblioteca = :bibliotecaId)
        AND (:rolId IS NULL OR c.rol.id_role = :rolId)
        AND c.tipoPrestamo = :tipoPrestamo
      ORDER BY
          CASE WHEN c.biblioteca IS NOT NULL AND c.rol IS NOT NULL THEN 1
               WHEN c.biblioteca IS NOT NULL AND c.rol IS NULL    THEN 2
               WHEN c.biblioteca IS NULL    AND c.rol IS NULL    THEN 3
               ELSE 4 END ASC
      """)
  List<ConfiguracionPrestamo> findConfiguracionesAplicables(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("rolId") Long rolId,
      @Param("tipoPrestamo") TipoPrestamo tipoPrestamo);

  // Detecta conflicto de duplicados antes de insertar
  @Query("""
      SELECT COUNT(c) > 0 FROM ConfiguracionPrestamo c
      WHERE (c.biblioteca.idBiblioteca = :bibliotecaId
             OR (c.biblioteca IS NULL AND :bibliotecaId IS NULL))
        AND (c.rol.id_role = :rolId
             OR (c.rol IS NULL AND :rolId IS NULL))
        AND c.tipoPrestamo = :tipoPrestamo
        AND (:excludeId IS NULL OR c.idConfig != :excludeId)
      """)
  boolean existeConfiguracionDuplicada(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("rolId") Long rolId,
      @Param("tipoPrestamo") TipoPrestamo tipoPrestamo,
      @Param("excludeId") Long excludeId);

  // Listar todas las configuraciones de una biblioteca
  @Query("""
      SELECT c FROM ConfiguracionPrestamo c
      LEFT JOIN FETCH c.biblioteca b
      LEFT JOIN FETCH c.rol r
      WHERE b.idBiblioteca = :bibliotecaId
      """)
  List<ConfiguracionPrestamo> findAllByBibliotecaId(@Param("bibliotecaId") Long bibliotecaId);
}

package com.proyecto.fhce.library.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.HistorialEstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoEjemplar;

@Repository
public interface HistorialEstadoEjemplarRepository extends JpaRepository<HistorialEstadoEjemplar, Long> {

  List<HistorialEstadoEjemplar> findByEjemplar_IdEjemplar(Long ejemplarId);

  @Query("SELECT h FROM HistorialEstadoEjemplar h WHERE h.ejemplar.id_ejemplar = :ejemplarId " +
      "ORDER BY h.fechaCambio DESC")
  List<HistorialEstadoEjemplar> findByEjemplarOrderByFechaDesc(@Param("ejemplarId") Long ejemplarId);

  @Query("SELECT h FROM HistorialEstadoEjemplar h WHERE h.fechaCambio BETWEEN :fechaInicio AND :fechaFin")
  List<HistorialEstadoEjemplar> findByFechaCambioBetween(
      @Param("fechaInicio") LocalDateTime fechaInicio,
      @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT h FROM HistorialEstadoEjemplar h WHERE h.ejemplar.biblioteca.id_biblioteca = :bibliotecaId " +
      "AND h.estadoNuevo = :estado")
  List<HistorialEstadoEjemplar> findByBibliotecaAndEstadoNuevo(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoEjemplar estado);
}
package com.proyecto.fhce.library.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Auditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

  List<Auditoria> findByUsuario_IdUsuario(Long usuarioId);

  List<Auditoria> findByTablaAfectada(String tablaAfectada);

  List<Auditoria> findByAccion(String accion);

  @Query("SELECT a FROM Auditoria a WHERE a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
      "ORDER BY a.fechaHora DESC")
  List<Auditoria> findByFechaHoraBetween(
      @Param("fechaInicio") LocalDateTime fechaInicio,
      @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT a FROM Auditoria a WHERE a.tablaAfectada = :tabla " +
      "AND a.id_registro = :idRegistro ORDER BY a.fechaHora DESC")
  List<Auditoria> findByTablaAndRegistro(
      @Param("tabla") String tabla,
      @Param("idRegistro") Long idRegistro);

  @Query("SELECT a FROM Auditoria a WHERE a.usuario.idUsuario = :usuarioId " +
      "ORDER BY a.fechaHora DESC")
  Page<Auditoria> findByUsuarioOrderByFechaDesc(
      @Param("usuarioId") Long usuarioId,
      Pageable pageable);

  @Query("SELECT a.accion, COUNT(a) FROM Auditoria a GROUP BY a.accion")
  List<Object[]> countByAccion();
}
package com.proyecto.fhce.library.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.enums.EstadoCertificado;

@Repository
public interface CertificadoNoDeudaRepository extends JpaRepository<CertificadoNoDeuda, Long> {

  Optional<CertificadoNoDeuda> findByCodigoVerificacion(String codigoVerificacion);

  List<CertificadoNoDeuda> findByUsuario_IdUsuario(Long usuarioId);

  List<CertificadoNoDeuda> findByEstadoCertificado(EstadoCertificado estadoCertificado);

  @Query("SELECT c FROM CertificadoNoDeuda c WHERE c.usuario.idUsuario = :usuarioId " +
      "AND c.estadoCertificado = 'VIGENTE' " +
      "ORDER BY c.fechaEmision DESC")
  List<CertificadoNoDeuda> findCertificadosVigentesByUsuario(@Param("usuarioId") Long usuarioId);

  @Query("SELECT c FROM CertificadoNoDeuda c WHERE c.estadoCertificado = 'VIGENTE' " +
      "AND c.fechaVencimiento < :fecha")
  List<CertificadoNoDeuda> findCertificadosVencidos(@Param("fecha") LocalDateTime fecha);

  @Query("SELECT c FROM CertificadoNoDeuda c WHERE c.fechaEmision BETWEEN :fechaInicio AND :fechaFin")
  List<CertificadoNoDeuda> findByFechaEmisionBetween(
      @Param("fechaInicio") LocalDateTime fechaInicio,
      @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT COUNT(c) FROM CertificadoNoDeuda c WHERE c.bibliotecario.idUsuario = :bibliotecarioId")
  Long countByBibliotecario(@Param("bibliotecarioId") Long bibliotecarioId);
}

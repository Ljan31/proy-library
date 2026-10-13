package com.proyecto.fhce.library.repositories;

import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.FormatoCertificado;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface FormatoCertificadoRepository extends JpaRepository<FormatoCertificado, Long> {

  @Query("""
          SELECT f
          FROM FormatoCertificado f
          JOIN FETCH f.biblioteca
          WHERE f.idFormato = :id
      """)
  Optional<FormatoCertificado> buscarPorIdConBiblioteca(
      @Param("id") Long id);

  // Plantilla activa de una biblioteca
  Optional<FormatoCertificado> findByBiblioteca_IdBibliotecaAndActivoTrue(Long bibliotecaId);

  // Historial completo de versiones de una biblioteca
  List<FormatoCertificado> findByBiblioteca_IdBibliotecaOrderByVersionDesc(Long bibliotecaId);

  // Desactivar todas las versiones de una biblioteca (para rotación)
  @Modifying
  @Query("UPDATE FormatoCertificado f SET f.activo = false " +
      "WHERE f.biblioteca.idBiblioteca = :bibliotecaId")
  void desactivarTodas(@Param("bibliotecaId") Long bibliotecaId);
}
package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.UsuarioCarrera;

@Repository
public interface UsuarioCarreraRepository extends JpaRepository<UsuarioCarrera, Long> {

  List<UsuarioCarrera> findByUsuario_IdUsuario(Long usuarioId);

  List<UsuarioCarrera> findByCarrera_IdCarrera(Long carreraId);

  Optional<UsuarioCarrera> findByUsuario_IdUsuarioAndCarrera_IdCarrera(
      Long usuarioId, Long carreraId);

  boolean existsByUsuario_IdUsuarioAndCarrera_IdCarrera(Long usuarioId, Long carreraId);

  @Query("SELECT COUNT(uc) FROM UsuarioCarrera uc WHERE uc.carrera.id_carrera = :carreraId")
  Long countEstudiantesByCarrera(@Param("carreraId") Long carreraId);
}
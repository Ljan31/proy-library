package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.fhce.library.entities.Permiso;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

  Optional<Permiso> findByNombrePermiso(String nombrePermiso);

  List<Permiso> findByModulo(String modulo);

  boolean existsByNombrePermiso(String nombrePermiso);

  @Query("SELECT DISTINCT p.modulo FROM Permiso p ORDER BY p.modulo")
  List<String> findDistinctModulos();

  @Query("SELECT p FROM Permiso p JOIN p.roles r WHERE r.id_role = :roleId")
  List<Permiso> findByRoleId(@Param("roleId") Long roleId);
}
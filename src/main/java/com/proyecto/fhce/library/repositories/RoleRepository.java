package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.fhce.library.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByName(String name);

  boolean existsByName(String name);

  @Query("SELECT r FROM Role r JOIN FETCH r.permisos WHERE r.id_role = :id")
  Optional<Role> findByIdWithPermisos(@Param("id") Long id);

  @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permisos")
  List<Role> findAllWithPermisos();
}

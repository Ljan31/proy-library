package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.fhce.library.entities.Usuario;

public interface UserRepository extends JpaRepository<Usuario, Long> {

  Optional<Usuario> findByUsername(String username);

  Optional<Usuario> findByPersona_Email(String email);

  Optional<Usuario> findByPersona_Ci(String ci);

  boolean existsByUsername(String username);

  List<Usuario> findByEnabledTrue();

  List<Usuario> findByEnabledFalse();

  @Query("SELECT u FROM Usuario u JOIN FETCH u.persona WHERE u.username = :username")
  Optional<Usuario> findByUsernameWithPersona(@Param("username") String username);

  @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.name = :roleName")
  List<Usuario> findByRoleName(@Param("roleName") String roleName);

  // @Query("SELECT u FROM Usuario u WHERE u.persona.nombre LIKE %:searchTerm% " +
  // "OR u.persona.apellido_pat LIKE %:searchTerm% " +
  // "OR u.username LIKE %:searchTerm% " +
  // "OR u.persona.matricula LIKE %:searchTerm%")
  // List<Usuario> searchUsuarios(@Param("searchTerm") String searchTerm);

  @Query("SELECT u FROM Usuario u WHERE " +
      "LOWER(u.persona.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
      "LOWER(u.persona.apellido_pat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
      "LOWER(u.persona.apellido_mat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
      "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
      "CAST(u.persona.ci AS string) LIKE CONCAT('%', :searchTerm, '%')")
  List<Usuario> searchUsuarios(@Param("searchTerm") String searchTerm);

  @Query("SELECT COUNT(u) FROM Usuario u JOIN u.roles r WHERE r.name = :roleName")
  Long countByRoleName(@Param("roleName") String roleName);

  // @Query("SELECT u FROM Usuario u WHERE u.intentosLogin >= :maxIntentos AND
  // u.enabled = true")
  // List<Usuario> findUsuariosBloqueados(@Param("maxIntentos") Integer
  // maxIntentos);
}

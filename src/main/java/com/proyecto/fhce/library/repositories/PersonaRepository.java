package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.fhce.library.entities.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

  Optional<Persona> findByCi(Integer ci);

  Optional<Persona> findByEmail(String email);

  Optional<Persona> findByMatricula(String matricula);

  boolean existsByCi(Integer ci);

  boolean existsByEmail(String email);

  boolean existsByMatricula(String matricula);
  // List<Persona>
  // findByNombreContainingIgnoreCaseOrApellido__patContainingIgnoreCase(
  // String nombre, String apellido_pat);

  @Query("""
          SELECT p FROM Persona p
          WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))
             OR LOWER(p.apellido_pat) LIKE LOWER(CONCAT('%', :termino, '%'))
      """)
  List<Persona> buscarPorNombreOApellidoPaterno(@Param("termino") String termino);
}
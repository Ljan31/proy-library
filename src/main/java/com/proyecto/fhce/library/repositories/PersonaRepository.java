package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.fhce.library.entities.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

  Optional<Persona> findByCi(Integer ci);

  Optional<Persona> findByEmail(String email);

  boolean existsByCi(Integer ci);

  boolean existsByEmail(String email);

  List<Persona> findByNombreContainingIgnoreCaseOrApellidoPatContainingIgnoreCase(
      String nombre, String apellidoPat);
}
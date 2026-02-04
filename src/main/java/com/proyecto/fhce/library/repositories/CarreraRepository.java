package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Carrera;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Long> {

  Optional<Carrera> findByCodigoCarrera(String codigoCarrera);

  boolean existsByCodigoCarrera(String codigoCarrera);

  @Query("SELECT c FROM Carrera c WHERE c.nombre_carrera LIKE %:nombre%")
  List<Carrera> findByNombreContaining(@Param("nombre") String nombre);

  @Query("SELECT c FROM Carrera c LEFT JOIN FETCH c.bibliotecas WHERE c.id_carrera = :id")
  Optional<Carrera> findByIdWithBibliotecas(@Param("id") Long id);

}

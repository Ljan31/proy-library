package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.enums.EstadoEjemplar;

@Repository
public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {

    Optional<Ejemplar> findByCodigoEjemplar(String codigoEjemplar);

    List<Ejemplar> findByLibro_IdLibro(Long libroId);

    List<Ejemplar> findByBiblioteca_IdBiblioteca(Long bibliotecaId);

    List<Ejemplar> findByEstadoEjemplar(EstadoEjemplar estadoEjemplar);

    boolean existsByCodigoEjemplar(String codigoEjemplar);

    @Query("SELECT e FROM Ejemplar e WHERE e.libro.idLibro = :libroId " +
            "AND e.biblioteca.idBiblioteca = :bibliotecaId " +
            "AND e.estadoEjemplar = :estado")
    List<Ejemplar> findByLibroAndBibliotecaAndEstado(
            @Param("libroId") Long libroId,
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("estado") EstadoEjemplar estado);

    @Query("SELECT e FROM Ejemplar e WHERE e.libro.idLibro = :libroId " +
            "AND e.estadoEjemplar = 'DISPONIBLE'")
    List<Ejemplar> findEjemplaresDisponiblesByLibro(@Param("libroId") Long libroId);

    @Query("SELECT COUNT(e) FROM Ejemplar e WHERE e.biblioteca.idBiblioteca = :bibliotecaId " +
            "AND e.estadoEjemplar = :estado")
    Long countByBibliotecaAndEstado(
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("estado") EstadoEjemplar estado);

    @Query("SELECT e FROM Ejemplar e WHERE e.biblioteca.idBiblioteca = :bibliotecaId " +
            "AND e.estadoEjemplar IN :estados")
    List<Ejemplar> findByBibliotecaAndEstadoIn(
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("estados") List<EstadoEjemplar> estados);

    @Query("SELECT e FROM Ejemplar e LEFT JOIN FETCH e.libro l " +
            "WHERE e.idEjemplar = :id")
    Optional<Ejemplar> findByIdWithLibro(@Param("id") Long id);
}
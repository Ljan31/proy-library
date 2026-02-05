package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

@Repository
public interface BibliotecaRepository extends JpaRepository<Biblioteca, Long> {
  List<Biblioteca> findByTipoBiblioteca(TipoBiblioteca tipoBiblioteca);

  List<Biblioteca> findByEstado(EstadoBiblioteca estado);

  List<Biblioteca> findByCarrera_IdCarrera(Long carreraId);

  Optional<Biblioteca> findByCarrera_IdCarreraAndTipoBiblioteca(
      Long carreraId, TipoBiblioteca tipoBiblioteca);

  @Query("SELECT b FROM Biblioteca b WHERE b.tipoBiblioteca = 'FACULTATIVA'")
  Optional<Biblioteca> findBibliotecaFacultativa();

  // @Query("SELECT b FROM Biblioteca b LEFT JOIN FETCH b.ejemplares WHERE
  // b.id_biblioteca = :id")
  // Optional<Biblioteca> findByIdWithEjemplares(@Param("id") Long id);

  @Query("SELECT b FROM Biblioteca b WHERE b.estado = :estado " +
      "ORDER BY b.tipoBiblioteca DESC, b.nombre ASC")
  List<Biblioteca> findByEstadoOrdered(@Param("estado") EstadoBiblioteca estado);

  List<Biblioteca> findByEncargado_IdUsuario(Long encargadoId);

  @Query("SELECT b FROM Biblioteca b WHERE " +
      "LOWER(b.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  List<Biblioteca> searchBibliotecas(@Param("searchTerm") String searchTerm);

  boolean existsByNombre(String nombre);

  @Query("SELECT COUNT(b) FROM Biblioteca b WHERE b.tipoBiblioteca = :tipo")
  Long countByTipo(@Param("tipo") TipoBiblioteca tipo);
}

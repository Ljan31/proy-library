package com.proyecto.fhce.library.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoPrestamo;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

  List<Prestamo> findByUsuario_IdUsuario(Long usuarioId);

  List<Prestamo> findByEjemplar_IdEjemplar(Long ejemplarId);

  List<Prestamo> findByBiblioteca_IdBiblioteca(Long bibliotecaId);

  List<Prestamo> findByEstadoPrestamo(EstadoPrestamo estadoPrestamo);

  @Query("SELECT p FROM Prestamo p WHERE p.usuario.id_usuario = :usuarioId " +
      "AND p.estadoPrestamo = :estado")
  List<Prestamo> findByUsuarioAndEstado(
      @Param("usuarioId") Long usuarioId,
      @Param("estado") EstadoPrestamo estado);

  @Query("SELECT p FROM Prestamo p WHERE p.estadoPrestamo = 'ACTIVO' " +
      "AND p.fechaDevolucionEstimada < :fecha")
  List<Prestamo> findPrestamosVencidos(@Param("fecha") LocalDate fecha);

  @Query("SELECT p FROM Prestamo p WHERE p.estadoPrestamo = 'ACTIVO' " +
      "AND p.fechaDevolucionEstimada BETWEEN :fechaInicio AND :fechaFin")
  List<Prestamo> findPrestamosPorVencer(
      @Param("fechaInicio") LocalDate fechaInicio,
      @Param("fechaFin") LocalDate fechaFin);

  @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.id_usuario = :usuarioId " +
      "AND p.estadoPrestamo = 'ACTIVO'")
  Long countPrestamosActivosByUsuario(@Param("usuarioId") Long usuarioId);

  @Query("SELECT p FROM Prestamo p LEFT JOIN FETCH p.ejemplar e " +
      "LEFT JOIN FETCH e.libro WHERE p.usuario.id_usuario = :usuarioId " +
      "AND p.estadoPrestamo = 'ACTIVO'")
  List<Prestamo> findPrestamosActivosWithDetalles(@Param("usuarioId") Long usuarioId);

  @Query("SELECT p FROM Prestamo p WHERE p.biblioteca.id_biblioteca = :bibliotecaId " +
      "AND p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin")
  List<Prestamo> findByBibliotecaAndFechaBetween(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("fechaInicio") LocalDateTime fechaInicio,
      @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT p.ejemplar.libro.titulo, COUNT(p) FROM Prestamo p " +
      "WHERE p.biblioteca.id_biblioteca = :bibliotecaId " +
      "GROUP BY p.ejemplar.libro.id_libro, p.ejemplar.libro.titulo " +
      "ORDER BY COUNT(p) DESC")
  List<Object[]> findLibrosMasPrestadosByBiblioteca(@Param("bibliotecaId") Long bibliotecaId);

  @Query("SELECT p FROM Prestamo p WHERE p.usuario.id_usuario = :usuarioId " +
      "AND p.ejemplar.id_ejemplar = :ejemplarId " +
      "AND p.estadoPrestamo = 'ACTIVO'")
  Optional<Prestamo> findPrestamoActivoByUsuarioAndEjemplar(
      @Param("usuarioId") Long usuarioId,
      @Param("ejemplarId") Long ejemplarId);
}
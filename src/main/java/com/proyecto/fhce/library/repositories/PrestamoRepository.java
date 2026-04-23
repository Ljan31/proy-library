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

        @Query("""
                        SELECT p FROM Prestamo p
                        WHERE p.biblioteca.idBiblioteca = :bibliotecaId
                        AND (:estado IS NULL OR p.estadoPrestamo = :estado)
                                """)
        List<Prestamo> findByBibliotecaConFiltroEstado(
                        @Param("bibliotecaId") Long bibliotecaId,
                        @Param("estado") EstadoPrestamo estado);

        List<Prestamo> findByEstadoPrestamo(EstadoPrestamo estadoPrestamo);

        @Query("SELECT p FROM Prestamo p WHERE p.usuario.idUsuario = :usuarioId " +
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

        @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.idUsuario = :usuarioId " +
                        "AND p.estadoPrestamo = 'ACTIVO'")
        Long countPrestamosActivosByUsuario(@Param("usuarioId") Long usuarioId);

        @Query("SELECT p FROM Prestamo p LEFT JOIN FETCH p.ejemplar e " +
                        "LEFT JOIN FETCH e.edicion ed " +
                        "LEFT JOIN FETCH ed.libro " +
                        "WHERE p.usuario.idUsuario = :usuarioId " +
                        "AND p.estadoPrestamo = 'ACTIVO'")
        List<Prestamo> findPrestamosActivosWithDetalles(@Param("usuarioId") Long usuarioId);

        @Query("SELECT p FROM Prestamo p WHERE p.biblioteca.idBiblioteca = :bibliotecaId " +
                        "AND p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin")
        List<Prestamo> findByBibliotecaAndFechaBetween(
                        @Param("bibliotecaId") Long bibliotecaId,
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        @Query("SELECT p.ejemplar.edicion.libro.titulo, COUNT(p) FROM Prestamo p " +
                        "WHERE p.biblioteca.idBiblioteca = :bibliotecaId " +
                        "GROUP BY p.ejemplar.edicion.libro.idLibro, p.ejemplar.edicion.libro.titulo " +
                        "ORDER BY COUNT(p) DESC")
        List<Object[]> findLibrosMasPrestadosByBiblioteca(@Param("bibliotecaId") Long bibliotecaId);

        @Query("SELECT p FROM Prestamo p WHERE p.usuario.idUsuario = :usuarioId " +
                        "AND p.ejemplar.idEjemplar = :ejemplarId " +
                        "AND p.estadoPrestamo = 'ACTIVO'")
        Optional<Prestamo> findPrestamoActivoByUsuarioAndEjemplar(
                        @Param("usuarioId") Long usuarioId,
                        @Param("ejemplarId") Long ejemplarId);

        @Query("SELECT p FROM Prestamo p WHERE p.ejemplar.idEjemplar = :ejemplarId " +
                        "AND p.estadoPrestamo = 'ACTIVO'")
        Optional<Prestamo> findPrestamoActivoByEjemplar(@Param("ejemplarId") Long ejemplarId);

        @Query("SELECT COUNT(p) FROM Prestamo p " +
                        "WHERE p.usuario.idUsuario = :usuarioId " +
                        "AND p.estadoPrestamo = :estado")
        Long countPrestamosConEstadoByUsuario(
                        @Param("usuarioId") Long usuarioId,
                        @Param("estado") EstadoPrestamo estado);

        /**
         * Cuenta préstamos ACTIVOS de un usuario en una biblioteca específica.
         */
        @Query("SELECT COUNT(p) FROM Prestamo p " +
                        "WHERE p.usuario.idUsuario = :usuarioId " +
                        "AND p.biblioteca.idBiblioteca = :bibliotecaId " +
                        "AND p.estadoPrestamo = 'ACTIVO'")
        Long countPrestamosActivosByUsuarioAndBiblioteca(
                        @Param("usuarioId") Long usuarioId,
                        @Param("bibliotecaId") Long bibliotecaId);

        /**
         * Cuenta préstamos de un usuario en una biblioteca específica filtrado por
         * estado.
         * Sirve para VENCIDO y RENOVADO.
         */
        @Query("SELECT COUNT(p) FROM Prestamo p " +
                        "WHERE p.usuario.idUsuario = :usuarioId " +
                        "AND p.biblioteca.idBiblioteca = :bibliotecaId " +
                        "AND p.estadoPrestamo = :estado")
        Long countPrestamosConEstadoByUsuarioAndBiblioteca(
                        @Param("usuarioId") Long usuarioId,
                        @Param("bibliotecaId") Long bibliotecaId,
                        @Param("estado") EstadoPrestamo estado);

        /**
         * Carga todas las relaciones necesarias para que SancionService
         * pueda calcular multas sin lazy-loading adicional.
         * REQUERIDO por el módulo de Sanciones
         */
        @Query("""
                        SELECT p FROM Prestamo p
                        JOIN FETCH p.usuario u
                        JOIN FETCH u.roles
                        JOIN FETCH p.biblioteca
                        JOIN FETCH p.ejemplar
                        WHERE p.idPrestamo = :id
                        """)
        Optional<Prestamo> findByIdWithRelations(@Param("id") Long id);

        /**
         * Préstamos vencidos que aún no tienen sanción ACTIVA.
         * Usado por el CRON de SancionService para evitar duplicados.
         * REQUERIDO por el módulo de Sanciones
         */
        @Query("""
                        SELECT p FROM Prestamo p
                        WHERE p.fechaDevolucionEstimada < CURRENT_DATE
                        AND p.estadoPrestamo NOT IN ('DEVUELTO', 'CANCELADO')
                        AND NOT EXISTS (
                            SELECT 1 FROM Sancion s
                            WHERE s.prestamo = p AND s.estado = 'ACTIVA'
                        )
                        """)
        List<Prestamo> findPrestamosVencidosSinSancion();
}
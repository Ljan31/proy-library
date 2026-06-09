package com.proyecto.fhce.library.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoDTO;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.TipoPrestamo;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuario_IdUsuario(Long usuarioId);

    List<Prestamo> findByEjemplar_IdEjemplar(Long ejemplarId);

    boolean existsByEjemplar_IdEjemplar(Long ejemplarId);

    boolean existsByUsuario_IdUsuario(Long usuarioId);

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

    @Query("""
            SELECT COUNT(p)
            FROM Prestamo p
            WHERE p.estadoPrestamo = 'ACTIVO'
            """)
    Long countPrestamosActivos();

    @Query("""
            SELECT COUNT(p)
            FROM Prestamo p
            WHERE p.estadoPrestamo = 'VENCIDO'
            """)
    Long countPrestamosVencidos();

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

    @Query("""
                SELECT COUNT(p)
                FROM Prestamo p
                WHERE p.usuario.persona.ci = :ci
                AND p.biblioteca.id = :bibliotecaId
                AND p.estadoPrestamo = :estado
            """)
    Long countPrestamosConEstadoByCiAndBiblioteca(
            @Param("ci") String ci,
            @Param("bibliotecaId") Long bibliotecaId,
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

    @Query("""
            SELECT COUNT(p)
            FROM Prestamo p
            WHERE p.biblioteca.idBiblioteca = :bibliotecaId
            AND p.estadoPrestamo = 'ACTIVO'
            """)
    Long countPrestamosActivosByBiblioteca(
            @Param("bibliotecaId") Long bibliotecaId);

    @Query("""
                SELECT COUNT(p)
                FROM Prestamo p
                WHERE p.biblioteca.idBiblioteca = :bibliotecaId
                AND p.estadoPrestamo = 'VENCIDO'
            """)
    Long countPrestamosVencidosByBiblioteca(
            @Param("bibliotecaId") Long bibliotecaId);

    @Query("""
                  SELECT p
                  FROM Prestamo p
                  JOIN FETCH p.usuario u
                  JOIN FETCH u.persona
                  JOIN FETCH p.biblioteca
                  JOIN FETCH p.ejemplar e
                  JOIN FETCH e.edicion ed
                  JOIN FETCH ed.libro l
                  WHERE
                  (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
                 AND (:estado IS NULL OR p.estadoPrestamo = :estado)
            AND p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin
                          """)
    List<Prestamo> obtenerReportePrestamos(
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("estado") EstadoPrestamo estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("""
            SELECT COUNT(p)
            FROM Prestamo p
            WHERE p.estadoPrestamo = :estado
            AND (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
            """)
    Long countByEstado(
            @Param("estado") EstadoPrestamo estado,
            @Param("bibliotecaId") Long bibliotecaId);

    @Query("""
            SELECT COUNT(p)
            FROM Prestamo p
            WHERE (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
            """)
    Long countPrestamos(
            @Param("bibliotecaId") Long bibliotecaId);

    @Query("""
                SELECT p
                FROM Prestamo p
                JOIN FETCH p.usuario u
                JOIN FETCH u.persona
                JOIN FETCH p.biblioteca b
                JOIN FETCH p.ejemplar e
                JOIN FETCH e.edicion ed
                JOIN FETCH ed.libro l
                WHERE
                (:bibliotecaId IS NULL OR b.idBiblioteca = :bibliotecaId)

                AND (:usuarioId IS NULL OR u.idUsuario = :usuarioId)

                AND (:libroId IS NULL OR l.idLibro = :libroId)

                AND (:estado IS NULL OR p.estadoPrestamo = :estado)

                AND (:tipoPrestamo IS NULL OR p.tipoPrestamo = :tipoPrestamo)

                AND (
                    (:fechaInicio IS NULL OR p.fechaPrestamo >= :fechaInicio)
                    AND
                    (:fechaFin IS NULL OR p.fechaPrestamo <= :fechaFin)
                )

                ORDER BY p.fechaPrestamo DESC
            """)
    List<Prestamo> obtenerHistorialPrestamos(
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("usuarioId") Long usuarioId,
            @Param("libroId") Long libroId,
            @Param("estado") EstadoPrestamo estado,
            @Param("tipoPrestamo") TipoPrestamo tipoPrestamo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("""
            SELECT new com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoDTO(
                l.idLibro,
                l.titulo,
                ed.isbn,
                COUNT(p.idPrestamo)
            )
            FROM Prestamo p
            JOIN p.ejemplar e
            JOIN e.edicion ed
            JOIN ed.libro l
            WHERE
            (:bibliotecaId IS NULL
             OR p.biblioteca.idBiblioteca = :bibliotecaId)
            GROUP BY
            l.idLibro,
            l.titulo,
            ed.isbn
            ORDER BY COUNT(p.idPrestamo) DESC
            """)
    List<LibroMasPrestadoDTO> obtenerLibrosMasPrestados(
            @Param("bibliotecaId") Long bibliotecaId);

    @Query("""
                SELECT new com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoDTO(
                    l.idLibro,
                    l.titulo,
                    ed.isbn,
                    COUNT(p.idPrestamo)
                )
                FROM Prestamo p
                JOIN p.ejemplar e
                JOIN e.edicion ed
                JOIN ed.libro l
                WHERE
                (:bibliotecaId IS NULL
                 OR p.biblioteca.idBiblioteca = :bibliotecaId)
                GROUP BY
                l.idLibro,
                l.titulo,
                ed.isbn
                ORDER BY COUNT(p.idPrestamo) ASC
            """)
    List<LibroMasPrestadoDTO> obtenerLibrosMenosPrestados(
            @Param("bibliotecaId") Long bibliotecaId);
}
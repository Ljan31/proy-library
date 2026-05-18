package com.proyecto.fhce.library.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Reserva;
import com.proyecto.fhce.library.enums.EstadoReserva;

import jakarta.persistence.LockModeType;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

        List<Reserva> findByUsuario_IdUsuario(Long usuarioId);

        List<Reserva> findByBiblioteca_IdBiblioteca(Long bibliotecaId);

        List<Reserva> findByEstadoReserva(EstadoReserva estado);

        boolean existsByEjemplar_IdEjemplar(Long ejemplarId);

        /**
         * Carga la reserva con todas las relaciones necesarias para el servicio.
         * Usado en operaciones que requieren acceder a usuario, libro y biblioteca
         * sin lazy-loading adicional fuera de la sesión JPA.
         */
        @Query("""
                        SELECT r FROM Reserva r
                        JOIN FETCH r.usuario u
                        JOIN FETCH u.persona
                        JOIN FETCH u.roles
                        JOIN FETCH r.libro
                        JOIN FETCH r.biblioteca
                        LEFT JOIN FETCH r.ejemplar
                        WHERE r.idReserva = :id
                        """)
        Optional<Reserva> findByIdWithRelaciones(@Param("id") Long id);

        /**
         * Reservas de un usuario con sus relaciones cargadas.
         * Usado para mostrar el historial de reservas al usuario.
         */
        @Query("""
                        SELECT r FROM Reserva r
                        JOIN FETCH r.libro l
                        JOIN FETCH r.biblioteca b
                        LEFT JOIN FETCH r.ejemplar
                        WHERE r.usuario.idUsuario = :usuarioId
                        ORDER BY r.fechaReserva DESC
                        """)
        List<Reserva> findByUsuarioWithRelaciones(@Param("usuarioId") Long usuarioId);

        /**
         * Reservas de una biblioteca filtradas por estado, con relaciones cargadas.
         * Usado en el panel del bibliotecario.
         */
        @Query("""
                        SELECT r FROM Reserva r
                        JOIN FETCH r.usuario u
                        JOIN FETCH u.persona
                        JOIN FETCH r.libro
                        LEFT JOIN FETCH r.ejemplar
                        WHERE r.biblioteca.idBiblioteca = :bibliotecaId
                        AND (:estado IS NULL OR r.estadoReserva = :estado)
                        ORDER BY r.prioridad ASC
                        """)
        List<Reserva> findByBibliotecaConFiltroEstado(
                        @Param("bibliotecaId") Long bibliotecaId,
                        @Param("estado") EstadoReserva estado);

        // ==================== COLA DE PRIORIDAD ====================

        /**
         * Cola de espera para un libro en una biblioteca específica.
         * Ordenada por prioridad ASC (FIFO): el menor valor se atiende primero.
         * Solo incluye estados que aún están esperando un ejemplar.
         */
        @Query("""
                        SELECT r FROM Reserva r
                        JOIN FETCH r.usuario u
                        JOIN FETCH u.persona
                        JOIN FETCH u.roles
                        JOIN FETCH r.biblioteca
                        WHERE r.libro.idLibro = :libroId
                        AND r.biblioteca.idBiblioteca = :bibliotecaId
                        AND r.estadoReserva IN ('ACTIVA')
                        ORDER BY r.prioridad ASC
                        """)
        List<Reserva> findColaEsperaByLibroAndBiblioteca(
                        @Param("libroId") Long libroId,
                        @Param("bibliotecaId") Long bibliotecaId);

        /**
         * Primer usuario en la cola de espera para un libro.
         * Usado por el proceso de asignación automática de ejemplar.
         *
         * IMPORTANTE: se usa con @Lock(PESSIMISTIC_WRITE) para evitar
         * que dos hilos del CRON asignen el mismo ejemplar a dos reservas distintas.
         */
        // @Lock(LockModeType.PESSIMISTIC_WRITE)
        // @Query("""
        // SELECT r FROM Reserva r
        // JOIN FETCH r.usuario u
        // JOIN FETCH u.roles
        // JOIN FETCH r.biblioteca
        // WHERE r.libro.idLibro = :libroId
        // AND r.biblioteca.idBiblioteca = :bibliotecaId
        // AND r.estadoReserva = 'ACTIVA'
        // ORDER BY r.prioridad ASC
        // """)
        // Optional<Reserva> findPrimeraEnColaConLock(
        // @Param("libroId") Long libroId,
        // @Param("bibliotecaId") Long bibliotecaId);
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT r FROM Reserva r
                        JOIN FETCH r.usuario u
                        JOIN FETCH r.biblioteca
                        WHERE r.libro.idLibro = :libroId
                        AND r.biblioteca.idBiblioteca = :bibliotecaId
                        AND r.estadoReserva = 'ACTIVA'
                        ORDER BY r.prioridad ASC
                        """)
        List<Reserva> findPrimeraEnColaConLock(
                        Long libroId,
                        Long bibliotecaId,
                        Pageable pageable);
        // ==================== VALIDACIONES ====================

        /**
         * Verifica si un usuario ya tiene una reserva activa o notificada
         * del mismo libro en la misma biblioteca.
         * Previene duplicados (un usuario no puede reservar el mismo libro dos veces).
         */
        @Query("""
                        SELECT COUNT(r) > 0 FROM Reserva r
                        WHERE r.usuario.idUsuario = :usuarioId
                        AND r.libro.idLibro = :libroId
                        AND r.biblioteca.idBiblioteca = :bibliotecaId
                        AND r.estadoReserva IN ('ACTIVA', 'NOTIFICADA')
                        """)
        boolean existsReservaActivaByUsuarioAndLibro(
                        @Param("usuarioId") Long usuarioId,
                        @Param("libroId") Long libroId,
                        @Param("bibliotecaId") Long bibliotecaId);

        /**
         * Cuenta reservas activas o notificadas de un usuario en una biblioteca.
         * Usado para validar el límite de reservas simultáneas.
         */
        @Query("""
                        SELECT COUNT(r) FROM Reserva r
                        WHERE r.usuario.idUsuario = :usuarioId
                        AND r.biblioteca.idBiblioteca = :bibliotecaId
                        AND r.estadoReserva IN ('ACTIVA', 'NOTIFICADA')
                        """)
        Long countReservasActivasByUsuarioAndBiblioteca(
                        @Param("usuarioId") Long usuarioId,
                        @Param("bibliotecaId") Long bibliotecaId);

        /**
         * Obtiene el siguiente valor de prioridad para la cola de un libro/biblioteca.
         * La prioridad se asigna como MAX + 1 para garantizar el orden FIFO.
         * Si no hay reservas previas, devuelve 0 y el servicio asigna 1.
         */
        @Query("""
                        SELECT COALESCE(MAX(r.prioridad), 0) FROM Reserva r
                        WHERE r.libro.idLibro = :libroId
                        AND r.biblioteca.idBiblioteca = :bibliotecaId
                        AND r.estadoReserva IN ('ACTIVA', 'NOTIFICADA')
                        """)
        Integer findMaxPrioridadByLibroAndBiblioteca(
                        @Param("libroId") Long libroId,
                        @Param("bibliotecaId") Long bibliotecaId);

        // ==================== PROCESOS CRON ====================

        /**
         * Reservas NOTIFICADAS cuya fecha de vencimiento ya pasó.
         * Usado por el CRON de expiración para cambiarlas a VENCIDA
         * y liberar los ejemplares asignados.
         */
        @Query("""
                        SELECT r FROM Reserva r
                        JOIN FETCH r.usuario u
                        JOIN FETCH r.libro
                        JOIN FETCH r.biblioteca
                        JOIN FETCH r.ejemplar e
                        WHERE r.estadoReserva = 'NOTIFICADA'
                        AND r.fechaVencimientoReserva < :hoy
                        """)
        List<Reserva> findReservasVencidasParaExpirar(@Param("hoy") LocalDate hoy);

        /**
         * Reservas ACTIVAS donde el libro ya tiene ejemplares disponibles.
         * Usado por el CRON de asignación automática para detectar
         * qué reservas pueden ser notificadas.
         *
         * La lógica de asignación con locking se hace en el servicio
         * con findPrimeraEnColaConLock() por libro/biblioteca.
         */
        @Query("""
                        SELECT DISTINCT r.libro.idLibro, r.biblioteca.idBiblioteca
                        FROM Reserva r
                        WHERE r.estadoReserva = 'ACTIVA'
                        AND EXISTS (
                            SELECT e FROM Ejemplar e
                            WHERE e.edicion.libro = r.libro
                            AND e.biblioteca = r.biblioteca
                            AND e.estadoEjemplar = 'DISPONIBLE'
                        )
                        """)
        List<Object[]> findLibrosBibliotecaConReservaActivaYEjemplarDisponible();

        @Query("""
                            SELECT COUNT(r) > 0 FROM Reserva r
                            WHERE r.ejemplar.idEjemplar = :ejemplarId
                            AND r.usuario.idUsuario = :usuarioId
                            AND r.estadoReserva = :estado
                        """)
        boolean existsByEjemplarIdAndUsuarioIdAndEstadoReserva(
                        @Param("ejemplarId") Long ejemplarId,
                        @Param("usuarioId") Long usuarioId,
                        @Param("estado") EstadoReserva estado);
}

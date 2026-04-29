package com.proyecto.fhce.library.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.enums.notificaciones.EstadoEnvio;
import com.proyecto.fhce.library.enums.notificaciones.TipoNotificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

  // -------------------------------------------------------
  // Consultas para el CRON y procesador de envíos
  // -------------------------------------------------------

  /**
   * Recupera notificaciones pendientes de envío ordenadas por fecha de creación.
   * Usada por el scheduler para procesar el backlog.
   */
  @Query("SELECT n FROM Notificacion n WHERE n.estadoEnvio = :estado ORDER BY n.fechaCreacion ASC")
  List<Notificacion> findByEstadoEnvio(@Param("estado") EstadoEnvio estado);

  /**
   * Notificaciones fallidas que aún pueden reintentarse (intentos < maxIntentos).
   * El scheduler de reintentos usa esta query para evitar bucles infinitos.
   */
  @Query("""
      SELECT n FROM Notificacion n
      WHERE n.estadoEnvio = 'FALLIDO'
        AND n.intentosEnvio < :maxIntentos
      ORDER BY n.fechaCreacion ASC
      """)
  List<Notificacion> findReintentos(@Param("maxIntentos") int maxIntentos);

  // -------------------------------------------------------
  // Validación de duplicados — crítico para no generar spam
  // -------------------------------------------------------

  /**
   * Verifica si ya existe una notificación activa del mismo tipo
   * para el mismo usuario y la misma entidad referenciada.
   * Se usa antes de crear una nueva notificación para evitar duplicados.
   */
  @Query("""
      SELECT COUNT(n) > 0 FROM Notificacion n
      WHERE n.idUsuario        = :idUsuario
        AND n.tipoNotificacion = :tipo
        AND n.idReferencia     = :idReferencia
        AND n.estadoEnvio     IN ('PENDIENTE', 'ENVIADO')
      """)
  boolean existeNotificacionActiva(
      @Param("idUsuario") Long idUsuario,
      @Param("tipo") TipoNotificacion tipo,
      @Param("idReferencia") Long idReferencia);

  // -------------------------------------------------------
  // Consultas para la API (bandeja del usuario)
  // -------------------------------------------------------

  Page<Notificacion> findByIdUsuarioOrderByFechaCreacionDesc(Long idUsuario, Pageable pageable);

  @Query("""
      SELECT n FROM Notificacion n
      WHERE n.idUsuario = :idUsuario
        AND n.fechaLectura IS NULL
      ORDER BY n.fechaCreacion DESC
      """)
  List<Notificacion> findNoLeidasByUsuario(@Param("idUsuario") Long idUsuario);

  @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.idUsuario = :idUsuario AND n.fechaLectura IS NULL")
  long countNoLeidasByUsuario(@Param("idUsuario") Long idUsuario);

  Optional<Notificacion> findByIdNotificacionAndIdUsuario(Long idNotificacion, Long idUsuario);

  // -------------------------------------------------------
  // Operaciones en batch — más eficientes que iterar y guardar
  // -------------------------------------------------------

  /**
   * Marca todas las notificaciones no leídas de un usuario como leídas en un solo
   * UPDATE.
   * Mucho más eficiente que cargar cada entidad y llamar save() una por una.
   */
  @Modifying
  @Query("""
      UPDATE Notificacion n
      SET n.fechaLectura = :ahora
      WHERE n.idUsuario = :idUsuario
        AND n.fechaLectura IS NULL
      """)
  int marcarTodasComoLeidas(@Param("idUsuario") Long idUsuario, @Param("ahora") LocalDateTime ahora);

  // -------------------------------------------------------
  // Limpieza de registros antiguos (housekeeping)
  // -------------------------------------------------------

  @Modifying
  @Query("""
      DELETE FROM Notificacion n
      WHERE n.estadoEnvio = 'ENVIADO'
        AND n.fechaEnvio  < :fechaLimite
      """)
  int eliminarNotificacionesAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);
}

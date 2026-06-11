package com.proyecto.fhce.library.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.dto.reportes.inventario.EstadoEjemplarBibliotecaDTO;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.enums.EstadoEjemplar;

import jakarta.persistence.LockModeType;

@Repository
public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {

  Optional<Ejemplar> findByCodigoEjemplar(String codigoEjemplar);

  List<Ejemplar> findByEdicion_IdEdicion(Long edicionId);

  // List<Ejemplar> findByLibro_IdLibro(Long libroId);
  @Query("SELECT e FROM Ejemplar e WHERE e.edicion.libro.idLibro = :libroId")
  List<Ejemplar> findByLibroId(@Param("libroId") Long libroId);

  List<Ejemplar> findByBiblioteca_IdBiblioteca(Long bibliotecaId);

  List<Ejemplar> findByEstadoEjemplar(EstadoEjemplar estadoEjemplar);

  boolean existsByCodigoEjemplar(String codigoEjemplar);

  @Query("SELECT e FROM Ejemplar e WHERE e.edicion.libro.idLibro = :libroId " +
      "AND e.biblioteca.idBiblioteca = :bibliotecaId " +
      "AND e.estadoEjemplar = :estado")
  List<Ejemplar> findByLibroAndBibliotecaAndEstado(
      @Param("libroId") Long libroId,
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoEjemplar estado);

  // @Lock(LockModeType.PESSIMISTIC_WRITE)
  // @Query("SELECT e FROM Ejemplar e WHERE e.edicion.libro.idLibro = :libroId " +
  // "AND e.biblioteca.idBiblioteca = :bibliotecaId " +
  // "AND e.estadoEjemplar = :estado")
  // Optional<Ejemplar> findFirstDisponibleConLock(
  // @Param("libroId") Long libroId,
  // @Param("bibliotecaId") Long bibliotecaId,
  // @Param("estado") EstadoEjemplar estado);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT e FROM Ejemplar e
      WHERE e.edicion.libro.idLibro = :libroId
      AND e.biblioteca.idBiblioteca = :bibliotecaId
      AND e.estadoEjemplar = :estado
      ORDER BY e.idEjemplar
      """)
  List<Ejemplar> findDisponibleConLock(
      Long libroId,
      Long bibliotecaId,
      EstadoEjemplar estado,
      Pageable pageable);

  @Query("SELECT e FROM Ejemplar e WHERE e.edicion.libro.idLibro = :libroId " +
      "AND e.estadoEjemplar = 'DISPONIBLE'")
  List<Ejemplar> findEjemplaresDisponiblesByLibro(@Param("libroId") Long libroId);

  @Query("SELECT e FROM Ejemplar e " +
      "WHERE e.edicion.idEdicion = :edicionId " +
      "AND e.estadoEjemplar = 'DISPONIBLE'")
  List<Ejemplar> findDisponiblesByEdicion(@Param("edicionId") Long edicionId);

  @Query("""
      SELECT COUNT(e)
      FROM Ejemplar e
      WHERE
      (:bibliotecaId IS NULL
       OR e.biblioteca.idBiblioteca = :bibliotecaId)
      AND
      (:estado IS NULL
       OR e.estadoEjemplar = :estado)
      """)
  Long countByBibliotecaAndEstado(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoEjemplar estado);

  Long countByBiblioteca_IdBiblioteca(Long bibliotecaId);

  @Query("SELECT e FROM Ejemplar e WHERE e.biblioteca.idBiblioteca = :bibliotecaId " +
      "AND e.estadoEjemplar IN :estados")
  List<Ejemplar> findByBibliotecaAndEstadoIn(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estados") List<EstadoEjemplar> estados);

  @Query("SELECT e FROM Ejemplar e " +
      "LEFT JOIN FETCH e.edicion ed " +
      "LEFT JOIN FETCH ed.libro " +
      "WHERE e.idEjemplar = :id")
  Optional<Ejemplar> findByIdWithLibro(@Param("id") Long id);

  // =========================================================
  // NUEVAS CONSULTAS PARA CLASIFICACIÓN
  // =========================================================

  List<Ejemplar> findByClasificacionDecimal(String clasificacionDecimal);

  List<Ejemplar> findByCutterAutor(String cutterAutor);

  List<Ejemplar> findByCutterTitulo(String cutterTitulo);

  @Query("""
      SELECT e
      FROM Ejemplar e
      WHERE e.clasificacionDecimal = :decimal
      AND e.biblioteca.idBiblioteca = :bibliotecaId
      """)
  List<Ejemplar> findByClasificacionAndBiblioteca(
      @Param("decimal") String decimal,
      @Param("bibliotecaId") Long bibliotecaId);

  // =========================================================
  // VALIDACIÓN DE CÓDIGO TOPOGRÁFICO
  // =========================================================

  @Query("""
      SELECT COUNT(e) > 0
      FROM Ejemplar e
      WHERE e.clasificacionDecimal = :decimal
      AND e.cutterAutor = :autor
      AND e.cutterTitulo = :titulo
      AND e.biblioteca.idBiblioteca = :bibliotecaId
      """)
  boolean existsCodigoTopografico(
      @Param("decimal") String decimal,
      @Param("autor") String autor,
      @Param("titulo") String titulo,
      @Param("bibliotecaId") Long bibliotecaId);

  @Query("""
      SELECT COUNT(e)
      FROM Ejemplar e
      """)
  Long countTotalEjemplares();

  @Query("""
          SELECT e
          FROM Ejemplar e
          JOIN FETCH e.edicion ed
          JOIN FETCH ed.libro l
          JOIN FETCH e.biblioteca b
          WHERE
          (:bibliotecaId IS NULL OR b.idBiblioteca = :bibliotecaId)
          AND (:estado IS NULL OR e.estadoEjemplar = :estado)
          AND (:clasificacion IS NULL OR e.clasificacionDecimal = :clasificacion)
          AND (:categoriaId IS NULL OR l.categoria.idCategoria = :categoriaId)
          ORDER BY l.titulo
      """)
  List<Ejemplar> obtenerInventario(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoEjemplar estado,
      @Param("clasificacion") String clasificacion,
      @Param("categoriaId") Long categoriaId);

  @Query("""
          SELECT COUNT(e)
          FROM Ejemplar e
          WHERE e.estadoEjemplar = :estado
          AND (:bibliotecaId IS NULL
               OR e.biblioteca.idBiblioteca = :bibliotecaId)
      """)
  Long countPorEstado(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoEjemplar estado);

  @Query("""
          SELECT new com.proyecto.fhce.library.dto.reportes.inventario.EstadoEjemplarBibliotecaDTO(
              b.nombre,
              SUM(CASE WHEN e.estadoEjemplar = 'DISPONIBLE' THEN 1 ELSE 0 END),
              SUM(CASE WHEN e.estadoEjemplar = 'PRESTADO' THEN 1 ELSE 0 END),
              SUM(CASE WHEN e.estadoEjemplar = 'RESERVADO' THEN 1 ELSE 0 END),
              SUM(CASE WHEN e.estadoEjemplar = 'EN_REPARACION' THEN 1 ELSE 0 END),
              SUM(CASE WHEN e.estadoEjemplar = 'PERDIDO' THEN 1 ELSE 0 END),
              SUM(CASE WHEN e.estadoEjemplar = 'DAÑADO' THEN 1 ELSE 0 END),
              SUM(CASE WHEN e.estadoEjemplar = 'DETERIORADO' THEN 1 ELSE 0 END),
              SUM(CASE WHEN e.estadoEjemplar = 'BAJA' THEN 1 ELSE 0 END)
          )
          FROM Ejemplar e
          JOIN e.biblioteca b
          WHERE (:bibliotecaId IS NULL OR b.idBiblioteca = :bibliotecaId)
          GROUP BY b.idBiblioteca, b.nombre
          ORDER BY b.nombre
      """)
  List<EstadoEjemplarBibliotecaDTO> obtenerEstadoEjemplaresPorBiblioteca(
      @Param("bibliotecaId") Long bibliotecaId);

  // Ejemplares que nunca aparecen en la tabla loans
  @Query("""
      SELECT e FROM Ejemplar e
      JOIN FETCH e.edicion ed
      JOIN FETCH ed.libro l
      JOIN FETCH e.biblioteca b
      WHERE NOT EXISTS (
          SELECT 1 FROM Prestamo p WHERE p.ejemplar = e
      )
      AND (:bibliotecaId IS NULL OR b.idBiblioteca = :bibliotecaId)
      ORDER BY b.nombre, l.titulo
      """)
  List<Ejemplar> findEjemplaresNuncaPrestados(
      @Param("bibliotecaId") Long bibliotecaId);

  @Query("""
      SELECT COUNT(e) FROM Ejemplar e
      WHERE NOT EXISTS (
          SELECT 1 FROM Prestamo p WHERE p.ejemplar = e
      )
      AND (:bibliotecaId IS NULL OR e.biblioteca.idBiblioteca = :bibliotecaId)
      """)
  Long countEjemplaresNuncaPrestados(
      @Param("bibliotecaId") Long bibliotecaId);

}
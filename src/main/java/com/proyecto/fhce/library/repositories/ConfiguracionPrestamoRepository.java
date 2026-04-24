package com.proyecto.fhce.library.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;

@Repository
public interface ConfiguracionPrestamoRepository
        extends JpaRepository<ConfiguracionPrestamo, Long> {

    // Búsqueda principal: una sola configuración por biblioteca
    @Query("""
            SELECT c FROM ConfiguracionPrestamo c
            LEFT JOIN FETCH c.biblioteca
            WHERE c.biblioteca.idBiblioteca = :bibliotecaId
            """)
    Optional<ConfiguracionPrestamo> findByBibliotecaId(
            @Param("bibliotecaId") Long bibliotecaId);

    // Verificación de duplicado (excluye el registro actual en edición)
    @Query("""
            SELECT COUNT(c) > 0 FROM ConfiguracionPrestamo c
            WHERE c.biblioteca.idBiblioteca = :bibliotecaId
              AND (:excludeId IS NULL OR c.idConfig != :excludeId)
            """)
    boolean existeConfiguracionParaBiblioteca(
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("excludeId") Long excludeId);
}

package com.proyecto.fhce.library.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.enums.RolEncargado;

@Repository
public interface BibliotecaEncargadoRepository extends JpaRepository<BibliotecaEncargado, Long> {

    List<BibliotecaEncargado> findByBiblioteca_IdBibliotecaAndActivoTrue(Long bibliotecaId);

    List<BibliotecaEncargado> findByUsuario_IdUsuarioAndActivoTrue(Long usuarioId);

    Optional<BibliotecaEncargado> findByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(
            Long bibliotecaId, Long usuarioId);

    boolean existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(
            Long bibliotecaId, Long usuarioId);

    @Query("""
                SELECT be FROM BibliotecaEncargado be
                JOIN FETCH be.usuario u
                JOIN FETCH u.persona p
                WHERE be.biblioteca.idBiblioteca = :bibliotecaId
                  AND be.activo = true
                ORDER BY be.rolEncargado ASC, be.fechaAsignacion ASC
            """)
    List<BibliotecaEncargado> findEncargadosActivosConDetalle(@Param("bibliotecaId") Long bibliotecaId);

    @Query("""
                SELECT be FROM BibliotecaEncargado be
                WHERE be.biblioteca.idBiblioteca = :bibliotecaId
                  AND be.rolEncargado = :rol
                  AND be.activo = true
            """)
    Optional<BibliotecaEncargado> findEncargadoPrincipal(
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("rol") RolEncargado rol);

    @Modifying
    @Query("""
                UPDATE BibliotecaEncargado be
                SET be.activo = false, be.fechaFin = :fechaFin
                WHERE be.biblioteca.idBiblioteca = :bibliotecaId
                  AND be.usuario.idUsuario = :usuarioId
            """)
    void desactivarEncargado(
            @Param("bibliotecaId") Long bibliotecaId,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaFin") LocalDateTime fechaFin);

    boolean existsByUsuario_IdUsuarioAndActivoTrue(Long usuarioId);
}
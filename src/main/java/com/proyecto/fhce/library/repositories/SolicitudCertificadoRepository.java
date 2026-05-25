package com.proyecto.fhce.library.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.enums.EstadoSolicitud;

import java.util.List;

@Repository
public interface SolicitudCertificadoRepository
                extends JpaRepository<SolicitudCertificado, Long> {

        // Obtener solicitudes por estado
        List<SolicitudCertificado> findByEstado(
                        EstadoSolicitud estado);

        // Obtener solicitudes de una biblioteca
        List<SolicitudCertificado> findByBiblioteca_IdBiblioteca(
                        Long bibliotecaId);

        // Obtener solicitudes de un usuario
        List<SolicitudCertificado> findByUsuario_IdUsuario(
                        Long usuarioId);

        // Buscar por CI
        List<SolicitudCertificado> findByCi(
                        String ci);

        // Buscar por matrícula
        List<SolicitudCertificado> findByMatricula(
                        String matricula);

        // Obtener solicitudes por biblioteca y estado
        List<SolicitudCertificado> findByBiblioteca_IdBibliotecaAndEstado(
                        Long bibliotecaId,
                        EstadoSolicitud estado);

        Page<SolicitudCertificado> findByUsuario_IdUsuario(
                        Long usuarioId,
                        Pageable pageable);

        Page<SolicitudCertificado> findAll(Pageable pageable);

        /**
         * Verifica si existe una solicitud PENDIENTE para el mismo CI,
         * misma biblioteca y misma razón.
         */
        boolean existsByCiAndBiblioteca_IdBibliotecaAndRazonCertificado_IdRazonAndEstado(
                        String ci,
                        Long bibliotecaId,
                        Long razonId,
                        EstadoSolicitud estado);

        // Opcional: más flexible (cualquier solicitud pendiente en la biblioteca)
        boolean existsByCiAndBiblioteca_IdBibliotecaAndEstado(
                        String ci,
                        Long bibliotecaId,
                        EstadoSolicitud estado);

}
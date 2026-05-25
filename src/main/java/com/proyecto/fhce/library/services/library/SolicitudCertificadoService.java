package com.proyecto.fhce.library.services.library;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.request.library.SolicitudCertificadoRequest;
import com.proyecto.fhce.library.dto.response.library.SolicitudCertificadoResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoSolicitud;
import com.proyecto.fhce.library.enums.RolEncargado;
import com.proyecto.fhce.library.enums.notificaciones.TipoNotificacion;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.RazonCertificadoRepository;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.loads.CertificadoNoDeudaServiceImpl;
import com.proyecto.fhce.library.services.notificaciones.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SolicitudCertificadoService {

    private final SolicitudCertificadoRepository solicitudRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final UserRepository usuarioRepository;
    private final RazonCertificadoRepository razonCertificadoRepository;
    private final NotificacionService notificacionService;
    private static final Logger log = LoggerFactory.getLogger(CertificadoNoDeudaServiceImpl.class);

    public SolicitudCertificadoService(
            SolicitudCertificadoRepository solicitudRepository,
            BibliotecaRepository bibliotecaRepository,
            UserRepository usuarioRepository,
            RazonCertificadoRepository razonCertificadoRepository,
            NotificacionService notificacionService) {
        this.solicitudRepository = solicitudRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.usuarioRepository = usuarioRepository;
        this.razonCertificadoRepository = razonCertificadoRepository;
        this.notificacionService = notificacionService;
    }

    // =====================================================
    // CREAR SOLICITUD
    // =====================================================
    @Transactional
    public SolicitudCertificadoResponse crearSolicitud(
            SolicitudCertificadoRequest request,
            Long solicitanteId,
            Collection<? extends GrantedAuthority> authorities) {

        Biblioteca biblioteca = bibliotecaRepository.findById(
                request.getBibliotecaId()).orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

        RazonCertificado razon = razonCertificadoRepository
                .findById(request.getRazonCertificadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Razón no encontrada"));
        if (razon.getBiblioteca() != null &&
                !razon.getBiblioteca().getIdBiblioteca()
                        .equals(biblioteca.getIdBiblioteca())) {

            throw new BusinessException(
                    "Esta razón no está disponible para la biblioteca seleccionada");
        }

        boolean yaTieneSolicitudPendiente = solicitudRepository
                .existsByCiAndBiblioteca_IdBibliotecaAndRazonCertificado_IdRazonAndEstado(
                        request.getCi(),
                        biblioteca.getIdBiblioteca(),
                        razon.getIdRazon(),
                        EstadoSolicitud.PENDIENTE);

        if (yaTieneSolicitudPendiente) {
            throw new BusinessException(
                    "El usuario ya tiene una solicitud pendiente para esta razón en esta biblioteca. " +
                            "Debe esperar a que sea procesada antes de realizar otra.");
        }

        SolicitudCertificado solicitud = new SolicitudCertificado();

        solicitud.setBiblioteca(biblioteca);

        // intentar vincular usuario si existe
        Optional<Usuario> usuarioOpt = usuarioRepository.findByPersona_Ci(request.getCi());

        usuarioOpt.ifPresent(solicitud::setUsuario);
        solicitud.setBiblioteca(biblioteca);

        solicitud.setNombres(request.getNombres());
        solicitud.setApellidos(request.getApellidos());
        solicitud.setCi(request.getCi());
        solicitud.setMatricula(request.getMatricula());
        solicitud.setEmail(request.getEmail());
        solicitud.setTelefono(request.getTelefono());
        solicitud.setRazonCertificado(razon);
        solicitud.setDescripcion(request.getDescripcion());

        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setAtendidoPor(null);
        SolicitudCertificado saved = solicitudRepository.save(solicitud);
        enviarNotificacionesEncargados(saved, biblioteca);
        return mapToResponse(saved);
    }

    // =====================================================
    // LISTAR POR BIBLIOTECA
    // =====================================================
    @Transactional(readOnly = true)
    public List<SolicitudCertificadoResponse> listarPorBiblioteca(
            Long bibliotecaId,
            EstadoSolicitud estado,
            Long solicitanteId,
            Collection<? extends GrantedAuthority> authorities) {

        List<SolicitudCertificado> lista;

        if (estado != null) {
            lista = solicitudRepository
                    .findByBiblioteca_IdBibliotecaAndEstado(
                            bibliotecaId,
                            estado);
        } else {
            lista = solicitudRepository
                    .findByBiblioteca_IdBiblioteca(bibliotecaId);
        }

        return lista.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // OBTENER POR ID
    // =====================================================
    @Transactional(readOnly = true)
    public SolicitudCertificadoResponse obtenerPorId(
            Long id,
            Long solicitanteId,
            Collection<? extends GrantedAuthority> authorities) {

        SolicitudCertificado solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitud no encontrada"));

        return mapToResponse(solicitud);
    }

    // =====================================================
    // APROBAR
    // =====================================================
    @Transactional
    public SolicitudCertificadoResponse aprobarSolicitud(
            Long id,
            String observacion,
            Long solicitanteId,
            Collection<? extends GrantedAuthority> authorities) {

        SolicitudCertificado solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitud no encontrada"));
        // Cargar el usuario que está atendiendo
        Usuario bibliotecario = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservacionRespuesta(observacion);
        solicitud.setAtendidoPor(bibliotecario);

        return mapToResponse(
                solicitudRepository.save(solicitud));
    }

    // =====================================================
    // RECHAZAR
    // =====================================================
    @Transactional
    public SolicitudCertificadoResponse rechazarSolicitud(
            Long id,
            String observacion,
            Long solicitanteId,
            Collection<? extends GrantedAuthority> authorities) {

        SolicitudCertificado solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitud no encontrada"));
        Usuario bibliotecario = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservacionRespuesta(observacion);
        solicitud.setAtendidoPor(bibliotecario);

        return mapToResponse(
                solicitudRepository.save(solicitud));
    }

    // =====================================================
    // MIS SOLICITUDES (para el estudiante)
    // =====================================================
    @Transactional(readOnly = true)
    public Page<SolicitudCertificadoResponse> obtenerMisSolicitudes(
            Long usuarioId,
            Pageable pageable) {

        if (usuarioId == null) {
            throw new BusinessException("Usuario no autenticado");
        }

        Page<SolicitudCertificado> solicitudes = solicitudRepository
                .findByUsuario_IdUsuario(usuarioId, pageable);

        return solicitudes.map(this::mapToResponse);
    }

    // =====================================================
    // TODAS LAS SOLICITUDES (solo ADMIN)
    // =====================================================
    @Transactional(readOnly = true)
    public Page<SolicitudCertificadoResponse> listarTodasLasSolicitudes(Pageable pageable) {

        Page<SolicitudCertificado> solicitudes = solicitudRepository
                .findAll(pageable);

        return solicitudes.map(this::mapToResponse);
    }

    private void enviarNotificacionesEncargados(SolicitudCertificado solicitud,
            Biblioteca biblioteca) {

        // Nombre del solicitante (funciona tanto si está registrado como si no)
        String nombreSolicitante = solicitud.getUsuario() != null
                ? solicitud.getUsuario().getPersona().getApellido_pat() + " " +
                        solicitud.getUsuario().getPersona().getNombre()
                : solicitud.getNombres() + " " + solicitud.getApellidos();

        String idSolicitante = solicitud.getUsuario() != null
                ? solicitud.getUsuario().getId_usuario().toString()
                : solicitud.getCi();

        // Razón del certificado
        String nombreRazon = solicitud.getRazonCertificado() != null
                ? solicitud.getRazonCertificado().getNombre()
                : ""; // fallback por si acaso

        String asunto = "Nueva solicitud de Certificado No Deuda";

        String mensaje = String.format("""
                Se ha recibido una nueva solicitud de Certificado de No Deuda.

                Solicitante: %s
                %s: %s
                Razón: %s
                Biblioteca: %s

                Descripción: %s
                Por favor revisar y procesar la solicitud.""",

                nombreSolicitante,
                solicitud.getUsuario() != null ? "ID Usuario" : "CI",
                idSolicitante,
                nombreRazon,
                biblioteca.getNombre(),
                StringUtils.hasText(solicitud.getDescripcion()) ? solicitud.getDescripcion()
                        : "Sin descripción");

        // Obtener todos los encargados de la biblioteca
        for (BibliotecaEncargado encargado : biblioteca.getEncargados()) {
            Usuario usuarioEncargado = encargado.getUsuario();

            // Filtrar solo quienes tengan ROLE_BIBLIOTECARIO o ROLE_AUXILIAR
            if (encargado.getRolEncargado() == RolEncargado.PRINCIPAL ||
                    encargado.getRolEncargado() == RolEncargado.AUXILIAR) {

                CrearNotificacionRequest notifRequest = new CrearNotificacionRequest(
                        usuarioEncargado.getId_usuario(),
                        TipoNotificacion.CERTIFICADO, // Puedes crear SOLICITUD_CERTIFICADO después
                        asunto,
                        mensaje,
                        null, // canal
                        solicitud.getId(), // idReferencia
                        "SOLICITUD_CERTIFICADO" // tipoReferencia
                );

                try {
                    notificacionService.crear(notifRequest);
                } catch (Exception e) {
                    log.warn("No se pudo crear notificación para usuario {}",
                            usuarioEncargado.getId_usuario(), e);
                }
            }
        }
    }

    // =====================================================
    // MAPPER
    // =====================================================
    private SolicitudCertificadoResponse mapToResponse(
            SolicitudCertificado solicitud) {

        SolicitudCertificadoResponse response = new SolicitudCertificadoResponse();

        response.setId(solicitud.getId());
        response.setNombres(solicitud.getNombres());
        response.setApellidos(solicitud.getApellidos());
        response.setCi(solicitud.getCi());
        response.setMatricula(solicitud.getMatricula());
        response.setUsuarioId(solicitud.getUsuario().getId_usuario());
        response.setEmail(solicitud.getEmail());
        response.setTelefono(solicitud.getTelefono());
        response.setBibliotecaId(solicitud.getBiblioteca().getIdBiblioteca());
        response.setBibliotecaNombre(
                solicitud.getBiblioteca().getNombre());

        response.setRazonId(solicitud.getRazonCertificado().getIdRazon());
        response.setRazonNombre(solicitud.getRazonCertificado().getNombre());
        response.setRequisitos(solicitud.getRazonCertificado().getRequisitos());
        response.setDescripcion(solicitud.getDescripcion());

        response.setEstado(
                solicitud.getEstado().name());

        response.setFechaSolicitud(
                solicitud.getFechaSolicitud());

        response.setFechaRespuesta(
                solicitud.getFechaRespuesta());

        response.setObservacionRespuesta(
                solicitud.getObservacionRespuesta());
        if (solicitud.getAtendidoPor() != null) {
            response.setAtendidoPorId(solicitud.getAtendidoPor().getId_usuario());
            response.setAtendidoPorNombre(solicitud.getAtendidoPor().getPersona().getApellido_pat() + " "
                    + solicitud.getAtendidoPor().getPersona().getNombre()); // o como tengas el nombre
        }
        return response;
    }
}
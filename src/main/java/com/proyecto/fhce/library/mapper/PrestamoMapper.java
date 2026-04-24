package com.proyecto.fhce.library.mapper;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.dto.response.library.BibliotecaSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.EdicionSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.library.LibroSimpleResponse;
import com.proyecto.fhce.library.dto.response.loads.PrestamoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoPrestamo;

@Component
public class PrestamoMapper {

  public PrestamoResponse toResponse(Prestamo prestamo) {
    PrestamoResponse response = new PrestamoResponse();

    response.setId_prestamo(prestamo.getIdPrestamo());
    response.setEstadoPrestamo(prestamo.getEstadoPrestamo());
    response.setFechaPrestamo(prestamo.getFechaPrestamo());
    response.setFechaDevolucionEstimada(prestamo.getFechaDevolucionEstimada());
    response.setFechaDevolucionReal(prestamo.getFechaDevolucionReal());
    response.setRenovaciones(prestamo.getRenovaciones());
    response.setObservaciones(prestamo.getObservaciones());
    response.setTipoDocumentoGarantia(prestamo.getTipoDocumentoGarantia());
    response.setCondicionEntrega(prestamo.getCondicionEntrega());
    response.setCondicionDevolucion(prestamo.getCondicionDevolucion());
    response.setIdConfigUsado(prestamo.getIdConfigUsado());

    // Flag de conveniencia: ¿está vencido en este momento?
    boolean vencido = (prestamo.getEstadoPrestamo() == EstadoPrestamo.ACTIVO
        || prestamo.getEstadoPrestamo() == EstadoPrestamo.RENOVADO)
        && LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada());
    response.setVencido(vencido);

    mapEjemplar(prestamo, response);
    mapUsuario(prestamo.getUsuario(), response);
    mapBiblioteca(prestamo, response);
    mapBibliotecarios(prestamo, response);

    return response;
  }

  // ==================== PRIVATE HELPERS ====================

  private void mapEjemplar(Prestamo prestamo, PrestamoResponse response) {
    if (prestamo.getEjemplar() == null)
      return;

    EjemplarResponse er = new EjemplarResponse();
    er.setId_ejemplar(prestamo.getEjemplar().getIdEjemplar());
    er.setCodigoEjemplar(prestamo.getEjemplar().getCodigoEjemplar());
    er.setEstadoEjemplar(prestamo.getEjemplar().getEstadoEjemplar());

    if (prestamo.getEjemplar().getEdicion() != null) {
      EdicionSimpleResponse ed = new EdicionSimpleResponse();
      ed.setIdEdicion(prestamo.getEjemplar().getEdicion().getIdEdicion());
      ed.setIsbn(prestamo.getEjemplar().getEdicion().getIsbn());
      ed.setEditorial(prestamo.getEjemplar().getEdicion().getEditorial());
      ed.setAnoPublicacion(prestamo.getEjemplar().getEdicion().getAnoPublicacion());
      ed.setEdicion(prestamo.getEjemplar().getEdicion().getEdicion());
      ed.setImagenPortada(prestamo.getEjemplar().getEdicion().getImagenPortada());

      if (prestamo.getEjemplar().getEdicion().getLibro() != null) {
        ed.setIdLibro(prestamo.getEjemplar().getEdicion().getLibro().getIdLibro());
        ed.setTitulo(prestamo.getEjemplar().getEdicion().getLibro().getTitulo());

        LibroSimpleResponse libro = new LibroSimpleResponse();
        libro.setIdLibro(prestamo.getEjemplar().getEdicion().getLibro().getIdLibro());
        libro.setTitulo(prestamo.getEjemplar().getEdicion().getLibro().getTitulo());
        er.setLibro(libro);
      }
      er.setEdicion(ed);
    }

    response.setEjemplar(er);
  }

  private void mapUsuario(Usuario usuario, PrestamoResponse response) {
    if (usuario == null)
      return;
    response.setUsuario(buildUsuarioSimple(usuario));
  }

  private void mapBiblioteca(Prestamo prestamo, PrestamoResponse response) {
    if (prestamo.getBiblioteca() == null)
      return;
    BibliotecaSimpleResponse br = new BibliotecaSimpleResponse();
    br.setId_biblioteca(prestamo.getBiblioteca().getIdBiblioteca());
    br.setNombre(prestamo.getBiblioteca().getNombre());
    response.setBiblioteca(br);
  }

  private void mapBibliotecarios(Prestamo prestamo, PrestamoResponse response) {
    if (prestamo.getBibliotecarioPrestamo() != null) {
      response.setBibliotecarioPrestamo(buildUsuarioSimple(prestamo.getBibliotecarioPrestamo()));
    }
    if (prestamo.getBibliotecarioDevolucion() != null) {
      response.setBibliotecarioDevolucion(buildUsuarioSimple(prestamo.getBibliotecarioDevolucion()));
    }
  }

  private UsuarioSimpleResponse buildUsuarioSimple(Usuario usuario) {
    UsuarioSimpleResponse u = new UsuarioSimpleResponse();
    u.setId_usuario(usuario.getId_usuario());
    u.setUsername(usuario.getUsername());
    if (usuario.getPersona() != null) {
      u.setNombreCompleto(usuario.getPersona().getApellido_pat() + " " + usuario.getPersona().getNombre());
      u.setCi(usuario.getPersona().getCi());
    }
    return u;
  }
}

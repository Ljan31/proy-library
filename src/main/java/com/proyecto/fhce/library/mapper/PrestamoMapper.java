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
    boolean vencido = prestamo.getEstadoPrestamo() == EstadoPrestamo.VENCIDO
        || ((prestamo.getEstadoPrestamo() == EstadoPrestamo.ACTIVO
            || prestamo.getEstadoPrestamo() == EstadoPrestamo.RENOVADO)
            && LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada()));
    response.setVencido(vencido);

    mapEjemplar(prestamo, response);
    mapUsuario(prestamo.getUsuario(), response);
    mapBiblioteca(prestamo, response);
    mapBibliotecarios(prestamo, response);

    return response;
  }

  // ==================== PRIVATE HELPERS ====================

  private void mapEjemplar(Prestamo prestamo, PrestamoResponse response) {
    if (prestamo.getEjemplar() == null) {
      return;
    }

    var ejemplar = prestamo.getEjemplar();

    EjemplarResponse er = new EjemplarResponse();
    er.setId_ejemplar(ejemplar.getIdEjemplar());
    er.setCodigoEjemplar(ejemplar.getCodigoEjemplar());
    er.setEstadoEjemplar(ejemplar.getEstadoEjemplar());

    // Agregar si existe este campo y su setter en EjemplarResponse
    er.setCodigoTopograficoConcat(ejemplar.getCodigoTopograficoConcat());
    if (ejemplar.getEdicion() != null) {
      var edicion = ejemplar.getEdicion();

      EdicionSimpleResponse ed = new EdicionSimpleResponse();
      ed.setIdEdicion(edicion.getIdEdicion());
      ed.setEditorial(edicion.getEditorial());
      ed.setAnoPublicacion(edicion.getAnoPublicacion());
      ed.setEdicion(edicion.getEdicion());
      ed.setImagenPortada(edicion.getImagenPortada());

      if (edicion.getLibro() != null) {
        var libroEntidad = edicion.getLibro();

        ed.setIdLibro(libroEntidad.getIdLibro());
        ed.setTitulo(libroEntidad.getTitulo());

        LibroSimpleResponse libro = new LibroSimpleResponse();
        libro.setIdLibro(libroEntidad.getIdLibro());
        libro.setTitulo(libroEntidad.getTitulo());

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

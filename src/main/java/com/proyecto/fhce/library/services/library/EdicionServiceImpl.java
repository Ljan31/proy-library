package com.proyecto.fhce.library.services.library;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.library.EdicionRequest;
import com.proyecto.fhce.library.dto.response.library.EdicionResponse;
import com.proyecto.fhce.library.dto.response.library.LibroSimpleResponse;
import com.proyecto.fhce.library.entities.Edicion;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.EdicionRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;

@Service
@Transactional
public class EdicionServiceImpl implements EdicionService {

  @Autowired
  private EdicionRepository edicionRepository;

  @Autowired
  private LibroRepository libroRepository;

  @Autowired
  private EjemplarRepository ejemplarRepository;

  public EdicionResponse create(EdicionRequest request) {
    if (edicionRepository.existsByIsbn(request.getIsbn())) {
      throw new DuplicateResourceException("Ya existe una edición con ISBN: " + request.getIsbn());
    }

    Libro libro = libroRepository.findById(request.getLibroId())
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + request.getLibroId()));

    Edicion edicion = new Edicion();
    edicion.setIsbn(request.getIsbn());
    edicion.setEditorial(request.getEditorial());
    edicion.setAnoPublicacion(request.getAnoPublicacion());
    edicion.setEdicion(request.getEdicion());
    edicion.setNumeroPaginas(request.getNumeroPaginas());
    edicion.setImagenPortada(request.getImagenPortada());
    edicion.setLibro(libro);

    return mapToResponse(edicionRepository.save(edicion));
  }

  public EdicionResponse update(Long id, EdicionRequest request) {
    Edicion edicion = edicionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Edición no encontrada con id: " + id));

    if (!edicion.getIsbn().equals(request.getIsbn()) &&
        edicionRepository.existsByIsbn(request.getIsbn())) {
      throw new DuplicateResourceException("Ya existe una edición con ISBN: " + request.getIsbn());
    }

    edicion.setIsbn(request.getIsbn());
    edicion.setEditorial(request.getEditorial());
    edicion.setAnoPublicacion(request.getAnoPublicacion());
    edicion.setEdicion(request.getEdicion());
    edicion.setNumeroPaginas(request.getNumeroPaginas());
    edicion.setImagenPortada(request.getImagenPortada());

    return mapToResponse(edicionRepository.save(edicion));
  }

  @Transactional(readOnly = true)
  public EdicionResponse findById(Long id) {
    Edicion edicion = edicionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Edición no encontrada con id: " + id));
    return mapToResponse(edicion);
  }

  @Transactional(readOnly = true)
  public List<EdicionResponse> findByLibro(Long libroId) {
    return edicionRepository.findByLibro_IdLibro(libroId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public void delete(Long id) {
    Edicion edicion = edicionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Edición no encontrada con id: " + id));

    boolean tieneEjemplares = !ejemplarRepository.findByEdicion_IdEdicion(id).isEmpty();
    if (tieneEjemplares) {
      throw new BusinessException("No se puede eliminar una edición que tiene ejemplares registrados");
    }

    edicionRepository.delete(edicion);
  }

  private EdicionResponse mapToResponse(Edicion edicion) {
    EdicionResponse response = new EdicionResponse();
    response.setIdEdicion(edicion.getIdEdicion());
    response.setIsbn(edicion.getIsbn());
    response.setEditorial(edicion.getEditorial());
    response.setAnoPublicacion(edicion.getAnoPublicacion());
    response.setEdicion(edicion.getEdicion());
    response.setNumeroPaginas(edicion.getNumeroPaginas());
    response.setImagenPortada(edicion.getImagenPortada());

    if (edicion.getLibro() != null) {
      LibroSimpleResponse libro = new LibroSimpleResponse();
      libro.setIdLibro(edicion.getLibro().getIdLibro());
      libro.setTitulo(edicion.getLibro().getTitulo());
      response.setLibro(libro);
    }

    // Conteo de ejemplares de esta edición
    List<Ejemplar> ejemplares = ejemplarRepository.findByEdicion_IdEdicion(edicion.getIdEdicion());
    response.setEjemplaresTotal(ejemplares.size());
    response.setEjemplaresDisponibles(
        (int) ejemplares.stream()
            .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.DISPONIBLE)
            .count());

    return response;
  }
}
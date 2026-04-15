package com.proyecto.fhce.library.services.library;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.library.BusquedaLibroRequest;
import com.proyecto.fhce.library.dto.request.library.LibroRequest;
import com.proyecto.fhce.library.dto.response.PageResponse;
import com.proyecto.fhce.library.dto.response.library.CategoriaLibroResponse;
import com.proyecto.fhce.library.dto.response.library.EdicionResponse;
import com.proyecto.fhce.library.dto.response.library.LibroResponse;
import com.proyecto.fhce.library.entities.CategoriaLibro;
import com.proyecto.fhce.library.entities.Edicion;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.CategoriaLibroRepository;
import com.proyecto.fhce.library.repositories.EdicionRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;

@Service
public class LibroServiceImpl implements LibroService {
  @Autowired
  private LibroRepository libroRepository;

  @Autowired
  private CategoriaLibroRepository categoriaRepository;

  @Autowired
  private EjemplarRepository ejemplarRepository;

  @Autowired
  private EdicionRepository edicionRepository;

  @Transactional
  public LibroResponse create(LibroRequest request) {
    Libro libro = new Libro();
    libro.setTitulo(request.getTitulo());
    libro.setIdioma(request.getIdioma());
    libro.setDescripcion(request.getDescripcion());

    if (request.getCategoriaId() != null) {
      CategoriaLibro categoria = categoriaRepository.findById(request.getCategoriaId())
          .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
      libro.setCategoria(categoria);
    }

    return mapToResponse(libroRepository.save(libro));
  }

  @Transactional
  public LibroResponse update(Long id, LibroRequest request) {
    Libro libro = libroRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));

    libro.setTitulo(request.getTitulo());
    libro.setIdioma(request.getIdioma());
    libro.setDescripcion(request.getDescripcion());

    if (request.getCategoriaId() != null) {
      CategoriaLibro categoria = categoriaRepository.findById(request.getCategoriaId())
          .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
      libro.setCategoria(categoria);
    }

    return mapToResponse(libroRepository.save(libro));
  }

  @Transactional(readOnly = true)
  public LibroResponse findById(Long id) {
    // Libro libro = libroRepository.findByIdWithAutores(id)
    Libro libro = libroRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));
    return mapToResponse(libro);
  }

  @Transactional(readOnly = true)
  public List<LibroResponse> findAll() {
    return libroRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LibroResponse> search(String searchTerm, Long bibliotecaId) {
    return libroRepository.searchLibros(searchTerm, bibliotecaId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public PageResponse<LibroResponse> busquedaAvanzada(BusquedaLibroRequest request, Pageable pageable) {
    // Aquí implementarías una búsqueda con Specification o Criteria API
    // Por simplicidad, usamos búsqueda básica
    // List<Libro> libros = libroRepository.findAll();
    Specification<Libro> spec = Specification.where(null);

    // Aplicar filtros
    if (request.getTitulo() != null) {
      spec = spec.and((root, query, cb) -> cb.like(
          cb.lower(root.get("titulo")),
          "%" + request.getTitulo().toLowerCase() + "%"));
    }

    if (request.getCategoriaId() != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("idCategoria"),
          request.getCategoriaId()));
    }

    // ✅ isbn y editorial ahora viven en Edicion — se buscan por join
    if (request.getIsbn() != null) {
      spec = spec.and((root, query, cb) -> {
        query.distinct(true);
        return cb.equal(root.join("ediciones").get("isbn"), request.getIsbn());
      });
    }
    // ResultadoBusquedaLibroResponse response = new
    // ResultadoBusquedaLibroResponse();
    // response.setLibros(libros.stream().map(this::mapToResponse).collect(Collectors.toList()));
    // response.setTotalResultados(libros.size());
    // Page<Libro> page = libroRepository.findAll(spec, pageable);

    // ResultadoBusquedaLibroResponse response = new
    // ResultadoBusquedaLibroResponse();
    // response.setLibros(
    // page.getContent().stream()
    // .map(this::mapToResponse)
    // .collect(Collectors.toList())
    // );
    // response.setTotalResultados(page.getTotalElements());
    Page<LibroResponse> page = libroRepository.findAll(spec, pageable).map(this::mapToResponse);
    return new PageResponse<>(page);
  }

  @Transactional
  public void delete(Long id) {
    Libro categoria = libroRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrada con id: " + id));

    libroRepository.delete(categoria);

  }

  private LibroResponse mapToResponse(Libro libro) {
    LibroResponse response = new LibroResponse();
    response.setIdLibro(libro.getIdLibro());
    response.setTitulo(libro.getTitulo());
    response.setIdioma(libro.getIdioma());
    response.setDescripcion(libro.getDescripcion());

    // Categoría
    if (libro.getCategoria() != null) {
      response.setCategoria(mapCategoriaToResponse(libro.getCategoria()));
    }

    // ✅ Ediciones del libro
    List<Edicion> ediciones = edicionRepository.findByLibro_IdLibro(libro.getIdLibro());
    response.setEdiciones(ediciones.stream().map(ed -> {
      EdicionResponse er = new EdicionResponse();
      er.setIdEdicion(ed.getIdEdicion());
      er.setIsbn(ed.getIsbn());
      er.setEditorial(ed.getEditorial());
      er.setAnoPublicacion(ed.getAnoPublicacion());
      er.setEdicion(ed.getEdicion());
      er.setImagenPortada(ed.getImagenPortada());
      return er;
    }).collect(Collectors.toList()));

    // ✅ Totales agregados de todos los ejemplares del libro (via ediciones)
    List<Ejemplar> todosEjemplares = ejemplarRepository.findByLibroId(libro.getIdLibro());
    response.setEjemplaresTotal(todosEjemplares.size());
    response.setEjemplaresDisponibles(
        (int) todosEjemplares.stream()
            .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.DISPONIBLE)
            .count());

    return response;
  }

  private CategoriaLibroResponse mapCategoriaToResponse(CategoriaLibro categoria) {
    CategoriaLibroResponse response = new CategoriaLibroResponse();
    response.setId_categoria(categoria.getId_categoria());
    response.setNombre_categoria(categoria.getNombre_categoria());
    response.setDescripcion(categoria.getDescripcion());
    response.setCodigo_dewey(categoria.getCodigo_dewey());
    return response;
  }
}

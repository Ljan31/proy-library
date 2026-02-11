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
import com.proyecto.fhce.library.dto.response.library.LibroResponse;
import com.proyecto.fhce.library.entities.CategoriaLibro;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.CategoriaLibroRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;

@Service
public class LibroServiceImpl implements LibroService {
  @Autowired
  private LibroRepository libroRepository;

  @Autowired
  private CategoriaLibroRepository categoriaRepository;

  // @Autowired
  // private EjemplarRepository ejemplarRepository;
  @Transactional
  public LibroResponse create(LibroRequest request) {
    if (request.getIsbn() != null && libroRepository.existsByIsbn(request.getIsbn())) {
      throw new DuplicateResourceException("Ya existe un libro con ISBN: " + request.getIsbn());
    }

    Libro libro = new Libro();
    libro.setIsbn(request.getIsbn());
    libro.setTitulo(request.getTitulo());
    libro.setEditorial(request.getEditorial());
    libro.setAnoPublicacion(request.getAnoPublicacion());
    libro.setEdicion(request.getEdicion());
    libro.setNumero_paginas(request.getNumero_paginas());
    libro.setIdioma(request.getIdioma());
    libro.setDescripcion(request.getDescripcion());
    libro.setImagen_portada(request.getImagen_portada());

    // Asignar categoría
    if (request.getCategoriaId() != null) {
      CategoriaLibro categoria = categoriaRepository.findById(request.getCategoriaId())
          .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
      libro.setCategoria(categoria);
    }

    Libro saved = libroRepository.save(libro);
    return mapToResponse(saved);
  }

  @Transactional
  public LibroResponse update(Long id, LibroRequest request) {
    Libro libro = libroRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));

    // Validar ISBN si cambió
    if (request.getIsbn() != null &&
        !request.getIsbn().equals(libro.getIsbn()) &&
        libroRepository.existsByIsbn(request.getIsbn())) {
      throw new DuplicateResourceException("Ya existe un libro con ISBN: " + request.getIsbn());
    }

    libro.setIsbn(request.getIsbn());
    libro.setTitulo(request.getTitulo());
    libro.setEditorial(request.getEditorial());
    libro.setAnoPublicacion(request.getAnoPublicacion());
    libro.setEdicion(request.getEdicion());
    libro.setNumero_paginas(request.getNumero_paginas());
    libro.setIdioma(request.getIdioma());
    libro.setDescripcion(request.getDescripcion());
    libro.setImagen_portada(request.getImagen_portada());

    if (request.getCategoriaId() != null) {
      CategoriaLibro categoria = categoriaRepository.findById(request.getCategoriaId())
          .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
      libro.setCategoria(categoria);
    }

    Libro updated = libroRepository.save(libro);
    return mapToResponse(updated);
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
  public List<LibroResponse> search(String searchTerm) {
    return libroRepository.searchLibros(searchTerm).stream()
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

    if (request.getIsbn() != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("isbn"), request.getIsbn()));
    }

    if (request.getCategoriaId() != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("idCategoria"),
          request.getCategoriaId()));
    }

    // ResultadoBusquedaLibroResponse response = new
    // ResultadoBusquedaLibroResponse();
    // response.setLibros(libros.stream().map(this::mapToResponse).collect(Collectors.toList()));
    // response.setTotalResultados(libros.size());
    Page<Libro> page = libroRepository.findAll(spec, pageable);

    // ResultadoBusquedaLibroResponse response = new
    // ResultadoBusquedaLibroResponse();
    // response.setLibros(
    // page.getContent().stream()
    // .map(this::mapToResponse)
    // .collect(Collectors.toList())
    // );
    // response.setTotalResultados(page.getTotalElements());
    Page<LibroResponse> dtoPage = page.map(this::mapToResponse);
    // return response;
    return new PageResponse<>(dtoPage);
  }

  @Transactional
  public void delete(Long id) {
    Libro categoria = libroRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrada con id: " + id));

    libroRepository.delete(categoria);

  }

  private LibroResponse mapToResponse(Libro libro) {
    LibroResponse response = new LibroResponse();
    response.setId_libro(libro.getId_libro());
    response.setIsbn(libro.getIsbn());
    response.setTitulo(libro.getTitulo());
    response.setEditorial(libro.getEditorial());
    response.setAnoPublicacion(libro.getAnoPublicacion());
    response.setEdicion(libro.getEdicion());
    response.setNumero_paginas(libro.getNumero_paginas());
    response.setIdioma(libro.getIdioma());
    response.setDescripcion(libro.getDescripcion());
    response.setImagen_portada(libro.getImagen_portada());

    // Categoría
    if (libro.getCategoria() != null) {
      response.setCategoria(mapCategoriaToResponse(libro.getCategoria()));
    }

    // Calcular disponibilidad
    // List<Ejemplar> ejemplares =
    // ejemplarRepository.findByLibro_IdLibro(libro.getId_libro());
    // response.setEjemplaresTotal(ejemplares.size());
    // response.setEjemplaresDisponibles(
    // (int) ejemplares.stream()
    // .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.DISPONIBLE)
    // .count()
    // );

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

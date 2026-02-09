package com.proyecto.fhce.library.services.library;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.library.CategoriaLibroRequest;
import com.proyecto.fhce.library.dto.response.library.CategoriaConConteoResponse;
import com.proyecto.fhce.library.dto.response.library.CategoriaDetalleResponse;
import com.proyecto.fhce.library.dto.response.library.CategoriaLibroResponse;
import com.proyecto.fhce.library.dto.response.library.EstadisticasCategoriasResponse;
import com.proyecto.fhce.library.dto.response.library.LibroSimpleResponse;
import com.proyecto.fhce.library.entities.CategoriaLibro;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.CategoriaLibroRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;

@Service
public class CategoriaLibroServiceImpl implements CategoriaLibroService {

  @Autowired
  private CategoriaLibroRepository categoriaRepository;

  @Autowired
  private LibroRepository libroRepository;

  // @Autowired
  // private AuditoriaService auditoriaService;
  @Transactional
  public CategoriaLibroResponse create(CategoriaLibroRequest request) {
    // Validar código Dewey único si se proporciona
    if (request.getCodigo_dewey() != null &&
        categoriaRepository.existsByCodigoDewey(request.getCodigo_dewey())) {
      throw new DuplicateResourceException("Ya existe una categoría con código Dewey: " +
          request.getCodigo_dewey());
    }

    CategoriaLibro categoria = new CategoriaLibro();
    categoria.setNombre_categoria(request.getNombre_categoria());
    categoria.setDescripcion(request.getDescripcion());
    categoria.setCodigo_dewey(request.getCodigo_dewey());

    CategoriaLibro saved = categoriaRepository.save(categoria);

    // auditoriaService.registrar("CREATE_CATEGORY", "book_categories",
    // saved.getId_categoria(), null, saved.getNombre_categoria());

    return mapToResponse(saved);
  }

  public CategoriaLibroResponse update(Long id, CategoriaLibroRequest request) {
    CategoriaLibro categoria = categoriaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

    // Validar código Dewey único si cambió
    if (request.getCodigo_dewey() != null &&
        !request.getCodigo_dewey().equals(categoria.getCodigo_dewey()) &&
        categoriaRepository.existsByCodigoDewey(request.getCodigo_dewey())) {
      throw new DuplicateResourceException("Ya existe una categoría con código Dewey: " +
          request.getCodigo_dewey());
    }

    String nombreAnterior = categoria.getNombre_categoria();

    categoria.setNombre_categoria(request.getNombre_categoria());
    categoria.setDescripcion(request.getDescripcion());
    categoria.setCodigo_dewey(request.getCodigo_dewey());

    CategoriaLibro updated = categoriaRepository.save(categoria);

    // auditoriaService.registrar("UPDATE_CATEGORY", "book_categories",
    // updated.getId_categoria(), nombreAnterior, updated.getNombre_categoria());

    return mapToResponse(updated);
  }

  public void delete(Long id) {
    CategoriaLibro categoria = categoriaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

    // Validar que no tenga libros asociados
    Long librosCount = libroRepository.countByCategoria_IdCategoria(id);
    // Long librosCount = libroRepository.countByCategoria_IdCategoria(id);
    if (librosCount != null && librosCount > 0) {
      throw new BusinessException("No se puede eliminar la categoría porque tiene " +
          librosCount + " libro(s) asociado(s)");
    }

    categoriaRepository.delete(categoria);

    // auditoriaService.registrar("DELETE_CATEGORY", "book_categories",
    // categoria.getId_categoria(), categoria.getNombre_categoria(), null);
  }

  @Transactional(readOnly = true)
  public List<CategoriaLibroResponse> findAll() {
    return categoriaRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<CategoriaLibroResponse> findAllOrdenadas() {
    return categoriaRepository.findAllByOrderByNombreCategoriaAsc().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CategoriaLibroResponse findById(Long id) {
    CategoriaLibro categoria = categoriaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    return mapToResponse(categoria);
  }

  @Transactional(readOnly = true)
  public CategoriaLibroResponse findByCodigoDewey(String codigo) {
    CategoriaLibro categoria = categoriaRepository.findByCodigoDewey(codigo)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con código Dewey: " + codigo));
    return mapToResponse(categoria);
  }

  @Transactional(readOnly = true)
  public List<CategoriaLibroResponse> search(String searchTerm) {
    return categoriaRepository.findByNombreCategoriaContainingIgnoreCase(searchTerm).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<CategoriaLibroResponse> findCategoriasConLibros() {
    return categoriaRepository.findCategoriasOrderByLibrosCount().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CategoriaDetalleResponse findByIdWithDetalle(Long id) {
    CategoriaLibro categoria = categoriaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

    CategoriaDetalleResponse response = new CategoriaDetalleResponse();
    response.setId_categoria(categoria.getId_categoria());
    response.setNombre_categoria(categoria.getNombre_categoria());
    response.setDescripcion(categoria.getDescripcion());
    response.setCodigo_dewey(categoria.getCodigo_dewey());

    // Contar libros
    Long librosCount = libroRepository.countByCategoria_IdCategoria(id);
    response.setLibrosCount(librosCount != null ? librosCount.intValue() : 0);

    // Obtener libros de la categoría (limitados a 10 más recientes)
    List<Libro> libros = libroRepository.findTop10ByCategoria_IdCategoriaOrderByAnoPublicacionDesc(id);
    List<LibroSimpleResponse> librosResponse = libros.stream()
        .map(this::mapLibroToSimple)
        .collect(Collectors.toList());
    response.setLibrosRecientes(librosResponse);

    return response;
  }

  @Transactional(readOnly = true)
  public EstadisticasCategoriasResponse obtenerEstadisticas() {
    EstadisticasCategoriasResponse stats = new EstadisticasCategoriasResponse();

    Long totalCategorias = categoriaRepository.count();
    stats.setTotalCategorias(totalCategorias != null ? totalCategorias.intValue()
        : 0);

    // Categorías con más libros (Top 10)
    List<Object[]> topCategorias = categoriaRepository.findTopCategoriasConMasLibros();
    List<CategoriaConConteoResponse> top = topCategorias.stream()
        .limit(10)
        .map(arr -> {
          CategoriaConConteoResponse cat = new CategoriaConConteoResponse();
          cat.setId_categoria((Long) arr[0]);
          cat.setNombre_categoria((String) arr[1]);
          cat.setCantidadLibros((Long) arr[2]);
          return cat;
        })
        .collect(Collectors.toList());
    stats.setCategoriasConMasLibros(top);

    // Categorías sin libros
    Long categoriasSinLibros = categoriaRepository.countCategoriasSinLibros();
    stats.setCategoriasSinLibros(categoriasSinLibros != null ? categoriasSinLibros.intValue() : 0);

    return stats;
  }

  @Transactional(readOnly = true)
  public Map<String, Integer> obtenerDistribucionPorRangoDewey() {
    List<CategoriaLibro> categorias = categoriaRepository.findAll();

    Map<String, Integer> distribucion = new LinkedHashMap<>();
    distribucion.put("000-099 Generalidades", 0);
    distribucion.put("100-199 Filosofía", 0);
    distribucion.put("200-299 Religión", 0);
    distribucion.put("300-399 Ciencias Sociales", 0);
    distribucion.put("400-499 Lenguas", 0);
    distribucion.put("500-599 Ciencias Naturales", 0);
    distribucion.put("600-699 Tecnología", 0);
    distribucion.put("700-799 Artes", 0);
    distribucion.put("800-899 Literatura", 0);
    distribucion.put("900-999 Historia y Geografía", 0);
    distribucion.put("Sin clasificar", 0);

    for (CategoriaLibro cat : categorias) {
      if (cat.getCodigo_dewey() == null || cat.getCodigo_dewey().isEmpty()) {
        distribucion.put("Sin clasificar", distribucion.get("Sin clasificar") + 1);
        continue;
      }

      try {
        int codigo = Integer.parseInt(cat.getCodigo_dewey().split("\\.")[0]);
        String rango = obtenerRangoDewey(codigo);
        distribucion.put(rango, distribucion.get(rango) + 1);
      } catch (NumberFormatException e) {
        distribucion.put("Sin clasificar", distribucion.get("Sin clasificar") + 1);
      }
    }

    return distribucion;
  }

  private String obtenerRangoDewey(int codigo) {
    if (codigo < 100)
      return "000-099 Generalidades";
    if (codigo < 200)
      return "100-199 Filosofía";
    if (codigo < 300)
      return "200-299 Religión";
    if (codigo < 400)
      return "300-399 Ciencias Sociales";
    if (codigo < 500)
      return "400-499 Lenguas";
    if (codigo < 600)
      return "500-599 Ciencias Naturales";
    if (codigo < 700)
      return "600-699 Tecnología";
    if (codigo < 800)
      return "700-799 Artes";
    if (codigo < 900)
      return "800-899 Literatura";
    if (codigo < 1000)
      return "900-999 Historia y Geografía";
    return "Sin clasificar";
  }

  private CategoriaLibroResponse mapToResponse(CategoriaLibro categoria) {
    CategoriaLibroResponse response = new CategoriaLibroResponse();
    response.setId_categoria(categoria.getId_categoria());
    response.setNombre_categoria(categoria.getNombre_categoria());
    response.setDescripcion(categoria.getDescripcion());
    response.setCodigo_dewey(categoria.getCodigo_dewey());

    // Contar libros de esta categoría
    if (categoria.getLibros() != null) {
      response.setLibrosCount(categoria.getLibros().size());
    } else {
      Long count = libroRepository.countByCategoria_IdCategoria(categoria.getId_categoria());
      response.setLibrosCount(count != null ? count.intValue() : 0);
    }

    return response;
  }

  private LibroSimpleResponse mapLibroToSimple(Libro libro) {
    LibroSimpleResponse response = new LibroSimpleResponse();
    response.setId_libro(libro.getId_libro());
    response.setIsbn(libro.getIsbn());
    response.setTitulo(libro.getTitulo());
    response.setEditorial(libro.getEditorial());
    response.setAnoPublicacion(libro.getAnoPublicacion());
    response.setImagen_portada(libro.getImagen_portada());

    // // Autores concatenados
    // if (libro.getAutores() != null && !libro.getAutores().isEmpty()) {
    // String autores = libro.getAutores().stream()
    // .map(a -> a.getNombre() + (a.getApellido() != null ? " " + a.getApellido() :
    // ""))
    // .collect(Collectors.joining(", "));
    // response.setAutores(autores);
    // }

    return response;
  }
}
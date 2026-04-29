package com.proyecto.fhce.library.services.library;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.proyecto.fhce.library.dto.request.library.AutorRequest;
import com.proyecto.fhce.library.dto.response.library.AutorResponse;
import com.proyecto.fhce.library.entities.Autor;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.AutorRepository;

@Service
public class AutorService {

  private final AutorRepository autorRepository;

  public AutorService(AutorRepository autorRepository) {
    this.autorRepository = autorRepository;
  }

  public AutorResponse create(AutorRequest request) {

    if (autorRepository.existsByNombreIgnoreCase(request.getNombre())) {
      throw new DuplicateResourceException("Ya existe un autor con nombre: " + request.getNombre());
    }

    Autor autor = new Autor();
    autor.setNombre(request.getNombre());

    return mapToResponse(autorRepository.save(autor));
  }

  public List<AutorResponse> findAll() {
    return autorRepository.findAll()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public AutorResponse findById(Long id) {
    Autor autor = autorRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con id: " + id));

    return mapToResponse(autor);
  }

  public List<AutorResponse> searchByNombre(String nombre) {
    return autorRepository.findByNombreContainingIgnoreCase(nombre)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<AutorResponse> findByLibro(Long libroId) {
    return autorRepository.findByLibroId(libroId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public AutorResponse update(Long id, AutorRequest request) {

    Autor autor = autorRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con id: " + id));

    if (!autor.getNombre().equalsIgnoreCase(request.getNombre()) &&
        autorRepository.existsByNombreIgnoreCase(request.getNombre())) {
      throw new DuplicateResourceException("Ya existe un autor con nombre: " + request.getNombre());
    }

    autor.setNombre(request.getNombre());

    return mapToResponse(autorRepository.save(autor));
  }

  public void delete(Long id) {
    Autor autor = autorRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con id: " + id));

    autorRepository.delete(autor);
  }

  private AutorResponse mapToResponse(Autor autor) {
    AutorResponse response = new AutorResponse();
    response.setIdAutor(autor.getIdAutor());
    response.setNombre(autor.getNombre());
    return response;
  }
}
package com.proyecto.fhce.library.services.library;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.proyecto.fhce.library.dto.request.library.BusquedaLibroRequest;
import com.proyecto.fhce.library.dto.request.library.LibroRequest;
import com.proyecto.fhce.library.dto.response.PageResponse;
import com.proyecto.fhce.library.dto.response.library.LibroResponse;

public interface LibroService {
  public LibroResponse create(LibroRequest request);

  public LibroResponse update(Long id, LibroRequest request);

  public LibroResponse findById(Long id);

  public List<LibroResponse> findAll();

  public List<LibroResponse> search(String searchTerm);

  public PageResponse<LibroResponse> busquedaAvanzada(BusquedaLibroRequest request, Pageable pageable);

  public void delete(Long id);
}

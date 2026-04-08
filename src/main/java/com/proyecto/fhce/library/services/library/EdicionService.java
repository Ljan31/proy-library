package com.proyecto.fhce.library.services.library;

import java.util.List;

import com.proyecto.fhce.library.dto.request.library.EdicionRequest;
import com.proyecto.fhce.library.dto.response.library.EdicionResponse;

public interface EdicionService {
  EdicionResponse create(EdicionRequest request);

  EdicionResponse update(Long id, EdicionRequest request);

  EdicionResponse findById(Long id);

  List<EdicionResponse> findByLibro(Long libroId);

  void delete(Long id);
}
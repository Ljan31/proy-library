package com.proyecto.fhce.library.services.library;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.dto.request.library.EdicionRequest;
import com.proyecto.fhce.library.dto.response.library.EdicionResponse;

public interface EdicionService {
  public EdicionResponse create(EdicionRequest request, MultipartFile portadaFile);

  public EdicionResponse update(Long id, EdicionRequest request, MultipartFile portadaFile);

  EdicionResponse findById(Long id);

  List<EdicionResponse> findByLibro(Long libroId);

  void delete(Long id);
}
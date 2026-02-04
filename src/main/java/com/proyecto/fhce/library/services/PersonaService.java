package com.proyecto.fhce.library.services;

import com.proyecto.fhce.library.dto.request.PersonaRequest;
import com.proyecto.fhce.library.dto.response.PersonaResponse;

public interface PersonaService {
  PersonaResponse create(PersonaRequest request);

  PersonaResponse update(Long id, PersonaRequest request);

  PersonaResponse findById(Long id);

  PersonaResponse findByIdSecure(Long id, String username);
}

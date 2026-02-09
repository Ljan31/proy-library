package com.proyecto.fhce.library.services.users;

import com.proyecto.fhce.library.dto.request.users.PersonaRequest;
import com.proyecto.fhce.library.dto.response.users.PersonaResponse;

public interface PersonaService {
  PersonaResponse create(PersonaRequest request);

  PersonaResponse update(Long id, PersonaRequest request);

  PersonaResponse findById(Long id);

  PersonaResponse findByIdSecure(Long id, String username);
}

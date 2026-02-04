package com.proyecto.fhce.library.services.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.PersonaRequest;
import com.proyecto.fhce.library.dto.response.PersonaResponse;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class PersonaServiceImpl implements PersonaService {
  @Autowired
  private PersonaRepository personaRepository;

  @Autowired
  private UserRepository userRepository;

  public PersonaResponse create(PersonaRequest request) {
    if (personaRepository.existsByCi(request.getCi())) {
      throw new DuplicateResourceException("Ya existe una persona con CI: " + request.getCi());
    }

    if (request.getEmail() != null && personaRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Ya existe una persona con email: " + request.getEmail());
    }

    Persona persona = new Persona();
    persona.setNombre(request.getNombre());
    persona.setApellido_pat(request.getApellido_pat());
    persona.setApellido_mat(request.getApellido_mat());
    persona.setCi(request.getCi());
    persona.setCelular(request.getCelular());
    persona.setEmail(request.getEmail());

    Persona saved = personaRepository.save(persona);
    return mapToResponse(saved);
  }

  public PersonaResponse update(Long id, PersonaRequest request) {
    Persona persona = personaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + id));

    // Validar CI único si cambió
    if (persona.getCi() != request.getCi() &&
        personaRepository.existsByCi(request.getCi())) {
      throw new DuplicateResourceException("Ya existe una persona con CI: " + request.getCi());
    }

    // Validar email único si cambió
    if (request.getEmail() != null &&
        !request.getEmail().equals(persona.getEmail()) &&
        personaRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Ya existe una persona con email: " + request.getEmail());
    }

    persona.setNombre(request.getNombre());
    persona.setApellido_pat(request.getApellido_pat());
    persona.setApellido_mat(request.getApellido_mat());
    persona.setCi(request.getCi());
    persona.setCelular(request.getCelular());
    persona.setEmail(request.getEmail());

    Persona updated = personaRepository.save(persona);
    return mapToResponse(updated);
  }

  @Transactional(readOnly = true)
  public PersonaResponse findById(Long id) {
    Persona persona = personaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + id));
    return mapToResponse(persona);
  }

  private PersonaResponse mapToResponse(Persona persona) {
    PersonaResponse response = new PersonaResponse();
    response.setId_persona(persona.getId_persona());
    response.setNombre(persona.getNombre());
    response.setApellido_pat(persona.getApellido_pat());
    response.setApellido_mat(persona.getApellido_mat());
    response.setNombreCompleto(persona.getNombre() + " " +
        persona.getApellido_pat() +
        (persona.getApellido_mat() != null ? " " + persona.getApellido_mat() : ""));
    response.setCi(persona.getCi());
    response.setCelular(persona.getCelular());
    response.setEmail(persona.getEmail());
    return response;
  }

  public PersonaResponse findByIdSecure(Long id, String username) {
    // Obtengo el usuario autenticado
    Usuario authUser = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

    boolean isAdminOrBiblio = authUser.getRoles().stream()
        .anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_BIBLIOTECARIO"));

    // Si no es admin ni bibliotecario y el id no coincide con su persona → acceso
    // denegado
    if (!isAdminOrBiblio && !authUser.getPersona().getId_persona().equals(id)) {
      throw new AccessDeniedException("No tienes permiso para ver esta persona");
    }

    Persona persona = personaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));

    return mapToResponse(persona);
  }

}

package com.proyecto.fhce.library.services.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.users.PersonaRequest;
import com.proyecto.fhce.library.dto.response.users.PersonaResponse;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;

@Service
@Transactional(readOnly = true)
public class PersonaServiceImpl implements PersonaService {
  private final PersonaRepository personaRepository;
  private final UserRepository userRepository;
  private final AuditoriaService auditoriaService;

  public PersonaServiceImpl(PersonaRepository personaRepository, UserRepository userRepository,
      AuditoriaService auditoriaService) {
    this.personaRepository = personaRepository;
    this.userRepository = userRepository;
    this.auditoriaService = auditoriaService;
  }

  @Transactional
  public PersonaResponse create(PersonaRequest request) {
    if (personaRepository.existsByCi(request.getCi())) {
      throw new DuplicateResourceException("Ya existe una persona con CI: " + request.getCi());
    }

    if (request.getEmail() != null && personaRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Ya existe una persona con email: " + request.getEmail());
    }
    // if (request.getMatricula() != null &&
    // personaRepository.existsByMatricula(request.getMatricula())) {
    // throw new DuplicateResourceException("Ya existe una persona con matrícula: "
    // + request.getMatricula());
    // }
    Persona persona = new Persona();
    persona.setNombre(request.getNombre());
    persona.setApellido_pat(request.getApellido_pat());
    persona.setApellido_mat(request.getApellido_mat());
    persona.setCi(request.getCi());
    persona.setCelular(request.getCelular());
    persona.setEmail(request.getEmail());

    Persona saved = personaRepository.save(persona);
    PersonaResponse response = mapToResponse(saved);
    auditoriaService.registrar(
        "CREAR",
        "Personas",
        saved.getId_persona(),
        null,
        response);
    return mapToResponse(saved);
  }

  @Transactional
  public PersonaResponse update(Long id, PersonaRequest request) {
    Persona persona = personaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + id));
    // Capturar el estado anterior antes de modificar
    PersonaResponse valoresAnteriores = mapToResponse(persona);
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
    // if (request.getMatricula() != null &&
    // !request.getMatricula().equals(persona.getMatricula()) &&
    // personaRepository.existsByMatricula(request.getMatricula())) {
    // throw new DuplicateResourceException("Ya existe una persona con matrícula: "
    // + request.getMatricula());
    // }
    persona.setNombre(request.getNombre());
    persona.setApellido_pat(request.getApellido_pat());
    persona.setApellido_mat(request.getApellido_mat());
    persona.setCi(request.getCi());
    persona.setCelular(request.getCelular());
    persona.setEmail(request.getEmail());

    Persona updated = personaRepository.save(persona);
    PersonaResponse valoresNuevos = mapToResponse(updated);

    auditoriaService.registrar(
        "ACTUALIZAR",
        "personas",
        updated.getId_persona(),
        valoresAnteriores,
        valoresNuevos);
    return mapToResponse(updated);
  }

  @Transactional
  public void delete(Long id) {
    Persona persona = personaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Persona no encontrada con id: " + id));

    PersonaResponse valoresAnteriores = mapToResponse(persona);

    personaRepository.delete(persona);

    auditoriaService.registrar(
        "ELIMINAR",
        "personas",
        persona.getId_persona(),
        valoresAnteriores,
        null);
  }

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

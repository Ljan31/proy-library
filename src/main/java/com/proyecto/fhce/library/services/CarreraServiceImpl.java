package com.proyecto.fhce.library.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.CarreraRequest;
import com.proyecto.fhce.library.dto.response.CarreraDetalleResponse;
import com.proyecto.fhce.library.dto.response.CarreraResponse;
import com.proyecto.fhce.library.dto.response.library.BibliotecaSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Carrera;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.CarreraRepository;

@Service
public class CarreraServiceImpl implements CarreraService {
  @Autowired
  private CarreraRepository carreraRepository;

  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  // @Autowired
  // private AuditoriaService auditoriaService;
  @Transactional
  public CarreraResponse create(CarreraRequest request) {
    // Validar código único
    if (carreraRepository.existsByCodigoCarrera(request.getCodigo_carrera())) {
      throw new DuplicateResourceException("Ya existe una carrera con código: " +
          request.getCodigo_carrera());
    }

    Carrera carrera = new Carrera();
    carrera.setNombre_carrera(request.getNombre_carrera());
    carrera.setCodigo_carrera(request.getCodigo_carrera());

    Carrera saved = carreraRepository.save(carrera);

    // auditoriaService.registrar("CREATE_CAREER", "careers",
    // saved.getId_carrera(), null, saved.getNombre_carrera());

    return mapToResponse(saved);
  }

  @Transactional
  public CarreraResponse update(Long id, CarreraRequest request) {
    Carrera carrera = carreraRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id: " + id));

    // Validar código único si cambió
    if (!carrera.getCodigo_carrera().equals(request.getCodigo_carrera()) &&
        carreraRepository.existsByCodigoCarrera(request.getCodigo_carrera())) {
      throw new DuplicateResourceException("Ya existe una carrera con código: " +
          request.getCodigo_carrera());
    }

    // String nombreAnterior = carrera.getNombre_carrera();

    carrera.setNombre_carrera(request.getNombre_carrera());
    carrera.setCodigo_carrera(request.getCodigo_carrera());

    Carrera updated = carreraRepository.save(carrera);

    // auditoriaService.registrar("UPDATE_CAREER", "careers",
    // updated.getId_carrera(), nombreAnterior, updated.getNombre_carrera());

    return mapToResponse(updated);
  }

  @Transactional
  public void delete(Long id) {
    Carrera carrera = carreraRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id: " + id));

    // Validar que no tenga estudiantes activos
    // Long estudiantesActivos =
    // usuarioCarreraRepository.countEstudiantesByCarrera(id);
    // if (estudiantesActivos > 0) {
    // throw new BusinessException("No se puede eliminar la carrera porque tiene " +
    // estudiantesActivos + " estudiante(s) activo(s)");
    // }

    // Validar que no tenga bibliotecas
    List<Biblioteca> bibliotecas = bibliotecaRepository.findByCarrera_IdCarrera(id);
    if (!bibliotecas.isEmpty()) {
      throw new BusinessException("No se puede eliminar la carrera porque tiene " +
          bibliotecas.size() + " biblioteca(s) asociada(s)");
    }

    carreraRepository.delete(carrera);

    // auditoriaService.registrar("DELETE_CAREER", "careers",
    // carrera.getId_carrera(), carrera.getNombre_carrera(), null);
  }

  @Transactional(readOnly = true)
  public List<CarreraResponse> findAll() {
    return carreraRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CarreraResponse findById(Long id) {
    Carrera carrera = carreraRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id: " + id));
    return mapToResponse(carrera);
  }

  @Transactional(readOnly = true)
  public CarreraResponse findByCodigo(String codigo) {
    Carrera carrera = carreraRepository.findByCodigoCarrera(codigo)
        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con código: " + codigo));
    return mapToResponse(carrera);
  }

  @Transactional(readOnly = true)
  public List<CarreraResponse> search(String nombre) {
    return carreraRepository.findByNombreContaining(nombre).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CarreraDetalleResponse findByIdWithDetalle(Long id) {
    Carrera carrera = carreraRepository.findByIdWithBibliotecas(id)
        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id: " + id));

    CarreraDetalleResponse response = new CarreraDetalleResponse();
    response.setId_carrera(carrera.getId_carrera());
    response.setNombre_carrera(carrera.getNombre_carrera());
    response.setCodigo_carrera(carrera.getCodigo_carrera());

    // Contar estudiantes por estado
    // Long activos = usuarioCarreraRepository.countEstudiantesByCarrera(id);

    // response.setEstudiantesActivos(activos != null ? activos.intValue() : 0);

    // Bibliotecas
    if (carrera.getBibliotecas() != null && !carrera.getBibliotecas().isEmpty()) {
      List<BibliotecaSimpleResponse> bibliotecas = carrera.getBibliotecas().stream()
          .map(this::mapBibliotecaToSimple)
          .collect(Collectors.toList());
      response.setBibliotecas(bibliotecas);
      response.setBibliotecasCount(bibliotecas.size());
    } else {
      response.setBibliotecas(new ArrayList<>());
      response.setBibliotecasCount(0);
    }

    return response;
  }

  private CarreraResponse mapToResponse(Carrera carrera) {
    CarreraResponse response = new CarreraResponse();
    response.setId_carrera(carrera.getId_carrera());
    response.setNombre_carrera(carrera.getNombre_carrera());
    response.setCodigo_carrera(carrera.getCodigo_carrera());

    // Contar estudiantes activos
    // Long estudiantesActivos =
    // usuarioCarreraRepository.countEstudiantesByCarrera(carrera.getId_carrera());
    // response.setEstudiantesActivos(estudiantesActivos != null ?
    // estudiantesActivos.intValue() : 0);

    // Contar bibliotecas
    if (carrera.getBibliotecas() != null) {
      response.setBibliotecasCount(carrera.getBibliotecas().size());
    } else {
      // Si no está cargada la relación, hacer query
      List<Biblioteca> bibliotecas = bibliotecaRepository.findByCarrera_IdCarrera(carrera.getId_carrera());
      response.setBibliotecasCount(bibliotecas.size());
    }

    return response;
  }

  private BibliotecaSimpleResponse mapBibliotecaToSimple(Biblioteca biblioteca) {
    BibliotecaSimpleResponse response = new BibliotecaSimpleResponse();
    response.setId_biblioteca(biblioteca.getId_biblioteca());
    response.setNombre(biblioteca.getNombre());
    response.setTipoBiblioteca(biblioteca.getTipoBiblioteca());
    return response;
  }
}

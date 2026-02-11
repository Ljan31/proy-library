package com.proyecto.fhce.library.services.library;

import java.util.List;

import com.proyecto.fhce.library.dto.request.library.EjemplarRequest;
import com.proyecto.fhce.library.dto.request.library.EjemplarUpdateEstadoRequest;
import com.proyecto.fhce.library.dto.response.library.DisponibilidadLibroResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.loads.HistorialEstadoResponse;
import com.proyecto.fhce.library.enums.EstadoEjemplar;

public interface EjemplarService {

  public EjemplarResponse create(EjemplarRequest request);

  public EjemplarResponse update(Long id, EjemplarRequest request);

  public EjemplarResponse actualizarEstado(Long id, EjemplarUpdateEstadoRequest request);

  public void darDeBaja(Long id, String motivo);

  public void reportarPerdido(Long id, String motivo);

  public void transferirBiblioteca(Long ejemplarId, Long nuevaBibliotecaId, String motivo);

  public List<EjemplarResponse> findAll();

  public EjemplarResponse findById(Long id);

  public EjemplarResponse findByCodigo(String codigo);

  public List<EjemplarResponse> findByLibro(Long libroId);

  public List<EjemplarResponse> findByBiblioteca(Long bibliotecaId);

  public List<EjemplarResponse> findByEstado(EstadoEjemplar estado);

  public List<EjemplarResponse> findDisponiblesByLibro(Long libroId);

  public List<EjemplarResponse> findByBibliotecaAndEstado(Long bibliotecaId, EstadoEjemplar estado);

  public List<HistorialEstadoResponse> obtenerHistorial(Long ejemplarId);

  public DisponibilidadLibroResponse verificarDisponibilidad(Long libroId);

}
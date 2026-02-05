package com.proyecto.fhce.library.services.library;

import java.util.List;

import com.proyecto.fhce.library.dto.request.BibliotecaRequest;
import com.proyecto.fhce.library.dto.response.BibliotecaResponse;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

public interface BibliotecaService {
  public BibliotecaResponse create(BibliotecaRequest request);

  public BibliotecaResponse update(Long id, BibliotecaRequest request);

  public void delete(Long id);

  public void cambiarEstado(Long id, EstadoBiblioteca nuevoEstado);

  public void asignarEncargado(Long bibliotecaId, Long usuarioId);

  public List<BibliotecaResponse> findAll();

  public List<BibliotecaResponse> findActivas();

  public BibliotecaResponse findById(Long id);

  public List<BibliotecaResponse> findByTipo(TipoBiblioteca tipo);

  public BibliotecaResponse findBibliotecaFacultativa();

  public List<BibliotecaResponse> findByCarrera(Long carreraId);

  public List<BibliotecaResponse> search(String searchTerm);

  // public EstadisticasBibliotecaResponse obtenerEstadisticas(Long id);
  // public InventarioBibliotecaResponse obtenerInventario(Long id);
}

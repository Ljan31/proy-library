package com.proyecto.fhce.library.services.library;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.dto.request.library.BibliotecaRequest;
import com.proyecto.fhce.library.dto.response.library.BibliotecaResponse;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

public interface BibliotecaService {
  public BibliotecaResponse create(BibliotecaRequest request, MultipartFile logoFile);

  public BibliotecaResponse update(Long id, BibliotecaRequest request, MultipartFile logoFile);

  public void delete(Long id);

  public void cambiarEstado(Long id, EstadoBiblioteca nuevoEstado);

  public void asignarEncargado(Long bibliotecaId, List<Long> usuariosIds, MultipartFile respaldoFile,
      long usuarioActualId);

  public String uploadEncargadoImagen(
      Long bibliotecaId,
      Long usuarioId,
      MultipartFile imagen);

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

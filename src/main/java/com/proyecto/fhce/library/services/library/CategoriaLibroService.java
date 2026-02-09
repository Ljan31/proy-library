package com.proyecto.fhce.library.services.library;

import java.util.List;
import java.util.Map;

import com.proyecto.fhce.library.dto.request.library.CategoriaLibroRequest;
import com.proyecto.fhce.library.dto.response.library.CategoriaDetalleResponse;
import com.proyecto.fhce.library.dto.response.library.CategoriaLibroResponse;
import com.proyecto.fhce.library.dto.response.library.EstadisticasCategoriasResponse;

public interface CategoriaLibroService {
  CategoriaLibroResponse create(CategoriaLibroRequest request);

  CategoriaLibroResponse update(Long id, CategoriaLibroRequest request);

  void delete(Long id);

  List<CategoriaLibroResponse> findAll();

  List<CategoriaLibroResponse> findAllOrdenadas();

  CategoriaLibroResponse findById(Long id);

  CategoriaLibroResponse findByCodigoDewey(String codigo);

  List<CategoriaLibroResponse> search(String searchTerm);

  List<CategoriaLibroResponse> findCategoriasConLibros();

  public EstadisticasCategoriasResponse obtenerEstadisticas();

  CategoriaDetalleResponse findByIdWithDetalle(Long id);

  Map<String, Integer> obtenerDistribucionPorRangoDewey();
}

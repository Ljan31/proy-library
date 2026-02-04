package com.proyecto.fhce.library.services;

import java.util.List;

import com.proyecto.fhce.library.dto.request.CarreraRequest;
import com.proyecto.fhce.library.dto.response.CarreraDetalleResponse;
import com.proyecto.fhce.library.dto.response.CarreraResponse;

public interface CarreraService {
  public CarreraResponse create(CarreraRequest request);

  public CarreraResponse update(Long id, CarreraRequest request);

  public void delete(Long id);

  public List<CarreraResponse> findAll();

  public CarreraResponse findById(Long id);

  public CarreraResponse findByCodigo(String codigo);

  public List<CarreraResponse> search(String nombre);

  public CarreraDetalleResponse findByIdWithDetalle(Long id);
}

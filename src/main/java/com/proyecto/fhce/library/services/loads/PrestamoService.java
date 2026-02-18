package com.proyecto.fhce.library.services.loads;

import java.util.List;

import com.proyecto.fhce.library.dto.request.loads.DevolucionRequest;
import com.proyecto.fhce.library.dto.request.loads.FiltroPrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.PrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.RenovacionRequest;
import com.proyecto.fhce.library.dto.response.loads.PrestamoResponse;
import com.proyecto.fhce.library.enums.EstadoPrestamo;

public interface PrestamoService {
  public List<PrestamoResponse> findAll();

  public PrestamoResponse findById(Long id);

  public List<PrestamoResponse> findByUsuario(Long usuarioId);

  public List<PrestamoResponse> findByEstado(EstadoPrestamo estado);

  public List<PrestamoResponse> findPrestamosVencidos();

  public List<PrestamoResponse> findPrestamosPorVencer(int dias);

  public List<PrestamoResponse> filtrar(FiltroPrestamoRequest filtro);

  public PrestamoResponse realizarPrestamo(PrestamoRequest request, Long bibliotecarioId);

  public PrestamoResponse realizarDevolucion(DevolucionRequest request, Long bibliotecarioId);

  public PrestamoResponse renovarPrestamo(RenovacionRequest request);

  public void procesarPrestamosVencidos();

  public void notificarPrestamosPorVencer();

}

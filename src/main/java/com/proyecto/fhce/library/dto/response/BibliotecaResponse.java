package com.proyecto.fhce.library.dto.response;

import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

public class BibliotecaResponse {
  private Long id_biblioteca;
  private String nombre;
  private TipoBiblioteca tipoBiblioteca;
  private CarreraSimpleResponse carrera;
  private String direccion;
  private String telefono;
  private String email;
  private String horario_atencion;
  private UsuarioSimpleResponse encargado;
  private EstadoBiblioteca estado;
  private Integer ejemplaresTotal;
  private Integer ejemplaresDisponibles;
}

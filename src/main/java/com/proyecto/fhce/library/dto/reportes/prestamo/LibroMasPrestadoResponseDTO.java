package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.util.List;

public class LibroMasPrestadoResponseDTO {

  private ResumenLibrosPrestadosDTO resumen;

  private List<LibroMasPrestadoDTO> libros;

  public LibroMasPrestadoResponseDTO() {
  }

  public ResumenLibrosPrestadosDTO getResumen() {
    return resumen;
  }

  public void setResumen(ResumenLibrosPrestadosDTO resumen) {
    this.resumen = resumen;
  }

  public List<LibroMasPrestadoDTO> getLibros() {
    return libros;
  }

  public void setLibros(List<LibroMasPrestadoDTO> libros) {
    this.libros = libros;
  }

}

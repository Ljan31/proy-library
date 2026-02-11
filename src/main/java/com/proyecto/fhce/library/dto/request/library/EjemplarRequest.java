package com.proyecto.fhce.library.dto.request.library;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public class EjemplarRequest {
  @NotNull(message = "Libro ID es requerido")
  private Long libroId;

  @NotNull(message = "Biblioteca ID es requerido")
  private Long bibliotecaId;

  @NotBlank(message = "Código de ejemplar es requerido")
  @Size(max = 50)
  private String codigo_ejemplar;

  @Size(max = 100)
  private String codigo_topografico;

  @Size(max = 200)
  private String ubicacion_fisica;

  private EstadoEjemplar estadoEjemplar;

  @PastOrPresent(message = "Fecha de adquisición no puede ser futura")
  private LocalDate fechaAdquisicion;

  @DecimalMin(value = "0.0", inclusive = false, message = "Precio debe ser mayor a 0")
  private BigDecimal precio_compra;

  private String observaciones;

  public Long getLibroId() {
    return libroId;
  }

  public void setLibroId(Long libroId) {
    this.libroId = libroId;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getCodigo_ejemplar() {
    return codigo_ejemplar;
  }

  public void setCodigo_ejemplar(String codigo_ejemplar) {
    this.codigo_ejemplar = codigo_ejemplar;
  }

  public String getCodigo_topografico() {
    return codigo_topografico;
  }

  public void setCodigo_topografico(String codigo_topografico) {
    this.codigo_topografico = codigo_topografico;
  }

  public String getUbicacion_fisica() {
    return ubicacion_fisica;
  }

  public void setUbicacion_fisica(String ubicacion_fisica) {
    this.ubicacion_fisica = ubicacion_fisica;
  }

  public EstadoEjemplar getEstadoEjemplar() {
    return estadoEjemplar;
  }

  public void setEstadoEjemplar(EstadoEjemplar estadoEjemplar) {
    this.estadoEjemplar = estadoEjemplar;
  }

  public LocalDate getFechaAdquisicion() {
    return fechaAdquisicion;
  }

  public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
    this.fechaAdquisicion = fechaAdquisicion;
  }

  public BigDecimal getPrecio_compra() {
    return precio_compra;
  }

  public void setPrecio_compra(BigDecimal precio_compra) {
    this.precio_compra = precio_compra;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

}

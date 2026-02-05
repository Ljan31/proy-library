package com.proyecto.fhce.library.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "carreras")
public class Carrera {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false, name = "id_carrera")
  private Long idCarrera;

  @Column(nullable = false, length = 200)
  private String nombre_carrera;

  @Column(unique = true, nullable = false, length = 20, name = "codigo_carrera")
  private String codigoCarrera;

  // @OneToMany(mappedBy = "carrera")
  // private List<Biblioteca> bibliotecas;

  public Long getId_carrera() {
    return idCarrera;
  }

  public void setId_carrera(Long id_carrera) {
    this.idCarrera = id_carrera;
  }

  public String getNombre_carrera() {
    return nombre_carrera;
  }

  public void setNombre_carrera(String nombre_carrera) {
    this.nombre_carrera = nombre_carrera;
  }

  public String getCodigo_carrera() {
    return codigoCarrera;
  }

  public void setCodigo_carrera(String codigo_carrera) {
    this.codigoCarrera = codigo_carrera;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((idCarrera == null) ? 0 : idCarrera.hashCode());
    result = prime * result + ((nombre_carrera == null) ? 0 : nombre_carrera.hashCode());
    result = prime * result + ((codigoCarrera == null) ? 0 : codigoCarrera.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Carrera other = (Carrera) obj;
    if (idCarrera == null) {
      if (other.idCarrera != null)
        return false;
    } else if (!idCarrera.equals(other.idCarrera))
      return false;
    if (nombre_carrera == null) {
      if (other.nombre_carrera != null)
        return false;
    } else if (!nombre_carrera.equals(other.nombre_carrera))
      return false;
    if (codigoCarrera == null) {
      if (other.codigoCarrera != null)
        return false;
    } else if (!codigoCarrera.equals(other.codigoCarrera))
      return false;
    return true;
  }

}
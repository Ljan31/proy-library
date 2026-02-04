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
  @Column(unique = true, nullable = false)
  private Long id_carrera;

  @Column(nullable = false, length = 200)
  private String nombre_carrera;

  @Column(unique = true, nullable = false, length = 20)
  private String codigo_carrera;

  // @OneToMany(mappedBy = "carrera")
  // private List<Biblioteca> bibliotecas;

  public Long getId_carrera() {
    return id_carrera;
  }

  public void setId_carrera(Long id_carrera) {
    this.id_carrera = id_carrera;
  }

  public String getNombre_carrera() {
    return nombre_carrera;
  }

  public void setNombre_carrera(String nombre_carrera) {
    this.nombre_carrera = nombre_carrera;
  }

  public String getCodigo_carrera() {
    return codigo_carrera;
  }

  public void setCodigo_carrera(String codigo_carrera) {
    this.codigo_carrera = codigo_carrera;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id_carrera == null) ? 0 : id_carrera.hashCode());
    result = prime * result + ((nombre_carrera == null) ? 0 : nombre_carrera.hashCode());
    result = prime * result + ((codigo_carrera == null) ? 0 : codigo_carrera.hashCode());
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
    if (id_carrera == null) {
      if (other.id_carrera != null)
        return false;
    } else if (!id_carrera.equals(other.id_carrera))
      return false;
    if (nombre_carrera == null) {
      if (other.nombre_carrera != null)
        return false;
    } else if (!nombre_carrera.equals(other.nombre_carrera))
      return false;
    if (codigo_carrera == null) {
      if (other.codigo_carrera != null)
        return false;
    } else if (!codigo_carrera.equals(other.codigo_carrera))
      return false;
    return true;
  }

}
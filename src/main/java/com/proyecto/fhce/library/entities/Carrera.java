package com.proyecto.fhce.library.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "carrera")
public class Carrera {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id_carrera;

  private String nombre;

  // @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
  // private List<Persona> personas = new ArrayList<>();

  public Long getIdCarrera() {
    return id_carrera;
  }

  public void setIdCarrera(Long id) {
    this.id_carrera = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  // public List<Persona> getPersonas() {
  // return personas;
  // }

  // public void setPersonas(List<Persona> personas) {
  // this.personas = personas;
  // }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id_carrera == null) ? 0 : id_carrera.hashCode());
    result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
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
    if (nombre == null) {
      if (other.nombre != null)
        return false;
    } else if (!nombre.equals(other.nombre))
      return false;
    return true;
  }

}
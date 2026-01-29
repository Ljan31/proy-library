package com.proyecto.fhce.library.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "persons")
public class Persona {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false)
  private Long id_persona;

  private String nombre;
  private String apellido_pat;
  private String apellido_mat;
  @Column(unique = true, nullable = false)
  private Integer ci;

  private String celular;

  @Column(unique = true)
  private String email;

  public Long getId_persona() {
    return id_persona;
  }

  public void setId_persona(Long id_persona) {
    this.id_persona = id_persona;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido_pat() {
    return apellido_pat;
  }

  public void setApellido_pat(String apellido_pat) {
    this.apellido_pat = apellido_pat;
  }

  public String getApellido_mat() {
    return apellido_mat;
  }

  public void setApellido_mat(String apellido_mat) {
    this.apellido_mat = apellido_mat;
  }

  public int getCi() {
    return ci;
  }

  public void setCi(int ci) {
    this.ci = ci;
  }

  public String getCelular() {
    return celular;
  }

  public void setCelular(String celular) {
    this.celular = celular;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id_persona == null) ? 0 : id_persona.hashCode());
    result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
    result = prime * result + ci;
    result = prime * result + ((celular == null) ? 0 : celular.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
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
    Persona other = (Persona) obj;
    if (id_persona == null) {
      if (other.id_persona != null)
        return false;
    } else if (!id_persona.equals(other.id_persona))
      return false;
    if (nombre == null) {
      if (other.nombre != null)
        return false;
    } else if (!nombre.equals(other.nombre))
      return false;
    if (ci != other.ci)
      return false;
    if (celular == null) {
      if (other.celular != null)
        return false;
    } else if (!celular.equals(other.celular))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    return true;
  }

}

package com.proyecto.fhce.library.entities;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class Permiso {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false)
  private Long id_permiso;

  @Column(name = "nombre_permiso", unique = true, nullable = false, length = 100)
  private String nombrePermiso;

  private String descripcion;

  @Column(length = 50)
  private String modulo;

  @ManyToMany(mappedBy = "permisos")
  @JsonIgnoreProperties({ "permisos", "handler", "hibernateLazyInitializer" })
  private Set<Role> roles;

  public Long getId_permiso() {
    return id_permiso;
  }

  public void setId_permiso(Long id_permiso) {
    this.id_permiso = id_permiso;
  }

  public String getNombre_permiso() {
    return nombrePermiso;
  }

  public void setNombre_permiso(String nombrePermiso) {
    this.nombrePermiso = nombrePermiso;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getModulo() {
    return modulo;
  }

  public void setModulo(String modulo) {
    this.modulo = modulo;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id_permiso == null) ? 0 : id_permiso.hashCode());
    result = prime * result + ((nombrePermiso == null) ? 0 : nombrePermiso.hashCode());
    result = prime * result + ((modulo == null) ? 0 : modulo.hashCode());
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
    Permiso other = (Permiso) obj;
    if (id_permiso == null) {
      if (other.id_permiso != null)
        return false;
    } else if (!id_permiso.equals(other.id_permiso))
      return false;
    if (nombrePermiso == null) {
      if (other.nombrePermiso != null)
        return false;
    } else if (!nombrePermiso.equals(other.nombrePermiso))
      return false;
    if (modulo == null) {
      if (other.modulo != null)
        return false;
    } else if (!modulo.equals(other.modulo))
      return false;
    return true;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

}
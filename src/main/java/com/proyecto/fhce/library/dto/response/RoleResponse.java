package com.proyecto.fhce.library.dto.response;

import java.util.Set;

public class RoleResponse {
  private Long id_role;
  private String name;
  private Set<PermisoResponse> permisos;
  private Integer usuariosCount;

  public Long getId_role() {
    return id_role;
  }

  public void setId_role(Long id_role) {
    this.id_role = id_role;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<PermisoResponse> getPermisos() {
    return permisos;
  }

  public void setPermisos(Set<PermisoResponse> permisos) {
    this.permisos = permisos;
  }

  public Integer getUsuariosCount() {
    return usuariosCount;
  }

  public void setUsuariosCount(Integer usuariosCount) {
    this.usuariosCount = usuariosCount;
  }

}

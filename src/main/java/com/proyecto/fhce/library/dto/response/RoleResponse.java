package com.proyecto.fhce.library.dto.response;

import java.util.Set;

public class RoleResponse {
  private Long id_role;
  private String name;
  private Set<PermisoResponse> permisos;
  private Integer usuariosCount;
}

package com.proyecto.fhce.library.dto.response;

import java.util.Set;

public class UsuarioResponse {
  private Long id_usuario;
  private String username;
  private Boolean enabled;
  private Integer intentosLogin;
  private PersonaResponse persona;
  private Set<RoleSimpleResponse> roles;
  // private List<CarreraSimpleResponse> carreras;
  // private LocalDateTime fechaRegistro;
}
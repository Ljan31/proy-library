package com.proyecto.fhce.library.dto.response;

import java.util.Set;

public class LoginResponse {
  private String token;
  private String type = "Bearer";
  private Long id;
  private String username;
  private String email;
  private Set<String> roles;
  private PersonaResponse persona;
}
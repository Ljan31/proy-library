package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_careers")
public class UsuarioCarrera {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "career_id", nullable = false)
  private Carrera carrera;

  @Column(length = 50)
  private String matricula;

  @Column(name = "ano_ingreso")
  private Integer anoIngreso;

  @Column(name = "fecha_asignacion")
  private LocalDateTime fechaAsignacion;

  @PrePersist
  protected void onCreate() {
    fechaAsignacion = LocalDateTime.now();
  }
}
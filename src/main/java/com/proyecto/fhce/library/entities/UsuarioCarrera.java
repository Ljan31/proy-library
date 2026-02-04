package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Carrera getCarrera() {
    return carrera;
  }

  public void setCarrera(Carrera carrera) {
    this.carrera = carrera;
  }

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

  public Integer getAnoIngreso() {
    return anoIngreso;
  }

  public void setAnoIngreso(Integer anoIngreso) {
    this.anoIngreso = anoIngreso;
  }

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
    result = prime * result + ((carrera == null) ? 0 : carrera.hashCode());
    result = prime * result + ((matricula == null) ? 0 : matricula.hashCode());
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
    UsuarioCarrera other = (UsuarioCarrera) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (usuario == null) {
      if (other.usuario != null)
        return false;
    } else if (!usuario.equals(other.usuario))
      return false;
    if (carrera == null) {
      if (other.carrera != null)
        return false;
    } else if (!carrera.equals(other.carrera))
      return false;
    if (matricula == null) {
      if (other.matricula != null)
        return false;
    } else if (!matricula.equals(other.matricula))
      return false;
    return true;
  }

}
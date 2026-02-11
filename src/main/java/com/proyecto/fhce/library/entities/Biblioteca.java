package com.proyecto.fhce.library.entities;

import java.util.List;

import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "libraries")
public class Biblioteca {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false, name = "id_biblioteca")
  private Long idBiblioteca;

  @Column(nullable = false, length = 200)
  private String nombre;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_biblioteca", nullable = false)
  private TipoBiblioteca tipoBiblioteca;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "career_id")
  private Carrera carrera;

  private String direccion;

  @Column(length = 20)
  private String telefono;

  @Column(length = 100)
  private String email;

  @Column(length = 500)
  private String horario_atencion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "encargado_id")
  private Usuario encargado;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoBiblioteca estado;

  @OneToMany(mappedBy = "biblioteca")
  private List<Ejemplar> ejemplares;

  public Long getId_biblioteca() {
    return idBiblioteca;
  }

  public void setId_biblioteca(Long id_biblioteca) {
    this.idBiblioteca = id_biblioteca;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public TipoBiblioteca getTipoBiblioteca() {
    return tipoBiblioteca;
  }

  public void setTipoBiblioteca(TipoBiblioteca tipoBiblioteca) {
    this.tipoBiblioteca = tipoBiblioteca;
  }

  public Carrera getCarrera() {
    return carrera;
  }

  public void setCarrera(Carrera carrera) {
    this.carrera = carrera;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getHorario_atencion() {
    return horario_atencion;
  }

  public void setHorario_atencion(String horario_atencion) {
    this.horario_atencion = horario_atencion;
  }

  public Usuario getEncargado() {
    return encargado;
  }

  public void setEncargado(Usuario encargado) {
    this.encargado = encargado;
  }

  public EstadoBiblioteca getEstado() {
    return estado;
  }

  public void setEstado(EstadoBiblioteca estado) {
    this.estado = estado;
  }

  public Long getIdBiblioteca() {
    return idBiblioteca;
  }

  public void setIdBiblioteca(Long idBiblioteca) {
    this.idBiblioteca = idBiblioteca;
  }

  public List<Ejemplar> getEjemplares() {
    return ejemplares;
  }

  public void setEjemplares(List<Ejemplar> ejemplares) {
    this.ejemplares = ejemplares;
  }

  // @OneToMany(mappedBy = "biblioteca")
  // private List<Prestamo> prestamos;

  // @OneToMany(mappedBy = "biblioteca")
  // private List<Reserva> reservas;

  // @OneToMany(mappedBy = "biblioteca")
  // private List<ConfiguracionPrestamo> configuraciones;

}
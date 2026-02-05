package com.proyecto.fhce.library.entities;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "libraries")
public class Biblioteca {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false)
  private Long id_biblioteca;

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

  // @OneToMany(mappedBy = "biblioteca")
  // private List<Ejemplar> ejemplares;

  // @OneToMany(mappedBy = "biblioteca")
  // private List<Prestamo> prestamos;

  // @OneToMany(mappedBy = "biblioteca")
  // private List<Reserva> reservas;

  // @OneToMany(mappedBy = "biblioteca")
  // private List<ConfiguracionPrestamo> configuraciones;

}
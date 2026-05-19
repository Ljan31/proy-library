package com.proyecto.fhce.library.dto.request.library;

import java.util.List;

public class RazonCertificadoRequestDTO {

    private Long bibliotecaId;
    private String nombre;
    private String descripcion;
    private List<String> requisitos;
    private Boolean activo;

    public Long getBibliotecaId() {
        return bibliotecaId;
    }

    public void setBibliotecaId(Long bibliotecaId) {
        this.bibliotecaId = bibliotecaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(List<String> requisitos) {
        this.requisitos = requisitos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

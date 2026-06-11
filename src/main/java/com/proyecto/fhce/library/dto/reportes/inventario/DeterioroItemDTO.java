package com.proyecto.fhce.library.dto.reportes.inventario;

public class DeterioroItemDTO {
    private Long idPrestamo;
    private Long idEjemplar;
    private String codigoEjemplar;
    private String libro;
    private String isbn;
    private String biblioteca;
    private String usuario;
    private String ci;
    private String condicionEntrega;
    private String condicionDevolucion;
    private int diferenciaNivel; // cuántos niveles bajó: 0=sin cambio, 1=bajó uno, 2=bajó dos, etc.
    private java.time.LocalDateTime fechaDevolucion;

    public Long getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public Long getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(Long idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public String getCodigoEjemplar() {
        return codigoEjemplar;
    }

    public void setCodigoEjemplar(String codigoEjemplar) {
        this.codigoEjemplar = codigoEjemplar;
    }

    public String getLibro() {
        return libro;
    }

    public void setLibro(String libro) {
        this.libro = libro;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getBiblioteca() {
        return biblioteca;
    }

    public void setBiblioteca(String biblioteca) {
        this.biblioteca = biblioteca;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getCondicionEntrega() {
        return condicionEntrega;
    }

    public void setCondicionEntrega(String condicionEntrega) {
        this.condicionEntrega = condicionEntrega;
    }

    public String getCondicionDevolucion() {
        return condicionDevolucion;
    }

    public void setCondicionDevolucion(String condicionDevolucion) {
        this.condicionDevolucion = condicionDevolucion;
    }

    public int getDiferenciaNivel() {
        return diferenciaNivel;
    }

    public void setDiferenciaNivel(int diferenciaNivel) {
        this.diferenciaNivel = diferenciaNivel;
    }

    public java.time.LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(java.time.LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

}

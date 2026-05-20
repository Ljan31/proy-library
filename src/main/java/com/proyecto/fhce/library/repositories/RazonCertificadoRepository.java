package com.proyecto.fhce.library.repositories;

import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.entities.Biblioteca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RazonCertificadoRepository extends JpaRepository<RazonCertificado, Long> {

  List<RazonCertificado> findByBiblioteca(Biblioteca biblioteca);

  List<RazonCertificado> findByBibliotecaIdBiblioteca(Long bibliotecaId);

  List<RazonCertificado> findByActivoTrue();

  Optional<RazonCertificado> findByIdRazonAndBibliotecaIdBiblioteca(Long idRazon, Long bibliotecaId);
}
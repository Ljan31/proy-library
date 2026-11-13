package com.proyecto.fhce.library.iteracion2.unit;

import static com.proyecto.fhce.library.support.TestData.biblioteca;
import static com.proyecto.fhce.library.support.TestData.edicion;
import static com.proyecto.fhce.library.support.TestData.ejemplar;
import static com.proyecto.fhce.library.support.TestData.libro;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.fhce.library.dto.request.library.EdicionRequest;
import com.proyecto.fhce.library.dto.request.library.EjemplarRequest;
import com.proyecto.fhce.library.dto.request.library.EjemplarUpdateEstadoRequest;
import com.proyecto.fhce.library.dto.response.library.EdicionResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Edicion;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.HistorialEstadoEjemplar;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EdicionRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.HistorialEstadoEjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.ReservaRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.security.SecurityService;
import com.proyecto.fhce.library.services.library.EdicionServiceImpl;
import com.proyecto.fhce.library.services.library.EjemplarServiceImpl;
import com.proyecto.fhce.library.services.library.StorageService;

/** Iteración 2 · ediciones y ejemplares: reglas de negocio (unitarias). */
@DisplayName("Iteración 2 · Ediciones y ejemplares (unitarias)")
class EdicionEjemplarServiciosTest {

  // =====================================================================
  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("EdicionServiceImpl")
  class Ediciones {

    @Mock EdicionRepository edicionRepository;
    @Mock LibroRepository libroRepository;
    @Mock EjemplarRepository ejemplarRepository;
    @Mock StorageService storageService;
    @Mock AuditoriaService auditoriaService;
    @InjectMocks EdicionServiceImpl service;

    private EdicionRequest request(Long libroId) {
      EdicionRequest r = new EdicionRequest();
      r.setEditorial("Sudamericana");
      r.setAnoPublicacion(1967);
      r.setEdicion("1ra");
      r.setLibroId(libroId);
      return r;
    }

    @Test
    @DisplayName("IT2-U-EDI-01: crear edición de un libro inexistente lanza ResourceNotFoundException")
    void create_libroInexistente() {
      when(libroRepository.findById(9L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.create(request(9L), null, null))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Libro no encontrado con id: 9");
      verify(edicionRepository, never()).save(any());
    }

    @Test
    @DisplayName("IT2-U-EDI-02: crear edición válida sin archivos la guarda con sus datos y audita")
    void create_ok() {
      Libro l = libro(1L, "Cien años de soledad");
      when(libroRepository.findById(1L)).thenReturn(Optional.of(l));
      when(edicionRepository.save(any(Edicion.class))).thenAnswer(inv -> {
        Edicion e = inv.getArgument(0);
        e.setIdEdicion(20L);
        return e;
      });

      EdicionResponse r = service.create(request(1L), null, null);

      assertThat(r.getIdEdicion()).isEqualTo(20L);
      assertThat(r.getEditorial()).isEqualTo("Sudamericana");
      assertThat(r.getAnoPublicacion()).isEqualTo(1967);
      assertThat(r.getLibro().getTitulo()).isEqualTo("Cien años de soledad");
      assertThat(r.getEjemplaresTotal()).isZero();
      verify(auditoriaService).registrar(eq("CREAR_EDICION"), eq("Ediciones"), eq(20L), any(), any());
    }

    @Test
    @DisplayName("IT2-U-EDI-03: no se elimina una edición que tiene ejemplares")
    void delete_conEjemplares() {
      Edicion e = edicion(1L, libro(1L, "X"));
      when(edicionRepository.findById(1L)).thenReturn(Optional.of(e));
      when(ejemplarRepository.findByEdicion_IdEdicion(1L))
          .thenReturn(List.of(ejemplar(1L, e, biblioteca(1L, "B", TipoBiblioteca.FACULTATIVA), EstadoEjemplar.DISPONIBLE)));

      assertThatThrownBy(() -> service.delete(1L))
          .isInstanceOf(BusinessException.class)
          .hasMessage("No se puede eliminar una edición que tiene ejemplares registrados");
      verify(edicionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("IT2-U-EDI-04: al eliminar una edición sin ejemplares se borran también su portada y su PDF")
    void delete_ok_eliminaArchivos() {
      Edicion e = edicion(1L, libro(1L, "X"));
      e.setImagenPortada("/portadas/a.png");
      e.setPdfUrl("/pdfs/a.pdf");
      when(edicionRepository.findById(1L)).thenReturn(Optional.of(e));
      when(ejemplarRepository.findByEdicion_IdEdicion(1L)).thenReturn(List.of());

      service.delete(1L);

      verify(storageService).eliminar(eq("/portadas/a.png"), any());
      verify(storageService).eliminar(eq("/pdfs/a.pdf"), any());
      verify(edicionRepository).delete(e);
    }
  }

  // =====================================================================
  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("EjemplarServiceImpl")
  class Ejemplares {

    @Mock EjemplarRepository ejemplarRepository;
    @Mock EdicionRepository edicionRepository;
    @Mock LibroRepository libroRepository;
    @Mock BibliotecaRepository bibliotecaRepository;
    @Mock HistorialEstadoEjemplarRepository historialRepository;
    @Mock PrestamoRepository prestamoRepository;
    @Mock ReservaRepository reservaRepository;
    @Mock SecurityService securityService;
    @Mock AuditoriaService auditoriaService;
    @InjectMocks EjemplarServiceImpl service;

    private final Biblioteca bib = biblioteca(1L, "Biblioteca Central", TipoBiblioteca.FACULTATIVA);
    private final Edicion ed = edicion(10L, libro(1L, "Libro"));

    private Ejemplar existente(EstadoEjemplar estado) {
      Ejemplar e = ejemplar(7L, ed, bib, estado);
      when(ejemplarRepository.findById(7L)).thenReturn(Optional.of(e));
      return e;
    }

    private EjemplarUpdateEstadoRequest cambio(EstadoEjemplar nuevo) {
      EjemplarUpdateEstadoRequest r = new EjemplarUpdateEstadoRequest();
      r.setNuevoEstado(nuevo);
      r.setMotivo("Motivo de prueba");
      return r;
    }

    @Test
    @DisplayName("IT2-U-EJE-01: un ejemplar nuevo sin estado ni fecha queda DISPONIBLE con fecha de hoy")
    void create_valoresPorDefecto() {
      when(edicionRepository.findById(10L)).thenReturn(Optional.of(ed));
      when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(bib));
      when(ejemplarRepository.save(any(Ejemplar.class))).thenAnswer(inv -> {
        Ejemplar e = inv.getArgument(0);
        e.setIdEjemplar(33L);
        return e;
      });
      EjemplarRequest req = new EjemplarRequest();
      req.setEdicionId(10L);
      req.setBibliotecaId(1L);
      req.setCodigoEjemplar("EJ-033");

      EjemplarResponse r = service.create(req);

      assertThat(r.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.DISPONIBLE);
      assertThat(r.getFechaAdquisicion()).isEqualTo(LocalDate.now());
      assertThat(r.getCodigoEjemplar()).isEqualTo("EJ-033");
      verify(auditoriaService).registrar(eq("CREATE_EJEMPLAR"), eq("Ejemplares"), eq(33L), any(), any());
    }

    @Test
    @DisplayName("IT2-U-EJE-02: crear ejemplar con edición inexistente lanza ResourceNotFoundException")
    void create_edicionInexistente() {
      when(edicionRepository.findById(10L)).thenReturn(Optional.empty());
      EjemplarRequest req = new EjemplarRequest();
      req.setEdicionId(10L);
      req.setBibliotecaId(1L);

      assertThatThrownBy(() -> service.create(req)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("IT2-U-EJE-03: crear ejemplar con biblioteca inexistente lanza ResourceNotFoundException")
    void create_bibliotecaInexistente() {
      when(edicionRepository.findById(10L)).thenReturn(Optional.of(ed));
      when(bibliotecaRepository.findById(1L)).thenReturn(Optional.empty());
      EjemplarRequest req = new EjemplarRequest();
      req.setEdicionId(10L);
      req.setBibliotecaId(1L);

      assertThatThrownBy(() -> service.create(req))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Biblioteca no encontrada con id: 1");
    }

    @Test
    @DisplayName("IT2-U-EJE-04: cambiar a DISPONIBLE→EN_REPARACION actualiza el estado y registra el historial")
    void actualizarEstado_ok_registraHistorial() {
      Ejemplar e = existente(EstadoEjemplar.DISPONIBLE);
      when(ejemplarRepository.save(any(Ejemplar.class))).thenAnswer(inv -> inv.getArgument(0));

      EjemplarResponse r = service.actualizarEstado(7L, cambio(EstadoEjemplar.EN_REPARACION));

      assertThat(r.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.EN_REPARACION);
      ArgumentCaptor<HistorialEstadoEjemplar> cap = ArgumentCaptor.forClass(HistorialEstadoEjemplar.class);
      verify(historialRepository).save(cap.capture());
      assertThat(cap.getValue().getEstadoAnterior()).isEqualTo(EstadoEjemplar.DISPONIBLE);
      assertThat(cap.getValue().getEstadoNuevo()).isEqualTo(EstadoEjemplar.EN_REPARACION);
      assertThat(cap.getValue().getMotivo()).isEqualTo("Motivo de prueba");
      assertThat(cap.getValue().getEjemplar()).isSameAs(e);
    }

    @Test
    @DisplayName("IT2-U-EJE-05: cambiar al mismo estado lanza BusinessException")
    void actualizarEstado_mismoEstado() {
      existente(EstadoEjemplar.DISPONIBLE);

      assertThatThrownBy(() -> service.actualizarEstado(7L, cambio(EstadoEjemplar.DISPONIBLE)))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("ya se encuentra en estado");
    }

    @Test
    @DisplayName("IT2-U-EJE-06: un ejemplar dado de baja no puede cambiar de estado")
    void actualizarEstado_desdeBaja() {
      existente(EstadoEjemplar.BAJA);

      assertThatThrownBy(() -> service.actualizarEstado(7L, cambio(EstadoEjemplar.DISPONIBLE)))
          .isInstanceOf(BusinessException.class)
          .hasMessage("Un ejemplar dado de baja no puede cambiar de estado");
      verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("IT2-U-EJE-07: un ejemplar PRESTADO solo puede pasar a DISPONIBLE, EN_REPARACION o PERDIDO")
    void actualizarEstado_prestadoANoPermitido() {
      existente(EstadoEjemplar.PRESTADO);

      assertThatThrownBy(() -> service.actualizarEstado(7L, cambio(EstadoEjemplar.RESERVADO)))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("solo puede cambiar a DISPONIBLE, EN_REPARACION o PERDIDO");
    }

    @Test
    @DisplayName("IT2-U-EJE-08: no se da de baja un ejemplar prestado")
    void darDeBaja_prestado() {
      existente(EstadoEjemplar.PRESTADO);

      assertThatThrownBy(() -> service.darDeBaja(7L, "obsoleto"))
          .isInstanceOf(BusinessException.class)
          .hasMessage("No se puede dar de baja un ejemplar prestado");
    }

    @Test
    @DisplayName("IT2-U-EJE-09: dar de baja un ejemplar disponible lo deja en BAJA con historial")
    void darDeBaja_ok() {
      Ejemplar e = existente(EstadoEjemplar.DISPONIBLE);

      service.darDeBaja(7L, "obsoleto");

      assertThat(e.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.BAJA);
      verify(historialRepository).save(any(HistorialEstadoEjemplar.class));
    }

    @Test
    @DisplayName("IT2-U-EJE-10: habilitar por reposición un ejemplar PERDIDO lo devuelve a DISPONIBLE")
    void habilitarPorReposicion_perdido() {
      existente(EstadoEjemplar.PERDIDO);
      when(ejemplarRepository.save(any(Ejemplar.class))).thenAnswer(inv -> inv.getArgument(0));

      EjemplarResponse r = service.habilitarPorReposicion(7L, null);

      assertThat(r.getEstadoEjemplar()).isEqualTo(EstadoEjemplar.DISPONIBLE);
      ArgumentCaptor<HistorialEstadoEjemplar> cap = ArgumentCaptor.forClass(HistorialEstadoEjemplar.class);
      verify(historialRepository).save(cap.capture());
      assertThat(cap.getValue().getMotivo()).isEqualTo("Reposición física de ejemplar");
    }

    @Test
    @DisplayName("IT2-U-EJE-11: no se habilita por reposición un ejemplar que ya está DISPONIBLE")
    void habilitarPorReposicion_yaDisponible() {
      existente(EstadoEjemplar.DISPONIBLE);

      assertThatThrownBy(() -> service.habilitarPorReposicion(7L, null))
          .isInstanceOf(BusinessException.class)
          .hasMessage("El ejemplar ya se encuentra disponible.");
    }

    @Test
    @DisplayName("IT2-U-EJE-12: no se habilita por reposición un ejemplar RESERVADO (solo PERDIDO o DAÑADO)")
    void habilitarPorReposicion_estadoNoPermitido() {
      existente(EstadoEjemplar.RESERVADO);

      assertThatThrownBy(() -> service.habilitarPorReposicion(7L, null))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("no permite habilitarlo por reposición");
    }

    @Test
    @DisplayName("IT2-U-EJE-13: no se transfiere un ejemplar prestado a otra biblioteca")
    void transferir_prestado() {
      existente(EstadoEjemplar.PRESTADO);

      assertThatThrownBy(() -> service.transferirBiblioteca(7L, 2L, "traslado"))
          .isInstanceOf(BusinessException.class)
          .hasMessage("No se puede transferir un ejemplar prestado");
    }

    @Test
    @DisplayName("IT2-U-EJE-14: transferir un ejemplar disponible cambia su biblioteca")
    void transferir_ok() {
      Ejemplar e = existente(EstadoEjemplar.DISPONIBLE);
      Biblioteca destino = biblioteca(2L, "Biblioteca Carrera", TipoBiblioteca.CARRERA);
      when(bibliotecaRepository.findById(2L)).thenReturn(Optional.of(destino));

      service.transferirBiblioteca(7L, 2L, "traslado");

      assertThat(e.getBiblioteca()).isSameAs(destino);
      verify(ejemplarRepository).save(e);
    }

    @Test
    @DisplayName("IT2-U-EJE-15: no se elimina un ejemplar prestado")
    void delete_prestado() {
      existente(EstadoEjemplar.PRESTADO);

      assertThatThrownBy(() -> service.delete(7L))
          .isInstanceOf(BusinessException.class)
          .hasMessage("No se puede eliminar un ejemplar prestado");
    }

    @Test
    @DisplayName("IT2-U-EJE-16: no se elimina un ejemplar con préstamos o reservas históricas")
    void delete_conPrestamosOReservas() {
      existente(EstadoEjemplar.DISPONIBLE);
      when(prestamoRepository.existsByEjemplar_IdEjemplar(7L)).thenReturn(true);

      assertThatThrownBy(() -> service.delete(7L))
          .isInstanceOf(BusinessException.class)
          .hasMessage("No se puede eliminar un ejemplar con préstamos registrados");

      when(prestamoRepository.existsByEjemplar_IdEjemplar(7L)).thenReturn(false);
      when(reservaRepository.existsByEjemplar_IdEjemplar(7L)).thenReturn(true);

      assertThatThrownBy(() -> service.delete(7L))
          .isInstanceOf(BusinessException.class)
          .hasMessage("No se puede eliminar un ejemplar con reservas registradas");
    }

    @Test
    @DisplayName("IT2-U-EJE-17: se elimina un ejemplar sin préstamos ni reservas")
    void delete_ok() {
      Ejemplar e = existente(EstadoEjemplar.DISPONIBLE);

      service.delete(7L);

      verify(ejemplarRepository).delete(e);
    }

    @Test
    @DisplayName("IT2-U-EJE-18: buscar un ejemplar inexistente lanza ResourceNotFoundException")
    void findById_noExiste() {
      when(ejemplarRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.findById(99L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Ejemplar no encontrado con id: 99");
    }
  }
}

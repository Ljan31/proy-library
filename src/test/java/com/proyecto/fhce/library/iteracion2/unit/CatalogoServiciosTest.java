package com.proyecto.fhce.library.iteracion2.unit;

import static com.proyecto.fhce.library.support.TestData.autor;
import static com.proyecto.fhce.library.support.TestData.categoria;
import static com.proyecto.fhce.library.support.TestData.libro;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.fhce.library.dto.request.library.AutorRequest;
import com.proyecto.fhce.library.dto.request.library.CategoriaLibroRequest;
import com.proyecto.fhce.library.dto.request.library.LibroRequest;
import com.proyecto.fhce.library.dto.response.library.AutorResponse;
import com.proyecto.fhce.library.dto.response.library.CategoriaLibroResponse;
import com.proyecto.fhce.library.dto.response.library.LibroResponse;
import com.proyecto.fhce.library.entities.Autor;
import com.proyecto.fhce.library.entities.CategoriaLibro;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.AutorRepository;
import com.proyecto.fhce.library.repositories.CategoriaLibroRepository;
import com.proyecto.fhce.library.repositories.EdicionRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.HistorialEstadoEjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.ReservaRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.library.AutorService;
import com.proyecto.fhce.library.services.library.CategoriaLibroServiceImpl;
import com.proyecto.fhce.library.services.library.LibroServiceImpl;

/**
 * Iteración 2 · catálogo bibliográfico: libros, categorías y autores
 * (unitarias).
 */
@DisplayName("Iteración 2 · Catálogo: libros, categorías y autores (unitarias)")
class CatalogoServiciosTest {

  // =====================================================================
  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("LibroServiceImpl")
  class Libros {

    @Mock
    LibroRepository libroRepository;
    @Mock
    CategoriaLibroRepository categoriaRepository;
    @Mock
    EjemplarRepository ejemplarRepository;
    @Mock
    EdicionRepository edicionRepository;
    @Mock
    AutorRepository autorRepository;
    @Mock
    PrestamoRepository prestamoRepository;
    @Mock
    HistorialEstadoEjemplarRepository historialRepository;
    @Mock
    ReservaRepository reservaRepository;
    @Mock
    AuditoriaService auditoriaService;
    @InjectMocks
    LibroServiceImpl service;

    private LibroRequest request(Long categoriaId) {
      LibroRequest r = new LibroRequest();
      r.setTitulo("Cien años de soledad");
      r.setIdioma("Español");
      r.setDescripcion("Novela");
      r.setCategoriaId(categoriaId);
      return r;
    }

    @Test
    @DisplayName("IT2-U-LIB-01: crear libro con categoría lo guarda, la asocia y audita CREAR_LIBRO")
    void create_conCategoria() {
      when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria(5L, "Literatura", "800")));
      when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> {
        Libro l = inv.getArgument(0);
        l.setIdLibro(100L);
        return l;
      });

      LibroResponse response = service.create(request(5L));

      assertThat(response.getIdLibro()).isEqualTo(100L);
      assertThat(response.getTitulo()).isEqualTo("Cien años de soledad");
      assertThat(response.getCategoria().getNombre_categoria()).isEqualTo("Literatura");
      verify(auditoriaService).registrar(eq("CREAR_LIBRO"), any(), eq(100L), isNull(), any());
    }

    @Test
    @DisplayName("IT2-U-LIB-02: crear libro con categoría inexistente lanza ResourceNotFoundException")
    void create_categoriaInexistente() {
      when(categoriaRepository.findById(9L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.create(request(9L)))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Categoría no encontrada con id: 9");
      verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("IT2-U-LIB-03: buscar un libro inexistente lanza ResourceNotFoundException")
    void findById_noExiste() {
      when(libroRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.findById(99L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Libro no encontrado con id: 99");
    }

    @Test
    @DisplayName("IT2-U-LIB-04: actualizar un libro inexistente lanza ResourceNotFoundException")
    void update_noExiste() {
      when(libroRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.update(99L, request(null)))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("IT2-U-LIB-05: no se elimina un libro con préstamos asociados")
    void delete_conPrestamos() {
      when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(1L, "X")));
      when(prestamoRepository.existsByEjemplarEdicionLibroIdLibro(1L)).thenReturn(true);

      assertThatThrownBy(() -> service.delete(1L))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("préstamos asociados");
      // verify(libroRepository, never()).delete(any());
      verify(libroRepository, never()).delete(any(Libro.class));
    }

    @Test
    @DisplayName("IT2-U-LIB-06: no se elimina un libro con historial de estados de ejemplares")
    void delete_conHistorial() {
      when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(1L, "X")));
      when(prestamoRepository.existsByEjemplarEdicionLibroIdLibro(1L)).thenReturn(false);
      when(historialRepository.existsByEjemplarEdicionLibroIdLibro(1L)).thenReturn(true);

      assertThatThrownBy(() -> service.delete(1L))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("historial de estados");
    }

    @Test
    @DisplayName("IT2-U-LIB-07: no se elimina un libro con reservas asociadas")
    void delete_conReservas() {
      when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(1L, "X")));
      when(reservaRepository.existsByEjemplarEdicionLibroIdLibro(1L)).thenReturn(true);

      assertThatThrownBy(() -> service.delete(1L))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining("reservas asociadas");
    }

    @Test
    @DisplayName("IT2-U-LIB-08: se elimina un libro sin dependencias y se audita ELIMINAR_LIBRO")
    void delete_ok() {
      Libro l = libro(1L, "X");
      when(libroRepository.findById(1L)).thenReturn(Optional.of(l));

      service.delete(1L);

      verify(libroRepository).delete(l);
      verify(auditoriaService).registrar(eq("ELIMINAR_LIBRO"), any(), eq(1L), any(), isNull());
    }
  }

  // =====================================================================
  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("CategoriaLibroServiceImpl")
  class Categorias {

    @Mock
    CategoriaLibroRepository categoriaRepository;
    @Mock
    LibroRepository libroRepository;
    @Mock
    AuditoriaService auditoriaService;
    @InjectMocks
    CategoriaLibroServiceImpl service;

    private CategoriaLibroRequest request(String nombre, String dewey) {
      CategoriaLibroRequest r = new CategoriaLibroRequest();
      r.setNombre_categoria(nombre);
      r.setCodigo_dewey(dewey);
      return r;
    }

    @Test
    @DisplayName("IT2-U-CAT-01: crear categoría con código Dewey repetido lanza DuplicateResourceException")
    void create_deweyRepetido() {
      when(categoriaRepository.existsByCodigoDewey("800")).thenReturn(true);

      assertThatThrownBy(() -> service.create(request("Literatura", "800")))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Ya existe una categoría con código Dewey: 800");
      verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("IT2-U-CAT-02: crear categoría válida la guarda y audita CREAR_CATEGORIA")
    void create_ok() {
      when(categoriaRepository.existsByCodigoDewey("800")).thenReturn(false);
      when(categoriaRepository.save(any(CategoriaLibro.class))).thenAnswer(inv -> {
        CategoriaLibro c = inv.getArgument(0);
        c.setId_categoria(3L);
        return c;
      });

      CategoriaLibroResponse r = service.create(request("Literatura", "800"));

      assertThat(r.getId_categoria()).isEqualTo(3L);
      assertThat(r.getCodigo_dewey()).isEqualTo("800");
      verify(auditoriaService).registrar(eq("CREAR_CATEGORIA"), any(), eq(3L), isNull(), any());
    }

    @Test
    @DisplayName("IT2-U-CAT-03: actualizar categoría con el Dewey de OTRA categoría lanza DuplicateResourceException")
    void update_deweyDeOtra() {
      when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1L, "A", "100")));
      when(categoriaRepository.existsByCodigoDewey("800")).thenReturn(true);

      assertThatThrownBy(() -> service.update(1L, request("A", "800")))
          .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("IT2-U-CAT-04: actualizar categoría conservando su propio Dewey no valida duplicado")
    void update_mismoDewey() {
      when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1L, "A", "100")));
      when(categoriaRepository.save(any(CategoriaLibro.class))).thenAnswer(inv -> inv.getArgument(0));

      CategoriaLibroResponse r = service.update(1L, request("A nuevo", "100"));

      assertThat(r.getNombre_categoria()).isEqualTo("A nuevo");
      verify(categoriaRepository, never()).existsByCodigoDewey(any());
    }

    @Test
    @DisplayName("IT2-U-CAT-05: no se elimina una categoría que tiene libros")
    void delete_conLibros() {
      when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1L, "A", "100")));
      when(libroRepository.countByCategoria_IdCategoria(1L)).thenReturn(4L);

      assertThatThrownBy(() -> service.delete(1L))
          .isInstanceOf(BusinessException.class)
          .hasMessage("No se puede eliminar la categoría porque tiene 4 libro(s) asociado(s)");
    }

    @Test
    @DisplayName("IT2-U-CAT-06: se elimina una categoría sin libros")
    void delete_ok() {
      CategoriaLibro c = categoria(1L, "A", "100");
      when(categoriaRepository.findById(1L)).thenReturn(Optional.of(c));
      when(libroRepository.countByCategoria_IdCategoria(1L)).thenReturn(0L);

      service.delete(1L);

      verify(categoriaRepository).delete(c);
    }

    @Test
    @DisplayName("IT2-U-CAT-07: buscar categoría por Dewey inexistente lanza ResourceNotFoundException")
    void findByDewey_noExiste() {
      when(categoriaRepository.findByCodigoDewey("999")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.findByCodigoDewey("999"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Categoría no encontrada con código Dewey: 999");
    }
  }

  // =====================================================================
  @Nested
  @ExtendWith(MockitoExtension.class)
  @DisplayName("AutorService")
  class Autores {

    @Mock
    AutorRepository autorRepository;
    @InjectMocks
    AutorService service;

    private AutorRequest request(String nombre) {
      AutorRequest r = new AutorRequest();
      r.setNombre(nombre);
      return r;
    }

    @Test
    @DisplayName("IT2-U-AUT-01: crear autor con nombre repetido (sin importar mayúsculas) lanza DuplicateResourceException")
    void create_duplicado() {
      when(autorRepository.existsByNombreIgnoreCase("Gabriel García Márquez")).thenReturn(true);

      assertThatThrownBy(() -> service.create(request("Gabriel García Márquez")))
          .isInstanceOf(DuplicateResourceException.class);
      verify(autorRepository, never()).save(any());
    }

    @Test
    @DisplayName("IT2-U-AUT-02: crear autor válido devuelve su id y nombre")
    void create_ok() {
      when(autorRepository.existsByNombreIgnoreCase("Borges")).thenReturn(false);
      when(autorRepository.save(any(Autor.class))).thenAnswer(inv -> {
        Autor a = inv.getArgument(0);
        a.setIdAutor(8L);
        return a;
      });

      AutorResponse r = service.create(request("Borges"));

      assertThat(r.getIdAutor()).isEqualTo(8L);
      assertThat(r.getNombre()).isEqualTo("Borges");
    }

    @Test
    @DisplayName("IT2-U-AUT-03: renombrar un autor al nombre de otro lanza DuplicateResourceException")
    void update_nombreDeOtro() {
      when(autorRepository.findById(1L)).thenReturn(Optional.of(autor(1L, "Borges")));
      when(autorRepository.existsByNombreIgnoreCase("Cortázar")).thenReturn(true);

      assertThatThrownBy(() -> service.update(1L, request("Cortázar")))
          .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("IT2-U-AUT-04: cambiar solo mayúsculas del propio nombre es válido")
    void update_mismoNombreOtraCapitalizacion() {
      when(autorRepository.findById(1L)).thenReturn(Optional.of(autor(1L, "borges")));
      when(autorRepository.save(any(Autor.class))).thenAnswer(inv -> inv.getArgument(0));

      assertThat(service.update(1L, request("Borges")).getNombre()).isEqualTo("Borges");
      verify(autorRepository, never()).existsByNombreIgnoreCase(any());
    }

    @Test
    @DisplayName("IT2-U-AUT-05: eliminar un autor inexistente lanza ResourceNotFoundException")
    void delete_noExiste() {
      when(autorRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.delete(99L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Autor no encontrado con id: 99");
    }
  }
}

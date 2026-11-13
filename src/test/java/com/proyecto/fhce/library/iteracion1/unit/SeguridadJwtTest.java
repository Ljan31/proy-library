package com.proyecto.fhce.library.iteracion1.unit;

import static com.proyecto.fhce.library.support.TestData.role;
import static com.proyecto.fhce.library.support.TestData.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.security.CustomUserDetailsService;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.security.jwt.JwtTokenProvider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/** Iteración 1 · mecanismos de seguridad: JWT, UserDetails y carga de usuarios. */
@DisplayName("Iteración 1 · Seguridad JWT (unitarias)")
class SeguridadJwtTest {

  private static final String SECRET = "CLAVE_DE_PRUEBAS_CON_MAS_DE_32_CARACTERES_123456";

  private JwtTokenProvider provider;

  @BeforeEach
  void setUp() {
    provider = new JwtTokenProvider();
    ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
    ReflectionTestUtils.setField(provider, "jwtExpirationMs", 60_000L);
  }

  private Authentication autenticacionDe(Usuario u) {
    UserDetailsImpl details = UserDetailsImpl.build(u);
    return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
  }

  @Test
  @DisplayName("IT1-U-JWT-01: el token generado contiene el username y los roles del usuario")
  void generateToken_incluyeUsernameYRoles() {
    Usuario u = usuario(1L, "admin", role(1L, "ROLE_ADMIN"), role(3L, "ROLE_BIBLIOTECARIO"));

    String token = provider.generateToken(autenticacionDe(u));

    assertThat(token).isNotBlank();
    assertThat(provider.getUsernameFromJwt(token)).isEqualTo("admin");
    assertThat(provider.getRolesFromToken(token)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_BIBLIOTECARIO");
  }

  @Test
  @DisplayName("IT1-U-JWT-02: un token recién generado es válido")
  void validateToken_tokenValido() {
    String token = provider.generateTokenFromUsername("juan");

    assertThat(provider.validateToken(token)).isTrue();
  }

  @Test
  @DisplayName("IT1-U-JWT-03: un token expirado es inválido")
  void validateToken_tokenExpirado() {
    ReflectionTestUtils.setField(provider, "jwtExpirationMs", -1_000L);
    String expirado = provider.generateTokenFromUsername("juan");

    assertThat(provider.validateToken(expirado)).isFalse();
  }

  @Test
  @DisplayName("IT1-U-JWT-04: un token con la firma alterada es inválido")
  void validateToken_tokenAlterado() {
    String token = provider.generateTokenFromUsername("juan");
    String[] partes = token.split("\\.");
    String firma = partes[2];
    char primero = firma.charAt(0) == 'A' ? 'B' : 'A';
    String alterado = partes[0] + "." + partes[1] + "." + primero + firma.substring(1);

    assertThat(provider.validateToken(alterado)).isFalse();
  }

  @Test
  @DisplayName("IT1-U-JWT-05: un token firmado con otra clave es inválido")
  void validateToken_otraClave() {
    String ajeno = Jwts.builder()
        .setSubject("juan")
        .setExpiration(new Date(System.currentTimeMillis() + 60_000))
        .signWith(Keys.hmacShaKeyFor("OTRA_CLAVE_COMPLETAMENTE_DISTINTA_DE_32_CARACTERES".getBytes()),
            SignatureAlgorithm.HS256)
        .compact();

    assertThat(provider.validateToken(ajeno)).isFalse();
  }

  @Test
  @DisplayName("IT1-U-JWT-06: cadenas vacías o basura no son tokens válidos")
  void validateToken_basura() {
    assertThat(provider.validateToken("")).isFalse();
    assertThat(provider.validateToken("esto-no-es-un-jwt")).isFalse();
  }

  @Test
  @DisplayName("IT1-U-JWT-07: un token válido de un usuario DESHABILITADO no es aceptado")
  void validateTokenAndEnabled_usuarioDeshabilitado() {
    Usuario u = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    u.setEnabled(false);
    CustomUserDetailsService uds = mock(CustomUserDetailsService.class);
    when(uds.loadUserByUsername("juan")).thenReturn(UserDetailsImpl.build(u));
    String token = provider.generateTokenFromUsername("juan");

    assertThat(provider.validateTokenAndEnabled(token, uds)).isFalse();
  }

  @Test
  @DisplayName("IT1-U-JWT-08: un token válido de un usuario HABILITADO es aceptado")
  void validateTokenAndEnabled_usuarioHabilitado() {
    Usuario u = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    CustomUserDetailsService uds = mock(CustomUserDetailsService.class);
    when(uds.loadUserByUsername("juan")).thenReturn(UserDetailsImpl.build(u));
    String token = provider.generateTokenFromUsername("juan");

    assertThat(provider.validateTokenAndEnabled(token, uds)).isTrue();
  }

  // ---------- UserDetailsImpl ----------

  @Test
  @DisplayName("IT1-U-JWT-09: UserDetailsImpl.build copia id, username, email, enabled y authorities")
  void userDetails_build() {
    Usuario u = usuario(7L, "juan", role(4L, "ROLE_ESTUDIANTE"));

    UserDetailsImpl d = UserDetailsImpl.build(u);

    assertThat(d.getId()).isEqualTo(7L);
    assertThat(d.getUsername()).isEqualTo("juan");
    assertThat(d.getEmail()).isEqualTo("persona7@correo.com");
    assertThat(d.isEnabled()).isTrue();
    assertThat(d.getAuthorities().stream().map(a -> a.getAuthority()).toList())
        .containsExactly("ROLE_ESTUDIANTE");
  }

  // ---------- CustomUserDetailsService ----------

  @Test
  @DisplayName("IT1-U-JWT-10: loadUserByUsername devuelve el usuario existente")
  void loadUser_existente() {
    UserRepository repo = mock(UserRepository.class);
    Usuario u = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(repo.findByUsername("juan")).thenReturn(Optional.of(u));

    var details = new CustomUserDetailsService(repo).loadUserByUsername("juan");

    assertThat(details.getUsername()).isEqualTo("juan");
  }

  @Test
  @DisplayName("IT1-U-JWT-11: loadUserByUsername de un usuario inexistente lanza UsernameNotFoundException")
  void loadUser_inexistente() {
    UserRepository repo = mock(UserRepository.class);
    when(repo.findByUsername("nadie")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new CustomUserDetailsService(repo).loadUserByUsername("nadie"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Usuario no encontrado: nadie");
  }
}

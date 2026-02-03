// package com.proyecto.fhce.library.security;

// import java.util.Collection;
// import java.util.List;
// import java.util.Objects;
// import java.util.stream.Collectors;

// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;

// import com.proyecto.fhce.library.entities.Usuario;

// public class UserDetailsImpl implements UserDetails {

// private Long id;
// private String username;
// private String email;
// private String password;
// private boolean enabled;
// private Collection<? extends GrantedAuthority> authorities;

// public UserDetailsImpl(Long id, String username, String email, String
// password,
// boolean enabled, Collection<? extends GrantedAuthority> authorities) {
// this.id = id;
// this.username = username;
// this.email = email;
// this.password = password;
// this.enabled = enabled;
// this.authorities = authorities;
// }

// public static UserDetailsImpl build(Usuario usuario) {
// List<GrantedAuthority> authorities = usuario.getRoles().stream()
// .map(role -> new SimpleGrantedAuthority(role.getName()))
// .collect(Collectors.toList());

// return new UserDetailsImpl(
// usuario.getId_usuario(),
// usuario.getUsername(),
// usuario.getPersona().getEmail(),
// usuario.getPassword(),
// usuario.isEnabled(),
// authorities);
// }

// @Override
// public Collection<? extends GrantedAuthority> getAuthorities() {
// return authorities;
// }

// @Override
// public String getPassword() {
// return password;
// }

// @Override
// public String getUsername() {
// return username;
// }

// @Override
// public boolean isAccountNonExpired() {
// return true;
// }

// @Override
// public boolean isAccountNonLocked() {
// return enabled;
// }

// @Override
// public boolean isCredentialsNonExpired() {
// return true;
// }

// @Override
// public boolean isEnabled() {
// return enabled;
// }

// public Long getId() {
// return id;
// }

// public String getEmail() {
// return email;
// }

// @Override
// public boolean equals(Object o) {
// if (this == o)
// return true;
// if (o == null || getClass() != o.getClass())
// return false;
// UserDetailsImpl user = (UserDetailsImpl) o;
// return Objects.equals(id, user.id);
// }

// @Override
// public int hashCode() {
// return Objects.hash(id);
// }
// }
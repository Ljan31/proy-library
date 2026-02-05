package com.proyecto.fhce.library.security;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.proyecto.fhce.library.security.jwt.JwtAuthenticationEntryPoint;
// import com.proyecto.fhce.library.security.jwt.JwtAuthenticationEntryPoint;
import com.proyecto.fhce.library.security.jwt.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity

public class SpringSecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  // @Autowired
  // private final JwtAuthenticationEntryPoint unauthorizedHandler;

  public SpringSecurityConfig(UserDetailsService userDetailsService,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
    this.userDetailsService = userDetailsService;
    // this.unauthorizedHandler = unauthorizedHandler;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
  }

  @Bean
  JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  // @Bean
  // DaoAuthenticationProvider authenticationProvider() {
  // // return new DaoAuthenticationProvider(userDetailsService);
  // DaoAuthenticationProvider authProvider = new
  // DaoAuthenticationProvider(userDetailsService);
  // // authProvider.setUserDetailsService(userDetailsService);
  // authProvider.setPasswordEncoder(passwordEncoder());
  // return authProvider;
  // }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/carreras/**").permitAll()
            // .requestMatchers("/api/users/**").permitAll()
            // .requestMatchers("/api/personas/**").permitAll()
            // .requestMatchers("/api/libros/**").permitAll()
            // .requestMatchers("/api/roles/**").permitAll()
            // .requestMatchers("/api/permisos/**").permitAll()
            // .requestMatchers("/api/autores/**").permitAll()
            // .requestMatchers("/api/categorias/**").permitAll()
            // .requestMatchers("/api/bibliotecas").permitAll()
            // .requestMatchers("/api/certificados/validar/**").permitAll()
            // Swagger/OpenAPI
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/api/seed/**").hasRole("ADMIN")
            .anyRequest().authenticated());
    // http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    http.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(Arrays.asList("*"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }

  @Bean
  FilterRegistrationBean<CorsFilter> corsFilter() {
    FilterRegistrationBean<CorsFilter> corsBean = new FilterRegistrationBean<>(
        new CorsFilter(corsConfigurationSource()));
    corsBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return corsBean;
  }
}

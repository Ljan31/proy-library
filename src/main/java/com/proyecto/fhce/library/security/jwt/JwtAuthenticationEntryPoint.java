// package com.proyecto.fhce.library.security.jwt;

// import java.io.IOException;
// import java.time.LocalDateTime;
// import java.util.HashMap;
// import java.util.Map;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.AuthenticationEntryPoint;
// import org.springframework.stereotype.Component;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint
// {
// private static final Logger logger =
// LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

// @Override
// public void commence(HttpServletRequest request,
// HttpServletResponse response,
// AuthenticationException authException)
// throws IOException, ServletException {

// logger.error("Unauthorized error: {}", authException.getMessage());

// response.setContentType("application/json");
// response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

// Map<String, Object> body = new HashMap<>();
// body.put("success", false);
// body.put("message", "No autorizado: " + authException.getMessage());
// body.put("timestamp", LocalDateTime.now().toString());
// body.put("path", request.getServletPath());

// ObjectMapper mapper = new ObjectMapper();
// mapper.registerModule(new JavaTimeModule());
// mapper.writeValue(response.getOutputStream(), body);
// }
// }

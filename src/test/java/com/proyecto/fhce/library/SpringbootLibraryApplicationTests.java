package com.proyecto.fhce.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Prueba de humo: el contexto de Spring arranca con la BD en memoria (perfil "test"). */
@SpringBootTest
@ActiveProfiles("test")
class SpringbootLibraryApplicationTests {

	@Test
	@DisplayName("IT0-I-APP-01: el contexto de la aplicación arranca correctamente")
	void contextLoads() {
	}

}

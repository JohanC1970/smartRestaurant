package com.smartRestaurant;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmartRestaurantApplicationTests {

	@Test
	@Disabled("Requiere base de datos PostgreSQL — ejecutar solo en entorno con DB disponible")
	void contextLoads() {
	}

}

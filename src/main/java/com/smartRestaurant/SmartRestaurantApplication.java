package com.smartRestaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartRestaurantApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartRestaurantApplication.class, args);
	}

}

package com.smartRestaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sendgrid.SendGridAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = { SendGridAutoConfiguration.class })
@EnableAsync
public class SmartRestaurantApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartRestaurantApplication.class, args);
	}

}

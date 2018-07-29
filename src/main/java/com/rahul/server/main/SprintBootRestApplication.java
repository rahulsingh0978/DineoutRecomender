package com.rahul.server.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.rahul"})
public class SprintBootRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SprintBootRestApplication.class, args);
	}
}

package com.here.backend;

import org.springframework.boot.SpringApplication;

public class TestHereSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.from(backendSpringbootApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

package com.project2;

import org.springframework.boot.SpringApplication;

public class TestProject2Application {

	public static void main(String[] args) {
		SpringApplication.from(Project2Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}

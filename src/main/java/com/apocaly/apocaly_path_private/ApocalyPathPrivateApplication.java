package com.apocaly.apocaly_path_private;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class ApocalyPathPrivateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApocalyPathPrivateApplication.class, args);
	}

}

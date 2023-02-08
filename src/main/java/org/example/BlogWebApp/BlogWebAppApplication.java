package org.example.BlogWebApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.SpringFramework.SpringBoot.application.SpringApplication;
import org.example.SpringFramework.SpringBoot.annotations.SpringBootApplication;
import org.example.SpringFramework.SpringContainer.annotations.beans.Bean;

@SpringBootApplication
public class BlogWebAppApplication {
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(BlogWebAppApplication.class, args);
	}
}

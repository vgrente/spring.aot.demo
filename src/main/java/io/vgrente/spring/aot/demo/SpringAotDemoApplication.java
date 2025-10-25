package io.vgrente.spring.aot.demo;

import io.vgrente.spring.aot.demo.config.JacksonRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"io.vgrente.spring.aot.demo.controller", "io.vgrente.spring.aot.demo.config",
		"io.vgrente.spring.aot.demo.repository"})
@ImportRuntimeHints(JacksonRuntimeHints.class)
public class SpringAotDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAotDemoApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

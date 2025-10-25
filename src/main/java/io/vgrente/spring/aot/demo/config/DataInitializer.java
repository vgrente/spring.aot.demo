package io.vgrente.spring.aot.demo.config;

import io.vgrente.spring.aot.demo.model.Product;
import io.vgrente.spring.aot.demo.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
	private final ProductRepository productRepository;

	public DataInitializer(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public void run(String... args) {
		productRepository.save(new Product("Laptop", 999.99, "High performance laptop"));
		productRepository.save(new Product("Mouse", 29.99, "Wireless mouse"));
		productRepository.save(new Product("Keyboard", 79.99, "Mechanical keyboard"));
		productRepository.save(new Product("Monitor", 299.99, "27 inch 4K monitor"));
		productRepository.save(new Product("Headphones", 149.99, "Noise cancelling headphones"));

		log.info("Sample data initialized: {} products", productRepository.count());
	}
}

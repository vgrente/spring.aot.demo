package io.vgrente.spring.aot.demo.controller;

import java.util.List;

import io.vgrente.spring.aot.demo.exception.BadRequestException;
import io.vgrente.spring.aot.demo.exception.ResourceNotFoundException;
import io.vgrente.spring.aot.demo.model.Product;
import io.vgrente.spring.aot.demo.repository.ProductRepository;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository productRepository;

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@GetMapping
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Long id) {
		return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
	}

	@PostMapping
	public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
		if (product.getId() != null) {
			throw new BadRequestException("Product ID must not be provided when creating a new product");
		}

		Product savedProduct = productRepository.save(product);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}

	@PutMapping("/{id}")
	public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", id));

		existingProduct.setName(product.getName());
		existingProduct.setPrice(product.getPrice());
		existingProduct.setDescription(product.getDescription());

		return productRepository.save(existingProduct);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		if (!productRepository.existsById(id)) {
			throw new ResourceNotFoundException("Product", id);
		}

		productRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/search")
	public List<Product> searchProducts(@RequestParam String name) {
		if (name == null || name.isBlank()) {
			throw new BadRequestException("Search parameter 'name' must not be empty");
		}

		return productRepository.findByNameContainingIgnoreCase(name);
	}

}

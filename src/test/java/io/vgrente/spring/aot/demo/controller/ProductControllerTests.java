package io.vgrente.spring.aot.demo.controller;

import io.vgrente.spring.aot.demo.model.Product;
import io.vgrente.spring.aot.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ProductControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ProductRepository productRepository;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		productRepository.deleteAll();
	}

	@Test
	void shouldGetAllProducts() throws Exception {
		Product product = new Product("Test Product", 99.99, "Test Description");
		productRepository.save(product);

		mockMvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is("Test Product"))).andExpect(jsonPath("$[0].price", is(99.99)));
	}

	@Test
	void shouldCreateProduct() throws Exception {
		String productJson = """
				{
				    "name": "New Product",
				    "price": 149.99,
				    "description": "New Description"
				}
				""";

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(productJson))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name", is("New Product")))
				.andExpect(jsonPath("$.price", is(149.99))).andExpect(jsonPath("$.id", notNullValue()));
	}

	@Test
	void shouldGetProductById() throws Exception {
		Product product = new Product("Test Product", 99.99, "Test Description");
		Product saved = productRepository.save(product);

		mockMvc.perform(get("/api/products/" + saved.getId())).andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Test Product"))).andExpect(jsonPath("$.price", is(99.99)));
	}

	@Test
	void shouldReturnNotFoundForNonExistentProduct() throws Exception {
		mockMvc.perform(get("/api/products/999")).andExpect(status().isNotFound());
	}

	@Test
	void shouldUpdateProduct() throws Exception {
		Product product = new Product("Original Product", 99.99, "Original Description");
		Product saved = productRepository.save(product);

		String updatedJson = """
				{
				    "name": "Updated Product",
				    "price": 199.99,
				    "description": "Updated Description"
				}
				""";

		mockMvc.perform(
				put("/api/products/" + saved.getId()).contentType(MediaType.APPLICATION_JSON).content(updatedJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name", is("Updated Product")))
				.andExpect(jsonPath("$.price", is(199.99)));
	}

	@Test
	void shouldDeleteProduct() throws Exception {
		Product product = new Product("Test Product", 99.99, "Test Description");
		Product saved = productRepository.save(product);

		mockMvc.perform(delete("/api/products/" + saved.getId())).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/products/" + saved.getId())).andExpect(status().isNotFound());
	}

	@Test
	void shouldSearchProducts() throws Exception {
		productRepository.save(new Product("Laptop Pro", 999.99, "High end laptop"));
		productRepository.save(new Product("Gaming Laptop", 1499.99, "Gaming laptop"));
		productRepository.save(new Product("Mouse", 29.99, "Wireless mouse"));

		mockMvc.perform(get("/api/products/search?name=laptop")).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].name", hasItem(containsString("Laptop"))));
	}
}

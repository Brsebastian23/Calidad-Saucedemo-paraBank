package com.saucedemo.controller;

import com.saucedemo.model.Product;
import com.saucedemo.service.interfaces.IProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private IProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    @DisplayName("getAllProducts devuelve la lista de productos del service")
    void get_all_products_returns_list() {
        // Arrange
        Product backpack = buildProduct(1L, "Sauce Labs Backpack");
        Product bikeLight = buildProduct(2L, "Sauce Labs Bike Light");
        when(productService.getAllProducts()).thenReturn(List.of(backpack, bikeLight));

        // Act
        List<Product> result = productController.getAllProducts();

        // Assert
        assertAll("lista de productos",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Sauce Labs Backpack", result.get(0).getName())
        );

    }

    @Test
    @DisplayName("getAllProducts devuelve lista vacía cuando no hay productos")
    void get_all_products_returns_empty_list() {
        // Arrange
        when(productService.getAllProducts()).thenReturn(List.of());

        // Act
        List<Product> result = productController.getAllProducts();

        // Assert
        assertAll("lista vacía",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }


    @Test
    @DisplayName("getProductById devuelve 200 OK con el producto cuando existe")
    void get_product_by_id_returns_ok_when_found() {
        // Arrange
        Long id = 1L;
        Product product = buildProduct(id, "Sauce Labs Backpack");
        when(productService.getProductById(id)).thenReturn(Optional.of(product));

        // Act
        ResponseEntity<Product> response = productController.getProductById(id);

        // Assert
        assertAll("respuesta 200 con producto",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(id, response.getBody().getId()),
                () -> assertEquals("Sauce Labs Backpack", response.getBody().getName())
        );

    }

    @Test
    @DisplayName("getProductById devuelve 404 Not Found cuando el producto no existe")
    void get_product_by_id_returns_not_found_when_missing() {
        // Arrange
        Long id = 99L;
        when(productService.getProductById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Product> response = productController.getProductById(id);

        // Assert
        assertAll("respuesta 404 sin body",
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertNull(response.getBody())
        );

        
    }

    private Product buildProduct(Long id, String name) {
        Product product = new Product(
                "000" + id,
                name,
                "description",
                "detail description",
                10.0 * id,
                "http://img/" + id
        );
        product.setId(id);
        return product;
    }
}

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

    @Test
    @DisplayName("getAllProducts verifica la delegación completa a productService.getAllProducts()")
    void get_all_products_verifies_service_call() {
        // Arrange
        when(productService.getAllProducts()).thenReturn(List.of());

        // Act
        productController.getAllProducts();

        // Assert
        verify(productService, times(1)).getAllProducts();
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("getAllProducts retorna un solo producto en la lista correctamente")
    void get_all_products_returns_single_product_list() {
        // Arrange
        Product bikeLight = buildProduct(2L, "Sauce Labs Bike Light");
        when(productService.getAllProducts()).thenReturn(List.of(bikeLight));

        // Act
        List<Product> result = productController.getAllProducts();

        // Assert
        assertAll("lista con un producto",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(2L, result.get(0).getId()),
                () -> assertEquals("Sauce Labs Bike Light", result.get(0).getName())
        );
    }

    @Test
    @DisplayName("getProductById verifica la llamada al servicio con el ID específico")
    void get_product_by_id_verifies_service_call() {
        // Arrange
        Long id = 5L;
        when(productService.getProductById(id)).thenReturn(Optional.empty());

        // Act
        productController.getProductById(id);

        // Assert
        verify(productService, times(1)).getProductById(5L);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("getProductById devuelve 200 OK con todos los atributos del producto")
    void get_product_by_id_returns_complete_product_attributes() {
        // Arrange
        Long id = 3L;
        Product onesie = new Product(
                "0003",
                "Sauce Labs Onesie",
                "Short description",
                "Detailed long description",
                7.99,
                "https://saucedemo.com/onesie.jpg"
        );
        onesie.setId(id);
        when(productService.getProductById(id)).thenReturn(Optional.of(onesie));

        // Act
        ResponseEntity<Product> response = productController.getProductById(id);

        // Assert
        assertAll("todos los campos del producto",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(id, response.getBody().getId()),
                () -> assertEquals("0003", response.getBody().getCode()),
                () -> assertEquals("Sauce Labs Onesie", response.getBody().getName()),
                () -> assertEquals("Short description", response.getBody().getDescription()),
                () -> assertEquals("Detailed long description", response.getBody().getDetailDescription()),
                () -> assertEquals(7.99, response.getBody().getPrice()),
                () -> assertEquals("https://saucedemo.com/onesie.jpg", response.getBody().getImageUrl())
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

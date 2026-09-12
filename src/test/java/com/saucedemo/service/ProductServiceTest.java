package com.saucedemo.service;

import com.saucedemo.model.Product;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks 
    private ProductService productService;

    @Test
    @DisplayName("getProductById devuelve el producto cuando el id existe")
    void search_product_by_id_valid() {
        // Arrange
        Long id = 1L;
        Product expected = new Product(
                "0001",
                "Sauce Labs Backpack",
                "carry.allTheThings() with the sleek, streamlined Sly Pack",
                "Detailed description of the backpack",
                29.99,
                "https://www.saucedemo.com/static/media/sauce-backpack.jpg"
        );
        expected.setId(id);

        when(productRepository.findById(id)).thenReturn(Optional.of(expected));

        // Act
        Optional<Product> result = productService.getProductById(id);

        // Assert
        assertAll("producto encontrado por id",
                () -> assertTrue(result.isPresent(), "El Optional no debe estar vacío"),
                () -> assertEquals(id, result.get().getId()),
                () -> assertEquals("0001", result.get().getCode()),
                () -> assertEquals("Sauce Labs Backpack", result.get().getName()),
                () -> assertEquals(29.99, result.get().getPrice())
        );

    }

    @Test
    @DisplayName("getProductById devuelve Optional vacío cuando el id no existe")
    void search_product_by_id_not_found() {
        // Arrange
        Long id = 999L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(id);

        // Assert
        assertTrue(result.isEmpty(), "El Optional debe estar vacío");

    }

    @Test
    @DisplayName("getAllProducts devuelve la lista completa de productos")
    void get_all_products_returns_list() {
        // Arrange
        Product backpack = new Product(
                "0001", "Sauce Labs Backpack", "desc", "detail", 29.99, "url1");
        Product bikeLight = new Product(
                "0002", "Sauce Labs Bike Light", "desc", "detail", 9.99, "url2");
        List<Product> expected = List.of(backpack, bikeLight);

        when(productRepository.findAll()).thenReturn(expected);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertAll("lista de productos",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("0001", result.get(0).getCode()),
                () -> assertEquals("0002", result.get(1).getCode())
        );
    }

    @Test
    @DisplayName("getAllProducts devuelve lista vacía cuando no hay productos")
    void get_all_products_returns_empty_list() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of());

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertAll("lista vacía",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

    }
}

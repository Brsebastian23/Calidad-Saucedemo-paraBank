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

    @Test
    @DisplayName("getAllProducts verifica la interacción con productRepository.findAll")
    void get_all_products_verifies_repository_interaction() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of());

        // Act
        productService.getAllProducts();

        // Assert
        verify(productRepository, times(1)).findAll();
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("getAllProducts retorna un solo producto con todos sus atributos intactos")
    void get_all_products_returns_single_product_with_all_attributes() {
        // Arrange
        Product onesie = new Product(
                "0003",
                "Sauce Labs Onesie",
                "Rib snap infant onesie for the junior automation engineer",
                "Detailed description of onesie",
                7.99,
                "https://www.saucedemo.com/static/media/red-onesie.jpg"
        );
        onesie.setId(3L);
        when(productRepository.findAll()).thenReturn(List.of(onesie));

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertAll("un producto",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(3L, result.get(0).getId()),
                () -> assertEquals("0003", result.get(0).getCode()),
                () -> assertEquals("Sauce Labs Onesie", result.get(0).getName()),
                () -> assertEquals("Rib snap infant onesie for the junior automation engineer", result.get(0).getDescription()),
                () -> assertEquals("Detailed description of onesie", result.get(0).getDetailDescription()),
                () -> assertEquals(7.99, result.get(0).getPrice()),
                () -> assertEquals("https://www.saucedemo.com/static/media/red-onesie.jpg", result.get(0).getImageUrl())
        );
    }

    @Test
    @DisplayName("getProductById verifica la interacción con productRepository.findById")
    void get_product_by_id_verifies_repository_interaction() {
        // Arrange
        Long productId = 10L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        productService.getProductById(productId);

        // Assert
        verify(productRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("getProductById retorna todos los detalles del producto cuando existe")
    void get_product_by_id_returns_all_details() {
        // Arrange
        Long id = 4L;
        Product fleeceJacket = new Product(
                "0004",
                "Sauce Labs Fleece Jacket",
                "It's not every day that you come across a midweight quarter-zip fleece jacket",
                "Detailed description fleece jacket",
                49.99,
                "https://www.saucedemo.com/static/media/sauce-pullover.jpg"
        );
        fleeceJacket.setId(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(fleeceJacket));

        // Act
        Optional<Product> result = productService.getProductById(id);

        // Assert
        assertAll("detalles de chaqueta",
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(id, result.get().getId()),
                () -> assertEquals("0004", result.get().getCode()),
                () -> assertEquals("Sauce Labs Fleece Jacket", result.get().getName()),
                () -> assertEquals("It's not every day that you come across a midweight quarter-zip fleece jacket", result.get().getDescription()),
                () -> assertEquals("Detailed description fleece jacket", result.get().getDetailDescription()),
                () -> assertEquals(49.99, result.get().getPrice()),
                () -> assertEquals("https://www.saucedemo.com/static/media/sauce-pullover.jpg", result.get().getImageUrl())
        );
    }
}

package com.saucedemo.service;

import com.saucedemo.model.Product;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    private static final double PRICE_DELTA = 0.001;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("devuelve el producto cuando el id existe")
        void search_product_by_id_valid() {
            Long id = 1L;
            Product expected = buildProduct(
                    id,
                    "0001",
                    "Sauce Labs Backpack",
                    "carry.allTheThings() with the sleek, streamlined Sly Pack",
                    "Detailed description of the backpack",
                    29.99,
                    "https://www.saucedemo.com/static/media/sauce-backpack.jpg"
            );
            when(productRepository.findById(id)).thenReturn(Optional.of(expected));

            Optional<Product> result = productService.getProductById(id);

            assertAll("producto encontrado por id",
                    () -> assertTrue(result.isPresent(), "El Optional no debe estar vacío"),
                    () -> assertEquals(id, result.get().getId()),
                    () -> assertEquals("0001", result.get().getCode()),
                    () -> assertEquals("Sauce Labs Backpack", result.get().getName()),
                    () -> assertEquals(29.99, result.get().getPrice(), PRICE_DELTA)
            );
        }

        @Test
        @DisplayName("devuelve Optional vacío cuando el id no existe")
        void search_product_by_id_not_found() {
            Long id = 999L;
            when(productRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Product> result = productService.getProductById(id);

            assertTrue(result.isEmpty(), "El Optional debe estar vacío");
        }

        @Test
        @DisplayName("verifica la interacción con productRepository.findById")
        void verifies_repository_interaction() {
            Long productId = 10L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            productService.getProductById(productId);

            verify(productRepository, times(1)).findById(productId);
            verifyNoMoreInteractions(productRepository);
        }

        @Test
        @DisplayName("retorna todos los detalles del producto cuando existe")
        void returns_all_details() {
            Long id = 4L;
            Product fleeceJacket = buildProduct(
                    id,
                    "0004",
                    "Sauce Labs Fleece Jacket",
                    "It's not every day that you come across a midweight quarter-zip fleece jacket",
                    "Detailed description fleece jacket",
                    49.99,
                    "https://www.saucedemo.com/static/media/sauce-pullover.jpg"
            );
            when(productRepository.findById(id)).thenReturn(Optional.of(fleeceJacket));

            Optional<Product> result = productService.getProductById(id);

            assertAll("detalles de chaqueta",
                    () -> assertTrue(result.isPresent()),
                    () -> assertEquals(id, result.get().getId()),
                    () -> assertEquals("0004", result.get().getCode()),
                    () -> assertEquals("Sauce Labs Fleece Jacket", result.get().getName()),
                    () -> assertEquals("It's not every day that you come across a midweight quarter-zip fleece jacket", result.get().getDescription()),
                    () -> assertEquals("Detailed description fleece jacket", result.get().getDetailDescription()),
                    () -> assertEquals(49.99, result.get().getPrice(), PRICE_DELTA),
                    () -> assertEquals("https://www.saucedemo.com/static/media/sauce-pullover.jpg", result.get().getImageUrl())
            );
        }
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("devuelve la lista completa de productos")
        void returns_list() {
            Product backpack = buildProduct(1L, "0001", "Sauce Labs Backpack", "desc", "detail", 29.99, "url1");
            Product bikeLight = buildProduct(2L, "0002", "Sauce Labs Bike Light", "desc", "detail", 9.99, "url2");
            when(productRepository.findAll()).thenReturn(List.of(backpack, bikeLight));

            List<Product> result = productService.getAllProducts();

            assertAll("lista de productos",
                    () -> assertNotNull(result),
                    () -> assertEquals(2, result.size()),
                    () -> assertEquals("0001", result.get(0).getCode()),
                    () -> assertEquals("0002", result.get(1).getCode())
            );
        }

        @Test
        @DisplayName("devuelve lista vacía cuando no hay productos")
        void returns_empty_list() {
            when(productRepository.findAll()).thenReturn(List.of());

            List<Product> result = productService.getAllProducts();

            assertAll("lista vacía",
                    () -> assertNotNull(result),
                    () -> assertTrue(result.isEmpty())
            );
        }

        @Test
        @DisplayName("verifica la interacción con productRepository.findAll")
        void verifies_repository_interaction() {
            when(productRepository.findAll()).thenReturn(List.of());

            productService.getAllProducts();

            verify(productRepository, times(1)).findAll();
            verifyNoMoreInteractions(productRepository);
        }

        @Test
        @DisplayName("retorna un solo producto con todos sus atributos intactos")
        void returns_single_product_with_all_attributes() {
            Product onesie = buildProduct(
                    3L,
                    "0003",
                    "Sauce Labs Onesie",
                    "Rib snap infant onesie for the junior automation engineer",
                    "Detailed description of onesie",
                    7.99,
                    "https://www.saucedemo.com/static/media/red-onesie.jpg"
            );
            when(productRepository.findAll()).thenReturn(List.of(onesie));

            List<Product> result = productService.getAllProducts();

            assertAll("un producto",
                    () -> assertEquals(1, result.size()),
                    () -> assertEquals(3L, result.get(0).getId()),
                    () -> assertEquals("0003", result.get(0).getCode()),
                    () -> assertEquals("Sauce Labs Onesie", result.get(0).getName()),
                    () -> assertEquals("Rib snap infant onesie for the junior automation engineer", result.get(0).getDescription()),
                    () -> assertEquals("Detailed description of onesie", result.get(0).getDetailDescription()),
                    () -> assertEquals(7.99, result.get(0).getPrice(), PRICE_DELTA),
                    () -> assertEquals("https://www.saucedemo.com/static/media/red-onesie.jpg", result.get(0).getImageUrl())
            );
        }
    }

    private Product buildProduct(
            Long id,
            String code,
            String name,
            String description,
            String detailDescription,
            double price,
            String imageUrl
    ) {
        Product product = new Product(code, name, description, detailDescription, price, imageUrl);
        product.setId(id);
        return product;
    }
}

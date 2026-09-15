package com.saucedemo.config;

import com.saucedemo.model.Product;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    private static final int CATALOG_SIZE = 12;
    private static final double PRICE_DELTA = 0.001;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    @DisplayName("inserta los 12 productos del catálogo cuando la BD está vacía")
    void inserts_all_catalog_products_when_database_is_empty() {
        when(productRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataSeeder.run();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(CATALOG_SIZE)).save(captor.capture());

        List<Product> saved = captor.getAllValues();
        assertEquals(CATALOG_SIZE, saved.size());
        assertTrue(saved.stream().allMatch(product -> product.getId() == null));
        assertTrue(saved.stream().map(Product::getCode).anyMatch("mochila"::equals));
        assertTrue(saved.stream().map(Product::getCode).anyMatch("bolsa"::equals));
    }

    @Test
    @DisplayName("actualiza la fila existente cuando el producto ya existe por code")
    void updates_existing_product_found_by_code() {
        Product existing = new Product("mochila", "Nombre viejo", "desc vieja", "detalle viejo", 1.0, "old.png");
        existing.setId(10L);

        when(productRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByCode("mochila")).thenReturn(Optional.of(existing));
        when(productRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataSeeder.run();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(CATALOG_SIZE)).save(captor.capture());

        Product updatedMochila = captor.getAllValues().stream()
                .filter(product -> "mochila".equals(product.getCode()))
                .findFirst()
                .orElseThrow();

        assertAll("producto existente actualizado",
                () -> assertSame(existing, updatedMochila),
                () -> assertEquals(10L, updatedMochila.getId()),
                () -> assertEquals("Mochila Urban Trek", updatedMochila.getName()),
                () -> assertEquals("Mochila resistente para el dia a dia.", updatedMochila.getDescription()),
                () -> assertEquals(119900.0, updatedMochila.getPrice(), PRICE_DELTA),
                () -> assertEquals("Images/mochila.png", updatedMochila.getImageUrl())
        );
        verify(productRepository, never()).findByName("mochila");
    }

    @Test
    @DisplayName("adopta la fila antigua por name cuando no hay code")
    void adopts_legacy_row_found_by_name() {
        Product legacy = new Product();
        legacy.setId(20L);
        legacy.setName("mochila");

        when(productRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByName("mochila")).thenReturn(Optional.of(legacy));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataSeeder.run();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(CATALOG_SIZE)).save(captor.capture());

        Product adopted = captor.getAllValues().stream()
                .filter(product -> Long.valueOf(20L).equals(product.getId()))
                .findFirst()
                .orElseThrow();

        assertAll("fila antigua adoptada",
                () -> assertSame(legacy, adopted),
                () -> assertEquals("mochila", adopted.getCode()),
                () -> assertEquals("Mochila Urban Trek", adopted.getName()),
                () -> assertEquals(119900.0, adopted.getPrice(), PRICE_DELTA)
        );
    }

    @Test
    @DisplayName("findByCode tiene prioridad: no consulta findByName si el code ya resolvió")
    void find_by_code_has_priority_over_find_by_name() {
        when(productRepository.findByCode(anyString())).thenAnswer(invocation -> {
            Product existing = new Product();
            existing.setId(1L);
            existing.setCode(invocation.getArgument(0));
            return Optional.of(existing);
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataSeeder.run();

        verify(productRepository, times(CATALOG_SIZE)).findByCode(anyString());
        verify(productRepository, never()).findByName(anyString());
        verify(productRepository, times(CATALOG_SIZE)).save(any(Product.class));
    }
}

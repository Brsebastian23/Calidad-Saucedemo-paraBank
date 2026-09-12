package com.saucedemo.controller;

import com.saucedemo.dto.AddToCartRequest;
import com.saucedemo.dto.UpdateCartItemRequest;
import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.service.interfaces.ICartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private static final String SESSION_ID = "session-123";

    @Mock
    private ICartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    @DisplayName("getCart devuelve la lista de items de la sesión")
    void get_cart_returns_items() {
        // Arrange
        CartItem item = new CartItem(SESSION_ID, buildProduct(1L), 2);
        when(cartService.getCart(SESSION_ID)).thenReturn(List.of(item));

        // Act
        List<CartItem> result = cartController.getCart(SESSION_ID);

        // Assert
        assertAll("items del carrito",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(SESSION_ID, result.get(0).getSessionId())
        );

    }


    @Test
    @DisplayName("addToCart devuelve 200 OK con el item creado")
    void add_to_cart_returns_ok() {
        // Arrange
        AddToCartRequest request = new AddToCartRequest();
        request.setSessionId(SESSION_ID);
        request.setProductId(1L);
        request.setQuantity(3);

        CartItem created = new CartItem(SESSION_ID, buildProduct(1L), 3);
        when(cartService.addToCart(SESSION_ID, 1L, 3)).thenReturn(created);

        // Act
        ResponseEntity<CartItem> response = cartController.addToCart(request);

        // Assert
        assertAll("respuesta 200 con item",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(SESSION_ID, response.getBody().getSessionId()),
                () -> assertEquals(3, response.getBody().getQuantity())
        );

    }

    @Test
    @DisplayName("addToCart devuelve 404 cuando el producto no existe")
    void add_to_cart_returns_not_found_when_product_missing() {
        // Arrange
        AddToCartRequest request = new AddToCartRequest();
        request.setSessionId(SESSION_ID);
        request.setProductId(99L);
        request.setQuantity(1);

        when(cartService.addToCart(SESSION_ID, 99L, 1))
                .thenThrow(new NoSuchElementException("Producto no encontrado: 99"));

        // Act
        ResponseEntity<CartItem> response = cartController.addToCart(request);

        // Assert
        assertAll("respuesta 404",
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertNull(response.getBody())
        );

    }

    @Test
    @DisplayName("updateQuantity devuelve 200 OK con el item actualizado")
    void update_quantity_returns_ok() {
        // Arrange
        Long itemId = 10L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        CartItem updated = new CartItem(SESSION_ID, buildProduct(1L), 5);
        updated.setId(itemId);
        when(cartService.updateQuantity(itemId, 5)).thenReturn(updated);

        // Act
        ResponseEntity<CartItem> response = cartController.updateQuantity(itemId, request);

        // Assert
        assertAll("respuesta 200 con cantidad actualizada",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(5, response.getBody().getQuantity())
        );

    }

    @Test
    @DisplayName("updateQuantity devuelve 404 cuando el item no existe")
    void update_quantity_returns_not_found_when_item_missing() {
        // Arrange
        Long itemId = 99L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(4);

        when(cartService.updateQuantity(itemId, 4))
                .thenThrow(new NoSuchElementException("Item de carrito no encontrado: 99"));

        // Act
        ResponseEntity<CartItem> response = cartController.updateQuantity(itemId, request);

        // Assert
        assertAll("respuesta 404",
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertNull(response.getBody())
        );
    }


    @Test
    @DisplayName("removeItem devuelve 204 No Content")
    void remove_item_returns_no_content() {
        // Arrange
        Long itemId = 7L;

        // Act
        ResponseEntity<Void> response = cartController.removeItem(itemId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }


    private Product buildProduct(Long id) {
        Product product = new Product(
                "000" + id,
                "Product " + id,
                "description",
                "detail description",
                10.0 * id,
                "http://img/" + id
        );
        product.setId(id);
        return product;
    }
}

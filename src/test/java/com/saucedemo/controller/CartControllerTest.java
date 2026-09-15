package com.saucedemo.controller;

import com.saucedemo.dto.AddToCartRequest;
import com.saucedemo.dto.UpdateCartItemRequest;
import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.service.CartService;
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
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    @DisplayName("getCart devuelve la lista de items de la sesión")
    void get_cart_returns_items() {
        
        CartItem item = new CartItem(SESSION_ID, buildProduct(1L), 2);
        when(cartService.getCart(SESSION_ID)).thenReturn(List.of(item));

        
        List<CartItem> result = cartController.getCart(SESSION_ID);

        
        assertAll("items del carrito",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(SESSION_ID, result.get(0).getSessionId())
        );

    }


    @Test
    @DisplayName("addToCart devuelve 200 OK con el item creado")
    void add_to_cart_returns_ok() {
        
        AddToCartRequest request = new AddToCartRequest();
        request.setSessionId(SESSION_ID);
        request.setProductId(1L);
        request.setQuantity(3);

        CartItem created = new CartItem(SESSION_ID, buildProduct(1L), 3);
        when(cartService.addToCart(SESSION_ID, 1L, 3)).thenReturn(created);

        
        ResponseEntity<CartItem> response = cartController.addToCart(request);

        
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
        
        AddToCartRequest request = new AddToCartRequest();
        request.setSessionId(SESSION_ID);
        request.setProductId(99L);
        request.setQuantity(1);

        when(cartService.addToCart(SESSION_ID, 99L, 1))
                .thenThrow(new NoSuchElementException("Producto no encontrado: 99"));

        
        ResponseEntity<CartItem> response = cartController.addToCart(request);

        
        assertAll("respuesta 404",
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertNull(response.getBody())
        );

    }

    @Test
    @DisplayName("updateQuantity devuelve 200 OK con el item actualizado")
    void update_quantity_returns_ok() {
        
        Long itemId = 10L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        CartItem updated = new CartItem(SESSION_ID, buildProduct(1L), 5);
        updated.setId(itemId);
        when(cartService.updateQuantity(itemId, 5)).thenReturn(updated);

        
        ResponseEntity<CartItem> response = cartController.updateQuantity(itemId, request);

        
        assertAll("respuesta 200 con cantidad actualizada",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(5, response.getBody().getQuantity())
        );

    }

    @Test
    @DisplayName("updateQuantity devuelve 404 cuando el item no existe")
    void update_quantity_returns_not_found_when_item_missing() {
        
        Long itemId = 99L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(4);

        when(cartService.updateQuantity(itemId, 4))
                .thenThrow(new NoSuchElementException("Item de carrito no encontrado: 99"));

        
        ResponseEntity<CartItem> response = cartController.updateQuantity(itemId, request);

        
        assertAll("respuesta 404",
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertNull(response.getBody())
        );
    }


    @Test
    @DisplayName("removeItem devuelve 204 No Content")
    void remove_item_returns_no_content() {
        
        Long itemId = 7L;

        
        ResponseEntity<Void> response = cartController.removeItem(itemId);

        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("getCart devuelve lista vacía cuando el servicio no tiene items para la sesión")
    void get_cart_returns_empty_list_when_service_returns_empty() {
        
        String emptySession = "empty-session";
        when(cartService.getCart(emptySession)).thenReturn(List.of());

        
        List<CartItem> result = cartController.getCart(emptySession);

        
        assertAll("carrito vacío",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
        verify(cartService, times(1)).getCart(emptySession);
    }

    @Test
    @DisplayName("getCart devuelve múltiples items y verifica la delegación completa al servicio")
    void get_cart_returns_multiple_items_and_verifies_service_call() {
        
        CartItem item1 = new CartItem(SESSION_ID, buildProduct(1L), 2);
        CartItem item2 = new CartItem(SESSION_ID, buildProduct(2L), 5);
        when(cartService.getCart(SESSION_ID)).thenReturn(List.of(item1, item2));

        
        List<CartItem> result = cartController.getCart(SESSION_ID);

        
        assertAll("múltiples items",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(2, result.get(0).getQuantity()),
                () -> assertEquals(5, result.get(1).getQuantity())
        );
        verify(cartService, times(1)).getCart(SESSION_ID);
    }

    @Test
    @DisplayName("addToCart pasa los parámetros exactos del request al servicio")
    void add_to_cart_verifies_exact_service_arguments() {
        
        AddToCartRequest request = new AddToCartRequest();
        request.setSessionId("custom-session-xyz");
        request.setProductId(42L);
        request.setQuantity(10);

        CartItem created = new CartItem("custom-session-xyz", buildProduct(42L), 10);
        when(cartService.addToCart("custom-session-xyz", 42L, 10)).thenReturn(created);

        
        ResponseEntity<CartItem> response = cartController.addToCart(request);

        
        assertAll("respuesta de addToCart",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("custom-session-xyz", response.getBody().getSessionId()),
                () -> assertEquals(10, response.getBody().getQuantity())
        );
        verify(cartService, times(1)).addToCart("custom-session-xyz", 42L, 10);
    }

    @Test
    @DisplayName("updateQuantity pasa los parámetros exactos de itemId y quantity al servicio")
    void update_quantity_verifies_exact_service_arguments() {
        
        Long itemId = 55L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(8);

        CartItem updated = new CartItem(SESSION_ID, buildProduct(1L), 8);
        updated.setId(itemId);
        when(cartService.updateQuantity(itemId, 8)).thenReturn(updated);

        
        ResponseEntity<CartItem> response = cartController.updateQuantity(itemId, request);

        
        assertAll("respuesta de updateQuantity",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(itemId, response.getBody().getId()),
                () -> assertEquals(8, response.getBody().getQuantity())
        );
        verify(cartService, times(1)).updateQuantity(itemId, 8);
    }

    @Test
    @DisplayName("removeItem delega correctamente la eliminación al servicio con el id especificado")
    void remove_item_verifies_service_invocation() {
        
        Long itemId = 99L;

        
        ResponseEntity<Void> response = cartController.removeItem(itemId);

        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService, times(1)).removeItem(itemId);
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

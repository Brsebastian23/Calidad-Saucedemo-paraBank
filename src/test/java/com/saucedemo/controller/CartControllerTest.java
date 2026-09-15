package com.saucedemo.controller;

import com.saucedemo.dto.AddToCartRequest;
import com.saucedemo.dto.UpdateCartItemRequest;
import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.service.interfaces.ICartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("devuelve la lista de items de la sesión")
        void returns_items() {
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
        @DisplayName("devuelve lista vacía cuando el servicio no tiene items")
        void returns_empty_list_when_service_returns_empty() {
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
        @DisplayName("devuelve múltiples items y delega al servicio")
        void returns_multiple_items_and_verifies_service_call() {
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
    }

    @Nested
    @DisplayName("addToCart")
    class AddToCart {

        @Test
        @DisplayName("devuelve 200 OK con el item creado")
        void returns_ok() {
            AddToCartRequest request = buildAddRequest(SESSION_ID, 1L, 3);
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
        @DisplayName("devuelve 404 cuando el producto no existe")
        void returns_not_found_when_product_missing() {
            AddToCartRequest request = buildAddRequest(SESSION_ID, 99L, 1);
            when(cartService.addToCart(SESSION_ID, 99L, 1))
                    .thenThrow(new NoSuchElementException("Producto no encontrado: 99"));

            ResponseEntity<CartItem> response = cartController.addToCart(request);

            assertAll("respuesta 404",
                    () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                    () -> assertNull(response.getBody())
            );
        }

        @Test
        @DisplayName("pasa los parámetros exactos del request al servicio")
        void verifies_exact_service_arguments() {
            AddToCartRequest request = buildAddRequest("custom-session-xyz", 42L, 10);
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
        @DisplayName("reenvía campos null del request al servicio (sin validar el body)")
        void forwards_null_request_fields_to_service() {
            AddToCartRequest request = new AddToCartRequest();
            when(cartService.addToCart(null, null, null))
                    .thenReturn(new CartItem(null, null, null));

            ResponseEntity<CartItem> response = cartController.addToCart(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(cartService, times(1)).addToCart(null, null, null);
        }

        @Test
        @DisplayName("lanza NPE y no llama al servicio si el request es null")
        void throws_when_request_is_null() {
            assertThrows(NullPointerException.class, () -> cartController.addToCart(null));
            verifyNoInteractions(cartService);
        }
    }

    @Nested
    @DisplayName("updateQuantity")
    class UpdateQuantity {

        @Test
        @DisplayName("devuelve 200 OK con el item actualizado")
        void returns_ok() {
            Long itemId = 10L;
            UpdateCartItemRequest request = buildUpdateRequest(5);
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
        @DisplayName("devuelve 404 cuando el item no existe")
        void returns_not_found_when_item_missing() {
            Long itemId = 99L;
            UpdateCartItemRequest request = buildUpdateRequest(4);
            when(cartService.updateQuantity(itemId, 4))
                    .thenThrow(new NoSuchElementException("Item de carrito no encontrado: 99"));

            ResponseEntity<CartItem> response = cartController.updateQuantity(itemId, request);

            assertAll("respuesta 404",
                    () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                    () -> assertNull(response.getBody())
            );
        }

        @Test
        @DisplayName("pasa itemId y quantity exactos al servicio")
        void verifies_exact_service_arguments() {
            Long itemId = 55L;
            UpdateCartItemRequest request = buildUpdateRequest(8);
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
    }

    @Nested
    @DisplayName("removeItem")
    class RemoveItem {

        @Test
        @DisplayName("devuelve 204 No Content y delega al servicio")
        void returns_no_content_and_delegates() {
            Long itemId = 99L;

            ResponseEntity<Void> response = cartController.removeItem(itemId);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(cartService, times(1)).removeItem(itemId);
        }

        @Test
        @DisplayName("propaga la excepción del servicio; DELETE no traduce a 404")
        void propagates_service_exception() {
            Long itemId = 7L;
            doThrow(new NoSuchElementException("Item de carrito no encontrado: 7"))
                    .when(cartService).removeItem(itemId);

            assertThrows(NoSuchElementException.class, () -> cartController.removeItem(itemId));
        }
    }

    private AddToCartRequest buildAddRequest(String sessionId, Long productId, Integer quantity) {
        AddToCartRequest request = new AddToCartRequest();
        request.setSessionId(sessionId);
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private UpdateCartItemRequest buildUpdateRequest(Integer quantity) {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(quantity);
        return request;
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

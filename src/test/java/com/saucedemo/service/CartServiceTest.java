package com.saucedemo.service;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.repository.CartItemRepository;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String SESSION_ID = "session-123";

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("devuelve los items asociados a la sesión")
        void returns_items_for_session() {
            CartItem item1 = new CartItem(SESSION_ID, buildProduct(1L), 2);
            CartItem item2 = new CartItem(SESSION_ID, buildProduct(2L), 1);
            when(cartItemRepository.findBySessionId(SESSION_ID))
                    .thenReturn(List.of(item1, item2));

            List<CartItem> result = cartService.getCart(SESSION_ID);

            assertAll("carrito de la sesión",
                    () -> assertNotNull(result),
                    () -> assertEquals(2, result.size()),
                    () -> assertEquals(SESSION_ID, result.get(0).getSessionId())
            );
        }

        @Test
        @DisplayName("devuelve lista vacía cuando la sesión no tiene items")
        void returns_empty_list_when_no_items() {
            when(cartItemRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());

            List<CartItem> result = cartService.getCart(SESSION_ID);

            assertAll("carrito vacío",
                    () -> assertNotNull(result),
                    () -> assertTrue(result.isEmpty())
            );
        }

        @Test
        @DisplayName("consulta el repositorio una sola vez con el sessionId provisto")
        void verifies_repository_interaction() {
            String specificSession = "session-custom-abc";
            when(cartItemRepository.findBySessionId(specificSession)).thenReturn(List.of());

            List<CartItem> result = cartService.getCart(specificSession);

            assertNotNull(result);
            verify(cartItemRepository, times(1)).findBySessionId(specificSession);
            verifyNoMoreInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("retorna un único item con todos sus detalles")
        void returns_single_item_with_complete_details() {
            Product product = buildProduct(100L);
            CartItem singleItem = new CartItem(SESSION_ID, product, 4);
            singleItem.setId(50L);
            when(cartItemRepository.findBySessionId(SESSION_ID)).thenReturn(List.of(singleItem));

            List<CartItem> result = cartService.getCart(SESSION_ID);

            assertAll("un solo item en el carrito",
                    () -> assertEquals(1, result.size()),
                    () -> assertEquals(50L, result.get(0).getId()),
                    () -> assertEquals(SESSION_ID, result.get(0).getSessionId()),
                    () -> assertEquals(product, result.get(0).getProduct()),
                    () -> assertEquals(4, result.get(0).getQuantity())
            );
            verify(cartItemRepository, times(1)).findBySessionId(SESSION_ID);
        }
    }

    @Nested
    @DisplayName("addToCart")
    class AddToCart {

        @Test
        @DisplayName("crea un nuevo item cuando el producto no está en el carrito")
        void creates_new_item() {
            Long productId = 1L;
            Integer quantity = 3;
            Product product = buildProduct(productId);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CartItem result = cartService.addToCart(SESSION_ID, productId, quantity);

            assertAll("nuevo item creado",
                    () -> assertNotNull(result),
                    () -> assertEquals(SESSION_ID, result.getSessionId()),
                    () -> assertEquals(product, result.getProduct()),
                    () -> assertEquals(quantity, result.getQuantity())
            );
        }

        @Test
        @DisplayName("acumula la cantidad cuando el producto ya está en el carrito")
        void updates_existing_item() {
            Long productId = 1L;
            Product product = buildProduct(productId);
            CartItem existing = new CartItem(SESSION_ID, product, 2);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.of(existing));
            when(cartItemRepository.save(existing)).thenReturn(existing);

            CartItem result = cartService.addToCart(SESSION_ID, productId, 3);

            assertAll("item existente actualizado",
                    () -> assertNotNull(result),
                    () -> assertEquals(5, result.getQuantity())
            );
        }

        @Test
        @DisplayName("no llama a save cuando el producto no existe")
        void never_saves_when_product_not_found() {
            Long invalidProductId = 999L;
            when(productRepository.findById(invalidProductId)).thenReturn(Optional.empty());

            NoSuchElementException ex = assertThrows(
                    NoSuchElementException.class,
                    () -> cartService.addToCart(SESSION_ID, invalidProductId, 2)
            );

            assertEquals("Producto no encontrado: 999", ex.getMessage());
            verify(productRepository, times(1)).findById(invalidProductId);
            verify(cartItemRepository, never()).findBySessionIdAndProductId(anyString(), anyLong());
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("guarda un nuevo item y verifica las interacciones con los repositorios")
        void creates_new_item_and_verifies_all_interactions() {
            Long productId = 5L;
            Integer quantity = 2;
            Product product = buildProduct(productId);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
                CartItem item = invocation.getArgument(0);
                item.setId(123L);
                return item;
            });

            CartItem result = cartService.addToCart(SESSION_ID, productId, quantity);

            assertAll("nuevo item con id generado",
                    () -> assertNotNull(result),
                    () -> assertEquals(123L, result.getId()),
                    () -> assertEquals(SESSION_ID, result.getSessionId()),
                    () -> assertEquals(product, result.getProduct()),
                    () -> assertEquals(quantity, result.getQuantity())
            );

            verify(productRepository, times(1)).findById(productId);
            verify(cartItemRepository, times(1)).findBySessionIdAndProductId(SESSION_ID, productId);
            verify(cartItemRepository, times(1)).save(any(CartItem.class));
        }

        @Test
        @DisplayName("suma cantidades grandes en un item existente")
        void accumulates_large_quantity_on_existing_item() {
            Long productId = 2L;
            Product product = buildProduct(productId);
            CartItem existingItem = new CartItem(SESSION_ID, product, 10);
            existingItem.setId(88L);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.of(existingItem));
            when(cartItemRepository.save(existingItem)).thenReturn(existingItem);

            CartItem result = cartService.addToCart(SESSION_ID, productId, 15);

            assertAll("cantidad acumulada",
                    () -> assertNotNull(result),
                    () -> assertEquals(88L, result.getId()),
                    () -> assertEquals(25, result.getQuantity())
            );
            verify(cartItemRepository, times(1)).save(existingItem);
        }

        @Test
        @DisplayName("lanza NullPointerException si la cantidad es null sobre un item existente")
        void throws_when_quantity_is_null_on_existing_item() {
            Long productId = 1L;
            Product product = buildProduct(productId);
            CartItem existing = new CartItem(SESSION_ID, product, 2);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.of(existing));

            assertThrows(
                    NullPointerException.class,
                    () -> cartService.addToCart(SESSION_ID, productId, null)
            );
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("persiste cantidad negativa al crear un item nuevo")
        void persists_negative_quantity_on_new_item() {
            Long productId = 1L;
            Product product = buildProduct(productId);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CartItem result = cartService.addToCart(SESSION_ID, productId, -3);

            assertEquals(-3, result.getQuantity());
            verify(cartItemRepository, times(1)).save(any(CartItem.class));
        }

        @Test
        @DisplayName("resta la cantidad negativa sobre un item existente")
        void subtracts_negative_quantity_on_existing_item() {
            Long productId = 1L;
            Product product = buildProduct(productId);
            CartItem existing = new CartItem(SESSION_ID, product, 5);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.of(existing));
            when(cartItemRepository.save(existing)).thenReturn(existing);

            CartItem result = cartService.addToCart(SESSION_ID, productId, -2);

            assertEquals(3, result.getQuantity());
        }
    }

    @Nested
    @DisplayName("updateQuantity")
    class UpdateQuantity {

        @Test
        @DisplayName("actualiza la cantidad del item existente")
        void updates_existing_item() {
            Long itemId = 10L;
            CartItem item = new CartItem(SESSION_ID, buildProduct(1L), 1);
            item.setId(itemId);

            when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(cartItemRepository.save(item)).thenReturn(item);

            CartItem result = cartService.updateQuantity(itemId, 5);

            assertAll("cantidad actualizada",
                    () -> assertNotNull(result),
                    () -> assertEquals(5, result.getQuantity())
            );
        }

        @Test
        @DisplayName("no llama a save cuando el item no existe")
        void never_saves_when_item_not_found() {
            Long invalidItemId = 888L;
            when(cartItemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

            NoSuchElementException ex = assertThrows(
                    NoSuchElementException.class,
                    () -> cartService.updateQuantity(invalidItemId, 10)
            );

            assertEquals("Item de carrito no encontrado: 888", ex.getMessage());
            verify(cartItemRepository, times(1)).findById(invalidItemId);
            verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("actualiza la cantidad a cero")
        void sets_zero_quantity() {
            Long itemId = 15L;
            CartItem item = new CartItem(SESSION_ID, buildProduct(1L), 3);
            item.setId(itemId);

            when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(cartItemRepository.save(item)).thenReturn(item);

            CartItem result = cartService.updateQuantity(itemId, 0);

            assertAll("cantidad actualizada a cero",
                    () -> assertNotNull(result),
                    () -> assertEquals(itemId, result.getId()),
                    () -> assertEquals(0, result.getQuantity())
            );
            verify(cartItemRepository, times(1)).findById(itemId);
            verify(cartItemRepository, times(1)).save(item);
        }
    }

    @Nested
    @DisplayName("removeItem")
    class RemoveItem {

        @Test
        @DisplayName("llama a deleteById con el itemId correcto")
        void calls_delete_by_id_with_correct_id() {
            Long itemId = 42L;

            cartService.removeItem(itemId);

            verify(cartItemRepository, times(1)).deleteById(42L);
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("no consulta existsById; solo deleta aunque el id no exista")
        void deletes_by_id_without_existence_check() {
            Long missingId = 777L;

            cartService.removeItem(missingId);

            verify(cartItemRepository, times(1)).deleteById(missingId);
            verify(cartItemRepository, never()).existsById(anyLong());
            verify(cartItemRepository, never()).findById(anyLong());
        }
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

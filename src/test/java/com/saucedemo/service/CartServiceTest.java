package com.saucedemo.service;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.repository.CartItemRepository;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
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


    @Test
    @DisplayName("getCart devuelve los items asociados a la sesión")
    void get_cart_returns_items_for_session() {
        // Arrange
        CartItem item1 = new CartItem(SESSION_ID, buildProduct(1L), 2);
        CartItem item2 = new CartItem(SESSION_ID, buildProduct(2L), 1);
        when(cartItemRepository.findBySessionId(SESSION_ID))
                .thenReturn(List.of(item1, item2));

        // Act
        List<CartItem> result = cartService.getCart(SESSION_ID);

        // Assert
        assertAll("carrito de la sesión",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(SESSION_ID, result.get(0).getSessionId())
        );

    }

    @Test
    @DisplayName("getCart devuelve lista vacía cuando la sesión no tiene items")
    void get_cart_returns_empty_list_when_no_items() {
        // Arrange
        when(cartItemRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());

        // Act
        List<CartItem> result = cartService.getCart(SESSION_ID);

        // Assert
        assertAll("carrito vacío",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }


    @Test
    @DisplayName("addToCart crea un nuevo item cuando el producto no está en el carrito")
    void add_to_cart_creates_new_item() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 3;
        Product product = buildProduct(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CartItem result = cartService.addToCart(SESSION_ID, productId, quantity);

        // Assert
        assertAll("nuevo item creado",
                () -> assertNotNull(result),
                () -> assertEquals(SESSION_ID, result.getSessionId()),
                () -> assertEquals(product, result.getProduct()),
                () -> assertEquals(quantity, result.getQuantity())
        );

    }

    @Test
    @DisplayName("addToCart acumula la cantidad cuando el producto ya está en el carrito")
    void add_to_cart_updates_existing_item() {
        // Arrange
        Long productId = 1L;
        Integer existingQuantity = 2;
        Integer addedQuantity = 3;
        Product product = buildProduct(productId);
        CartItem existing = new CartItem(SESSION_ID, product, existingQuantity);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findBySessionIdAndProductId(SESSION_ID, productId)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        // Act
        CartItem result = cartService.addToCart(SESSION_ID, productId, addedQuantity);

        // Assert
        assertAll("item existente actualizado",
                () -> assertNotNull(result),
                () -> assertEquals(existingQuantity + addedQuantity, result.getQuantity())
        );

    }

    @Test
    @DisplayName("addToCart lanza NoSuchElementException cuando el producto no existe")
    void add_to_cart_throws_when_product_not_found() {
        // Arrange
        Long productId = 99L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act + Assert
        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> cartService.addToCart(SESSION_ID, productId, 1)
        );

        assertTrue(ex.getMessage().contains(String.valueOf(productId)));

    }


    @Test
    @DisplayName("updateQuantity actualiza la cantidad del item existente")
    void update_quantity_updates_existing_item() {
        // Arrange
        Long itemId = 10L;
        Integer newQuantity = 5;
        CartItem item = new CartItem(SESSION_ID, buildProduct(1L), 1);
        item.setId(itemId);

        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(item)).thenReturn(item);

        // Act
        CartItem result = cartService.updateQuantity(itemId, newQuantity);

        // Assert
        assertAll("cantidad actualizada",
                () -> assertNotNull(result),
                () -> assertEquals(newQuantity, result.getQuantity())
        );
    }

    @Test
    @DisplayName("updateQuantity lanza NoSuchElementException cuando el item no existe")
    void update_quantity_throws_when_item_not_found() {
        // Arrange
        Long itemId = 99L;
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act + Assert
        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> cartService.updateQuantity(itemId, 4)
        );

        assertTrue(ex.getMessage().contains(String.valueOf(itemId)));
    }


    @Test
    @DisplayName("removeItem delega el borrado en el repositorio")
    void remove_item_delegates_to_repository() {
        // Arrange
        Long itemId = 7L;

        // Act
        cartService.removeItem(itemId);

        // Assert
        verify(cartItemRepository, times(1)).deleteById(itemId);
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

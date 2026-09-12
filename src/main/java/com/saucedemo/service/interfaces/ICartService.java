package com.saucedemo.service.interfaces;

import java.util.List;

import com.saucedemo.model.CartItem;

public interface ICartService {

    List<CartItem> getCart(String sessionId);
    CartItem addToCart(String sessionId, Long productId, Integer quantity);
    CartItem updateQuantity(Long itemId, Integer quantity);
    void removeItem(Long itemId);

}

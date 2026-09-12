package com.saucedemo.repository;

import com.saucedemo.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findBySessionId(String sessionId);

    Optional<CartItem> findBySessionIdAndProductId(String sessionId, Long productId);
}

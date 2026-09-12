package com.saucedemo.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.saucedemo.model.Product;

public interface IProductService {

    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);

}

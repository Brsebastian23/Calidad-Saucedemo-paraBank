package com.saucedemo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    private Product product;


    @Test
    void productCreationWithValidDataIsSuccessful(){
        //Arrange

        String code = "0001";
        String name = "Test product";
        String description = "Test description";
        String detailDescription = "Test detail description";
        Double price = 100.8;
        String imageUrl = "wwww.test/url";

        //Act
        product = new Product(code, name, description, detailDescription, price, imageUrl);

        //Assert
        assertEquals("0001", product.getCode());


    }

}

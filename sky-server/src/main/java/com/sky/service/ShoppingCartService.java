package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCart> showShoppingCart();

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}

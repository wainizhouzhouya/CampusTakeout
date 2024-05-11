package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/13 18:24
 *Description:
 */
public interface ShoppingCartService {
    /**
     * 添加菜品/套餐到购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     */
    List<ShoppingCart> list();

    /**
     * 清空购物车
     */
    void deleteAll();


    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}

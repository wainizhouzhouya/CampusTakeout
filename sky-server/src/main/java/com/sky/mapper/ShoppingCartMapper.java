package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/13 18:25
 *Description:
 */
@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车是否含有某样菜品/套餐
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新菜品数量(添加)
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart cart);

    /**
     * 加入 菜品/套餐 到购物车表
     */
    @Insert("insert into shopping_cart values (null,#{name},#{image},#{userId},#{dishId}" +
            ",#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteAll(ShoppingCart shoppingCart);


    /**
     * 删除购物车的一个商品
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量加入 菜品/套餐 到购物车表
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}

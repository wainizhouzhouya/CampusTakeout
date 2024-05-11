package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/13 18:24
 *Description:
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealMapper setMealMapper;


    /**
     * 添加菜品/套餐到购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //1 判断购物车表是否有该菜品/套餐
        //1.1 创建ShoppingCart对象并封装
        ShoppingCart shoppingCart = new ShoppingCart();
        //1.2 将shoppingCartDTO数据拷贝到shoppingCart
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //1.3 设置userId值
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //1.4 进行数据库表查询
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //2. 如果存在，则在原菜品/套餐的 number+1
        if (list != null && list.size() > 0) {
            //2.1 获取 菜品/套餐
            ShoppingCart cart = list.get(0);
            //2.2 设置 菜品/套餐 数量+1
            cart.setNumber(cart.getNumber() + 1);
            //2.3 更新数据库
            shoppingCartMapper.update(cart);
        } else {  //3. 如果不存在，则将 菜品/套餐 加入购物车表中
            //3.1 获取需要添加入 购物车表中的是 菜品/套餐？
            Long dishId = shoppingCartDTO.getDishId();
            //3.2 判断dishId是否为空
            if (dishId != null) {//3.2.1 如果dishId不为空，添加的则是菜品
                Dish dish = dishMapper.getById(dishId);
                //给shoppingCart赋值基本信息
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {//3.2.2 如果dishId为空，添加的则是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setMealMapper.getById(setmealId);
                //给setmeal赋值基本信息
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            //设置数量
            shoppingCart.setNumber(1);
            //设置创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());

            //加入数据库 购物车表
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     */
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        return list;
    }

    /**
     * 清空购物车
     */
    public void deleteAll() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        shoppingCartMapper.deleteAll(shoppingCart);
    }

    /**
     * 删除购物车的一个商品
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //1 判断要删除的商品的数量是否大于1
        //1.1 创建ShoppingCart对象并封装
        ShoppingCart shoppingCart = new ShoppingCart();
        //1.2 将shoppingCartDTO数据拷贝到shoppingCart
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //1.3 设置查询条件，查询当前登录用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //1.4 查询购物车表中该商品数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && list.size() > 0 ) {
            ShoppingCart cart = list.get(0);
            if(cart.getNumber()==1){//2.1 如果商品的数量等于1

                //2.2 如果商品数量等于1
                shoppingCartMapper.deleteById(cart.getId());
            }else{
                //2.2 如果商品数量大于1
                //将商品数量-1
                cart.setNumber(cart.getNumber() - 1);

                shoppingCartMapper.update(cart);

            }
        }
    }
}

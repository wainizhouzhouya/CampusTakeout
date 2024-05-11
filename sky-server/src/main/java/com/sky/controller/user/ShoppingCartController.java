package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/13 18:07
 *Description:
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "c端-购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品/套餐到购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加菜品/套餐到购物车")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加菜品/套餐到购物车:{}" + shoppingCartDTO);

        shoppingCartService.addShoppingCart(shoppingCartDTO);


        return Result.success();
    }


    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车");
        List<ShoppingCart> list = shoppingCartService.list();

        return Result.success(list);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result deleteAll() {
        log.info("清空购物车");
        shoppingCartService.deleteAll();

        return Result.success();
    }


    @PostMapping ("/sub")
    @ApiOperation("删除购物车的一个商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车的一个商品");
        shoppingCartService.subShoppingCart(shoppingCartDTO);

        return Result.success();
    }
}

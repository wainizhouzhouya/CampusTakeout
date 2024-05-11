package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/12 9:41
 *Description:
 */
@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据菜品分类id查询菜品
     * @param categoryId 菜品分类id
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品:{}" + categoryId);
        //构造redis的key  规则--->dish_菜品分类id
        String key = "dish_" + categoryId;
        //查询redis是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null && list.size() > 0) {
            //如果存在，直接返回
            return Result.success(list);
        }

        //2 如果不存在，查询数据库，将查询到的数据放入redis中
          //2.1 查询启售中的菜品
        Dish dish = new Dish();
        dish.setStatus(StatusConstant.ENABLE);
        dish.setCategoryId(categoryId);
         //2.2 查询数据库
        list = dishService.listWithFlavor(dish);
         //2.3 放入redis
        redisTemplate.opsForValue().set(key,list);

        return Result.success(list);
    }
}

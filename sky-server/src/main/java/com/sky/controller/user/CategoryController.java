package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/12 9:26
 *Description:
 */
@RestController("userCategoryController")
@Slf4j
@RequestMapping("/user/category")
@Api(tags = "c端-分类接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @ApiOperation("查询分类(套餐/菜品)")
    public Result<List<Category>> list(Integer type) {
        log.info("查询分类(套餐/菜品)：{}" + type);
        List<Category> list=categoryService.getByType(type);

        return Result.success(list);
    }
}

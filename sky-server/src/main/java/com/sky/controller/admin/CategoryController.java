package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.impl.CategoryServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/01 12:08
 *Description:
 */
@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "菜品分类相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品分类")
    public Result saveCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增菜品分类: {}" + categoryDTO);
        categoryService.saveCategory(categoryDTO);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("菜品分类分页查询: {}" + categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 禁用或启用菜品分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("禁用或启用菜品分类")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("禁用或启用菜品分类: {},{}" + status, id);
        categoryService.startOrStop(status, id);

        return Result.success();
    }

    /**
     * 根据id查询菜品分类
     * @param id
     * @return
     */
    /*@GetMapping("/{id}")
    @ApiOperation("根据id查询菜品分类")
    private Result<Category> getById(@PathVariable Long id) {
        log.info("根据id查询菜品分类: {}" + id);
        Category category = categoryService.getById(id);

        return Result.success(category);
    }*/

    /**
     * 修改菜品分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品分类")
    public Result modifyCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改菜品分类: {}" + categoryDTO);
        categoryService.modifyCategory(categoryDTO);

        return Result.success();
    }

    /**
     * 根据类型查询菜品分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询菜品分类")
    public Result<List<Category>> getByType(Integer type) {
        log.info("根据类型查询菜品分类： {}" + type);
        List<Category> category = categoryService.getByType(type);

        return Result.success(category);
    }

    /**
     * 根据id删除菜品分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除菜品分类")
    public Result deleteById(Integer id){
        log.info("根据id删除菜品分类: {}"+id);
        categoryService.deleteById(id);

        return Result.success();
    }

}

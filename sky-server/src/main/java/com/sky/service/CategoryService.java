package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/01 12:07
 *Description:
 */
public interface CategoryService {
    /**
     * 新增菜品分类
     */
    void saveCategory(CategoryDTO categoryDTO);


    /**
     * 菜品分页查询
     * @param categoryPageQueryDTO
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);


    /**
     * 禁用或启用菜品分类
     */
    void startOrStop(Integer status, Long id);



    /**
     * 修改菜品分类
     */
    void modifyCategory(CategoryDTO categoryDTO);


    /**
     *根据类型查询菜品分类
     */
    List<Category> getByType(Integer type);

    /**
     * 根据id删除菜品分类
     */
    void deleteById(Integer id);


}

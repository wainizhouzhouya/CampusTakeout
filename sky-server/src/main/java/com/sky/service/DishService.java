package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/02 16:03
 *Description:
 */
public interface DishService {
    /**
     * 新增菜品
     */
    void saveWithFlavour(DishDTO dishDto);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id查询菜品
     */
    DishVO getById(Long id);

    /**
     * 根据id修改菜品及其口味
     * @param dishDTo
     * @return
     */
    void modifyDish(DishDTO dishDTo);


    /**
     * 根据菜品分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 根据分类id查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);


    /**
     * 禁售/启售菜品
     * @param status
     */
    void startOrStopDish(Integer status,Long id);
}

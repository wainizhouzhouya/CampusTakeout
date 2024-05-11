package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/02 16:12
 *Description:
 */
@Mapper
public interface DishFlavorMapper {
    /**
     * 向数据库的口味表插入菜品口味数据
     * @param flavors
     */
    void insert(List<DishFlavor> flavors);

    /**
     * 根据被删除菜品id 批量删除相关菜品口味
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据菜品id查询菜品口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getById(Long dishId);

    /**
     * 根据菜品id删除菜品口味
     * @param dishId
     * @return
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteById(Long dishId);
}

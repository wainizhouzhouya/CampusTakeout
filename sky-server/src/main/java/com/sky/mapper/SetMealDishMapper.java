package com.sky.mapper;

import com.sky.entity.SetmealDish;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/02 20:21
 *Description:
 */
@Mapper
public interface SetMealDishMapper {
    /**
     * 根据菜品id查询对应的套餐id  如果存在套餐 则不允许删除
     */
    List<Long> getSetMealIds(List<Long> setMealIds);

    /**
     * 插入相关套餐的 套餐菜品关系
     */
    void insertBatch(List<SetmealDish> setMealDishes);

    /**
     * 删除/批量删除 套餐菜品关系表的数据
     */
    void deleteByIds(List<Long> ids);


    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where id = #{id}")
    List<SetmealDish>  getById(Long id);

    /**
     * 删除 修改前该套餐所有菜品
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteById(Long setmealId);


}

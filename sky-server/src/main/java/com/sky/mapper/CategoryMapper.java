package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/01 12:05
 *Description:
 */
@Mapper
public interface CategoryMapper {

    /**
     * 添加新的菜品分类
     */
    @Insert("insert into category values (null,#{type},#{name},#{sort},#{status}," +
            "#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Category category);

    /**
     * 菜品分页查询
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 禁用或启用菜品分类
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

    /**
     * 根据类型查询菜品分类  1为菜品分类 2为套餐分类
     */
    List<Category> getByType(Integer type);

    /**
     * 根据id删除菜品分类
     */
    @Delete("delete from category where id = #{id}")
    void deleteById(Integer id);


}

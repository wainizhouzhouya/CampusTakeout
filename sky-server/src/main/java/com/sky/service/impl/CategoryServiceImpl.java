package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/01 12:07
 *Description:
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增菜品分类
     * @param categoryDTO
     */
    @Override
    public void saveCategory(CategoryDTO categoryDTO) {
        //创建Category对象
        Category category = new Category();

        //拷贝categoryDTO数据到category中
        BeanUtils.copyProperties(categoryDTO, category);

        //设置新增的菜品为 0 默认为0 1为启用 0为禁用
        category.setStatus(0);

        //设置创建时间和修改时间
        //category.setCreateTime(LocalDateTime.now());
        //category.setUpdateTime(LocalDateTime.now());

        //设置创建人和修改人
        //category.setCreateUser(BaseContext.getCurrentId());
        //category.setUpdateUser(BaseContext.getCurrentId());

        //加入数据库
        categoryMapper.insert(category);

    }

    /**
     * 菜品分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        //调用数据库分页查询
        Page<Category> pages = categoryMapper.pageQuery(categoryPageQueryDTO);

        //获取总记录数
        long total = pages.getTotal();
        //获取所有数据
        List<Category> result = pages.getResult();

        //创建PageResult对象并返回
        return new PageResult(total, result);
    }

    /**
     * 禁用或启用菜品分类
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //创建对象
        Category category = Category.builder()
                .status(status)
                .id(id)
                .build();

        //修改数据库数据
        categoryMapper.update(category);
    }



    /**
     * 修改菜品分类
     * @param categoryDTO
     */
    @Override
    public void modifyCategory(CategoryDTO categoryDTO) {
        //创建Category对象
        Category category = new Category();

        //将categoryDTO数据拷贝到category中
        BeanUtils.copyProperties(categoryDTO,category);

        //设置修改人即修改时间
        //category.setUpdateTime(LocalDateTime.now());
        //category.setUpdateUser(BaseContext.getCurrentId());

        //进行数据库修改操作
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询菜品分类
     * @param type 1-->菜品分类   2-->套餐分类
     * @return
     */
    public List<Category> getByType(Integer type) {
        //进行数据库查询
        List<Category> categorys = categoryMapper.getByType(type);

        return categorys;
    }

    /**
     * 根据id删除菜品分类
     * @param id
     */
    @Override
    public void deleteById(Integer id) {
        //删除数据库相关数据
        categoryMapper.deleteById(id);
    }

}

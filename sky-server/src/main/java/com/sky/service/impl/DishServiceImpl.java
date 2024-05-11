package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/02 16:04
 *Description:
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavourMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;


    /**
     * 新增菜品
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavour(DishDTO dishDto) {
        //创建Dish对象
        Dish dish = new Dish();

        //将dishDto的数据拷贝到dish
        BeanUtils.copyProperties(dishDto, dish);

        //向菜品表插入数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        //添加菜品的口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //将菜品id赋值给口味表中的id属性
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向数据库的口味表插入菜品口味数据
            dishFlavourMapper.insert(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //调用数据库菜品分页查询方法
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        //获取总记录数
        long total = page.getTotal();
        //获取所有数据
        List<DishVO> result = page.getResult();

        return new PageResult(total, result);
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //判断当前菜品是否启售----   启售--不能删除  禁售--可以删除
        for (Long id : ids) {
            //获取菜品的数据
            Dish dish = dishMapper.getById(id);
            //进行判断
            if (dish.getStatus() == 1) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前菜品是否被套餐关联----  关联--不能删除  未关联--可以删除
        //获取被关联的套餐
        List<Long> setMealIds = setMealDishMapper.getSetMealIds(ids);
        //如果 setMealIds 不为空且长度不为0 则说明有菜品被关联---->不能删除
        if (setMealIds != null && setMealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }


        //删除菜品表中的菜品数据
        dishMapper.deleteByIds(ids);
        //删除菜品关联的菜品口味数据
        dishFlavourMapper.deleteByIds(ids);

    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        //根据id查询菜品
        Dish dish = dishMapper.getById(id);
        //根据id查询口味
        List<DishFlavor> dishFlavor = dishFlavourMapper.getById(id);
        //封装dishVO对象
        DishVO dishVO = new DishVO();
        //将dish数据拷贝到dishVO中
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavor);

        return dishVO;
    }

    /**
     * 根据id修改菜品及其口味
     * @param dishDTo
     * @return
     */
    @Transactional
    public void modifyDish(DishDTO dishDTo) {
        Dish dish = new Dish();
        //将dishVO的数据拷贝到dish中
        BeanUtils.copyProperties(dishDTo, dish);
        //修改菜品表基本信息
        dishMapper.update(dish);
        //删除该菜品所有口味
        dishFlavourMapper.deleteById(dishDTo.getId());
        //加入该菜品所有口味
        List<DishFlavor> flavors = dishDTo.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //将菜品id赋值给口味表中的id属性
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTo.getId());
            });
            //向数据库的口味表插入菜品口味数据
            dishFlavourMapper.insert(flavors);
        }
    }



    /**
     * 根据菜品分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        //封装要查询的菜品的条件  (1-->菜品分类  2-->起售中)
        Dish dish=Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        List<Dish> list=dishMapper.list(dish);

        return list;
    }

    /**
     * 根据分类id查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        //查询菜品基本信息
        List<Dish> list = dishMapper.list(dish);

        List<DishVO> dishVOList=new ArrayList<>();

        for (Dish d :list) {
            //创建DishVO对象
            DishVO dishVO = new DishVO();
            //将list中 每个菜品的数据 拷贝给 每个dishVO
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应菜品的口味
            List<DishFlavor> dishFlavors = dishFlavourMapper.getById(d.getId());
            //将 口味数据 存储到dishVO中
            dishVO.setFlavors(dishFlavors);

            dishVOList.add(dishVO);
        }


        return dishVOList;
    }

    /**
     * 禁售/启售菜品
     * @param status
     */
    public void startOrStopDish(Integer status,Long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);

        dishMapper.update(dish);
    }
}

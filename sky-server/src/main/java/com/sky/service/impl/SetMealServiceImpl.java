package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/12 10:08
 *Description:
 */
@Service
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐,同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Transactional
    public void saveMeal(SetmealDTO setmealDTO) {
        //创建setMeal对象
        Setmeal setmeal = new Setmeal();
        //将setmealDTO的数据拷贝到setmeal
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //将数据加入到套餐表
        setMealMapper.insert(setmeal);

        //获取insert语句生成的主键值（id）
        Long mealId = setmeal.getId();

        //添加套餐菜品关系
        List<SetmealDish> setMealDishes = setmealDTO.getSetmealDishes();

        if (setMealDishes != null && setMealDishes.size() > 0) {
            //将套餐的id赋值给setMealDishes中的每一个菜品
            setMealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(mealId);
            });
        }
        //向数据库中套餐菜品关系表插入相关数据
        setMealDishMapper.insertBatch(setMealDishes);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //调用数据库菜品分页查询方法
        Page<SetmealVO> page = setMealMapper.pageQuery(setmealPageQueryDTO);

        //获取总记录数
        long total = page.getTotal();
        //获取所有数据
        List<SetmealVO> result = page.getResult();

        return new PageResult(total, result);
    }

    /**
     * 删除/批量删除
     * @param ids
     * @return
     */
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //判断当前套餐是否启售  启售---->不能删除  禁售---->可以删除
        for (Long id : ids) {
            //获取id对应的套餐信息
            Setmeal setmeal = setMealMapper.getById(id);

            //判断是否启售
            if (StatusConstant.ENABLE == setmeal.getStatus()) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //删除/批量删除 相关套餐数据
        setMealMapper.deleteByIds(ids);
        //删除/批量删除 套餐菜品关系表的数据
        setMealDishMapper.deleteByIds(ids);

    }


    /**
     * 根据id查询套餐 即 套餐菜品相关数据
     * @param id
     * @return
     */
    public SetmealVO getById(Long id) {
        //根据id查询 套餐相关数据
        Setmeal setmeal = setMealMapper.getById(id);
        //根据id查询 套餐菜品相关数据
        List<SetmealDish> setmealDish = setMealDishMapper.getById(id);

        //创建并封装 SetmealVO对象
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDish);

        return setmealVO;
    }


    /**
     * 修改套餐 即 套餐菜品相关数据
     * @param setmealDTO
     */
    public void modifySetMeal(SetmealDTO setmealDTO) {
        //创建Setmeal对象
        Setmeal setmeal = new Setmeal();
        //将SetmealDTO数据拷贝到setmeal
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //修改套餐表的 基本信息
        setMealMapper.update(setmeal);

        //删除 修改前该套餐所有菜品
        setMealDishMapper.deleteById(setmealDTO.getId());
        //获取 修改后套餐菜品关系 并 赋值套餐id
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            //将修改后套餐菜品关系 加入套餐菜品表
            setMealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 启售或停售套餐
     * @param status
     */
    public void startOrStop(Integer status, Long id) {
        Setmeal build = Setmeal.builder()
                .status(status)
                .id(id)
                .build();

        setMealMapper.update(build);
    }

    /**
     * 根据菜品分类id查询套餐
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setMealMapper.list(setmeal);

        return list;
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param setmealId
     * @return
     */
    public List<DishItemVO> getDishItemById(Long setmealId) {
        List<DishItemVO> dishItemVOList = setMealMapper.getDishItemById(setmealId);

        return dishItemVOList;
    }
}

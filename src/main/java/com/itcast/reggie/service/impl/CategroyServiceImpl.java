package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.common.CustomException;
import com.itcast.reggie.entity.Category;
import com.itcast.reggie.entity.Dish;
import com.itcast.reggie.entity.Setmeal;
import com.itcast.reggie.entity.mapper.CategoryMapper;
import com.itcast.reggie.service.CategoryService;
import com.itcast.reggie.service.DishService;
import com.itcast.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategroyServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类（删除之前需要进行判断当前分类是否关联了相应的菜品Dish和套餐Setmeal）
     * @param id
     */
    @Override
    public void myRemove(Long id) {
        //1.查询当前分类是否关联了菜品Dish，如果关联了菜品则不能直接删除抛出业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //构造查询条件根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        if (count1 > 0) {
            //已经关联了菜品抛出业务异常
            throw new CustomException("当前分类已关联了菜品，不能删除");
        }

        //2.查询当前分类是否关联套餐Setmeal，如果关联了套餐则不能直接删除抛出业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //构造查询条件根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = dishService.count(dishLambdaQueryWrapper);

        if (count2 > 0) {
            //已经关联了套餐抛出业务异常
            throw new CustomException("当前分类已关联了套餐，不能删除");
        }

        //3.既没有关联餐品也没有关联套餐则可正常删除分类,使用IService框架提供的removeById方法
        super.removeById(id);
    }
}

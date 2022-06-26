package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.entity.Dish;
import com.itcast.reggie.entity.DishFlavor;
import com.itcast.reggie.entity.mapper.DishMapper;
import com.itcast.reggie.service.DishFlavorService;
import com.itcast.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 多表操作
     * 新增菜品Dish同时保存DishFlavor口味数据
     * @param dishDto
     */
    @Transactional//多表操作加入事务控制
    public void saveWithFlavor(DishDto dishDto) {
        //1.保存菜品的基本信息到dish表
        this.save(dishDto);

        //2.获取dishId
        Long dishId = dishDto.getId();

        //3.处理List<DishFlavor>集合，为每一个DishFlavor添加上DishId
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //4.保存flavor菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);//saveBatch保存集合

    }
}

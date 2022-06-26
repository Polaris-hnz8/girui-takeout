package com.itcast.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品,同时插入菜品对应的口味数据,多表操作dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);
}

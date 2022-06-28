package com.itcast.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itcast.reggie.dto.SetmealDto;
import com.itcast.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
    //新增逃禅，同时需要保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);
}

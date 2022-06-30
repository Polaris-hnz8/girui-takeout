package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.common.CustomException;
import com.itcast.reggie.dto.SetmealDto;
import com.itcast.reggie.entity.Setmeal;
import com.itcast.reggie.entity.SetmealDish;
import com.itcast.reggie.mapper.SetmealMapper;
import com.itcast.reggie.service.SetmealDishService;
import com.itcast.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增逃禅，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional//操作两张表需要添加事务注解保证事务的一致性（要么全成功/失败）
    public void saveWithDish(SetmealDto setmealDto) {
        //1.保存套餐的基本信息 操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //2.使用stream流处理setmealDishes对象
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //3.保存套餐和菜品的关联信息 操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时删除其与菜品的关联数据
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1, 2, 3) and status = 1
        //1.查询套餐状态，确定套餐是否可以进行删除
        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(Setmeal::getId, ids);
        queryWrapper1.eq(Setmeal::getStatus, 1);

        int count  = this.count(queryWrapper1);
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不可删除");
        }

        //2.先删除套餐表中的数据
        this.removeByIds(ids);

        //3.再删除关系表中的数据（不能直接根据ids对表setmeal_dish进行删除，非主键无法删除）
        //delete from setmeal dish where setmeal id in (1, 2, 3)
        LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper2);
    }
}


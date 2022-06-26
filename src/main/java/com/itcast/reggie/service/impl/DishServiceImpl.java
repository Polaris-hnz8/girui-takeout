package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.entity.Dish;
import com.itcast.reggie.entity.DishFlavor;
import com.itcast.reggie.entity.mapper.DishMapper;
import com.itcast.reggie.service.DishFlavorService;
import com.itcast.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //1.从dish表中查询菜品的基本信息
        Dish dish = this.getById(id);

        //2.属性拷贝
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //3.从dish_flavor表中 查询当前菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //4.将dish_flavor表中 查询到的当前菜品对应的口味信息 封装到DishDto中并返回
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional//添加事务注解保证数据一致性
    public void updateWithFlavor(DishDto dishDto) {
        //1.更新dish表
        this.updateById(dishDto);//传入父类同样可用

        //2.清理当前菜品对应的口味flavor数据（dish_flavor delete）
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //3.添加当前提交菜品对应的口味flavor数据（dish_flavor insert）
        List<DishFlavor> flavors = dishDto.getFlavors();

        //处理List<DishFlavor>集合，为每一个DishFlavor添加上DishId
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}

package com.itcast.reggie.controller;

import com.itcast.reggie.common.R;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.service.DishFlavorService;
import com.itcast.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavourService;

    /**
     * 新增菜品Dish
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {//接收json数据需要添加@RequestBody注解
        log.info(dishDto.toString());
        //dishService.save(dishDto);（不能使用IService框架提供的方法需要自定义save方法进行多表操作）
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }
}

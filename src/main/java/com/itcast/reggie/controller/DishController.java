package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itcast.reggie.common.R;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.entity.Category;
import com.itcast.reggie.entity.Dish;
import com.itcast.reggie.service.CategoryService;
import com.itcast.reggie.service.DishFlavorService;
import com.itcast.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private CategoryService categoryService;

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

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //1.构造分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);

        //2.构造条件构造器 并添加过滤条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);//根据name进行模糊查询
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //3.执行分页操作
        dishService.page(dishPage, queryWrapper);

        //4.封装页面需要的新数据（dish页面展示需要使用到typeName信息，这需要查询两张表的信息多表查询！！！）
        //（1）对dishPage对象中的非records属性进行拷贝
        Page<DishDto>  dishDtoPage = new Page<>();
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        //（2）对dishPage对象中的records数据进行处理
        List<Dish> records = dishPage.getRecords();
        List<DishDto> newRecords = records.stream().map((item)->{

            DishDto dishDto = new DishDto();

            //（2）-1首先获取（拷贝）普通数据
            BeanUtils.copyProperties(item, dishDto);

            //（2）-2再将获取（通过id联查）其他数据
            Long categoryId = item.getCategoryId();//获取分类id

            Category category = categoryService.getById(categoryId);//根据分类id获取分类对象

            if (category != null) {
                String categoryName = category.getName();//根据分类对象获取分类名称
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;

        }).collect(Collectors.toList());

        //（3）新数据封装
        dishDtoPage.setRecords(newRecords);

        //4.返回数据
        return R.success(dishDtoPage);
    }

    /**
     * 菜品回显
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> find(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询菜品dish数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {//Long categoryId
        //1.构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //2.进行查询操作
        List<Dish> list = dishService.list(queryWrapper);

        //3.返回查询数据结果
        return R.success(list);
    }
}

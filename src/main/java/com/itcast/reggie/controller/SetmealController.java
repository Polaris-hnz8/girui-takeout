package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itcast.reggie.common.R;
import com.itcast.reggie.dto.SetmealDto;
import com.itcast.reggie.entity.Category;
import com.itcast.reggie.entity.Setmeal;
import com.itcast.reggie.service.CategoryService;
import com.itcast.reggie.service.SetmealDishService;
import com.itcast.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //1.分页构造器
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);

        //2.添加查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //3.进行查询
        setmealService.page(setmealPage, queryWrapper);

        //4.封装页面需要的新数据（dish页面展示需要使用到typeName信息，这需要查询两张表的信息多表查询！！！）
        //（1）对setmealPage对象中的非records属性进行拷贝
        Page<SetmealDto> setmealDtoPage = new Page<>();
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        //（2）对dishPage对象中的records数据进行处理
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> newRecords = records.stream().map((item)->{

            SetmealDto setmealDto = new SetmealDto();

            //首先获取（拷贝）普通数据
            BeanUtils.copyProperties(item, setmealDto);

            //再将获取（通过id联查）其他数据
            Long categoryId = item.getCategoryId();//获取分类id

            Category category = categoryService.getById(categoryId);//根据分类id获取分类对象

            if (category != null) {
                String categoryName = category.getName();//根据分类对象获取分类名称
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;

        }).collect(Collectors.toList());

        //（3）新数据封装
        setmealDtoPage.setRecords(newRecords);

        //4.返回数据
        return R.success(setmealDtoPage);

    }

    /**
     * 删除套餐（同时删除该套餐与其中菜品的关联关系）
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 移动端使用
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> lsit(Setmeal setmeal) {//@RequestBody只有传递json数据时使用
        //1.构造查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //2.进行查询
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}

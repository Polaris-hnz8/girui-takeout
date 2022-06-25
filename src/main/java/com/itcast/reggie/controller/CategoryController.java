package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itcast.reggie.common.R;
import com.itcast.reggie.entity.Category;
import com.itcast.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category:{}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        log.info("page = {}, pageSize = {}", page, pageSize);
        //1.基于MybatisPlus创建分页查询对象
        Page<Category> pafeInfo = new Page<>(page, pageSize);

        //2.构造一个条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);

        //3.调用service进行分页查询
        categoryService.page(pafeInfo, queryWrapper);
        return R.success(pafeInfo);
    }
}

package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itcast.reggie.common.R;
import com.itcast.reggie.entity.Category;
import com.itcast.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除分类，id为:{}", id);
        //categoryService.removeById(id);（不能使用IService框架提供的方法需要自定义remove方法）
        categoryService.myRemove(id);
        return R.success("分类信息删除成功");
    }

    /**
     * 根据id修改分类信息(回显功能已由前端页面实现、updatetime&updateuser会由MyMetaObjectHandler自动填充)
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息：{}", category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 分类数据回显
     * 根据传递的条件（type值）来动态的查询分类数据
     * 页面通过数据绑定的方式自动显示到下拉框中
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> listCategory(Category category) {//接收String type参数
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //1.按照类型查询
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());

        //2.按照sort顺序排序查询结果,如果sort相同则使用updatetime降序排列
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);

        //3.返回排序结果
        return R.success(list);
    }
}

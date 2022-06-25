package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.entity.Category;
import com.itcast.reggie.mapper.CategoryMapper;
import com.itcast.reggie.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategroyServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
}

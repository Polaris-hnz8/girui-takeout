package com.itcast.reggie.dto;

import com.itcast.reggie.entity.Dish;
import com.itcast.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    //菜品所对应的口味数据flavors
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}

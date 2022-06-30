package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcast.reggie.common.BaseContext;
import com.itcast.reggie.common.R;
import com.itcast.reggie.entity.ShoppingCart;
import com.itcast.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据：{}", shoppingCart);
        //1.设置购物车所属的用户Id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //2.查询当前菜品or套餐是否已经存在购物车中（如果是则数量+1）
        //（1）查询数据购买记录
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        if (dishId != null) {
            //添加到购物车的是菜品（根据UserId和DishId唯一确定 购买菜品记录）
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //添加到购物车的是套餐（根据UserId和setmealId唯一确定 购买套餐记录）
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //SQL: select * from shopping_cart where user_id = ? and dish_id = ?/setmeal_id = ?
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);

        //（2）判断查询结果是否为空后，进行逻辑处理
        if (shoppingCartOne != null) {
            //如果已经存在 则在原来的数量上 +1
            Integer number = shoppingCartOne.getNumber();
            shoppingCartOne.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCartOne);
        } else {
            //如果不存在 则添加到购物车数量默认为1
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCartOne = shoppingCart;
        }

        //3.添加购物车成功后返回数据
        return R.success(shoppingCartOne);
    }
}

package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.common.BaseContext;
import com.itcast.reggie.common.CustomException;
import com.itcast.reggie.entity.*;
import com.itcast.reggie.mapper.OrderMapper;
import com.itcast.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        /**
         * 总结：下单submit方法总共会操作数据库中的3张table
         * 1.订单表orders数据插入、2.订单明细表order_details数据插入、3.购物车shopping_cart数据删除
         */
        //1.获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //2.查询当前用户的购物车中的 所有商品数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new CustomException("购物车为空，无法下单");
        }

        //3.order订单数据补足
        //（1）用户数据查询
        User user = userService.getById(userId);
        //（2）地址数据查询
        long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("地址信息为空，请先填写收货地址");
        }
        //（3）订单明细表数据封装，顺便计算总金额
        long orderId = IdWorker.getId();//生成订单编号
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();//订单明细实体
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());//计算总金额累加
            return orderDetail;
        }).collect(Collectors.toList());

        //（4）订单数据封装
        orders.setId(orderId);//设置订单号
        orders.setNumber(String.valueOf(orderId));//设置订单号
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//订单总金额

        orders.setUserId(userId);//设置订单对应的用户id值
        orders.setUserName(user.getName());//设置收件人姓名
        orders.setConsignee(addressBook.getConsignee());//设置收货人
        orders.setPhone(addressBook.getPhone());//设置收件人手机号
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
            + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
            + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
            + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //4.下单操作
        //（1）向订单表插入数据 一条数据
        this.save(orders);
        //（2）向订单明细表插入数据 多条数据
        orderDetailService.saveBatch(orderDetails);
        //（3）下单完成后需要清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }
}

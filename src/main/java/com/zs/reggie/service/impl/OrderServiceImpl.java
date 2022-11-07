package com.zs.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zs.reggie.mapper.OrderMapper;
import com.zs.reggie.pojo.*;
import com.zs.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveOrderWithDetail(Orders order, Long userId) {

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        //增加订单
        long orderId = IdWorker.getId();
        order.setId(orderId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(2);
        AddressBook addressBook = addressBookService.getById(order.getAddressBookId());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        User user = userService.getById(userId);
        order.setUserId(userId);
        order.setUserName(user.getName());
        //查询总额,并处理购物车数据得到订单明细
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetailList = list.stream().map((item) -> {
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            return orderDetail;
        }).collect(Collectors.toList());
        order.setAmount(new BigDecimal(amount.get()));
        //保存订单
        this.save(order);
        //保存订单明细
        orderDetailService.saveBatch(orderDetailList);
        //清空购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);
    }
}

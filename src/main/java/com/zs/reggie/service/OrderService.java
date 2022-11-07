package com.zs.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zs.reggie.pojo.Orders;


public interface OrderService extends IService<Orders> {
    public void saveOrderWithDetail(Orders orders, Long userId);
}

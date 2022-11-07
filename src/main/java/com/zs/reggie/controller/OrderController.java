package com.zs.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zs.reggie.common.R;
import com.zs.reggie.dto.OrdersDto;
import com.zs.reggie.pojo.OrderDetail;
import com.zs.reggie.pojo.Orders;
import com.zs.reggie.service.OrderDetailService;
import com.zs.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单支付
     * @param order
     * @param request
     * @return
     */
    @PostMapping("/submit")
    public R<String> addOrder(@RequestBody Orders order, HttpServletRequest request){
        //log.info("addressId={},payMethod={},remark={}",order.getAddressBookId(),order.getPayMethod(),order.getRemark());
        Long userId = (Long) request.getSession().getAttribute("user");
        orderService.saveOrderWithDetail(order,userId);
        return R.success("支付成功");
    }
    @GetMapping("userPage")
    public R<Page<OrdersDto>> getUserOrdersPage(int page,int pageSize,HttpServletRequest request){
        //log.info("page={},pageSize={}",page,pageSize);
        Long userId = (Long) request.getSession().getAttribute("user");
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> dtoPageInfo = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,Orders::getUserId,userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);
        BeanUtils.copyProperties(pageInfo,dtoPageInfo,"records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> dtoList = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper1);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());
        dtoPageInfo.setRecords(dtoList);
        return R.success(dtoPageInfo);
    }
}

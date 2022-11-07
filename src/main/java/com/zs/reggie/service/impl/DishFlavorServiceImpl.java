package com.zs.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zs.reggie.mapper.DishFlavorMapper;
import com.zs.reggie.pojo.Dish;
import com.zs.reggie.pojo.DishFlavor;
import com.zs.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}

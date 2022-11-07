package com.zs.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zs.reggie.dto.DishDto;
import com.zs.reggie.pojo.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入口味数据，需要操作两张表
    public void saveWithFlavor(DishDto dishDto);
    //修改菜品，同时修改口味数据
    public void updateWithFlavor(DishDto dishDto);
    //根据id得到DishDto对象
    public DishDto queryDishDtoById(Long id);
}

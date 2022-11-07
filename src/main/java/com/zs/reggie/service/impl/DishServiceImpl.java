package com.zs.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zs.reggie.dto.DishDto;
import com.zs.reggie.mapper.DishMapper;
import com.zs.reggie.pojo.Category;
import com.zs.reggie.pojo.Dish;
import com.zs.reggie.pojo.DishFlavor;
import com.zs.reggie.service.CategoryService;
import com.zs.reggie.service.DishFlavorService;
import com.zs.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    //新增菜品，同时保存口味数据
    @Transactional //执行事务操作
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息，保存到菜品表
        this.save(dishDto);
        //
        Long dishId = dishDto.getId();
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存口味到dishFlavor表
        dishFlavorService.saveBatch(flavors);
    }

    //新增菜品，同时保存口味数据
    @Transactional //执行事务操作
    public void updateWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息，保存到菜品表
        this.updateById(dishDto);
        Long dishId = dishDto.getId();
        //先清除之前的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(queryWrapper);
        //添加新的口味数据
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //菜品口味
        dishFlavorService.saveBatch(flavors);
    }

    //@Transactional
    public DishDto queryDishDtoById(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        if(dish!=null){
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,id);
            List<DishFlavor> list = dishFlavorService.list(queryWrapper);
            BeanUtils.copyProperties(dish,dishDto);
            dishDto.setFlavors(list);
            Category category = categoryService.getById(dish.getCategoryId());
            dishDto.setCategoryName(category.getName());
        }
        return dishDto;
    }
}

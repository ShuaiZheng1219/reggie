package com.zs.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zs.reggie.dto.SetmealDto;
import com.zs.reggie.mapper.SetMealMapper;
import com.zs.reggie.pojo.Setmeal;
import com.zs.reggie.pojo.SetmealDish;
import com.zs.reggie.service.SetMealDishService;
import com.zs.reggie.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {

    @Autowired
    private SetMealDishService setMealDishService;

    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        Long id = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(id);
            return item;
        }).collect(Collectors.toList());
        setMealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto querySetMealById(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        if(setmeal!=null){
            BeanUtils.copyProperties(setmeal,setmealDto);
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId,id);
            List<SetmealDish> list = setMealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(list);
        }
        return setmealDto;
    }

    @Transactional
    public void updateSetMealWithDishs(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        Long id = setmealDto.getId();
        //清除之前的套餐菜品数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        setMealDishService.remove(queryWrapper);
        //插入新数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
           item.setSetmealId(id);
           return item;
        }).collect(Collectors.toList());
        setMealDishService.saveBatch(setmealDishes);
    }
}

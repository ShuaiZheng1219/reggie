package com.zs.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zs.reggie.dto.SetmealDto;
import com.zs.reggie.pojo.Setmeal;

public interface SetMealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);

    public SetmealDto querySetMealById(Long id);

    public void updateSetMealWithDishs(SetmealDto setmealDto);
}

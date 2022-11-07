package com.zs.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zs.reggie.pojo.Dish;
import com.zs.reggie.pojo.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
}

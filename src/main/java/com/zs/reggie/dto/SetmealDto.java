package com.zs.reggie.dto;


import com.zs.reggie.pojo.Setmeal;
import com.zs.reggie.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}

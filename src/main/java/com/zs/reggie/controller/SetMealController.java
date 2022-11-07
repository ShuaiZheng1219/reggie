package com.zs.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zs.reggie.common.R;
import com.zs.reggie.dto.SetmealDto;
import com.zs.reggie.pojo.Category;
import com.zs.reggie.pojo.Setmeal;
import com.zs.reggie.pojo.SetmealDish;
import com.zs.reggie.service.CategoryService;
import com.zs.reggie.service.SetMealDishService;
import com.zs.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> addSetMeal(@RequestBody SetmealDto setmealDto) {
        setMealService.saveWithDish(setmealDto);
        return R.success("保存成功");
    }

    /**
     * 获取套餐分页数据
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPageInfo = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setMealService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, dtoPageInfo, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> dtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        dtoPageInfo.setRecords(dtoList);
        return R.success(dtoPageInfo);
    }

    /**
     * 修改时获得套餐数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> querySetMealById(@PathVariable("id") Long id){
        SetmealDto setmealDto = setMealService.querySetMealById(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> updateSetMealWithDishs(@RequestBody SetmealDto setmealDto){
        setMealService.updateSetMealWithDishs(setmealDto);
        return R.success("修改成功");
    }
    @DeleteMapping
    public R<String> deleteSetMeal(long[] ids){
        for(int i =0;i<ids.length;i++){
            setMealService.removeById(ids[i]);
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId,ids[i]);
            setMealDishService.remove(queryWrapper);
        }
        return R.success("删除成功");
    }
    @PostMapping("/status/{status}")
    public R<String> statusHandler(@PathVariable("status")int status, long[] ids){
        //log.info("status:{}",status);
        for(int i=0;i<ids.length;i++){
            Setmeal setmeal = setMealService.getById(ids[i]);
            setmeal.setStatus(status);
            setMealService.updateById(setmeal);
        }
        return R.success("售卖状态修改成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Long categoryId){
        //log.info("categoryId={}",categoryId);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,categoryId);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setMealService.list(queryWrapper);
        return R.success(list);
    }
}

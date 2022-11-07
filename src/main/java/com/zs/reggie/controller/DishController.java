package com.zs.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zs.reggie.common.R;
import com.zs.reggie.dto.DishDto;
import com.zs.reggie.pojo.Category;
import com.zs.reggie.pojo.Dish;
import com.zs.reggie.pojo.DishFlavor;
import com.zs.reggie.service.CategoryService;
import com.zs.reggie.service.DishFlavorService;
import com.zs.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 查询菜品分页信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> pageDish(int page,int pageSize,String name){
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> pageDtoInfo = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>();
        queryWrapper.eq(Dish::getIsDeleted,0);
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByAsc(Dish::getSort);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝,不拷贝records
        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> dishDtoList = records.stream().map((item) -> {
            //将Dish对象转为Dish对象
            DishDto dishDto = new DishDto();
            //对Dish属性进行拷贝
            BeanUtils.copyProperties(item, dishDto);
            //通过Dish对象id查询名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                //菜品分类名称保存进DishDto对象中
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        //设置分页对象中的records
        pageDtoInfo.setRecords(dishDtoList);

        return R.success(pageDtoInfo);
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        String key = "dish_"+dishDto.getCategoryId();
        redisTemplate.delete(key);
        return R.success("添加成功");
    }

    /**
     * 查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable("id") Long id){
        DishDto dishDto = dishService.queryDishDtoById(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        String key = "dish_"+dishDto.getCategoryId();
        redisTemplate.delete(key);
        return R.success("修改成功");
    }

    /**
     * 起手停售、批量起售停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> statusHandeler(@PathVariable("status") int status, Long[] ids){
        for(int i =0;i<ids.length;i++){
            Dish dish = dishService.getById(ids[i]);
            dish.setStatus(status);
            dishService.updateById(dish);
            //清除缓存
            String key = "dish_"+dish.getCategoryId();
            redisTemplate.delete(key);
        }
        return R.success("修改成功");
    }

    /**
     * 删除和批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        List<Long> idList = Arrays.asList(ids);
        idList.forEach(id->{
            dishService.removeById(id);
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,id);
            dishFlavorService.remove(queryWrapper);
        });
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> getDishListByCategoryId(Long categoryId){
        String key = "dish_"+categoryId;
        //先从redis中获取缓存数据
        List<DishDto> dishDtos  = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回
        if(dishDtos!=null){
            return R.success(dishDtos);
        }
        //如果不存在，查数据库
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,categoryId);
        queryWrapper.eq(Dish::getStatus,1);
        List<Dish> list = dishService.list(queryWrapper);
        dishDtos = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(dishId != null, DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        redisTemplate.opsForValue().set(key,dishDtos,60, TimeUnit.MINUTES);
        return R.success(dishDtos);
    }
}

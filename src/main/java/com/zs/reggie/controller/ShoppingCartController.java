package com.zs.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zs.reggie.common.R;
import com.zs.reggie.pojo.Dish;
import com.zs.reggie.pojo.Setmeal;
import com.zs.reggie.pojo.ShoppingCart;
import com.zs.reggie.service.DishService;
import com.zs.reggie.service.SetMealService;
import com.zs.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetMealService setMealService;

    //查询购物车中的商品
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpServletRequest request){
        //log.info("user_id:{}",request.getSession().getAttribute("user"));
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }
    /**
     * 购物车中增加菜品或者套餐
     * @param shoppingCart
     * @param request
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> addCart(@RequestBody ShoppingCart shoppingCart,HttpServletRequest request){
        //log.info("shoppingCart={}",shoppingCart.toString());
        //设置用户id
        Long userId = (Long) request.getSession().getAttribute("user");
        shoppingCart.setUserId(userId);

        //查询购物车中是否有该菜品
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        if(cart!=null){//说明购物车中有该商品
            cart.setNumber(cart.getNumber()+1);
            shoppingCartService.updateById(cart);
        }else{//购物车没有该商品
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }
        return R.success(cart);
    }

    /**
     * 从购物车减少一件商品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart,HttpServletRequest request){
        //log.info("shopping={}",shoppingCart.toString());
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        if(cart!=null){
            int number = cart.getNumber();
            if(number>=2){
                cart.setNumber(number-1);
                shoppingCartService.updateById(cart);
            }else{
                shoppingCartService.removeById(cart);
            }
            return R.success(cart);
        }
        return R.error("没有该商品信息");
    }

    /**
     * 清空购物车
     * @param request
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> cleanShoppingCart(HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空成功");
    }
}

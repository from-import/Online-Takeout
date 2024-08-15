package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxx.takeout.common.BaseContext;
import com.xxx.takeout.common.R;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.entity.ShoppingCart;
import com.xxx.takeout.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加购物车中");

        // 设置用户ID
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 查询当前传入购物车的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();

        // userID 构造
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);


        if(dishId != null){
            // 添加到购物车的是菜品
            // userID 和 dishID 联合查询
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }
        else{
            // 添加到购物车的是套餐
            // userID 和 mealId 联合查询
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        // 对应的SQL语句为 SELECT * FROM shopping_cart WHERE user_id = ? AND dish_id/setmeal_id = ?
        // 查找当前用户购物车中是否已经存在该菜品
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);

        if(shoppingCart1 != null){
            // 菜品已存在
            Integer number = shoppingCart1.getNumber();
            shoppingCart1.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCart1);
        }
        else{
            shoppingCart.setNumber(1); // 此菜品此前并未添加过，因此设置数量为1
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart); // save and updateById
            shoppingCart1 = shoppingCart;
        }
        return R.success(shoppingCart1);
    }


    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("正在查看购物车");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("正在清空购物车");
        // SQL: DELETE FROM shopping_cart WHERE user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }
}

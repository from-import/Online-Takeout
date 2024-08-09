package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.takeout.common.CustomException;
import com.xxx.takeout.entity.Category;
import com.xxx.takeout.entity.Dish;
import com.xxx.takeout.entity.Setmeal;
import com.xxx.takeout.mapper.CategoryMapper;
import com.xxx.takeout.mapper.SetmealMapper;
import com.xxx.takeout.service.CategoryService;
import com.xxx.takeout.service.DishService;
import com.xxx.takeout.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.invoke.LambdaMetafactory;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    // 根据ID删除分类
    @Override
    public void remove(Long id) {
        // 如果关联了菜品或套餐，则抛出异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id); // 添加查询条件，根据分类的ID
        int dishCount = dishService.count(dishLambdaQueryWrapper); // 目前关联的菜品数量

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id); // 添加查询条件，根据分类的ID
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper); // 目前关联的套餐数量

        if (dishCount > 0 || setmealCount > 0){
            // 关联了菜品或套餐
            throw new CustomException("本分类关联了菜品或套餐");
        }

        //  正常删除
        super.removeById(id);

    }
}

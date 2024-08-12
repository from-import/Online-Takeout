package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.entity.Dish;
import com.xxx.takeout.entity.DishFlavor;
import com.xxx.takeout.mapper.DishFlavorMapper;
import com.xxx.takeout.mapper.DishMapper;
import com.xxx.takeout.service.DishFlavorService;
import com.xxx.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    // 新增菜品并保存口味数据
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 1. 保存菜品基本信息
        this.save(dishDto);
        Long dishId = dishDto.getId(); // 菜品ID

        // 2. 保存口味数据到口味表
        List<DishFlavor> flavors = dishDto.getFlavors();
        List<DishFlavor> finalFlavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(finalFlavors);
    }


    // 根据id 查询菜品属性 + 菜品分类
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息 dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 查询菜品分类信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List <DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors); // 口味赋值

        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 1. 更新dish表
        this.updateById(dishDto);

        // 2. 清理口味表 dish_flavor delete
        // mySQL --> delete from dish_flavor where dish_id = ???
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);

        // 3. 更新口味表 dish_flavor insert
        List<DishFlavor> flavors = dishDto.getFlavors();
        List<DishFlavor> finalFlavors = flavors.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(finalFlavors);

    }
}

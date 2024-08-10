package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.entity.Dish;
import com.xxx.takeout.entity.DishFlavor;
import com.xxx.takeout.mapper.DishFlavorMapper;
import com.xxx.takeout.mapper.DishMapper;
import com.xxx.takeout.service.DishFlavorService;
import com.xxx.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.Collections;
import java.util.List;
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
}

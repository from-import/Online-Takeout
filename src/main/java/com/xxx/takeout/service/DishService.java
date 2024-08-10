package com.xxx.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.entity.Dish;

public interface DishService extends IService<Dish> {

    // 新增菜品并添加口味
    public void saveWithFlavor(DishDto dishDto);
}

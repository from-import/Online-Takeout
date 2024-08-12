package com.xxx.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.entity.Dish;

public interface DishService extends IService<Dish> {

    // 新增菜品并添加口味
    public void saveWithFlavor(DishDto dishDto);
    public DishDto getByIdWithFlavor(Long id);

    // 同时更新 菜品信息 与 口味信息
    public void updateWithFlavor(DishDto dishDto);
}

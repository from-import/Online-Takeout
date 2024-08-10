package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxx.takeout.common.R;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.entity.Category;
import com.xxx.takeout.entity.Dish;
import com.xxx.takeout.service.CategoryService;
import com.xxx.takeout.service.DishFlavorService;
import com.xxx.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 菜品管理
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    // 新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("新增菜品{}", dishDto.toString());
        // 写入SQL数据库
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    // 菜品展示界面
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 过滤条件
        queryWrapper.like(name != null, Dish::getName,name);
        // 排序条件
        queryWrapper.orderByAsc(Dish::getUpdateTime);

        // 将pageInfo 和 queryWrapper 传进 dishService
        dishService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo,dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);  // 将item的普通属性 copy to dishDto
            Long categoryId = item.getCategoryId();  // 菜品分类的ID
            Category category = categoryService.getById(categoryId);  // 根据菜品分类的ID获取的菜品分类对象
            String categoryName = category.getName();  // 菜品分类名称
            dishDto.setCategoryName(categoryName);  // 设置菜品分类名称
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
}

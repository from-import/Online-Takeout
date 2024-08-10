package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxx.takeout.common.R;
import com.xxx.takeout.entity.Category;
import com.xxx.takeout.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.smartcardio.Card;
import java.util.List;

// 分类管理
@RestController
@RequestMapping("category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // json格式传，@RequestBody注解
    // 新增分类
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("新增菜品分类成功");
    }

    // 套餐展示
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        // 分页构造
        Page<Category> pageInfo = new Page<>(page,pageSize);
        // 条件构造
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 排序条件
        queryWrapper.orderByAsc(Category::getSort);

        // 分页查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    // 套餐删除 通过 ids 明确指定 ids 参数的来源。
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除了套餐{}",ids);
        categoryService.remove(ids); // 自定义删除方法
        return R.success("删除成功");
    }

    // 套餐修改
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("分类信息修改:{}",category);
        categoryService.updateById(category);
        return R.success("分类修改成功");

    }


    // 根据条件查询全部分类
    @GetMapping("/list")
    public R<List<Category>> list(Category category){

        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 筛选条件
        queryWrapper.eq(category.getType() != null, Category::getType,category.getType());
        // 排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);

    }
}

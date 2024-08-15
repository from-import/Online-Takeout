package com.xxx.takeout.controller;

// 套餐管理


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxx.takeout.common.R;
import com.xxx.takeout.dto.SetmealDto;
import com.xxx.takeout.entity.Setmeal;
import com.xxx.takeout.service.CategoryService;
import com.xxx.takeout.service.SetmealDishService;
import com.xxx.takeout.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐添加信息: {}", setmealDto);
        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 分页查询套餐
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        Page<SetmealDto> dtoPage = new Page<>();
        // LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        // 根据name 进行模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        // 根据更新时间，降序排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo, dtoPage, "records"); // 对象属性copy
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) ->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto); // copy 属性
            Long categoryId = item.getCategoryId(); // 获取分类ID

            // 根据分类ID查询分类名称
            String categoryName = categoryService.getById(categoryId).getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(pageInfo);
    }


    /**
     * 批量删除套餐
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除套餐{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功");
    }


    /**
     * 修改套餐在售状态
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@RequestParam("ids") List<Long> ids, @PathVariable("status") Integer status){
        log.info("修改套餐状态{},{}", ids, status);
        if (ids != null && !ids.isEmpty()) {
            // 创建批量更新的 Setmeal 对象列表
            List<Setmeal> setmeals = ids.stream().map(id -> {
                Setmeal setmeal = new Setmeal();
                setmeal.setId(id);
                setmeal.setStatus(status);
                return setmeal;
            }).collect(Collectors.toList());

            // 批量更新套餐的状态
            setmealService.updateBatchById(setmeals);
        }
        return R.success("修改套餐状态成功");
    }

    /**
     * 按条件查询套餐
     * @param setmeal
     * @return
     */
    @GetMapping
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        wrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        wrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(wrapper);
        return R.success(list);
    }


}

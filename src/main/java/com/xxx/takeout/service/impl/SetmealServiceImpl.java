package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.takeout.dto.SetmealDto;
import com.xxx.takeout.entity.Setmeal;
import com.xxx.takeout.entity.SetmealDish;
import com.xxx.takeout.mapper.SetmealDishMapper;
import com.xxx.takeout.mapper.SetmealMapper;
import com.xxx.takeout.service.SetmealDishService;
import com.xxx.takeout.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐同时保存套餐与菜品的关联关系,多表操作用 @Transactional 确保事务一致性
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐 操作 setmeal
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map( (setmealDish) -> {
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());

        // 保存套餐与菜品的关联关系 操作 setmeal_dish
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());
    }


    /**
     * 删除套餐同时移除套餐与菜品的关联关系
     * @param ids
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确定能否删除
        // SELECT COUNT(*) FROM setmeal WHERE id IN (1,2,3) AND STATUS = 1
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId, ids); // 按ID查询
        queryWrapper.eq(Setmeal::getStatus, 1); // 状态为1

        // 若不能删除，抛出异常
        int count = this.count(queryWrapper);
        if(count > 0) {
            throw new RuntimeException("套餐状态不正确，不能删除");
        }

        // 删除套餐表中的数据
        this.removeByIds(ids);

        // 删除套餐与菜品的关联关系
        // DELETE FROM setmeal_dish WHERE setmeal_id IN (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lambdaQueryWrapper);

    }

}

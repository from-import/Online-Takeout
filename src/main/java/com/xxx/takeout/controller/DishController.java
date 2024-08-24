package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxx.takeout.common.R;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.entity.Category;
import com.xxx.takeout.entity.Dish;
import com.xxx.takeout.entity.DishFlavor;
import com.xxx.takeout.service.CategoryService;
import com.xxx.takeout.service.DishFlavorService;
import com.xxx.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedisTemplate redisTemplate; // Redis

    // 新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("新增菜品{}", dishDto.toString());
        // 写入SQL数据库
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    // 修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("更新菜品{}", dishDto.toString()); // 双表更新
        dishService.updateWithFlavor(dishDto); // 写入SQL数据库
//        // 清除所有菜品在Redis中的缓存数据
//        redisTemplate.keys("dish_*").forEach((key) -> {
//            redisTemplate.delete(key);
//        });

        // 精确清理菜品缓存
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    // 菜品分页展示界面
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // Page<Dish> 是 MyBatis-Plus 提供的分页对象，用于存储分页查询的结果。
        // 这里 pageInfo 是分页构造器，包含当前页码、每页条数等分页信息。
        Page<Dish> pageInfo = new Page<>(page,pageSize);

        // Page<DishDto> 用于存储最终返回给前端的分页数据，其中 DishDto 是包含额外数据（如分类名称）的 DTO（数据传输对象）。
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器 LambdaQueryWrapper<Dish> 是 MyBatis-Plus 提供的条件构造器，用于构建查询条件。
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 过滤条件 queryWrapper.like(name != null, Dish::getName, name)：如果 name 不为空，则添加 like 条件，用于模糊查询菜品名称。
        queryWrapper.like(name != null, Dish::getName,name);
        // 排序条件 queryWrapper.orderByAsc(Dish::getUpdateTime)：按 updateTime 进行升序排序。
        queryWrapper.orderByAsc(Dish::getUpdateTime );

        // 将pageInfo 和 queryWrapper 传进 dishService
        // 通过调用 dishService.page(pageInfo, queryWrapper)
        // 将分页构造器 pageInfo 和查询条件 queryWrapper 传递给 MyBatis-Plus 的 page 方法，执行数据库查询。
        dishService.page(pageInfo, queryWrapper);

        // BeanUtils.copyProperties(pageInfo, dishDtoPage, "records")：
        // 将 pageInfo 对象的属性（如总记录数、分页信息）拷贝到 dishDtoPage，但不拷贝 records 属性（即具体的数据列表）。
        BeanUtils.copyProperties(pageInfo,dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords(); // List<Dish> records = pageInfo.getRecords()：获取当前页的 Dish 数据列表。

        // 使用 stream 流和 map 操作，将 Dish 转换为 DishDto：
        // 将 Dish 列表 records 转换为一个 Stream 流，以便对其中的元素进行一系列的操作
        List<DishDto> list = records.stream()
                .map((item) -> {
                    // 使用 map 函数将每个 Dish 对象转换为 DishDto 对象，map 会对流中的每个元素执行给定的函数，并返回一个新的流
                    // 创建一个新的 DishDto 对象，作为转换后的结果对象
                    DishDto dishDto = new DishDto();

                    // 使用 BeanUtils 将 item (Dish 对象) 的普通属性复制到 dishDto 对象中，减少手动设置属性的代码量
                    BeanUtils.copyProperties(item, dishDto);

                    // 从 Dish 对象中获取 categoryId，即当前菜品所属的分类 ID
                    Long categoryId = item.getCategoryId();

                    // // 使用 categoryId 通过 categoryService 查询对应的 Category 对象，获取菜品的分类信息
                    Category category = categoryService.getById(categoryId);

                    String categoryName = category.getName();  // 从查询到的 Category 对象中获取分类名称
                    dishDto.setCategoryName(categoryName);  // 将分类名称设置到 dishDto 对象中，以便前端显示菜品所属分类
                    return dishDto;  // 返回转换后的 DishDto 对象，map 函数会将它收集到新的流中
                })
                .collect(Collectors.toList());  // 将转换后的 DishDto 流收集为一个 List 集合，并赋值给变量 list
        dishDtoPage.setRecords(list);
        // 返回结果：返回一个包含分页信息的统一响应对象 R<Page<DishDto>>。
        return R.success(dishDtoPage);
    }

    /**
     * 根据ID查询菜品属性 和 口味信息
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        // 双表查询 需要在 DishService 实现方法
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 根据条件查询对应菜品数据
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(DishDto dish){
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus(); // dish_5455547554_1
        String lockKey = key + "_lock"; // 用于加锁的键

        // 首先尝试从Redis中获取数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        // 如果获取到，直接返回数据
        if (dishDtoList != null){
            return R.success(dishDtoList);
        }

        // 当缓存中没有数据时，通过 setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS) 尝试获取分布式锁（互斥锁）。
        // 加锁机制防止缓存击穿
        try {
            // 尝试获取锁，设置超时时间防止死锁
            // 只有获取到锁的请求会继续访问数据库并更新缓存，未获取到锁的请求会稍作休眠并重试。
            // 通过这种机制，确保在缓存失效的情况下，只有一个线程能够访问数据库，避免多个线程同时访问数据库，造成数据库压力过大。
            Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

            if (isLock != null && isLock) {
                // 获取到锁，进入正常读取逻辑，然后添加到Redis
                // 构造条件构造器
                LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

                // 添加查询条件
                queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
                queryWrapper.eq(dish.getIsDeleted() != null, Dish::getIsDeleted, dish.getIsDeleted());
                queryWrapper.eq(Dish::getStatus,1); // status == 1

                // 添加排序条件
                queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

                // 4. 执行数据库查询
                List<Dish> list = dishService.list(queryWrapper);

                if (list == null || list.isEmpty()) {
                    // 5. 如果数据库中也没有数据，缓存空值并设置较短的过期时间（如5分钟）
                    // 当查询数据库时，如果返回的结果为空（即数据库中也没有该数据）
                    // 将空结果缓存到 Redis 中，并设置较短的过期时间（如 5 分钟）。这样可以避免缓存穿透。
                    redisTemplate.opsForValue().set(key, new ArrayList<>(), 5, TimeUnit.MINUTES);
                    return R.success(new ArrayList<>());
                }

                List<DishDto> dishDtos = list.stream().map((item) -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(item, dishDto);  // 将item的普通属性 copy to dishDto
                    Long categoryId = item.getCategoryId();  // 菜品分类的ID
                    Category category = categoryService.getById(categoryId);  // 根据菜品分类的ID获取的菜品分类对象
                    String categoryName = category.getName();  // 菜品分类名称
                    dishDto.setCategoryName(categoryName);  // 设置菜品分类名称

                    // 当前菜品的ID
                    Long dishID = item.getId();
                    LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
                    flavorQueryWrapper.eq(DishFlavor::getDishId, dishID);

                    // 菜品对应的口味
                    List<DishFlavor> flavors = dishFlavorService.list(flavorQueryWrapper);

                    // 最终SQL: SELECT * FROM dish_flavor WHERE dish_id = dishID
                    dishDto.setFlavors(flavors);  // 设置菜品对应的口味数据
                    return dishDto;
                }).collect(Collectors.toList());

                // 缓存到 Redis 数据库 , 同时防止出现缓存雪崩
                // 生成随机过期时间，范围是 2 小时到 2 小时 30 分钟之间
                //  在设置缓存过期时间时，你通过增加一个随机的波动值来避免大量缓存同时失效。
                //  例如，原本设置的缓存 TTL 为 1 小时，你可以随机增加或减少几分钟的时间，
                //  这样可以有效避免在同一时刻大量缓存失效，防止流量突然全部涌向数据库。
                int randomTime = 120 + (int)(Math.random() * 30); // 120 分钟 + 0-30 分钟
                redisTemplate.opsForValue().set(key, dishDtos, randomTime, TimeUnit.MINUTES);

                return R.success(dishDtos);
            } else {
                // 没有获取到锁，休眠一段时间后重试
                Thread.sleep(50);
                return list(dish); // 重试机制
            }
        } catch (InterruptedException e) {
            log.error("Error while handling cache breakdown", e);
            Thread.currentThread().interrupt();
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }

        return R.error("Cache breakdown occurred");
    }



    /*
        @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        // 构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(dish.getIsDeleted() != null, Dish::getIsDeleted, dish.getIsDeleted());
        queryWrapper.eq(Dish::getStatus,1); // status == 1

        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }
     */
}

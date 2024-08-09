package com.xxx.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.takeout.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}

package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.takeout.entity.ShoppingCart;
import com.xxx.takeout.mapper.ShoppingCartMapper;
import com.xxx.takeout.service.ShoppingCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Transactional
    public void addToCart(ShoppingCart cart) {
        // 添加购物车的业务逻辑
    }
}

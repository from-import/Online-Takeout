package com.xxx.takeout.controller;

import com.xxx.takeout.common.R;
import com.xxx.takeout.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/seckill")
@Slf4j
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @PostMapping("/execute")
    public R<String> executeSeckill(@RequestParam("userId") Long userId, @RequestParam("goodsId") Long goodsId) {
        try {
            boolean success = seckillService.executeSeckill(userId, goodsId);
            if (success) {
                log.info("用户 {} 秒杀成功", userId);
                return R.success("秒杀成功！");
            } else {
                return R.error("秒杀失败，库存不足或已经秒杀过该商品。");
            }
        } catch (Exception e) {
            return R.error("秒杀失败：" + e.getMessage());
        }
    }
}

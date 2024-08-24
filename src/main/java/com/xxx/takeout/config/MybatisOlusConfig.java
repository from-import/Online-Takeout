package com.xxx.takeout.config;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 配置mp的分页插件
@Configuration
public class MybatisOlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}

/**
 * 3. 解释配置细节:
 *@Configuration 注解: 这个类使用了 @Configuration 注解，表明这是一个 Spring 的配置类，Spring 会自动扫描并加载这个类中的配置。
 *
 *@Bean 注解: mybatisPlusInterceptor() 方法被 @Bean 注解标注，
 * 表示返回的 MybatisPlusInterceptor 对象会被 Spring 容器管理并注册为一个 Bean，这样 MyBatis-Plus 的分页功能在应用启动时就会自动配置好。
 *
 *MybatisPlusInterceptor 和 PaginationInnerInterceptor:
 *  MybatisPlusInterceptor 是 MyBatis-Plus 提供的一个插件管理器，可以用来添加各种插件。
 *  PaginationInnerInterceptor 是用于实现分页功能的拦截器，它会拦截 SQL 并自动添加分页逻辑，保证返回的数据是分页后的结果。
 */
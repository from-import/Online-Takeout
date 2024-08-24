package com.xxx.takeout.config;

import com.xxx.takeout.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    // 静态资源映射
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("静态资源映射");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    // 扩展MVC框架消息转换器
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，用Jackson 将 java class 转 json
        messageConverter.setObjectMapper(new JacksonObjectMapper());

        // 将消息转换器追加到mvc框架转换器集合第一位
        converters.add(0,messageConverter);
    }
}
/**
 * 这个 WebMvcConfig 类是一个自定义的 Spring MVC 配置类，用于扩展和修改 Spring MVC 的默认配置。
 * 这个类继承自 WebMvcConfigurationSupport，并重写了部分方法，以实现以下功能：
 * 1. 静态资源映射
 * @Override
 * protected void addResourceHandlers(ResourceHandlerRegistry registry) {
 *     log.info("静态资源映射");
 *     registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
 *     registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
 * }
 * 这段代码用于配置静态资源的映射路径。
 * 当浏览器访问 /backend/** 或 /front/** 路径时，Spring MVC 会自动从项目的 classpath 下对应的目录加载静态资源（如 HTML、CSS、JavaScript 文件等）。
 *
 * 2. 扩展 MVC 框架的消息转换器
 * @Override
 * protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
 *     MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
 *     // 设置对象转换器，用 Jackson 将 Java 对象转换为 JSON
 *     messageConverter.setObjectMapper(new JacksonObjectMapper());
 *
 *     // 将消息转换器追加到 MVC 框架转换器集合的第一位
 *     converters.add(0, messageConverter);
 * }
 * 消息转换器: Spring MVC 使用消息转换器 (HttpMessageConverter) 来处理 HTTP 请求和响应的转换。
 * 它可以将 Java 对象转换为不同的数据格式（如 JSON、XML 等），也可以将请求的数据格式转换为 Java 对象。
 *
 * Jackson: MappingJackson2HttpMessageConverter 是一个使用 Jackson 来处理 JSON 数据的消息转换器。
 * Jackson 是一个用于将 Java 对象转换为 JSON 字符串，或将 JSON 字符串转换为 Java 对象的库。
 * JacksonObjectMapper: 自定义的 JacksonObjectMapper 用于配置 Jackson 的序列化和反序列化行为，比如日期格式、忽略空值属性等。
 *
 * 添加顺序: converters.add(0, messageConverter) 表示将这个自定义的消息转换器放在转换器列表的第一位。
 * 这样，在处理 JSON 请求和响应时，Spring MVC 会优先使用这个配置好的转换器。
 */

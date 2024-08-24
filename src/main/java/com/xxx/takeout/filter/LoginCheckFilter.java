package com.xxx.takeout.filter;

// 用户登录check

import com.alibaba.fastjson.JSON;
import com.xxx.takeout.common.BaseContext;
import com.xxx.takeout.common.R;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.midi.Track;
import javax.xml.ws.WebFault;
import java.io.FileFilter;
import java.io.IOException;

/**
 * 过滤器是基于 Java Servlet 规范的，并通过 URL 模式来拦截请求和响应。
 * 它与 AOP 不同，AOP 是通过切面（Aspects）对程序中的方法执行进行增强，
 * 而过滤器是在整个请求-响应生命周期中发挥作用的，可以对进入的请求和出来的响应进行全局的、统一的处理。
 */

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取请求的URI
        String requestURI = request.getRequestURI();

        // 获取请求头中的 User-Agent
        String userAgent = request.getHeader("User-Agent");

        // 如果请求来自 Postman，则放行
        if (userAgent != null && userAgent.contains("Postman")) {
            log.info("Postman request detected, skipping login check.");
            filterChain.doFilter(request, response);
            return;
        }

        // 无需处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
        };

        // 判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        // 无需处理
        if (check) {
            log.info("not filtered");
            filterChain.doFilter(request, response);
            return;
        }

        // 如果员工已经登陆
        if (request.getSession().getAttribute("employee") != null) {
            log.info("already login");
            log.info("username = {}", request.getSession().getAttribute("employee"));

            // 将empId封装到线程中
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        // 如果用户已经登陆
        if (request.getSession().getAttribute("user") != null) {
            log.info("already login");
            log.info("username = {}", request.getSession().getAttribute("user"));

            // 将userId封装到线程中
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        // 如果未登录
        log.info("not login");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("拦截到请求{}", request.getRequestURI());
    }

    // 检测本次匹配是否通过
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}

package com.xxx.takeout.filter;

// 用户登录check

import com.alibaba.fastjson.JSON;
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

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取请求的URI
        String requestURI = request.getRequestURI();

        // 无需处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"

        };

        // 判断本次请求是否需要处理
        boolean check = check(urls,requestURI);

        // 无需处理
        if(check){
            log.info("not filtered");
            filterChain.doFilter(request,response);
            return;
        }

        // 如果已经登陆
        if(request.getSession().getAttribute("employee") != null){
            log.info("already login");
            log.info("username = {}", request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }

        // 如果未登录
        log.info("not login");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("拦截到请求{}", request.getRequestURI());
    }

    // 检测本次匹配是否通过
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}

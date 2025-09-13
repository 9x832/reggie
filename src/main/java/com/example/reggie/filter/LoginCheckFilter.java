package com.example.reggie.filter;

import com.example.reggie.commom.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录检查过滤器
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    //创建ObjectMapper实例，用于将Java对象转为JSON
    private static final ObjectMapper ObjectMapper = new ObjectMapper();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        log.info("拦截到请求：{}", request.getRequestURI());

        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();

        //2.判断本次请求是否需要处理
        String [] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };
        boolean check = check(urls, requestURI);

        //3.如果不需要处理，则直接放行
        if (check){
//            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4.如果请求需要处理，则判断登录状态，如果已登录则放行
        if (request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));
            filterChain.doFilter(request, response);
            return;
        }

        //5.未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(ObjectMapper.writeValueAsString(R.error("NOTLOGIN")));
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check( String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url,requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}

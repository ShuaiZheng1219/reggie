package com.zs.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.zs.reggie.common.R;
import com.zs.reggie.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符 /** 样式的路径
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request  = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        /**
         1、获取本次求情uri
         2、判断本次求情是否需要处理，如果不需要处理，则直接放行
         3、判断登录状态，如果已登录，放行
         4、如果未登录，返回登录页面
         */
        //1、获取本次求情uri
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/login",//移动端登录
                "/user/sendMsg"//发送短信
        };
        //2、判断本次求情是否需要处理，如果不需要处理，则直接放行
        boolean check = check(urls,requestURI);
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //3、判断管理端登录状态，如果已登录，放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("已经登录,用户id为{}",request.getSession().getAttribute("employee"));
            long id  = Thread.currentThread().getId();
            //log.info("线程id为{}",id);
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }
        //判断移动端登录状态
        if(request.getSession().getAttribute("user")!=null){
            log.info("已经登录,用户id为{}",request.getSession().getAttribute("user"));
            long id  = Thread.currentThread().getId();
            //log.info("线程id为{}",id);
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"));
            filterChain.doFilter(request,response);
            return;
        }
        //4、如果未登录，返回登录页面,通过输出流的方式向客户端页面响应数据
        log.info("未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }


    //检查路径是否需要被处理
    public boolean check(String[] urls,String requestURI){
        for(String url: urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}

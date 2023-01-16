package com.it.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileFilter;
import java.io.IOException;
@Slf4j
//检查用户是否已经登录
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "*") //*是检查所有
public class LoginCheckFilter implements Filter {
    //路径匹配器,支持通配符
    public static final AntPathMatcher PATH_MATCHER =new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse)  servletResponse;

        //获取本次请求的url
        String requestURI = request.getRequestURI();
        //不需要处理的请求路径数组
        String[] urls =new String[]{"/employee/login", "/employee/logout",
                                    "/backend/**","/front/**",
                                    "/common/**",
                                    "/user/sendMsg","/user/login","/user/addressBook"}; //移动端用户登录
        //判断本次请求是否需要处理
        boolean check= check(urls,requestURI);
        //如果不需要处理,则直接放行
        if(check){
            filterChain.doFilter(request,response); //放行
            return;
        }

        //需要处理 判断为已登录 则直接放行
       if( request.getSession().getAttribute("employee")!=null){


           //获取线程id 存到BaseContext工具类中
           Long empId =(Long)request.getSession().getAttribute("employee");
           BaseContext.setCurrentId(empId);

           filterChain.doFilter(request,response);
            return;
        }

        //需要处理 判断为已登录 则直接放行
        if( request.getSession().getAttribute("user")!=null){

            //获取线程id 存到BaseContext工具类中
            Long userId =(Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

       //需要处理 判断未登录 则返回未登录结果,通过输出流方式向客户端影响数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        log.info("拦截到请求:{}",request.getRequestURI());

    }

    //路径匹配 判断请求是否要放行
    public boolean check(String []urls ,String requestURI){
        for (String url : urls) {
            boolean match= PATH_MATCHER.match(url,requestURI); //判断是否匹配
            if(match){
                return true;
            }
        }
        return false;
    }
}

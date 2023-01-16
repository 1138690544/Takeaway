package com.it.reggie.common;

/*基于ThreadLocal封装的工具类,用户保存和获取当前登录用户的id*/
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    //设置线程id
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }
    //获取用户id
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}

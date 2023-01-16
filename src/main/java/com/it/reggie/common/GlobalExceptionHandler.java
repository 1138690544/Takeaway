package com.it.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

/*全局异常处理*/
//拦截类上加RestController和Controller注解的
@ControllerAdvice(annotations = {RestController.class,Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    //处理SQL的异常处理
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());//输出异常信息

        //这个错误是不符合唯一字段 错误信息返回名字＋已存在
        if (ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
           String msg = split[2]+"已存在";
           return  R.error(msg);
        }
        return R.error("未知错误");
    }

    //处理自己的的异常处理
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        //输出异常信息
        return R.error(ex.getMessage());
    }
}

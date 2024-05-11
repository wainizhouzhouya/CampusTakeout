package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获用户名相同异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result SQLIntegrityConstraintViolationExceptionHandler(SQLIntegrityConstraintViolationException ex){
        //Duplicate entry 'zhangqi' for key 'idx_username'
        String message=ex.getMessage();
        //如果信息异常信息包含该信息，说明是username重复
        if(message.contains("Duplicate entry")){
            //对异常信息进行切割，并获取用户名
            String[] split = message.split(" ");
            String username = split[2];
            String msg=username+ MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{//未知错误
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}

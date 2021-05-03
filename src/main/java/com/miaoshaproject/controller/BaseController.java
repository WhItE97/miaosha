package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";

    //定义exceptionhandler解决未被controller层吸收的exception
    //对我们实现的web应用：controller层就是我们业务处理的最后一道关口，所以必须要在这里解决掉异常！（类似spring的钩子类）
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody //handlerException只能返回页面路径！无法返回responsebody对应的VO类形式
    public Object handlerException(HttpServletRequest request, Exception ex){
        //定义一个通用返回类型CommonReturnType处理
        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setStatus("fail");
        Map<String,Object> responseData = new HashMap<>();
        //【final version：考虑鲁棒性->exception可能不是businessexception】
        if(ex instanceof BusinessException){
            BusinessException businessException = (BusinessException) ex;
            //        commonReturnType.setData(ex);
            //以上直接把所有exception信息都set进去了，不是我们要的效果；通过把ex强转回businessexception对象，只获取我们要的字段（errcode和errmsg）！
            responseData.put("errCode",businessException.getErrCode());
            responseData.put("errMsg",businessException.getErrMsg());
        }
        else{//如果exception并非businessexception，则把errCode和errMsg写死（在enumBusinessError中新增一个通用的“未知错误”）
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }
        commonReturnType.setData(responseData);
        return commonReturnType;
    }
}

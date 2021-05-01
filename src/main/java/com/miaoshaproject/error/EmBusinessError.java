package com.miaoshaproject.error;

import org.springframework.web.bind.annotation.RequestMapping;

//java的枚举enum其实也是一个类，可以有自己的成员变量
public enum EmBusinessError implements CommonError{
    //通用错误类型00001
    PARAMETER_VALIDATION_ERROR(00001,"参数不合法"),//这个只是default通用，它可以在下面的setErrMsg中通过定制化，进行改动

    //10000开头：用户信息相关错误定义
    USER_NOT_EXIST(10001,"用户不存在")
    ;

    private int errCode;
    private String errMsg;
    //实现有参构造函数
    private EmBusinessError(int errCode,String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
package com.miaoshaproject.error;

//【用到的设计模式：包装器业务异常类实现 】
public class BusinessException extends Exception implements CommonError{

    private CommonError commonError;

    //1.构造方法：直接接收EmBusinessException的传参 用于构造业务异常
    public BusinessException(CommonError commonError){
        super();//别忘了调父类Exception的super！
        this.commonError = commonError;
    }

    //2.接收自定义errMsg的方式 构造业务异常
    public BusinessException(CommonError commonError,String errMsg){
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }

    @Override
    public int getErrCode() {
        return this.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}

package com.miaoshaproject.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

//用于存放“参数校验”结果
public class ValidationResult {
    //1.校验结果是否有错
    private boolean hasErrors = false;

    //2.存放错误信息的Map
    private Map<String,String> errorMsgMap = new HashMap<>();

    //3.生成getter/setter方法
    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErrorMsgMap() {
        return errorMsgMap;
    }

    public void setErrorMsgMap(Map<String, String> errorMsgMap) {
        this.errorMsgMap = errorMsgMap;
    }

    //4.实现通用的通过格式化字符串信息获取错误结果的msg方法
    public String getErrMsg(){
        //"err1,err2..."
        return StringUtils.join(errorMsgMap.values().toArray(),",");
    }

}

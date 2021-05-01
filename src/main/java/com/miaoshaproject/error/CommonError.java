package com.miaoshaproject.error;

import org.springframework.web.bind.annotation.RequestMapping;

public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}

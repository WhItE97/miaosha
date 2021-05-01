package com.miaoshaproject.response;

import javax.naming.event.ObjectChangeListener;

public class CommonReturnType {
    //表明：对应请求的返回处理结果——>success或fail
    private String status;

    //若status==success：则data内返回前端需要的json数据
    //若status==fail：则data内使用通用的错误码格式
    private Object data;

    //定义一个通用的 创建方法
    //下面这个是：没带状态码——>默认success
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    //通用的创建方法！
    public static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

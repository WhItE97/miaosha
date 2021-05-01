package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.event.ObjectChangeListener;
import javax.servlet.http.HttpServletRequest;
import java.net.BindException;

@Controller("user") //用于spring扫描；该controller的name就是user
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的用户信息不存在
        if(userModel==null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
            //但这样单纯的抛出异常，只会抛到tomcat的容器层，tomcat容器层只能返回一个500 error
            //所以我们需要 拦截掉tomcat的异常处理方式，从而实现异常处理——>springboot提供【定义exceptionhandler，解决未被controller层吸收的exception（见下方）】
        }

        UserVO userVO = convertFromModel(userModel);

        //返回通用对象！
        return CommonReturnType.create(userVO);
    }

    //将核心领域模型Model转化为可供前端使用的View Object
    private UserVO convertFromModel(UserModel userModel){
        if(userModel ==null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

    //定义exceptionhandler解决未被controller层吸收的exception
    //对我们实现的web应用：controller层就是我们业务处理的最后一道关口，所以必须要在这里解决掉异常！（类似spring的钩子类）
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody //handlerException只能返回页面路径！无法返回responsebody对应的VO类形式
    public Object handlerException(HttpServletRequest request,Exception ex){
        //定义一个通用返回类型CommonReturnType处理
//        CommonReturnType commonReturnType = new CommonReturnType();
//        commonReturnType.setStatus("fail");
//        commonReturnType.setData(ex);
//        return commonReturnType;
        return null;
    }
}

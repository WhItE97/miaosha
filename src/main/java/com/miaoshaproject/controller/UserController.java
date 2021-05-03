package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.impl.UserServiceImpl;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.naming.event.ObjectChangeListener;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user") //用于spring扫描；该controller的name就是user
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;//用于获取session并把telphone和otpcode与session进行绑定

    //用户登录接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //1.入参校验
        if(StringUtils.isEmpty(telphone)||
        StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //2.用户登录service：校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telphone,this.EncodeByMd5(password));

        //3.如果登录没有异常—>将登录凭证加入到用户登陆成功的session内（一般登录凭证：token等，不会使用用户的session，之后会用分布式session解决分布式下的用户登录）
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);
    }

    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Byte gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //1.验证手机号 和 对应otpCode是否相符
        //在【获取otp短信接口】中把otpcode set进了session，现在从session中取出来比较
        //【虽然指定了@CrossOrigin这样的跨域请求的接收;但是对跨域请求对ajax来说是无法做到共享的（这里的session跨域后无法共享！所以这里get到的inSessionOtpcode是null）】
        //【解决方法：
        // 1.在后端：@CrossOrigin中配置allowCredentials（允许客户端携带验证信息如cookie之类的，以便跨域请求携带信息default=false）="true",allowHeaders（设置允许的请求方法）="*"
        // 2.在前端：getotp和register都需要设置xhrFields授信后使得跨域session共享！
        // 】
        String inSessionOtpCode = (String)this.httpServletRequest.getSession().getAttribute(telphone);
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){//字符串判等，库内工具已经实现好了
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }

        //2.用户注册流程
        //需要有一个service去处理对应的用户注册请求（定义在UserService中）
        UserModel userModel = new UserModel();
        userModel.setTelphone(telphone);
        userModel.setName(name);
        userModel.setGender(gender);
        userModel.setAge(age);
        userModel.setRegisterMode("byphone");
//        userModel.setEncrptPassword(MD5Encoder.encode(password.getBytes()));//【因为密码是明文，所以采用MD5加密！——>java自带的MD5仅支持16位，所以这里需要自己实现一个md5加密】
        //使用自定义的md5替换掉java自带的
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.create(null);//status=success,data=null;即返回一个注册成功
    }

    //java自带的MD5仅支持16位，所以自己实现一个md5对密码加密的方法
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //1.确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //2.加密密码字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));//使用utf-8获取str的bytes，然后使用md5的digest方法对该串进行encode
        return newstr;
    }

    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        //1.按照一定规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //2.将OTP验证码和对应用户的手机号关联
        //【<k=telphone,v=otpcode>容易想到redis实现（用户反复点击getotp code的时候，redis会做反复覆盖，保证kv最新的对应关系）】
        //这里暂时不用redis；先通过httpsession的方式把用户的httpsession和对应的telphone以及otpcode做绑定
        //【ATT1:tomcat是通过concurrentHashMap保存session的 Map<String,Session> sessions = new ConcurrentHashMap<String,Session>】
        //【ATT2:session对象可以保存会话的各种属性，如：session.setAttribute("username",username);可以保存许多属性，而session对象的attributes也是通过Map实现的！Map<String,Object> attributes = new ConcurrentHashMap<String,Object>】
        httpServletRequest.getSession().setAttribute(telphone,otpCode);

        //3.将OTP验证码通过短信通道发给用户（需要买第三方服务，通过HTTP POST发给对应用户；这里暂时选择控制台打印实现[实际中一般用log4j]）
        System.out.println("telphone="+telphone+",otpCode="+otpCode);

        return CommonReturnType.create(null);
    }

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

}

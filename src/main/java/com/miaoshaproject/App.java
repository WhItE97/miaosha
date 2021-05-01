package com.miaoshaproject;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = {"com.miaoshaproject"}) //将App的启动类托管给spring，可以支持自动化配置的一个bean；并且能够开启整个工程类的基于springboot的配置
@RestController //==@Controller(没有@Controller的浏览器是无法访问的)+@ResponseBody:方法的返回值直接以指定的格式写入Http response body中，而不是解析为跳转路径
@MapperScan("com.miaoshaproject.dao") //把dao存放目录配置进mapperscan
public class App 
{
    @Resource
    private UserDOMapper userDOMapper;

    @RequestMapping("/") //配置URL映射为根路径
    public String home(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(2);
        if(userDO==null){
            return "不存在用户";
        }
        else{
            return userDO.getName();
        }
    }

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class,args);//启动springboot项目
    }
}

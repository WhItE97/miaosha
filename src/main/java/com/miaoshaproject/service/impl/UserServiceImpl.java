package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service //注明service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userDOMapper获取到对应的用户dataobject——>这里之所以不直接返回User data object：【企业级思想】
        //spring mvc的model分为3层：1.最简单的Data Object【与数据库完全一一映射】2.service层不能把Data Object直接返回给想要这个service的服务，即【service层必须有一个model的概念】,所以返回值为service层的usermodel而非最原始的user data object
        //3.
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO==null){
            return null;
        }
        //userPasswordDO的查询不是靠自己的id查，而是靠userid查，所以需要去改造UserPasswordDOMapper.xml!
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);

        //因为返回值是service层的model类型(UserModel)，所以要先去实现一个类型转换的函数
        return convertFromDataObject(userDO,userPasswordDO);
    }

    //func：将userDO和userPasswordDO组装成返回给service层用的UserModel
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        //base:判空
        if(userDO==null){
            return null;
        }

        UserModel userModel = new UserModel();
        //自动copy对应属性
        BeanUtils.copyProperties(userDO,userModel);
        //userpassword不能再BeanUtils.copyProperties了！因为有重复的id字段，所以setter进去
        if(userPasswordDO!=null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;//【ATT.这里把领域模型userModel返回给前端，这里面包含了密码字段！所以前端不应全部展示，所以需要在controller层再加一个View Object】
    }
}

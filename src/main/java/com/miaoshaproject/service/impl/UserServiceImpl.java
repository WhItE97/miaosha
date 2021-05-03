package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional //把对userinfo和userpassword两张表的插入流程绑定为事务！
    public void register(UserModel userModel) throws BusinessException {
        if(userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //开始校验
        if(StringUtils.isEmpty(userModel.getName())
        ||userModel.getGender()==null
        ||userModel.getAge()==null
        ||StringUtils.isEmpty(userModel.getTelphone())){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //如果通过检验，则需要把对应用户信息注册进数据库
        //把Model转换为Data Object，以便写入数据库
        //1.实现一个UserModel转UserDO的方法,因为dao【data object access】对象访问层认得是Data Object
        UserDO userDO = convertFromModel(userModel);
        //考虑到telphone的unique索引后，进行异常的捕获
        try{
            //2.调用insertSelective【不调用insert是因为：selective在插入数据的时候，空值将插入数据库中设置的默认值！而单纯的insert会用空值覆盖！】
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已重复注册！");
        }
        userModel.setId(userDO.getId());
        //还需要写入UserPassword Table
        //1.同样的，首先要实现一个把passwordmodel【ATT。Model=userDO+passwordDO，所以这里依旧是userDO】转换成DO的方法
        UserPasswordDO userPasswordDO = convertFromPasswordModel(userModel);
        //2.插入进数据库
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return ;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //1.通过用户的手机获取用户信息——>userDOMapper里没有该select方法，需要自己编写！
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        //1.1.如果该手机号获取到的用户不存在，throw exception
        if(userDO==null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAILT);
        }
        //1.2.通过userdo中的id去取userpassword中的encryptpassword
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);

        //2.比对用户信息内加密的密码是否和传输进来的密码相匹配
        if(!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAILT);
        }

        //3.如果相等，直接return
        return userModel;
    }

    //UserModel中的password——>password DO
    public UserPasswordDO convertFromPasswordModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());//存在pro：get到的id=null
        return userPasswordDO;
    }
    //UserModel——>UserDO
    public UserDO convertFromModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
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

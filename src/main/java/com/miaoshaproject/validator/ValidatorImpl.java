package com.miaoshaproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {

    //该Validator：通过javax定义的一套接口，实现了一个validator工具
    private Validator validator;

    //spring bean初始化完成后，会回调ValidatorImpl的afterPropertiesSet()方法
    @Override
    public void afterPropertiesSet() throws Exception {
        //通过工厂的初始化方式 实例化 hibernate validator;有了这个校验器后，就可以去实现校验方法并返回校验结果
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    //实现校验方法
    public ValidationResult validate(Object bean){
        final ValidationResult result = new ValidationResult();
        //如果对应的bean里的某些参数的规则违背了对应validation定义的annotation的话，set中就会得到“违背了哪些”的值
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size()>0){
            //有错误
            result.setHasErrors(true);
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();//get到错误信息
                String propertyName = constraintViolation.getPropertyPath().toString();//get到 错的是哪一个字段
                result.getErrorMsgMap().put(propertyName,errMsg);//放回errMsg Map
            });
        }
        return result;
    }
}

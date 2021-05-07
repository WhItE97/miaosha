package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderService {
    //创建订单
    //【添加秒杀模块后】
    //1.【better】通过前端url上传秒杀活动id，然后在下单接口中校验对应id是否属于对应商品，并且活动已经开始
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动，如果存在进行中的秒杀活动，则以秒杀价格下单
    //原因：（1）一个商品可能同时存在多个活动，我们需要通过前端用户的访问路径确定他是哪个活动（2）订单接口中还要判断秒杀活动的领域模型的话，相当于无活动的商品也需要去查一次活动信息，浪费时间
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId,Integer amount) throws BusinessException;
}

package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException {

        //1.校验下单状态：（用户是否合法？商品是否存在？购买数量是否合法？等）
        UserModel userModel = userService.getUserById(userId);
        if(userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }

        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }

        if(amount<=0||amount>99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品数量不合法");
        }

        //【加入秒杀模块后】校验活动信息
        if(promoId!=null){//如果没有promo，则默认平销
            //(1)校验对应活动是否存在这个适用商品
            if(promoId.intValue()!=itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不符合");
            }
            //(2)校验活动是否在进行中！
            else if (itemModel.getPromoModel().getStatus().intValue()!=2){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动尚未开始");
            }
        }

        //2.采用【落单减库存】——>电商一般分2种：
        //（1）落单减库存（调用订单的createOrder下单之前，先把amount这个数量的库存锁定给这个用户/订单使用;如果锁库存失败：库存不够了——>下单失败！）
        //（2）支付减库存（落单的时候去看一下库存，不锁，等到支付成功才真正减库存——>无法保证支付成功之后对应amount的库存还有——>可能超卖(适用于商家保证交易率：有些退货的)！）
        //落单减库存的实现：在ItemService中新增一个扣减库存的service
        boolean decrease = itemService.decreaseStock(itemId,amount);
        if(!decrease){//减库存失败
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);

        if(promoId!=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }
        else{
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setPromoId(promoId);

        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        orderModel.setId(generateOrderNo());

        //3.2.ATT.这里的order表的主键是订单号，因为有特定长度和含义，所以没有采取自增！还需要按照规则先为其生成订单号！
        OrderDO orderDO = this.convertDOFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //4.订单入库之后还要对对应商品item修改销量！
        itemService.increaseSales(itemId,amount);

        //5.返回前端
        return orderModel;
    }

    //这里def 16位订单号
    @Transactional(propagation = Propagation.REQUIRES_NEW)//无论代码是否在事务中，都会开启一个新事务，并且这段代码执行完成之后把新事物提交掉
    String generateOrderNo(){
        StringBuilder sb = new StringBuilder();
        //1.前8位：时间信息年月日
        LocalDateTime now = LocalDateTime.now();
        sb.append(now.format(DateTimeFormatter.ISO_DATE).replace("-",""));

        //2.中间6位：自增序列
        //（1）获取当前sequence
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        //（2）获取sequence value
        int sequence = 0;
        sequence = sequenceDO.getCurrentValue();
        //（3）update数据库中的current value
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        //（4）将current value拼接满6位
        String sequenceStr = String.valueOf(sequence);
        for(int i=0;i<6-sequenceStr.length();i++){
            sb.append("0");
        }
        sb.append(sequenceStr);

        //3.最后两位：分库分表位！（用于大量数据、多个数据库多张表的时候，来判断当前数据在哪个库哪个表）（实际生产中会用到，这里暂时将其写死）
        sb.append("00");
        return sb.toString();
    }

    private OrderDO convertDOFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        //数据库中两个price都是double，而model里都是bigdecimal，所以copy是copy不了的！
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}

package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //1.获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        //2.将dataobjeect——>model
        PromoModel promoModel = this.convertModelFromPromoDO(promoDO);
        if(promoModel==null){
            return null;
        }

        //3.According to当前时间;判断：秒杀活动是否1.即将开始or2.正在进行 or3.已经结束;并set进Model
        DateTime now = new DateTime();
        if (promoModel.getStartTime().isAfterNow()){//start time比现在晚，说明还没开始
            promoModel.setStatus(1);
        }else if(promoModel.getEndTime().isBeforeNow()){
            promoModel.setStatus(3);//已经结束
        }else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    private PromoModel convertModelFromPromoDO(PromoDO promoDO){
        if(promoDO==null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartTime(new DateTime(promoDO.getStartTime()));//【ATT.Model里的date是java类型的日期！所以需要进行转换！】
        promoModel.setEndTime(new DateTime(promoDO.getEndTime()));
        return promoModel;
    }
}

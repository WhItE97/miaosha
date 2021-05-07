package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

public interface PromoService {
    //根据itemId查询 即将进行的 or 正在进行的 秒杀活动！
    PromoModel getPromoByItemId(Integer itemId);
}

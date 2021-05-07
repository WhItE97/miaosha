package com.miaoshaproject.service.model;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class PromoModel {
    private Integer id;
    //秒杀活动名称
    private String promoName;
    //秒杀活动开始时间
    private DateTime startTime;

    //手动新增：秒杀活动结束时间
    private DateTime endTime;

    //秒杀活动适用商品（简单起见：一个活动仅适用于一个商品）
    private Integer itemId;
    //秒杀活动的商品价格
    private BigDecimal promoItemPrice;

    //添加一个【与数据库无关】的秒杀活动状态
    //秒杀活动状态：1表示还未开始，2表示进行中，3表示已结束（通过now time和startTime以及endTime字段判断）
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

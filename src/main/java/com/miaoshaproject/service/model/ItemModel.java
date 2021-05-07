package com.miaoshaproject.service.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ItemModel {
    private Integer id;
    //商品名
    @NotBlank(message = "商品名称不能为空")
    private String title;
    //商品价格
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格必须大于0")
    private BigDecimal price;
    //商品库存
    @NotNull(message = "库存不能不填")
    private Integer stock;
    //商品描述
    @NotBlank(message = "商品描述不能为空")
    private String description;
    //商品销量【非传入的，所以不在入参校验范围内！】
    private Integer sales;
    //商品图片url
    @NotBlank(message = "商品图片不能为空")
    private String imgUrl;

    //使用【聚合模型（ItemModel中嵌套PromoModel）】来存放商品对应的秒杀活动信息
    //if promoModel！=null——>该商品还有【未结束（即将开始or正在进行）】的秒杀活动
    private PromoModel promoModel;

    public PromoModel getPromoModel() {
        return promoModel;
    }

    public void setPromoModel(PromoModel promoModel) {
        this.promoModel = promoModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}

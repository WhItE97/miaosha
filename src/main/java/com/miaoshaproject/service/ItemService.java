package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表浏览
    List<ItemModel> listItem();
    //商品详情浏览
    ItemModel getItemById(Integer id);

    //用于order的落单减库存的 库存扣减
    boolean decreaseStock(Integer itemId,Integer amount) throws BusinessException;

    //下单成功后增加商品销量
    void increaseSales(Integer itemId,Integer amount) throws BusinessException;
}

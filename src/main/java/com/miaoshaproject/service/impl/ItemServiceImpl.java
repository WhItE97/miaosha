package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //1.入参校验(validator实现)
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        //2.model转DO
        //包括model转成：1.ItemDO2.ItemStockDO（对应数据库里两张表&&两次插入）
        ItemDO itemDO = this.convertDOFromItemModel(itemModel);

        //3.插入数据库
        itemDOMapper.insertSelective(itemDO);
        //ATT：item表插入成功后itemDO中的id字段将得到设置，所以需要set进itemModel中
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);//【ATT：这一步Model转DO必须在这里！因为在执行item表的“插入”之前itemModel里还没有id值！】
        itemStockDOMapper.insertSelective(itemStockDO);

        //4.返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    private ItemDO convertDOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        //【ATT：ItemModel对应的price是bigdecimal，因为double存在精度问题，ItemDO中的是double】
        //BeanUtils.copy不会copy类型不一样的对象！所以要自己手动set price
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        //还需要set item_id
        itemStockDO.setItemId(itemModel.getId());
        return itemStockDO;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        //【java8通过stream将集合转换为“流”，从而对集合中的每个元素进行一系列并行or串行的流水线操作】
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {//map(T->R):将流中的每一个元素T映射成R
            //要把itemDO组装成itemModel，还需要取itemStockDO进行组装
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());//通过.collect(Collectors.toList()将处理后的每个元素又拼接回一个新的list)
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO==null){
            return null;
        }
        //否则需要操作拿到库存
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //将data object聚合、转换成——>model（以供前端转VO使用）
        return convertModelFromDataObject(itemDO,itemStockDO);
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}

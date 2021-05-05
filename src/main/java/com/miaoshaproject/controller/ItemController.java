package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController{

    @Autowired
    private ItemService itemService;

    //1.创建商品的controller
    @RequestMapping("/create")
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price")BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {
        //封装service请求用来创建商品
        //(1)创建itemModel对象
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        //(2)调用service层实现创建
        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        //(3)把创建成功的商品 的信息 转换成VO后，以data的形式存于CommonReturnType后返回给前端
        ItemVO itemVO = this.convertVOFromModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel){
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        return itemVO;
    }

    //2.商品列表页面浏览
    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType listItem(){
        //(1)通过service获得List<ItemModel>（本来sql获得的是DO，但是我们在service层进行了组装，得到了Model）
        List<ItemModel> itemModelList = itemService.listItem();

        //(2)通过stream() API将Model map(Model->VO)成 给前端的VO
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel->{
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        //(3)返回给前端
        return CommonReturnType.create(itemVOList);
    }

    //3.【自己实现的get】
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getItem(Integer id){
        //(1)通过id获取到商品信息
        ItemModel itemModel = itemService.getItemById(id);

        //(2)把itemModel转为itemVO
        ItemVO itemVO = this.convertVOFromModel(itemModel);

        //(3)返回给前端
        return CommonReturnType.create(itemVO);
    }
}

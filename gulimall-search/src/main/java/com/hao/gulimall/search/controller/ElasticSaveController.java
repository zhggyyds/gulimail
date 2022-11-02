package com.hao.gulimall.search.controller;

import com.hao.common.exception.BizCodeEnume;
import com.hao.common.to.SkuHasStockVo;
import com.hao.common.to.es.SkuEsModel;
import com.hao.common.utils.R;
import com.hao.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.search.controller
 * @Description:
 * @date 2022/9/19 14:53
 **/

@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        boolean result = false;// 是否执行成功
        try {
            result = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            // es客户端连接失败
            log.error("ElasticSaveController商品上架错误：{}", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!result) {
            return R.ok();// 执行成功
        } else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}

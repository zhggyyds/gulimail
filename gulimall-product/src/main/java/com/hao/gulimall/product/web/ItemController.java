package com.hao.gulimall.product.web;

import com.hao.gulimall.product.service.SkuInfoService;
import com.hao.gulimall.product.vo.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.web
 * @Description:
 * @date 2022/11/9 13:00
 **/
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;
    /*
     * @description 展示当前sku详情
     * @date 2022/11/9 13:02
     * @param null
     * @return null
     */
    @GetMapping("/{skuId}.html")
    public String itemPage(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVO itemVo =  skuInfoService.itemInfo(skuId);
        model.addAttribute("item",itemVo);
        return "item"; // 请求转发 + model 前端模版引擎可直接使用
    }
}

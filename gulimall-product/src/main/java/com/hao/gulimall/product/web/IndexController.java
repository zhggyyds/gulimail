package com.hao.gulimall.product.web;

import com.hao.gulimall.product.entity.CategoryEntity;
import com.hao.gulimall.product.service.CategoryService;
import com.hao.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.web
 * @Description:
 * @date 2022/9/23 15:53
 **/
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {

        // 查询所有1级分类
        List<CategoryEntity> categoryEntitys = categoryService.getLevel1Categorys();

        //请求转发配合model使用
        model.addAttribute("categorys", categoryEntitys);

        // 解析器自动拼装classpath:/templates/  + index +  .html =》 classpath:/templates/index.html
        // classpath表示类路径，编译前是resources文件夹，编译后resources文件夹内的文件会统一存放至classes文件夹内
        return "index";//请求转发
    }

    @ResponseBody
    @RequestMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatlogJson() throws InterruptedException {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }
}
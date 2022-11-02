package com.hao.gulimall.search.controller;

import com.hao.gulimall.search.service.MallSearchService;
import com.hao.gulimall.search.vo.SearchParam;
import com.hao.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.search.controller
 * @Description:
 * @date 2022/10/26 15:27
 **/

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping({"/list.html"})
    public ModelAndView listPage(SearchParam param , ModelAndView modelAndView , HttpServletRequest request){
        param.set_queryString(request.getQueryString()); // 从原生的HttpServletRequest中取出url中的 ?后的部分
        SearchResult result = mallSearchService.search(param);
        modelAndView.addObject("result",result); // 此处设置的名字在模版渲染thymeleaf和前端中用于取值
        modelAndView.setViewName("list");
        return modelAndView;
    }
}

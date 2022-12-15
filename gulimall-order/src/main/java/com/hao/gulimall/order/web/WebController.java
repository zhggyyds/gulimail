package com.hao.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.order.web
 * @Description:
 * @date 2022/11/29 09:33
 **/

@Controller
public class WebController {

    @GetMapping("/{page}.html")
    public String returnPage(@PathVariable("page") String page){
        return page;
    }

}

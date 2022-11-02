package com.hao.gulimall.search.service;

import com.hao.gulimall.search.vo.SearchParam;
import com.hao.gulimall.search.vo.SearchResult;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.search.service
 * @Description:
 * @date 2022/10/27 15:10
 **/
public interface MallSearchService {

    /**
     * 检索商品
     */
    SearchResult search(SearchParam param);
}

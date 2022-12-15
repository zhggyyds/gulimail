package com.hao.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hao.common.constant.es.EsConstant;
import com.hao.common.to.es.SkuEsModel;
import com.hao.gulimall.search.config.GuliEsSearchConfig;
import com.hao.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.search.service.impl
 * @Description:
 * @date 2022/9/19 14:57
 **/

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient client;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 1.给ES建立一个索引 product
        BulkRequest bulkRequest = new BulkRequest();
        // 2.构造保存请求
        for (SkuEsModel esModel : skuEsModels) {
            // 设置索引
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            // 设置索引id
            indexRequest.id(esModel.getSkuId().toString());
            indexRequest.source(JSONObject.toJSONString(esModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        // bulk批量保存
        BulkResponse bulk = client.bulk(bulkRequest, GuliEsSearchConfig.COMMON_OPTIONS);
        // 上传商品到ES是否有错误
        boolean hasFailures = bulk.hasFailures();
        if(hasFailures){
            List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId()).collect(Collectors.toList());
            log.error("商品上架错误：{}",collect);
        }
        return hasFailures;
    }
}

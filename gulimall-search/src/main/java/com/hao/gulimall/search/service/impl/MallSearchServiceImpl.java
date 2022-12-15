package com.hao.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hao.common.constant.es.EsConstant;
import com.hao.common.to.es.SkuEsModel;
import com.hao.common.utils.R;
import com.hao.gulimall.search.config.GuliEsSearchConfig;
import com.hao.gulimall.search.feign.ProductFeignService;
import com.hao.gulimall.search.service.MallSearchService;
import com.hao.gulimall.search.vo.AttrResponseVo;
import com.hao.gulimall.search.vo.BrandFeignVo;
import com.hao.gulimall.search.vo.SearchParam;
import com.hao.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hao.common.constant.es.EsConstant.PRODUCT_INDEX;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.search.service.impl
 * @Description:
 * @date 2022/10/27 15:11
 **/

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    /**
     * 检索商品
     */
    @Override
    public SearchResult search(SearchParam param) {
        // 1.准备检索请求，动态构建DSL语句
        SearchRequest request = buildSearchRequest(param);
        SearchResult result = null;
        try {
            SearchResponse searchResponse = client.search(request, GuliEsSearchConfig.COMMON_OPTIONS);
            result = buildSearchResult(searchResponse,param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 动态构建检索请求
     * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮 ,聚合分析
     * 【分析当前所有可选的规格、分类、品牌】
     */
    private SearchRequest buildSearchRequest(SearchParam param){
        // 构建SourceBuilder【构建DSL语句用于在ES服务器进行搜索使用】
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 动态构建查询DSL语句【参照dsl.json分析包装步骤】
        // 查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
        // 1.构建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1.构建must（模糊查询）
        if (!StringUtils.isEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        // 1.2.构建filter（过滤）
        // 1.2.1.三级分类
        if (!StringUtils.isEmpty(param.getCatalog3Id())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        // 1.2.2.品牌id
        if (!CollectionUtils.isEmpty(param.getBrandId())){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }
        // 1.2.3.属性
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            for (String attr : param.getAttrs()) {
                // attrs=1_白色:蓝色
                String[] attrs = attr.split("_");
                String attrId = attrs[0];// 1
                String[] attrValues = attrs[1].split(":");// ["白色","蓝色"]
                BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("attrs.attrId", attrId))
                        .must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 遍历每一个属性生成一个NestedQuery
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQueryBuilder, ScoreMode.None);// ScoreMode.None：不参与评分
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }
        // 1.2.4.库存
        if (param.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("hasStock",param.getHasStock() == 1));
        }
        // 1.2.5.价格区间【多种区间传参方式：1_500/_500/500_】
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            String[] prices = param.getSkuPrice().split("_");
            if (prices.length == 2) {
                // 1_500
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(prices[0]).lte(prices[1]));
            } else if (prices.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    // _500
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").lte(prices[0]));
                } else if (param.getSkuPrice().endsWith("_")) {
                    // 500_
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(prices[0]));
                }
            }
        }

        // 1.3.封装bool【bool封装了模糊查询+过滤】
        builder.query(boolQueryBuilder);

        // 1.4.排序，分页，高亮
        // 1.4.1.排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] sorts = param.getSort().split("_");
            builder.sort(sorts[0], sorts[1].toLowerCase().equals(SortOrder.ASC.toString()) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 1.4.2.分页
        builder.from(EsConstant.PRODUCT_PAGESIZE * (param.getPageNum() - 1));//从第几条数据起
        builder.size(EsConstant.PRODUCT_PAGESIZE);//总共显示几条

        // 1.4.3.高亮
        if(!StringUtils.isEmpty(param.getKeyword())){
            // 模糊匹配才需要高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        // 1.5.聚合分析【分析被查寻到的规格、分类、品牌】用于在导航栏进行点击选择
        // 1.5.1.品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 品牌聚合子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        builder.aggregation(brandAgg);

        // 1.5.2.分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        builder.aggregation(catalogAgg);

        // 1.5.3.属性嵌套聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 属性子聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        attr_agg.subAggregation(attrIdAgg);
        builder.aggregation(attr_agg);

        System.out.println("构建的DSL语句: " + builder.toString());
        return new SearchRequest(new String[]{PRODUCT_INDEX}, builder);
    }


    /**
     * 封装检索结果 - 返回给前端的数据
     * 1、返回所有查询到的商品
     * 2、分页信息
     * 3、当前所有商品涉及到的所有属性信息
     * 4、当前所有商品涉及到的所有品牌信息
     * 5、当前所有商品涉及到的所有分类信息
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        // ==========从命中结果获取===========hits
        SearchHits searchHits = response.getHits();// 获取命中结果

        // 1.返回所有查询到的商品
        List<SkuEsModel> products = new ArrayList<>();
        SearchHit[] hits = searchHits.getHits();
        if (!ArrayUtils.isEmpty(hits) && hits.length >0){
            for (SearchHit hit : hits) {
                SkuEsModel esModel = JSON.parseObject(hit.getSourceAsString(),SkuEsModel.class);//使用json工具类封装成对象
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    // 关键字不为空，返回结果包含高亮信息
                    // 高亮信息
                    String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].toString();
                    esModel.setSkuTitle(skuTitle);
                }
                products.add(esModel);
            }
        }
        result.setProducts(products);


        // 2.分页信息
        Long total  = searchHits.getTotalHits().value;
        result.setTotal(total);
        result.setPageNum(param.getPageNum());
        long totalPages = total % EsConstant.PRODUCT_PAGESIZE == 0? total/EsConstant.PRODUCT_PAGESIZE : total/EsConstant.PRODUCT_PAGESIZE+1;
        result.setTotalPages((int) totalPages);

        // ==========从聚合结果获取===========aggregations
        Aggregations aggregations = response.getAggregations();// 获取聚合结果
        // 3.当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());// 封装属性ID
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());// 封装属性名
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            // 封装属性值
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item ->
                item.getKeyAsString()).collect(Collectors.toList());

            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 4.当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brands = new ArrayList<>();
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            // 封装品牌ID
            SearchResult.BrandVo brand = new SearchResult.BrandVo();
            brand.setBrandId(bucket.getKeyAsNumber().longValue());
            // 封装品牌名
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            brand.setBrandName(brandNameAgg.getBuckets().get(0).getKeyAsString());
            // 封装品牌图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            brand.setBrandImg(brandImgAgg.getBuckets().get(0).getKeyAsString());
            brands.add(brand);
        }
        result.setBrands(brands);

        // 5.当前所有商品涉及到的所有分类信息
        List<SearchResult.CatalogVo> catalogs = new ArrayList<>();
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            // 封装分类ID
            SearchResult.CatalogVo catalog = new SearchResult.CatalogVo();
            catalog.setCatalogId(bucket.getKeyAsNumber().longValue());
            // 封装分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");// 子聚合
            catalog.setCatalogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());
            catalogs.add(catalog);
        }
        result.setCatalogs(catalogs);

        // 6.前端分页 each使用
        List<Integer> navs = new ArrayList<>();
        for(int i = 1;i<= result.getTotalPages();i++){
            navs.add(i);
        }
        result.setPageNavs(navs);

        // 7.面包屑导航封装
        // 7.1属性
        if(!CollectionUtils.isEmpty(param.getAttrs()) && param.getAttrs().size()>0){
            List<SearchResult.NavVo> breadCrumb = param.getAttrs().stream().map(item -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] attrSplit = item.split("_");
                navVo.setNavValue(attrSplit[1]);
                R r = productFeignService.attrInfo(Long.parseLong(attrSplit[0]));
                result.getAttrIds().add(Long.parseLong(attrSplit[0])); // 收集被选中的属性id，用于面包屑导航中被点击的div消失
                if(r.getCode() == 0){
                    AttrResponseVo attr = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attr.getAttrName());
                }else {
                    navVo.setNavName(attrSplit[0]);
                }

                String replace = getReplace(param.get_queryString(), item,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(breadCrumb);
        }
        //7.2品牌
        if(!CollectionUtils.isEmpty(param.getBrandId())){
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");

            R r = productFeignService.getBrandByIds(param.getBrandId());
            String replacedQueryString = param.get_queryString();
            if (r.getCode() == 0){
                // 远程服务成功
                List<BrandFeignVo> brandFeignVos = r.getData("brands", new TypeReference<List<BrandFeignVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                for (BrandFeignVo vo : brandFeignVos) {
                    buffer.append(vo.getName()+";");
                    replacedQueryString = getReplace(replacedQueryString, vo.getBrandId()+"","brandId");
                }
                //TODO 品牌可能存在多选，但是前端的面包屑的x只有一个，点击一次x就要将所有品牌都删除才行，在上面的循环中将所有与品牌相关的查询条件都去除后得到最终的replacedQueryString
                navVo.setLink("http://search.gulimall.com/list.html?" + replacedQueryString);
                navVo.setNavValue(buffer.toString());
            }
            List<SearchResult.NavVo> breadCrumb = result.getNavs();
            breadCrumb.add(navVo);
            result.setNavs(breadCrumb);
        }
        return result;
    }

    private String getReplace(String queryString, String item,String key) {
        String encode = null;
        try {
            // 前端的数据在传输到后端过程中经过了编码。所以需要将后端的汉字数据编码后才能完成下面的比对replace
            encode = URLEncoder.encode(item,"UTF-8");
            // 浏览器将空格转义成了%20，差异化处理，否则_queryString与encode匹配失败
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = queryString.replace("&"+key+"="+encode,"");
        //如果当且仅当目前一个搜索属性就用这个处理
        replace = replace.replace(key+"="+encode,"");
        return replace;
    }
}

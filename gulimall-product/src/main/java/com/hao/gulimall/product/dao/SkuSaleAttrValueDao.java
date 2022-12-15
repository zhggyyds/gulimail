package com.hao.gulimall.product.dao;

import com.hao.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hao.gulimall.product.vo.SkuItemSaleAttrVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVO> getSaleAttrBySpuId(Long spuId);

    List<String> getSkuSaleAttrValues(Long skuId);
}

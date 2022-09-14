package com.hao.gulimall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.product.entity.AttrEntity;
import com.hao.gulimall.product.vo.AttrRespVo;
import com.hao.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);


    AttrRespVo getAttr(AttrEntity attr);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelation(Long attrGroupId);

    PageUtils getNoRelations(Map<String, Object> params, Long attrGroupId);
}


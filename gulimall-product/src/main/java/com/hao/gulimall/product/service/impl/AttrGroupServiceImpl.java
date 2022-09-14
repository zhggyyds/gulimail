package com.hao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hao.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.hao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.hao.gulimall.product.entity.AttrEntity;
import com.hao.gulimall.product.service.AttrService;
import com.hao.gulimall.product.vo.AttrAttrGroupVo;
import com.hao.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.product.dao.AttrGroupDao;
import com.hao.gulimall.product.entity.AttrGroupEntity;
import com.hao.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<AttrGroupEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        // wrapper用于查询，里面封装了查询条件
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        // key不为空
        if (!StringUtils.isEmpty(key)) {
            // 关键字查询，id和name中出现带查询的关键字就要返回
            // like是模糊查询， %key%
            wrapper.eq("attr_group_id", key).or().like("attr_group_name", key);
        }
        IPage<AttrGroupEntity> page;

        // 0 表示查询所有的信息
        if (catelogId == 0) {
            // Query可以把map参数封装为IPage，用于传入PageUtils中进行返回
            page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);

        } else {
            // 增加查询的id信息
            wrapper.eq("catelog_id", catelogId);

            // Query可以把map参数和查询条件 封装为IPage，再传入PageUtils中返回给前端
            page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        }
        return new PageUtils(page);
    }

    // 批量删除
    @Override
    public void deleteRelation(AttrAttrGroupVo[] attrAttrGroupVos) {

        // 映射为AttrAttrgroupRelationEntity集合，传入dao方法进行删除
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(attrAttrGroupVos).stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());

        // 自定义一个批量删除方法
        relationDao.deleteBatch(collect);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //1、查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2、查询所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(group -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group,attrsVo);
            // 获得所有相关属性
            List<AttrEntity> attrs = attrService.getRelation(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());

        return collect;
    }

}
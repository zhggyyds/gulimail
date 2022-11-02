package com.hao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hao.common.constant.ProductConstant;
import com.hao.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.hao.gulimall.product.dao.AttrGroupDao;
import com.hao.gulimall.product.dao.CategoryDao;
import com.hao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.hao.gulimall.product.entity.AttrGroupEntity;
import com.hao.gulimall.product.entity.CategoryEntity;
import com.hao.gulimall.product.service.CategoryService;
import com.hao.gulimall.product.vo.AttrRespVo;
import com.hao.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.product.dao.AttrDao;
import com.hao.gulimall.product.entity.AttrEntity;
import com.hao.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }


    @Transactional
    @Override
    /*
     * @description 保存属性
     * @date 2022/10/9 13:15
     * @param attr
     */
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // 属性类型是规格属性并且组id前端有输入才需要将关联关系保存
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }


    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        String key = (String) params.get("key");
        // wrapper用于查询，里面封装了查询条件
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type"
                        , "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        // key不为空
        if (!StringUtils.isEmpty(key)) {
            // 关键字查询，id和name中出现带查询的关键字就要返回
            // like是模糊查询， %key%
            wrapper.eq("attr_id", key).or().like("attr_name", key);
        }

        // 0 表示查询所有的信息
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }

        // Query可以把map参数和查询条件 封装为IPage，再传入PageUtils中返回给前端
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();

        //处理成视图真正要用的数据
        List<AttrRespVo> new_records = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            // 设置分类名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }

            if (attrRespVo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
                // 设置分组名
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            return attrRespVo;
        }).collect(Collectors.toList());

        Collectors.toList();

        pageUtils.setList(new_records);

        return pageUtils;
    }

    @Override
    public AttrRespVo getAttr(AttrEntity attr) {
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attr, attrRespVo);

        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao
                .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", attr.getAttrId()));

        if (relationEntity != null) {
            attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
        }


        Long[] cateLogPath = categoryService.findCateLogPath(attr.getCatelogId());
        if (cateLogPath != null) {
            attrRespVo.setCatelogPath(cateLogPath);
        }

        return attrRespVo;
    }

    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);

        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());

            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao
                    .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attr.getAttrId()));

            if (relationEntity != null) {
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity
                        , new UpdateWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", attrAttrgroupRelationEntity.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }

    }

    @Override
    public List<AttrEntity> getRelation(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));

        List<Long> collect = list.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        return (List<AttrEntity>) this.listByIds(collect);


    }

    @Override
    public PageUtils getNoRelations(Map<String, Object> params, Long attrGroupId) {

        // 获得该分组的所属id
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 找到当前分类的所有属性组
        List<AttrGroupEntity> group = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        // 获得这些属性组的groupId用于筛选
        List<Long> groupIds = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        //找到已经被关联的属性（包括自身已经关联的）
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        // 获得属性id用于筛选
        List<Long> attrIds = relationEntities.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        // 筛选出当前分类下的所有 *规格属性*
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq(
                "attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()).eq("catelog_id",catelogId);

        // 关键字查询
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.eq("attr_id", key).or().like("attr_name", key);
        }

        // 筛选出没有被关联的属性
        if (attrIds != null && attrIds.size() != 0){
            wrapper.notIn("attr_id",attrIds);
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    // 返回可供搜索的属性id
    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return this.baseMapper.getSearchAttrIds(attrIds);
    }


}
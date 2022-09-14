package com.hao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hao.gulimall.product.dao.BrandDao;
import com.hao.gulimall.product.dao.CategoryDao;
import com.hao.gulimall.product.entity.BrandEntity;
import com.hao.gulimall.product.service.BrandService;
import com.hao.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.product.dao.CategoryBrandRelationDao;
import com.hao.gulimall.product.entity.CategoryBrandRelationEntity;
import com.hao.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandDao brandDao;
    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private BrandService brandService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        categoryBrandRelation.setBrandName(brandDao.selectById(brandId).getName());
        categoryBrandRelation.setCatelogName(categoryDao.selectById(catelogId).getName());

        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long id,String name){
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(id);
        categoryBrandRelationEntity.setBrandName(name);

        this.update(categoryBrandRelationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",id));

    }

    @Override
    public void updateCateLog(Long cateId, String name) {
        this.baseMapper.updateCateLog(cateId,name);
    }

    @Override
    public List<BrandEntity> getRelatedBrands(Long catelogId) {
        List<CategoryBrandRelationEntity> relationEntityList = this.baseMapper
                .selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catelogId));

        if (relationEntityList != null && relationEntityList.size() > 0){
            List<BrandEntity> collect = relationEntityList.stream().map(item -> {
                Long brandId = item.getBrandId();
                return brandService.getById(brandId);
            }).collect(Collectors.toList());

            return collect;
        }
        return null;
    }

}
package com.hao.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.alibaba.fastjson.TypeReference;
import com.hao.common.constant.ProductConstant;
import com.hao.common.to.SkuHasStockVo;
import com.hao.common.to.SkuReductionTo;
import com.hao.common.to.SpuBoundTo;
import com.hao.common.to.es.SkuEsModel;
import com.hao.common.utils.R;
import com.hao.gulimall.product.entity.*;
import com.hao.gulimall.product.feign.CouponFeignService;
import com.hao.gulimall.product.feign.SearchFeignService;
import com.hao.gulimall.product.feign.WareFeignService;
import com.hao.gulimall.product.service.*;
import com.hao.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveInfo(SpuSaveVo spuVo) {
        // 保存到 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = spuVo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());// 基本信息保存完毕后，会自动将数据库生成的id录入到spuInfoEntity对象
        descEntity.setDecript(String.join(",", decript)); // 将list集合中的string成员以 , 隔开拼成一个string
        spuInfoDescService.saveSpuInfoDesc(descEntity);


        //3、保存spu的图片集 pms_spu_images
        List<String> images = spuVo.getImages();
        imagesService.saveImages(spuInfoEntity.getId(), images);


        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuVo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);



        //5、保存当前spu对应的所有sku信息；

        List<Skus> skus = spuVo.getSkus();
        if (skus != null && skus.size() > 0) {
            // foreach + lambda 处理复杂的集合循环比较好，看起来也很方便
            skus.forEach(item -> {
                String defaultImg = ""; // 获得默认展示的图片
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }

                // 设置sku的基本信息
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、保存sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                // 获得skuid
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());

                //5.2）、保存sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());

                //5.3）、保存sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4）、sku的优惠、满减，会员价信息；gulimall_sms->sms_sku_ladder-> sms_sku_full_reduction-> sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                // 只有满减优惠和打折优惠有效的数据才需要保存
                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    // 调用coupon服务下的SkuFullReductionController下的自定义方法，在saveSkuReduction下调用的service逐步保存了信息
                    //gulimall_sms->sms_sku_ladder-> sms_sku_full_reduction-> sms_member_price
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }

        //6、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = spuVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageInfo(Map<String, Object> params) {

    //        key: '华为',//检索关键字
    //        catelogId: 6,//三级分类id
    //        brandId: 1,//品牌id
    //        status: 0,//商品状态
        String key = (String)params.get("key");
        String catalogId = (String)params.get("catalogId");
        String brandId = (String)params.get("brandId");
        String status = (String)params.get("status");

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();


        if (StringUtils.isNotEmpty(key)){
            // 使用and会将条件用括号括起来  (id = key or spu_name = key)
            wrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        if (StringUtils.isNotEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        if (StringUtils.isNotEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1、查出当前spuId对应的所有sku信息,品牌的名字
        List<SkuInfoEntity> skuInfoEntities=skuInfoService.getSkusBySpuId(spuId);

        // 2.封装待上传es数据集合（skuEsModel）
        // 查询spu关联的基本属性集合
        Map<Long, ProductAttrValueEntity> attrMap = productAttrValueService.getSpuAttr(spuId)
                .stream().collect(Collectors.toMap(key -> key.getAttrId(), val -> val));
        // 查询允许被检索的基本属性集合ID
        List<Long> searAttrIds = attrService.selectSearchAttrIds(new ArrayList<>(attrMap.keySet()));
        // 查询允许被检索的基本属性属性集合，并封装成attrEsModels
        List<SkuEsModel.Attr> attrEsModels = searAttrIds.stream().map(attrId -> {
            SkuEsModel.Attr attrModel = new SkuEsModel.Attr();
            ProductAttrValueEntity attrValue = attrMap.get(attrId);
            // 封装基本属性
            attrModel.setAttrId(attrValue.getAttrId());
            attrModel.setAttrName(attrValue.getAttrName());
            attrModel.setAttrValue(attrValue.getAttrValue());
            return attrModel;
        }).collect(Collectors.toList());

        //3、发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            List<Long> longList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            R skuHasStocks = wareFeignService.getSkuHasStocks(longList);
            // 转换成map方便后续设置库存
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            stockMap = skuHasStocks.getData("data",typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch(Exception e){
            log.error("远程调用库存服务失败,原因{}",e);
        }

        //4、封装每个skuEsModel的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //热度评分 0
            skuEsModel.setHotScore(0L);
            //查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            //设置可搜索属性
            skuEsModel.setAttrs(attrEsModels);
            //设置是否有库存
            skuEsModel.setHasStock(finalStockMap==null? false:finalStockMap.get(sku.getSkuId()));
            return skuEsModel;
        }).collect(Collectors.toList());

        //5、将数据发给es进行保存：gulimall-search
        R r = searchFeignService.productStatusUp(skuEsModels);
        if (r.getCode()==0){
            this.baseMapper.upSpuStatus(spuId, ProductConstant.PublishStatusEnum.SPU_UP.getCode());
        }else {
            log.error("商品远程es保存失败");
        }
    }

}
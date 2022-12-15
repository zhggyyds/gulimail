package com.hao.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hao.common.utils.R;
import com.hao.common.vo.seckill.SeckillSkuVO;
import com.hao.gulimall.product.entity.SkuImagesEntity;
import com.hao.gulimall.product.feign.SeckillFeignService;
import com.hao.gulimall.product.service.*;
import com.hao.gulimall.product.vo.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.product.dao.SkuInfoDao;
import com.hao.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageInfo(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and((wrapper)->{
                wrapper.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        // 参数不为0才需要传入作为查询参数
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){

            queryWrapper.eq("catalog_id",catelogId);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("brand_id",brandId);
        }

        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            queryWrapper.ge("price",min);
        }

        String max = (String) params.get("max");

        if(!StringUtils.isEmpty(max)){
            // max传入的可能是非法输入，处理java.lang.NumberFormatException
            try{
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0"))==1){
                    queryWrapper.le("price",max);
                }
            }catch (Exception e){
                System.out.println(e);
            }

        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVO itemInfo(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();

        CompletableFuture<SkuInfoEntity> future = CompletableFuture.supplyAsync(() -> {
            // 获取sku基本信息（pms_sku_info）【默认图片、标题、副标题、价格】
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVO.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        CompletableFuture<Void> spuInfoDescFuture = future.thenAcceptAsync(res -> {
            // 获取spu商品介绍（pms_spu_info_desc）【描述图片】
            skuItemVO.setDesc(spuInfoDescService.getById(res.getSpuId()));
        }, executor);

        CompletableFuture<Void> AttrGroupWithAttrsFuture = future.thenAcceptAsync(res -> {
            // 获取spu规格参数信息（pms_product_attr_value、pms_attr_attrgroup_relation、pms_attr_group）
            skuItemVO.setGroupAttrs(attrGroupService.getAttrGroupWithAttrs(res.getSpuId(), res.getCatalogId()));
        }, executor);

        CompletableFuture<Void> SaleAttrFuture = future.thenAcceptAsync(res -> {
            // 获取当前sku所属spu下的所有销售属性组合（pms_sku_info、pms_sku_sale_attr_value）
            skuItemVO.setSaleAttr(skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId()));
        }, executor);

        CompletableFuture<Void> ImagesFuture = CompletableFuture.runAsync(() -> {
            // 获取sku图片信息（pms_sku_images）
            skuItemVO.setImages(skuImagesService.getImagesBySkuId(skuId));
        }, executor);

        CompletableFuture<Void> seckillSkuFuture = CompletableFuture.runAsync(() -> {
            // 查询当前商品是否参与秒杀优惠
            R r = seckillFeignService.getSkuSecKilInfo(skuId);
            if (r.getCode() == 0) {
                SeckillSkuVO seckillSku = r.getData("data",new TypeReference<SeckillSkuVO>() {
                });
                skuItemVO.setSeckillSku(seckillSku);
            }
        }, executor);


        CompletableFuture.allOf(spuInfoDescFuture,AttrGroupWithAttrsFuture,SaleAttrFuture,ImagesFuture,seckillSkuFuture).get();
        return skuItemVO;
    }

    @Override
    public List<SkuInfoEntity> getByIds(List<Long> skuIds) {
        return this.baseMapper.selectBatchIds(skuIds);
    }


}
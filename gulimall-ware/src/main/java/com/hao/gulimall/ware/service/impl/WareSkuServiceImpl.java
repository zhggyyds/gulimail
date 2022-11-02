package com.hao.gulimall.ware.service.impl;

import com.hao.common.to.SkuHasStockVo;
import com.hao.common.utils.R;
import com.hao.gulimall.ware.feign.ProductFeignService;
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

import com.hao.gulimall.ware.dao.WareSkuDao;
import com.hao.gulimall.ware.entity.WareSkuEntity;
import com.hao.gulimall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        // 查找该商品是否已经存在
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));

        // 当数据库中不存在该商品
        if (entities.size() == 0 || entities == null){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);

            //TODO 远程查询sku的名字，如果失败，整个事务仍无需回滚
            //1、自己catch异常
            //TODO 2.还可以用什么办法让异常出现以后不回滚？高级
            try {
                // 获得商品名字
                R info = productFeignService.info(skuId);
                //数据通过json传输。对象经过传输以后，会从json对象自动转换为map对象。需要对传输通用对象json转换和逆转换，才能直接获得对象
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
            }

            wareSkuDao.insert(skuEntity);
        }else{
            // 当数据库中已经存在商品
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStocks(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId(skuId);
            Long hasStock = this.baseMapper.getSkuHasStocks(skuId);
            vo.setHasStock(hasStock != null && hasStock > 0);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

}
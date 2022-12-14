package com.hao.gulimall.ware.service.impl;

import com.hao.common.constant.ware.WareConstant;
import com.hao.gulimall.ware.Vo.MerageVo;
import com.hao.gulimall.ware.Vo.PurchaseDoneVo;
import com.hao.gulimall.ware.Vo.PurchaseItemDoneVo;
import com.hao.gulimall.ware.entity.PurchaseDetailEntity;
import com.hao.gulimall.ware.service.PurchaseDetailService;
import com.hao.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.ware.dao.PurchaseDao;
import com.hao.gulimall.ware.entity.PurchaseEntity;
import com.hao.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode())
                        .or().eq("status",WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void merge(MerageVo merageVo) {
        Long purchaseId = merageVo.getPurchaseId();
        if (purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setPriority(1);
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        //TODO ????????????????????????0,1???????????????

        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = merageVo.getItems().stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setId(item);

            return detailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }


    /**
     *
     * @param ids ?????????id
     */
    @Override
    public void received(List<Long> ids) {

        //1??????????????????????????????????????????????????????
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2???????????????????????????
        this.updateBatchById(collect);

        //3???????????????????????????
        collect.forEach((item)->{
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(entity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();

        //1???????????????????????????
        Boolean flag = true; // ???????????????????????????????????????????????????????????????????????????????????????
        List<PurchaseItemDoneVo> items = doneVo.getItems();

        List<PurchaseDetailEntity> updates = new ArrayList<>();
        // ??????????????????????????????
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else{
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                ////2?????????????????????????????????
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }

            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }

        purchaseDetailService.updateBatchById(updates);

        //3????????????????????????
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode()
                : WareConstant.PurchaseStatusEnum.HASERROR.getCode());

        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}
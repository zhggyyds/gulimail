package com.hao.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.hao.common.constant.order.OrderConstant;
import com.hao.common.constant.ware.WareOrderTaskConstant;
import com.hao.common.exception.NoStockException;
import com.hao.common.to.mq.OrderTO;
import com.hao.common.to.order.WareSkuLockTo;
import com.hao.common.to.ware.SkuWareHasStockTo;
import com.hao.common.to.mq.StockDetailTO;
import com.hao.common.to.mq.StockLockedTO;
import com.hao.common.vo.ware.SkuHasStockVo;
import com.hao.common.utils.R;
import com.hao.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.hao.gulimall.ware.entity.WareOrderTaskEntity;
import com.hao.gulimall.ware.feign.OrderFeignService;
import com.hao.gulimall.ware.feign.ProductFeignService;
import com.hao.gulimall.ware.service.WareOrderTaskDetailService;
import com.hao.gulimall.ware.service.WareOrderTaskService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.ware.dao.WareSkuDao;
import com.hao.gulimall.ware.entity.WareSkuEntity;
import com.hao.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;


    @Override
    /**
     * 库存解锁
     * 订单解锁触发，防止库存解锁消息优先于订单解锁消息到期，导致库存无法解锁
     */
    public void orderReleaseStock(OrderTO order) {
        String orderSn = order.getOrderSn();// 订单号
        // 1.根据订单号查询库存锁定工作单
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        // 2.按照工作单查询未解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> taskDetails = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", task.getId())
                .eq("lock_status", WareOrderTaskConstant.LockStatusEnum.LOCKED.getCode()));// 并发问题
        // 3.解锁库存
        for (WareOrderTaskDetailEntity taskDetail : taskDetails) {
            unlockStock(taskDetail.getSkuId(), taskDetail.getWareId(), taskDetail.getSkuNum(), taskDetail.getId());
        }
    }

    /*
     * @description 释放库存 处理死信队列消息
     * @date 2022/12/8 18:28
     * @param null
     * @return null
     */
    public void releaseStock(StockLockedTO stockLockedTO){
        Long taskId = stockLockedTO.getId();
        StockDetailTO detail = stockLockedTO.getDetail();
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detail.getId());

        // 检查工作单是否还存在，后续操作可能导致整个创建订单大事务回滚
        //        解锁逻辑
        //          查询数据库关于这个订单的工作单信息。
        //              有：证明库存锁定成功了
        //                  是否解锁：查看订单情况。
        //                      1、没有这个订单。必须解锁
        //                      2、有这个订单。不一定
        //              没有：库存锁定失败了，库存回滚了。这种情况无需解锁
//        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detail.getId());
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);
        if (taskEntity != null){
            R r= orderFeignService.getOrderByOrderSn(taskEntity.getOrderSn());
            if (r.getCode() == 0){
                OrderTO order = r.getData("data", new TypeReference<OrderTO>() {
                });
                // 订单不存在（大业务失败） / 订单被取消（超出支付时长被自动取消或者用户取消订单） - 解锁
                if (order == null || Objects.equals(order.getStatus(), OrderConstant.OrderStatusEnum.CANCLED.getCode())){
                    // 订单详情数据的状态位为锁定 才执行解锁
                    if (Objects.equals(detailEntity.getLockStatus(), WareOrderTaskConstant.LockStatusEnum.LOCKED.getCode())){
                        unlockStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum(),detailEntity.getId());
                    }
                }
            }else {
                throw new RuntimeException("远程调用失败");// 开启手动确认，消息重新入队
            }
        }

    }

    /*
     * @description 被动库存信息解锁
     * @date 2022/12/8 18:29
     * @param null
     * @return null
     */
    private void unlockStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {
        //库存恢复
        this.baseMapper.unLockStock(skuId,wareId,skuNum);
        // 修改order_detail表相关数据的状态信息
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(detailId);
        detailEntity.setLockStatus(WareOrderTaskConstant.LockStatusEnum.UNLOCKED.getCode());
        wareOrderTaskDetailService.updateById(detailEntity);

    }

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

    @Transactional
    @Override
    /*
     * @description 锁定库存大事务 锁定库存，插入工作单数据
     * @date 2022/12/8 18:30
     * @param wareSkuLockTo
     */
    public void lockStock(WareSkuLockTo wareSkuLockTo) {
        List<SkuWareHasStockTo> locks = wareSkuLockTo.getLocks();

        // 保存任务单对象
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareSkuLockTo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        // 封装任务单详情集合  - 一个商品代表一行数据，每行数据都与任务单id绑定
        List<WareOrderTaskDetailEntity> detailEntities = new ArrayList<>();


        // 查询仓库，锁定库存
        // 采用方案：获取每项商品在哪些仓库有库存，轮询尝试锁定（不能同时锁两个仓库的库存），任一商品锁定失败回滚

        // 分别查询每个商品的仓库号信息
        List<SkuWareHasStockTo> skuWareHasStocks = locks.stream()
                .peek(skuWareHasStockTo -> skuWareHasStockTo.setWareId(wareSkuDao.listWareId(skuWareHasStockTo.getSkuId())))
                .collect(Collectors.toList());

        // 遍历所有锁库存的商品
        for (SkuWareHasStockTo skuWareHasStock : skuWareHasStocks) {
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            boolean successLockStock = false; // 标志 - 本商品遍历仓库库存后是否满足购买条件锁定成功
            // 没有任何仓库能够满足库存条件
            if (wareIds == null && wareIds.size() == 0){
                throw new NoStockException(skuId);
            }
            //遍历所有库存的仓库，但并不一定满足购买需求的仓库
            for (Long wareId : wareIds) {
                // sql语句执行成功会返回影响的表行数
                Long influencedLines = wareSkuDao.lockSkuStock(wareId,skuId,skuWareHasStock.getCount());
                if (influencedLines == 1){
                    // 锁定成功,跳出循环
                    successLockStock = true;
                    // 封装任务单详情对象
                    WareOrderTaskDetailEntity taskDetail = new WareOrderTaskDetailEntity(null, skuId,
                            "", skuWareHasStock.getCount(), taskEntity.getId(),wareId,
                            WareOrderTaskConstant.LockStatusEnum.LOCKED.getCode());
                    detailEntities.add(taskDetail);
                    break;
                }
                // 尝试下一个仓库
            }
            if (!successLockStock){
                // 当前商品所有仓库都不能满足它的count需求，抛出异常，结束本订单的所有锁库存业务
                throw new NoStockException(skuId);
            }
        }

        // 保存任务单详情信息
        wareOrderTaskDetailService.saveBatch(detailEntities);
        // 到此所有商品都锁定了库存，再向rabbitmq发送消息，准备执行解锁操作。
        for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
            // 将数据封装成待传递的对象
            StockDetailTO detailTO = new StockDetailTO();
            BeanUtils.copyProperties(detailEntity,detailTO);

            StockLockedTO msg = new StockLockedTO();
            msg.setId(taskEntity.getId());
            msg.setDetail(detailTO);
            try{
                // TODO 持久化消息到mq_message表中，并设置消息状态为0-新建（保存日志记录）
                rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",msg);
            }catch (Exception e){
                // TODO 消息未抵达Broker，修改mq_message消息状态为2-错误抵达
            }

        }
    }



}
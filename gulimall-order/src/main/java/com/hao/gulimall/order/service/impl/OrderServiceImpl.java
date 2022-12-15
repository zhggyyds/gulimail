package com.hao.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hao.common.constant.ObjectConstant;
import com.hao.common.constant.order.OrderConstant;
import com.hao.common.exception.NoStockException;
import com.hao.common.to.mq.OrderTO;
import com.hao.common.to.mq.SeckillOrderTO;
import com.hao.common.to.order.WareSkuLockTo;
import com.hao.common.to.ware.SkuWareHasStockTo;
import com.hao.common.vo.order.PayVo;
import com.hao.gulimall.order.Alipay.vo.PayAsyncVo;
import com.hao.gulimall.order.entity.PaymentInfoEntity;
import com.hao.gulimall.order.service.OrderItemService;
import com.hao.gulimall.order.service.PaymentInfoService;
import com.hao.gulimall.order.to.OrderCreateTo;
import com.hao.common.to.product.SpuInfoTo;
import com.hao.common.utils.R;
import com.hao.common.vo.auth.MemberResponseVo;
import com.hao.common.vo.cart.CartItemVo;
import com.hao.common.vo.member.MemberAddressVo;
import com.hao.common.vo.order.OrderConfirmVo;
import com.hao.common.vo.order.OrderSubmitVo;
import com.hao.gulimall.order.vo.SubmitOrderResponseVo;
import com.hao.common.vo.ware.FareVo;
import com.hao.common.vo.ware.SkuHasStockVo;
import com.hao.gulimall.order.entity.OrderItemEntity;
import com.hao.gulimall.order.feign.CartFeignService;
import com.hao.gulimall.order.feign.MemberFeignService;
import com.hao.gulimall.order.feign.ProductFeignService;
import com.hao.gulimall.order.feign.WmsFeignService;
import com.hao.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.order.dao.OrderDao;
import com.hao.gulimall.order.entity.OrderEntity;
import com.hao.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    PaymentInfoService paymentInfoService;

    // 提交订单共享提交数据
    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    /*
     * @description 获得订单确认页数据
     * @date 2022/12/3 12:54
     * @return com.hao.common.vo.order.OrderConfirmVo
     */
    public OrderConfirmVo getOrderConfirmData() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.threadLocal.get();

        // 由于RequestContextHolder使用了threadLocal进行传递数据，异步编排后（从始至终可能是3个线程在执行这个服务），会导致LoginUserInterceptor取数据时request数据为空
        // 进行手工加入解决异步调用requestAttributes为空的问题
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 设置用户收货地址
        CompletableFuture<Void> getAddressFutrue = CompletableFuture.runAsync(() -> {
            // 同步上下文环境器，解决异步无法从ThreadLocal获取RequestAttributes
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(address);
        });

        //设置商品选中项
        CompletableFuture<Void> getCurrentCartItemsFuture = CompletableFuture.runAsync(() -> {
            // 同步上下文环境器，解决异步无法从ThreadLocal获取RequestAttributes
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<CartItemVo> currentCartItems = cartFeignService.getCheckedCartItems();
            confirmVo.setItems(currentCartItems);
        }).thenRun(()->{
            // 获取有无货信息
            List<Long> skuIds = confirmVo.getItems().stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wmsFeignService.getSkuHasStocks(skuIds);
            List<SkuHasStockVo> hasStockVos = r.getData("data", new TypeReference<List<SkuHasStockVo>>() {
            });
            Map<Long, Boolean> map = hasStockVos.stream().collect(Collectors.toMap(key -> key.getSkuId(), val -> val.getHasStock()));
            confirmVo.setStocks(map);
        });

        //设置用户积分，用于优惠
        confirmVo.setIntegration(memberResponseVo.getIntegration());

        //设置防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResponseVo.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setUniqueToken(token);

        CompletableFuture.allOf(getAddressFutrue,getCurrentCartItemsFuture).get();

        return confirmVo;
    }

//    @GlobalTransactional // AT模式不适合分布式事务的高并发场景
    @Transactional //runtime异常自动回滚
    @Override
    /*
     * @description  下单
     * @date 2022/12/3 12:51
     * @param orderSubmitVo
     * @return com.hao.common.vo.order.SubmitOrderResponseVo
     */
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) throws Exception {
        // 提交订单后的返回结果 对象
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        submitVoThreadLocal.set(orderSubmitVo);// 线程内共享前端数据，给createOrder方法使用

        MemberResponseVo memberResponseVo = LoginUserInterceptor.threadLocal.get();
        Long userID = memberResponseVo.getId();

        //1.验证令牌
        String uniqueToken = orderSubmitVo.getUniqueToken();//用户前端传来的令牌

        // 原子验证令牌和删除令牌 1-成功 0-失败
        String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";//lua脚本
        Long res = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX+userID), uniqueToken);

        if (res == 1L){
            //令牌验证成功
            submitOrderResponseVo.setCode(0);
            //2.创建订单
            OrderCreateTo orderCreateTo = createOrder();
            // 3.比价
            if(Math.abs(orderCreateTo.getPayPrice().subtract(orderSubmitVo.getPayPrice()).doubleValue()) < 0.01){
                // 比价成功
                //3.订单数据插入数据库
                saveOrder(orderCreateTo);

                // 4.构造远程服务需要的参数
                WareSkuLockTo wareSkuLockTo = new WareSkuLockTo();
                wareSkuLockTo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                List<SkuWareHasStockTo> collect = orderCreateTo.getOrderItems().stream().map(orderItemEntity -> {
                    SkuWareHasStockTo skuWareHasStockTo = new SkuWareHasStockTo();
                    // 只传入需要的数据
                    skuWareHasStockTo.setSkuId(orderItemEntity.getSkuId());
                    skuWareHasStockTo.setCount(orderItemEntity.getSkuQuantity());
                    return skuWareHasStockTo;
                }).collect(Collectors.toList());
                wareSkuLockTo.setLocks(collect);

                //5.调用远程库存服务
                R r = wmsFeignService.lockStock(wareSkuLockTo);
                if (r.getCode() == 0){
                    // 库存锁定成功
                    submitOrderResponseVo.setOrder(orderCreateTo.getOrder());
                    // 发送消息，后续检查订单状态，用于取消
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderCreateTo.getOrder());

                }else{
                    //库存锁定失败
                    submitOrderResponseVo.setCode(3);
                    throw new NoStockException(1L); // 库存不足异常,由于分布式原因，不手动抛异常会导致事务不能回滚
                }
                return submitOrderResponseVo;

            }else {
                //比价失败
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }

        }else {
            //验证码校验失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }
    }

    /*
     * @description 将订单信息插入数据库
     * @date 2022/12/4 15:59
     * @param null
     * @return null
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        this.save(order);
        orderItemService.saveBatch(orderCreateTo.getOrderItems());
    }

    /*
     * @description 创建订单大方法
     * @date 2022/12/3 20:53
     * @param null
     * @return null
     */
    private OrderCreateTo createOrder() throws Exception {
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        // 生成唯一订单id - orderSn
        String orderSn = IdWorker.getTimeId();

        // 生成订单
        OrderEntity orderEntity = buildOrder(orderSn);

        //创建OrderItems
        List<OrderItemEntity> orderItemEntities = buildOrderItemEntities(orderSn);

        // 汇总封装（封装订单价格[订单项价格之和]、封装订单积分/成长值[订单项积分、成长值之和]）
        summaryOrder(orderEntity, orderItemEntities);

        //封装TO返回
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);
        orderCreateTo.setFare(orderEntity.getFreightAmount());
        orderCreateTo.setPayPrice(orderEntity.getPayAmount());// 设置应付金额

        return orderCreateTo;
    }

    /**
     * 汇总封装订单
     * 1.计算订单总金额
     * 2.汇总积分、成长值
     * 3.汇总应付总额 = 订单总金额 + 运费
     *
     * @param orderEntity       订单
     * @param orderItemEntities 订单项
     */
    private void summaryOrder(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // 1.订单总额、优惠总金额（促销总金额、优惠券总金额、积分优惠总金额）
        BigDecimal total = new BigDecimal(0);
        BigDecimal coupon = new BigDecimal(0);
        BigDecimal promotion = new BigDecimal(0);
        BigDecimal integration = new BigDecimal(0);
        // 2.积分、成长值
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;
        for (OrderItemEntity itemEntity : orderItemEntities) {
            total = total.add(itemEntity.getRealAmount());// 订单总额
            coupon = coupon.add(itemEntity.getCouponAmount());// 促销总金额
            promotion = promotion.add(itemEntity.getPromotionAmount());// 优惠券总金额
            integration = integration.add(itemEntity.getIntegrationAmount());// 积分优惠总金额
            giftIntegration = giftIntegration + itemEntity.getGiftIntegration();// 积分
            giftGrowth = giftGrowth + itemEntity.getGiftGrowth();// 成长值
        }
        orderEntity.setTotalAmount(total);// 订单所有商品项总金额
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration); //积分总优惠金额
        orderEntity.setIntegration(giftIntegration);// 赠送总积分
        orderEntity.setGrowth(giftGrowth);// 赠送总成长值

        // 3.应付总额
        orderEntity.setPayAmount(orderEntity.getTotalAmount().add(orderEntity.getFreightAmount()));// 订单总额 +　运费
    }



    /*
     * @description 生成OrderEntity
     * @date 2022/12/3 20:41
     * @param null
     * @return null
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();// 订单实体类
        // 1.封装会员ID
        MemberResponseVo member = LoginUserInterceptor.threadLocal.get();// 拦截器获取登录信息
        orderEntity.setMemberId(member.getId());
        // 2.封装订单号
        orderEntity.setOrderSn(orderSn);
        // 3.封装运费
        OrderSubmitVo orderSubmitVO = submitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVO.getAddrId());// 获取地址
        FareVo fareVO = fare.getData("data",new TypeReference<FareVo>(){});
        orderEntity.setFreightAmount(fareVO.getFare());
        // 4.封装收货地址信息
        orderEntity.setReceiverName(fareVO.getAddress().getName());// 收货人名字
        orderEntity.setReceiverPhone(fareVO.getAddress().getPhone());// 收货人电话
        orderEntity.setReceiverProvince(fareVO.getAddress().getProvince());// 省
        orderEntity.setReceiverCity(fareVO.getAddress().getCity());// 市
        orderEntity.setReceiverRegion(fareVO.getAddress().getRegion());// 区
        orderEntity.setReceiverDetailAddress(fareVO.getAddress().getDetailAddress());// 详细地址
        orderEntity.setReceiverPostCode(fareVO.getAddress().getPostCode());// 收货人邮编
        // 5.封装订单状态信息
        orderEntity.setStatus(OrderConstant.OrderStatusEnum.CREATE_NEW.getCode());
        // 6.设置自动确认时间
        orderEntity.setAutoConfirmDay(OrderConstant.autoConfirmDay);// 7天
        // 7.设置未删除状态
        orderEntity.setDeleteStatus(ObjectConstant.BooleanIntEnum.NO.getCode());
        // 8.设置时间
        Date now = new Date();
        orderEntity.setCreateTime(now);
        orderEntity.setModifyTime(now);
        return orderEntity;

    }

    /*
     * @description  构建 List<OrderItemEntity> 所有订单项
     * @date 2022/12/3 20:53
     * @param null
     * @return null
     */
    private List<OrderItemEntity> buildOrderItemEntities(String orderSn) throws Exception {
        // 获得当前最有被选中的商品 check == true
        List<CartItemVo> checkedCartItems = cartFeignService.getCheckedCartItems();

        // 封装OrderItemEntity对象
        if (checkedCartItems != null && checkedCartItems.size() != 0){
            List<OrderItemEntity> collect = checkedCartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItemEntity(item,orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());

            return collect;
        }else {
            throw new Exception();
        }
    }

    /*
     * @description 构建 OrderItemEntity 订单项
     * @date 2022/12/3 20:54
     * @param null
     * @return null
     */
    private OrderItemEntity buildOrderItemEntity(CartItemVo cartItem, String orderSn) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //订单号
        itemEntity.setOrderSn(orderSn);

        // sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());// 商品sku图片
        itemEntity.setSkuPrice(cartItem.getPrice());// 这个是最新价格，购物车模块查询数据库得到
        itemEntity.setSkuQuantity(cartItem.getCount());// 当前商品数量
        String skuAttrsVals = String.join(";", cartItem.getSkuAttrValues()); // 将数组中的string 通过;分割，组合成一个字符串
        itemEntity.setSkuAttrsVals(skuAttrsVals);// 商品销售属性组合["颜色:星河银","版本:8GB+256GB"]

        // 积分信息 分值=单价*数量
        int score = cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue();
        // 赠送成长值
        itemEntity.setGiftGrowth(score);
        // 赠送积分
        itemEntity.setGiftIntegration(score);

        // spu信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(itemEntity.getSkuId());//远程服务调用查询
        SpuInfoTo spuInfoTo = spuInfo.getData("data", new TypeReference<SpuInfoTo>() {});
        itemEntity.setSpuId(spuInfoTo.getId());
        itemEntity.setSpuName(spuInfoTo.getSpuName());
        itemEntity.setSpuBrand(spuInfoTo.getSpuName());
        itemEntity.setCategoryId(spuInfoTo.getCatalogId());

        // 价格信息
        itemEntity.setPromotionAmount(BigDecimal.ZERO);// 促销金额
        itemEntity.setCouponAmount(BigDecimal.ZERO);// 优惠券金额
        itemEntity.setIntegrationAmount(BigDecimal.ZERO);// 积分优惠金额
        BigDecimal realAmount = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()))
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realAmount);// 实际金额，减去所有优惠金额

        return itemEntity;
    }


    /**
     * 通过 OrderSn 获取订单详情
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭订单
     */
    @Override
    public void closeOrder(OrderEntity order) {
        OrderEntity _order = getById(order.getId());
        if (OrderConstant.OrderStatusEnum.CREATE_NEW.getCode().equals(_order.getStatus())) {
            // 超过支付时长，待付款状态允许关单
            OrderEntity temp = new OrderEntity();
            temp.setId(order.getId());
            temp.setStatus(OrderConstant.OrderStatusEnum.CANCLED.getCode());
            updateById(temp);

            try {
                // 发送消息给MQ
                OrderTO orderTO = new OrderTO();
                BeanUtils.copyProperties(_order, orderTO);
                // TODO 持久化消息到mq_message表中，并设置消息状态为0-新建（保存日志记录）
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTO);
            } catch (Exception e) {
                // TODO 消息未抵达Broker，修改mq_message消息状态为2-错误抵达
            }
        }
    }

    @Override
    public PayVo getPayVo(String orderSn) {
        // 查询订单
        OrderEntity orderEntity = getOrderByOrderSn(orderSn);
        List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        PayVo payVo = new PayVo();
        payVo.setBody(itemEntities.get(0).getSkuAttrsVals());
        payVo.setSubject(itemEntities.get(0).getSkuName());
        payVo.setOut_trade_no(orderSn);

        // 将支付金额设置 保留小数点后两位，并且进位机制是全入 0.001 -> 0.01
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2,BigDecimal.ROUND_UP).toString());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItems(Map<String, Object> params) {

        // 获得当前登陆的会员信息
        MemberResponseVo member = LoginUserInterceptor.threadLocal.get();

        // 查询符合条件的订单
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",member.getId()).orderByDesc("create_time")
        );
        //设置每个订单的订单项
        List<OrderEntity> orderEntities = page.getRecords().stream().map(orderItem -> {
            List<OrderItemEntity> entities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderItem.getOrderSn()));
            orderItem.setItemEntities(entities);
            return orderItem;
        }).collect(Collectors.toList());
        page.setRecords(orderEntities);

        return new PageUtils(page);
    }

    @Override
    public void handlePayResult(PayAsyncVo aliVo) {
        // 保存交易流水信息
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setOrderSn(aliVo.getOut_trade_no());
        paymentInfo.setAlipayTradeNo(aliVo.getTrade_no());
        paymentInfo.setTotalAmount(new BigDecimal(aliVo.getBuyer_pay_amount()));
        paymentInfo.setSubject(aliVo.getBody());
        paymentInfo.setPaymentStatus(aliVo.getTrade_status());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setCallbackTime(aliVo.getNotify_time());
        paymentInfoService.save(paymentInfo);

        // 修改订单状态
        this.baseMapper.updateOrderStatus(OrderConstant.OrderStatusEnum.PAYED.getCode(),aliVo.getOut_trade_no());
    }

    @Override
    public void createSeckillOrder(SeckillOrderTO order) {
        // 1.创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(order.getOrderSn());
        orderEntity.setMemberId(order.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = order.getSeckillPrice().multiply(BigDecimal.valueOf(order.getNum()));// 应付总额
        orderEntity.setTotalAmount(totalPrice);// 订单总额
        orderEntity.setPayAmount(totalPrice);// 应付总额
        orderEntity.setStatus(OrderConstant.OrderStatusEnum.CREATE_NEW.getCode());
        // 保存订单
        this.save(orderEntity);

        // 2.创建订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(order.getOrderSn());
        orderItem.setRealAmount(totalPrice);
        orderItem.setSkuQuantity(order.getNum());

        // 保存商品的spu信息
        R r = productFeignService.getSpuInfoBySkuId(order.getSkuId());
        SpuInfoTo spuInfo = r.getData("data",new TypeReference<SpuInfoTo>() {
        });
        orderItem.setSpuId(spuInfo.getId());
        orderItem.setSpuName(spuInfo.getSpuName());
        orderItem.setCategoryId(spuInfo.getCatalogId());
        // 保存订单项数据
        orderItemService.save(orderItem);

        // TODO 创建订单后要给mq发送消息检查订单状态 若 订单取消（长时间未支付/手工取消订单）redis中信号量的恢复（库存的恢复）
    }

}
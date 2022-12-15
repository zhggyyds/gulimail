package com.hao.gulimall.ware.listener;

import com.hao.common.to.mq.OrderTO;
import com.hao.common.to.mq.StockLockedTO;
import com.hao.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.ware.listener
 * @Description:
 * @date 2022/12/8 16:08
 **/

@RabbitListener(queues = "stock.release.stock.queue")
@Service
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    // 根据消息传递的对象区分使用哪个handler
    @RabbitHandler
    public void handleStockRelease(StockLockedTO stockLockedTO, Message message, Channel channel) throws IOException {
        System.out.println("接受库存解锁消息成功");
        try{
            wareSkuService.releaseStock(stockLockedTO);
            // 解锁成功，手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            // 解锁失败，消息入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

    @RabbitHandler
    public void handleOrderReleaseStock(OrderTO order, Message message, Channel channel) throws IOException {
        System.out.println("接受订单的库存解锁消息成功");
        try{
            wareSkuService.orderReleaseStock(order);
            // 解锁成功，手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            // 解锁失败，消息入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}

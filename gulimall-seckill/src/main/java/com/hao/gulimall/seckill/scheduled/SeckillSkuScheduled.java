package com.hao.gulimall.seckill.scheduled;

import com.hao.common.constant.seckill.SeckillConstant;
import com.hao.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.seckill.scheduled
 * @Description:
 * @date 2022/12/11 19:59
 **/
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    @Scheduled(cron = "0 0 1 * * ?")
    public void uploadSeckillSkuLatest3Days(){
        System.out.println("商品上架定时任务执行中");
        RLock lock = redissonClient.getLock(SeckillConstant.UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        finally {
            lock.unlock();
        }

    }

}

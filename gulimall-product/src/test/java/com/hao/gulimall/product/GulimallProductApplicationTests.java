package com.hao.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hao.common.utils.R;
import com.hao.gulimall.product.entity.BrandEntity;
import com.hao.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
       BrandEntity brandEntity = new BrandEntity();
       brandEntity.setDescript("hello1");
       brandEntity.setName("苹果");



    }


    @Test
    void testRedisTemplate() {
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        stringStringValueOperations.set("hello","world");

        System.out.println(stringStringValueOperations.get("hello"));
    }

    @Test
    void test() {
        ArrayList<Long> longs = new ArrayList<>();
        longs.add((long)8);
        List<BrandEntity> brand = brandService.getBrandByIds(longs);
        System.out.println(brand);
    }

}

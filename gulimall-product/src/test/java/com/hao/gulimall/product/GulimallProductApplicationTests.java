package com.hao.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hao.common.utils.R;
import com.hao.gulimall.product.entity.BrandEntity;
import com.hao.gulimall.product.service.BrandService;
import com.hao.gulimall.product.service.SkuSaleAttrValueService;
import com.hao.gulimall.product.vo.SkuItemSaleAttrVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.*;

//@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("1",null);

        System.out.println(hashMap.get("2"));


    }


    @Test
    void testRedisTemplate() {
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        stringStringValueOperations.set("hello","world");

        System.out.println(stringStringValueOperations.get("hello"));
    }

    @Test
    void test() {
        List<SkuItemSaleAttrVO> saleAttrBySpuId = skuSaleAttrValueService.getSaleAttrBySpuId(15L);
        System.out.println(saleAttrBySpuId);

    }

}

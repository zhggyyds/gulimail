package com.hao.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hao.common.utils.R;
import com.hao.common.vo.member.MemberAddressVo;
import com.hao.common.vo.ware.FareVo;
import com.hao.gulimall.ware.feign.MemberFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.ware.dao.WareInfoDao;
import com.hao.gulimall.ware.entity.WareInfoEntity;
import com.hao.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired

    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id",key).or()
                    .like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    /*
     * @description 获得运费
     * @date 2022/12/1 17:55
     * @param null
     * @return null
     */
    @Override
    public FareVo getFare(Long addrId) {
        //收获地址的详细信息
        R addrInfo = memberFeignService.info(addrId);
        MemberAddressVo memberAddressVo = addrInfo.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (memberAddressVo != null) {
            FareVo fareVo = new FareVo();
            String phone = memberAddressVo.getPhone();
            //截取用户手机号码最后一位作为我们的运费计算
            BigDecimal fare = new BigDecimal(phone.substring(phone.length() - 1));
            fareVo.setFare(fare);
            fareVo.setAddress(memberAddressVo);
            return fareVo;
        }
        return null;
    }

}
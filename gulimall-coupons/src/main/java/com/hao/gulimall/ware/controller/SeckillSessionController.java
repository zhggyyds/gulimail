package com.hao.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hao.gulimall.ware.entity.SeckillSessionEntity;
import com.hao.gulimall.ware.service.SeckillSessionService;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.R;



/**
 * 秒杀活动场次
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-08-07 12:38:46
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {

    @Autowired
    private SeckillSessionService seckillSessionService;

    /*
     * @description 获得近三天的秒杀活动信息
     * @date 2022/12/11 16:34
     * @param null
     * @return null
     */
    @GetMapping(value = "/Latest3DaySession")
    public R getLates3DaySession(){
        List<SeckillSessionEntity> list = seckillSessionService.getLates3DaySession();
        return R.ok().setData(list);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("ware:seckillsession:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//   @RequiresPermissions("ware:seckillsession:info")
    public R info(@PathVariable("id") Long id){
		SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("ware:seckillsession:save")
    public R save(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("ware:seckillsession:update")
    public R update(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("ware:seckillsession:delete")
    public R delete(@RequestBody Long[] ids){
		seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

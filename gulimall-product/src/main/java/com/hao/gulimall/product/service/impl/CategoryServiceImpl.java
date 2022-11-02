package com.hao.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hao.gulimall.product.service.CategoryBrandRelationService;
import com.hao.gulimall.product.vo.Catelog2Vo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.product.dao.CategoryDao;
import com.hao.gulimall.product.entity.CategoryEntity;
import com.hao.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2 组装成父子的树形结构
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid().longValue() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> RemovedList) {
        //TODO 1 检查当前的菜单是否被别的地方所引用
        baseMapper.deleteBatchIds(RemovedList);
    }

    @Override
    public Long[] findCateLogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        paths = findParentPath(catelogId, paths);
        // 收集的时候是顺序 前端是逆序显示的 所以用集合工具类给它逆序一下
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }


    /*
    * 缓存和数据库一致性

        双写模式：写数据库后，写缓存
        问题：并发时，2写进入，写完DB后都写缓存。有暂时的脏数据
        失效模式：写完数据库后，删缓存
        问题：还没存入数据库呢，线程2又读到旧的DB了
        解决：缓存设置过期时间，定期更新
        解决：加分布式的读写锁。但是在经常写的场景下，对并发性有很大的影响

        解决方案：
            如果是用户纬度数据（订单数据、用户数据），这种并发几率非常小，不用考虑这个问题，缓存数据加上过期时间，每隔一段时间触发读的主动更新即可
            如果是菜单，商品介绍等基础数据，也可以去使用canal订阅binlog的方式
            缓存数据+过期时间也足够解决大部分业务对于缓存的要求。
            通过加锁保证并发读写，写写的时候按顺序排好队。读读无所谓。所以适合使用读写锁。（业务不关心脏数据，允许临时脏数据可忽略）；

        总结：
            我们能放入缓存的数据本就不应该是实时性、一致性要求超高的。所以缓存数据的时候加上过期时间，保证每天拿到当前最新数据即可。
        我们不应该过度设计，增加系统的复杂性。遇到实时性、一致性要求高的数据，就应该查数据库，即使慢点。

     */
    /*
    * 使用Caching注解中可以放置多个操作注解
    * */
//    @Caching(evict = {
//            @CacheEvict(value = {"category"},key = "'getLevel1Categorys'"),
//            @CacheEvict(value = {"category"},key = "'getCatalogJsonWithSpringCache'")
//    })
    /**
     * 级联更新所有关联表的冗余数据
     * 缓存策略：失效模式，方法执行完删除缓存
     *
     * @CachePut 双写模式，适合有返回值的方法
     *
     * 两种模式都存在着缓存不一致的问题，如果是实时性比较高的数据还是直接读数据库比较好，尽管慢一些！
     */
    @CacheEvict(value = {"category"}, allEntries = true) // 失效模式 allEntries = true --- 删除缓存category下的所有cache
    @Transactional
    @Override
    /*
     * @description 更新目录
     * @date 2022/10/24 15:22
     * @param category
     */
    public void updateCascadeById(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCateLog(category.getCatId(), category.getName());

    }

    // 自定义生成缓存的名称和存在redis中的key值，key值使用SpEL或字符串自定义（ 默认 value::key 就是redis中的key值 前缀：：key)
    //    SpEL的使用 https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-spel-context
    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys");
        List<CategoryEntity> list = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

        return list;
    }


    /*
    *
    *
    * lettuce堆外内存溢出bug 当进行压力测试时后期后出现堆外内存溢出OutOfDirectMemoryError
    *
        产生原因：
        1)、springboot2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
        2)、lettuce的bug导致netty堆外内存溢出。netty如果没有指定堆外内存，默认使用Xms的值，可以使用-Dio.netty.maxDirectMemory进行设置

        解决方案：由于是lettuce的bug造成，不要直接使用-Dio.netty.maxDirectMemory 去调大虚拟机堆外内存，治标不治本。

        1)、升级lettuce客户端。
        2)、切换使用jedis底层

        排除lettuce依赖，直接添加jedis依赖后不用修改源代码可以直接使用 why？
            lettuce和jedis是操作redis的底层客户端，RedisTemplate是再次封装
    *
    * */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson(){
        String catalogJson = (String) redisTemplate.opsForValue().get("catalogJson");

        if (StringUtils.isEmpty(catalogJson)){
            return getCatalogJsonWithSpringCache();
        }
        Map<String, List<Catelog2Vo>> res = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {});

        return res;
    }

        /*
    * SpringCache原理与不足
            1）读模式（可以解决）

            缓存穿透：查询一个null数据。解决方案：缓存空数据，可通过spring.cache.redis.cache-null-values=true
            缓存击穿：大量并发进来同时查询一个正好过期的数据。解决方案：加锁 ? 默认是无加锁的;
            使用sync = true来解决击穿问题
            缓存雪崩：大量的key同时过期。解决：加随机时间。

            2) 写模式：（缓存与数据库一致性问题，并不能完全解决）

            读写加锁。
            引入Canal，感知到MySQL的更新去更新Redis
            读多写多，直接去数据库查询就行

            3）总结：

            常规数据（读多写少，即时性，一致性要求不高的数据，完全可以使用Spring-Cache）：

            写模式(只要缓存的数据有过期时间就足够应对大部分场景)

            特殊数据：特殊设计
**/

    /*
     * @description 使用SpringCache来减少数据库的查询。sync属性为true表示使用RedisCache中加锁方法get进行查询，控制大并发，防止缓存击穿问题。false - lookup方法查询
     * @date 2022/10/25 10:44
     * @param null
     * @return null
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    public Map<String, List<Catelog2Vo>> getCatalogJsonWithSpringCache() {
        // 查询非空即返回
        String catlogJSON = (String) redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catlogJSON)) {
            // 查询成功直接返回不需要查询DB
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catlogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        // 查询所有一级分类
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        List<CategoryEntity> level1 = getChildrenFromArg0(categoryEntities, 0L);
        Map<String, List<Catelog2Vo>> catelog = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> level2 = getChildrenFromArg0(categoryEntities, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (level2 != null) {
                catelog2Vos =level2.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo =
                            new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getChildrenFromArg0(categoryEntities, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catelog2Vo.Catalog3Vo> catalog3Vos = level3.stream()
                                .map(l3 -> new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(),l3.getName()))
                                .collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return catelog;
    }

    /*
     * @description 使用redisson作为分布式锁
     * @date 2022/10/20 14:57
     * @param null
     * @return null
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonWithRedissonLock() {

        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock(30,TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> dataFromDb;
        try{
            dataFromDb = getCatalogFromDbToJsonType();
        }finally {
            lock.unlock();
        }
        return  dataFromDb;
    }


    /*
     * @description redis分布式锁，满足分布式环境的并发需求
     * @date 2022/10/18 17:03
     * @param null
     * @return null
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        // 设置时间保证满足大部分的业务处理（简单）或者在业务运行中给锁续上时间（复杂）
        Boolean isLocked = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (isLocked){
            Map<String, List<Catelog2Vo>> dataFromDb;
            try{
                dataFromDb = getCatalogFromDbToJsonType();
            }finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                // 执行脚本释放分布式锁，保证了释放锁的原子性
                redisTemplate.execute(
                        new DefaultRedisScript<>(script, Long.class), // 脚本和返回类型
                        Arrays.asList("lock"), // 参数
                        uuid); // 参数值，锁的值
            }
            return  dataFromDb;
        }else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 睡眠0.1s后，重新调用 //自旋方式
            return getCatalogJsonWithRedisLock();
        }

    }


    /*
     * @description 本地锁：查询数据库方法添加本地锁，确保在高并发时只有一个线程对db进行数据查询，但是在分布式中不能确保只有一个线程在执行
     * @date 2022/10/18 15:55
     * @param null
     * @return null
     */
    public synchronized Map<String, List<Catelog2Vo>> getDataFromDbWithLocalLock(){
        return getCatalogFromDbToJsonType();
    }

    /*
     * @description 先查询缓存，在查询数据库
     * @date 2022/10/26 15:06
     * @param null
     * @return null
     */
    public Map<String, List<Catelog2Vo>> getCatalogFromDbToJsonType() {
        // 查询非空即返回
        String catlogJSON = (String) redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catlogJSON)) {
            // 查询成功直接返回不需要查询DB
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catlogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        // 查询所有一级分类
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        List<CategoryEntity> level1 = getChildrenFromArg0(categoryEntities, 0L);
        Map<String, List<Catelog2Vo>> catelog = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> level2 = getChildrenFromArg0(categoryEntities, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (level2 != null) {
                catelog2Vos =level2.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo =
                            new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getChildrenFromArg0(categoryEntities, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catelog2Vo.Catalog3Vo> catalog3Vos = level3.stream()
                                .map(l3 -> new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(),l3.getName()))
                                .collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        String catelogJson = JSON.toJSONString(catelog);
        redisTemplate.opsForValue().set("catelogJson",catelogJson);
        return catelog;
    }

    private List<CategoryEntity> getChildrenFromArg0(List<CategoryEntity> list,Long parent_cid){
        List<CategoryEntity> collect = list.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != 0) {
            findParentPath(category.getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().longValue() == root.getCatId().longValue();  // 注意此处应该用longValue()来比较，否则会出先bug，因为parentCid和catId是long类型
        }).map(categoryEntity -> {
            // 1 找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            // 2 菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }


}
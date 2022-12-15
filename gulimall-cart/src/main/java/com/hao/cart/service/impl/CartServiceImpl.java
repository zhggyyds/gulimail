package com.hao.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hao.cart.feign.ProductFeignService;
import com.hao.cart.interceptor.CartInterceptor;
import com.hao.cart.service.CartService;
import com.hao.cart.to.UserInfoTo;
import com.hao.cart.vo.CartItemVo;
import com.hao.cart.vo.CartVo;
import com.hao.common.constant.cart.CartConstant;
import com.hao.common.to.product.SkuInfoTo;
import com.hao.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @PackageName:com.hao.cart.service.impl
 * @Description:
 * @date 2022/11/24 11:14
 **/
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    /*
     * @description  添加购物车
     * @date 2022/11/24 12:52
     * @param skuId
     * @param num
     */
    public void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> hashOps = getCartHashOps();

        // 获得购物车中的一条购物项的json格式数据
        String cartItemJsonString = (String) hashOps.get(skuId.toString());

        // 购物车中没有这个商品，新增操作
        if (StringUtils.isEmpty(cartItemJsonString)){
            CartItemVo cartItem = new CartItemVo();
            // 异步远程服务获得sku信息
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                R r = productFeignService.skuIfo(skuId);
                if (r.getCode() == 0){
                    SkuInfoTo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTo>() {
                    });
                    cartItem.setSkuId(skuInfo.getSkuId());// 商品ID
                    cartItem.setTitle(skuInfo.getSkuTitle());// 商品标题
                    cartItem.setImage(skuInfo.getSkuDefaultImg());// 商品默认图片
                    cartItem.setPrice(skuInfo.getPrice());// 商品单价
                    cartItem.setCount(num);// 商品件数
                    cartItem.setCheck(true);// 是否选中
                }
            }, executor);

            // 异步远程服务获得销售属性信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues =  productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttrValues(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfo,getSkuSaleAttrValues).get();
            hashOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        }else {
            // 购物车中有这个商品，增加数量
            CartItemVo cartItemVo = JSON.parseObject(cartItemJsonString, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount()+num);
            hashOps.put(skuId.toString(),JSON.toJSONString(cartItemVo));
        }

    }



    @Override
    /*
     * @description 获得购物车数据
     * @date 2022/11/25 15:51
     * @return com.hao.cart.vo.CartVo
     */
    public CartVo getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        CartVo cartVo = new CartVo();

        String tempCartKey = CartConstant.CART_PREFIX+userInfoTo.getUserKey();
        List<CartItemVo> tempCartItems = getCartItems(tempCartKey); // 获取临时购物车的数据

        //登陆
        if (userInfoTo.getUserId() != null){
            String cartKey =CartConstant.CART_PREFIX+userInfoTo.getUserId();
            // 合并购物车
            if (tempCartItems != null){
                for (CartItemVo cartItem : tempCartItems) {
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                clearCart(tempCartKey);
            }
            List<CartItemVo> cartItems = getCartItems(cartKey); //获得合并后的购物车数据
            cartVo.setItems(cartItems);

        }else {
            //未登陆
            cartVo.setItems(tempCartItems);
        }

        return cartVo;
    }

    @Override
    public CartItemVo getCartItem(Long skuId) {
        // 获取购物车redis操作对象
        BoundHashOperations<String, Object, Object> cartOps = getCartHashOps();
        String cartItemJSONString = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(cartItemJSONString, CartItemVo.class);
        return cartItemVo;
    }

    /**
     * 更改购物车商品选中状态
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        // 查询购物车商品信息
        CartItemVo cartItem = getCartItem(skuId);
        // 修改商品选中状态
        cartItem.setCheck(check == 1);
        // 更新到redis中
        BoundHashOperations<String, Object, Object> operations = getCartHashOps();
        operations.put(skuId.toString(), JSONObject.toJSONString(cartItem));
    }

    /**
     * 改变商品数量
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        // 查询购物车商品信息
        CartItemVo cartItem = getCartItem(skuId);
        // 修改商品数量
        cartItem.setCount(num);
        // 更新到redis中
        BoundHashOperations<String, Object, Object> operations = getCartHashOps();
        operations.put(skuId.toString(), JSONObject.toJSONString(cartItem));
    }

    /**
     * 删除购物项
     */
    @Override
    public void deleteIdCartInfo(Integer skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartHashOps();
        operations.delete(skuId.toString());
    }


    /*
    远程调用
    * 获得当前购物车选中项，进行结算
    * */
    @Override
    public List<CartItemVo> getCheckedCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null){ //未登陆
            return null;
        }else {//登陆
            List<CartItemVo> cartItems = getCartItems(CartConstant.CART_PREFIX + userInfoTo.getUserId());// 获得所有购物车项
            if (cartItems != null){
                // 筛选出选中项
                List<CartItemVo> collect = cartItems.stream()
                        .filter(item -> item.getCheck())
                        .map(item -> {
                            BigDecimal price = productFeignService.getPrice(item.getSkuId()); // 获得最新的价格，防止数据库价格信息并不是最新的而是当时加入购物车的信息
                            item.setPrice(price);
                            return item;
                        }).collect(Collectors.toList());
                return collect;
            }else {//购物车为空
                return null;
            }

        }
    }

    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /*
     * @description 获得redis中某个购物车的所有商品数据，并将json数据封装成cartVo
     * @date 2022/11/25 15:51
     * @param cartKey
     * @return List<CartItemVo>
     */
    private List<CartItemVo> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

        List<Object> values = operations.values(); //获得hash中所有的值
        if (values != null){
            // 将封装好的json格式进行返回
            return values.stream().map((item) -> {
                String json = (String) item;
                return JSON.parseObject(json, CartItemVo.class);
            }).collect(Collectors.toList());
        }
        return null;

    }

    /*
     * @description  获得redis中相关购物车的操作对象
     * @date 2022/12/3 20:14
     * @param null
     * @return null
     */
    private BoundHashOperations<String, Object, Object> getCartHashOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey ="";

        // 处理redis中购物车的key
        // 登陆
        if (userInfoTo.getUserId() != null){
            cartKey = CartConstant.CART_PREFIX+userInfoTo.getUserId();
        }else {
            // 未登陆
            cartKey = CartConstant.CART_PREFIX+userInfoTo.getUserKey();
        }

        // 获得redis中某个购物车（hash）的数据的操作对象
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

        return operations;
    }


}

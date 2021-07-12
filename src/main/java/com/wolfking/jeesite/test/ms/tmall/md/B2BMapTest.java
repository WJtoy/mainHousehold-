package com.wolfking.jeesite.test.ms.tmall.md;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BProductMapping;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.ms.tmall.md.dao.B2bCustomerMapDao;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bProductMap;
import com.wolfking.jeesite.ms.tmall.md.mapper.B2bCustomerMapMapper;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.tmall.md.service.B2bProductMapService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.NameValuePair;
import org.assertj.core.util.Lists;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试B2B与工单系统各种关联
 */
//@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class B2BMapTest {

    @Autowired
    private B2bCustomerMapService customerMapService;

    @Autowired
    private B2bProductMapService productMapService;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private B2bCustomerMapDao dao;

    @Autowired
    private GsonRedisSerializer gsonRedisSerializer;

    @Test
    public void testGetCustomerMap() {
        Long customerId = customerMapService.getCustomerIdByShopId(2,"123");
        System.out.println("cusomterId:" + customerId.toString());
        Assert.assertTrue("ok",customerId>0);
    }

    @Test
    public void testGetShopsMap() {
        List<B2bCustomerMap> shops = customerMapService.getShopListByCustomer(2,1482L);//1625
        //log.info(shops.stream().collect(Collectors.joining(",")));
        log.info(GsonUtils.toGsonString(shops));
        Assert.assertTrue("ok",shops.size()>0);
    }

    @Test
    public void testRedis(){
        String json = "[{\"name\":\"森太旗舰店\",\"value\":\"500295137\"}]";
        List<NameValuePair> list = GsonUtils.getInstance().getGson().fromJson(json,new TypeToken<List<NameValuePair>>() {}.getType());
        Assert.assertTrue("ok",list.size()>0);
        log.info("list size:" + list.size());
    }

    @Test
    public void testGetAllShopList(){
        List<B2bCustomerMap> shops = customerMapService.getAllShopList(2);
        log.info(GsonUtils.toGsonString(shops));
        Assert.assertTrue("ok",shops.size()>0);
    }

    @Test
    public void testGetProductMap() {
        List<Long> productIds = productMapService.getProductIdByShopId(2,"500295137","350503");
        log.info("productId:" + productIds.stream().map(t->t.toString()).collect(Collectors.joining(",")));
        Assert.assertTrue("ok",productIds.size()>0);
    }


    @Test
    public void testGetShopByCustomerRedis(){
        final List<NameValuePair> shops = Lists.newArrayList();
        int dataSource = 2;
        Long customerId = 1584l;
        String key = new String("");
        key = String.format(RedisConstant.B2B_CUSTOMER_TO_SHOPID_LIST, dataSource);
        /*
        List<B2bCustomerMap> maps = dao.getShopListByCustomer(dataSource, customerId);
        if (maps != null && maps.size() > 0) {
            shops.addAll(B2bCustomerMapMapper.INSTANCE.toPairs(maps));
            //redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_B2B_DB, key, GsonUtils.getInstance().toGson(shops), customerId, 0L);
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_B2B_DB, key, shops, customerId, 0L);
        }
        */
        //from cache
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_B2B_DB, key)) {
            StringBuffer json = new StringBuffer(500);
            Set<byte[]> sets = redisUtils.zRangeByScore(RedisConstant.RedisDBType.REDIS_B2B_DB, key, customerId, customerId);
            if(sets==null || sets.size() == 0){
                log.error("sets is empty");
                return;
            }
            if (sets != null && sets.size() > 0) {
                sets.stream().forEach(t -> {
                    JsonArray arry = (JsonArray)gsonRedisSerializer.deserialize(t,JsonArray.class);
                    for (JsonElement jsonElement : arry) {
                        shops.add(gsonRedisSerializer.getGson().fromJson(jsonElement, NameValuePair.class));
                    }
                    /*
                    Object obj = gsonRedisSerializer.deserialize(t);
                    log.info(obj.getClass().getName());
                    shops.addAll((List<NameValuePair>)obj);
                    */
                });
                shops.stream().forEach(t->{
                    log.info("{}={}",t.getName(),t.getValue());
                });
            }
            /*
                    List<NameValuePair> list = redisUtils.zRangeByScore(RedisConstant.RedisDBType.REDIS_B2B_DB, key, customerId, customerId,
                    new TypeToken<List<NameValuePair>>() {}.getRawType().getClass());
            list.stream().forEach(t->{
                log.info("{}={}",t.getName(),t.getValue());
            });
            */
        }
    }

    @Test
    public void testB2BProductMappingGroup(){
        List<B2BProductMapping> list = Lists.newArrayList();
        //region add entitys
        B2BProductMapping entity = new B2BProductMapping();
        entity.setDataSource(B2BDataSourceEnum.TMALL.id);
        entity.setProductId(75l);
        entity.setProductName("油烟机-A30001-01");
        //entity.setCustomerCategoryId("");
        //entity.setCustomerCategoryId("350511");
        entity.setProductCode("A30001-01");
        entity.setCreateById(1l);
        entity.setCreateDate(new Date());
        entity.setUpdateById(1l);
        entity.setUpdateDate(new Date());
        list.add(entity);
        //2
        entity = new B2BProductMapping();
        entity.setDataSource(B2BDataSourceEnum.TMALL.id);
        entity.setProductId(75l);
        entity.setProductName("油烟机-A30001-02");
        //entity.setCustomerCategoryId("350511");
        //entity.setCustomerCategoryId("");
        entity.setProductCode("A30001-02");
        entity.setCreateById(1l);
        entity.setCreateDate(new Date());
        entity.setUpdateById(1l);
        entity.setUpdateDate(new Date());
        list.add(entity);

        //3
        entity = new B2BProductMapping();
        entity.setDataSource(B2BDataSourceEnum.TMALL.id);
        entity.setProductId(75l);
        entity.setProductName("油烟机-A40001");
        entity.setCustomerCategoryId("568808148211");
        entity.setProductCode("A40001");
        entity.setCreateById(1l);
        entity.setCreateDate(new Date());
        entity.setUpdateById(1l);
        entity.setUpdateDate(new Date());
        list.add(entity);

        //4
        entity = new B2BProductMapping();
        entity.setDataSource(B2BDataSourceEnum.TMALL.id);
        entity.setProductId(75l);
        entity.setProductName("油烟机-7243");
        entity.setCustomerCategoryId("575713147243");
        entity.setProductCode("");
        entity.setCreateById(1l);
        entity.setCreateDate(new Date());
        entity.setUpdateById(1l);
        entity.setUpdateDate(new Date());
        list.add(entity);

        //5
        entity = new B2BProductMapping();
        entity.setDataSource(B2BDataSourceEnum.TMALL.id);
        entity.setProductId(75l);
        entity.setProductName("油烟机-8211");
        entity.setCustomerCategoryId("568808148211");
        entity.setProductCode("");
        entity.setCreateById(1l);
        entity.setCreateDate(new Date());
        entity.setUpdateById(1l);
        entity.setUpdateDate(new Date());
        list.add(entity);

        //endregion

        //region list to map
        /*
        Map<String,Map<String,List<B2BProductMapping>>> productMap;
        productMap = list.stream().collect(Collectors.groupingBy(B2BProductMapping::getCustomerCategoryId,Collectors.groupingBy(B2BProductMapping::getProductCode)));
        for (Map.Entry<String,Map<String,List<B2BProductMapping>>> entry : productMap.entrySet()) {
            log.info("categoryId:{}",entry.getKey());
            for(Map.Entry<String,List<B2BProductMapping>> subEntry:entry.getValue().entrySet()){
                log.info("  productCode:{}",subEntry.getKey());
                for (B2BProductMapping item : subEntry.getValue()) {
                    log.info(
                            "       dataSource:{} ,category:{} ,productCode:{},productId:{} ,productName:{}",
                            item.getId(),
                            item.getDataSource(),
                            item.getCustomerCategoryId(),
                            item.getProductCode(),
                            item.getProductId(),
                            item.getProductName()
                            );
                }
            }
        }
        */
        //to map
        Map<String,Map<String,B2BProductMapping>> productMap;
        productMap = list.stream().collect(Collectors.groupingBy(B2BProductMapping::getCustomerCategoryId,Collectors.toMap(B2BProductMapping::getProductCode, item -> item)));
        for (Map.Entry<String,Map<String,B2BProductMapping>> entry : productMap.entrySet()) {
            log.info("categoryId:{}",entry.getKey());
            for(Map.Entry<String,B2BProductMapping> subEntry:entry.getValue().entrySet()){
                log.info(
                        "   productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                        subEntry.getKey(),
                        subEntry.getValue().getDataSource(),
                        subEntry.getValue().getCustomerCategoryId(),
                        subEntry.getValue().getProductId(),
                        subEntry.getValue().getProductName()
                );
            }
        }
        //endregion

        //region search
        /*
        B2BProductMapping product = productMap.get("").get("A30001-03");
        if(product == null){
            log.error("get[A30001-03] -- has no");
        }else{
            log.info(
                    "get[A30001-03] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }

        product = productMap.get("").get("A30001-02");
        if(product == null){
            log.error("get[A30001-02] --has no");
        }else{
            log.info(
                    "get[A30001-02] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }

        product = productMap.get("568808148211").get("A40001");
        if(product == null){
            log.error("get[568808148211:A40001] --has no");
        }else{
            log.info(
                    "get[568808148211:A40001] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }

        product = productMap.get("568808148211").get("A40002");
        if(product == null){
            log.error("get[568808148211:A40002] --has no");
        }else{
            log.info(
                    "get[568808148211:A40002] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }
        */
        B2BProductMapping product = getProductMapping(productMap,"","A30001-03");
        if(product == null){
            log.error("get[:A30001-03] -- has no");
        }else{
            log.info(
                    "get[:A30001-03] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }

        product = getProductMapping(productMap,"","A30001-02");
        if(product == null){
            log.error("get[:A30001-02] --has no");
        }else{
            log.info(
                    "get[:A30001-02] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }

        product = getProductMapping(productMap,"568808148211","A40001");
        if(product == null){
            log.error("get[568808148211:A40001] --has no");
        }else{
            log.info(
                    "get[568808148211:A40001] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }

        product = getProductMapping(productMap,"568808148211","A40002");
        if(product == null){
            log.error("get[568808148211:A40002] --has no");
        }else{
            log.info(
                    "get[568808148211:A40002] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }

        product = getProductMapping(productMap,"575713147243","");
        if(product == null){
            log.error("get[575713147243:] --has no");
        }else{
            log.info(
                    "get[575713147243:] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }
        product = getProductMapping(productMap,"568808148211","123");
        if(product == null){
            log.error("get[568808148211:123] --has no");
        }else{
            log.info(
                    "get[568808148211:123] -- productCode:{}, dataSource:{} ,category:{} ,productId:{} ,productName:{}",
                    product.getProductCode(),
                    product.getDataSource(),
                    product.getCustomerCategoryId(),
                    product.getProductId(),
                    product.getProductName()
            );
        }
        //endregion
    }

    private static B2BProductMapping getProductMapping(Map<String,Map<String,B2BProductMapping>> map,String category,String productCode) {
        if(map == null || map.isEmpty()){
            return null;
        }
        if(category==null){
            category = "";
        }
        if(productCode == null){
            productCode = "";
        }
        Map<String,B2BProductMapping> subMap;
        //1.category
        subMap = map.get(category);
        if(subMap == null){
            return null;
        }
        //2.productCode
        B2BProductMapping product = subMap.get(productCode);
        if(product==null && StringUtils.isNotBlank(productCode)){
            product = subMap.get("");
        }
        return product;
    }
}

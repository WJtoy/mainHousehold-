package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.CustomerProductCompletePicDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.utils.ProductCompletePicItemMapper;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductPicMappingService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 品牌Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerProductCompletePicService extends LongIDCrudService<CustomerProductCompletePicDao, ProductCompletePic> {

    /*
    // mark on 2020-1-11
    @Autowired
    private RedisUtils redisUtils;
    */

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService  customerService;

    /*
    // mark on 2020-1-11
    @Autowired
    private CustomerProductCompletePicDao customerPicDao;
    */
    @Autowired
    private MSCustomerService msCustomerService;
    @Autowired
    private ProductCompletePicService completePicService;

    @Autowired
    private MSCustomerProductPicMappingService msCustomerProductPicMappingService;


    /**
     * 添加或修改数据
     * @param model
     */
    @Override
    @Transactional()
    public void save(ProductCompletePic model) {
        boolean isNew = model.getIsNewRecord();
        model.toJsonInfo();
        //super.save(model);   //mark on 2020-1-11
        //调用微服务 2019-9-24 start
        MSErrorCode msErrorCode = msCustomerProductPicMappingService.save(model, isNew);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("保存客户完工信息失败.失败原因:"+msErrorCode.getMsg());
        }
        //end
        /*
        // mark on 2020-1-11
        String customerKey = String.format(RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC,model.getCustomer().getId());
        model.setCreateBy(null);
        model.setCreateDate(null);
        redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, customerKey, model, model.getProduct().getId(), 0);
         */
    }

    /**
     * 删除数据
     * @param model
     */
    @Override
    @Transactional()
    public void delete(ProductCompletePic model) {
        //super.delete(model);   // mark on 2020-1-11
        //调用微服务 2019-9-25 start
        MSErrorCode msErrorCode = msCustomerProductPicMappingService.delete(model);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除客户完工信息失败.失败原因:"+msErrorCode.getMsg());
        }
        //end 2019-9-25
        // mark on 2020-1-11 begin
        /*
        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC, model.getProduct().getId(), model.getProduct().getId()) > 0) {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC, model.getProduct().getId(), model.getProduct().getId());
        }
         */
        // mark on 2020-1-11 end
    }

    /**
     * 根据Id获取
     * @param id
     */
    @Override
    public ProductCompletePic get(long id){
        //调用微服务 2019-9-25
        return msCustomerProductPicMappingService.getById(id);
    }



    /**
     * 分页查询
     */
    public Page<ProductCompletePic> findPage(Page<ProductCompletePic> page, ProductCompletePic entity) {
        entity.setPage(page);
        //List<ProductCompletePic> list = dao.findList(entity);
        Page<ProductCompletePic> productCompletePicPage = msCustomerProductPicMappingService.findList(page,entity);
        List<ProductCompletePic> list = productCompletePicPage.getList();
        if (list == null) {
            list = Lists.newArrayList();
        }
        if (list.isEmpty()) {
            page.setList(list);
            return page;
        }
        syncItemInfoFromDict(list);
        page.setCount(productCompletePicPage.getCount());
        page.setList(list);
        return page;
    }

    /**
     * 从数据字典中同步标题，排序，说明等信息,并读取产品信息
     *
     * @param list
     */
    public void syncItemInfoFromDict(List<ProductCompletePic> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Dict> dicts = MSDictUtils.getDictList(ProductCompletePic.DICTTYPE);
        if (dicts == null || dicts.isEmpty()) {
            return;
        }
        List<ProductCompletePicItem> items = Mappers.getMapper(ProductCompletePicItemMapper.class).listToPicItem(dicts);
        Map<String, ProductCompletePicItem> itemMaps = items.stream().collect(Collectors.toMap(ProductCompletePicItem::getPictureCode, item -> item));
        dicts = null;
        ProductCompletePicItem item;
        Product product;
        Customer customer;
        Set<Long> customerIds = list.stream().map(r->r.getCustomer().getId()).collect(Collectors.toSet());
        Map<Long, Customer> customerMap = msCustomerService.findCutomersWithIdsToMap(Lists.newArrayList(customerIds));
        Set<Long> productIds = list.stream().map(r->r.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.getProductMap(Lists.newArrayList(productIds));
        for (ProductCompletePic customerCompletePic : list) {

            customerCompletePic.parseItemsFromJson();//json to list
            product = productMap.get(customerCompletePic.getProduct().getId());
            customer = customerMap.get(customerCompletePic.getCustomer().getId());
            if (product != null) {
                customerCompletePic.setProduct(product);
            }
            if(customer !=null){
                customerCompletePic.setCustomer(customer);
            }
            if(!customerCompletePic.getItems().isEmpty()) {
                for (ProductCompletePicItem itm : customerCompletePic.getItems()) {
                    if (itemMaps.containsKey(itm.getPictureCode())) {
                        item = itemMaps.get(itm.getPictureCode());
                        itm.setTitle(item.getTitle());
                        itm.setRemarks(item.getRemarks());
                        itm.setSort(item.getSort());
                    }
                }
                customerCompletePic.setItems(customerCompletePic.getItems().stream().sorted(Comparator.comparing(ProductCompletePicItem::getSort)).collect(Collectors.toList()));
            }
        }
    }


    /**
     * 从数据字典中同步标题，排序，说明等信息
     *
     * @param entity
     */
    public void syncItem(ProductCompletePic entity){
        if(entity==null){
            return;
        }
        List<Dict> dicts = MSDictUtils.getDictList(ProductCompletePic.DICTTYPE);
        if (dicts == null || dicts.isEmpty()) {
            return;
        }
        List<ProductCompletePicItem> items = Mappers.getMapper(ProductCompletePicItemMapper.class).listToPicItem(dicts);
        Map<String, ProductCompletePicItem> itemMaps = items.stream().collect(Collectors.toMap(ProductCompletePicItem::getPictureCode, item -> item));
        entity.parseItemsFromJson();
        ProductCompletePicItem item;
        if (!entity.getItems().isEmpty()) {
            for (ProductCompletePicItem itm : entity.getItems()) {
                if (itemMaps.containsKey(itm.getPictureCode())) {
                    item = itemMaps.get(itm.getPictureCode());
                    itm.setTitle(item.getTitle());
                    itm.setRemarks(item.getRemarks());
                    itm.setSort(item.getSort());
                }
            }
            entity.setItems(entity.getItems().stream().sorted(Comparator.comparing(ProductCompletePicItem::getSort)).collect(Collectors.toList()));
        }
    }

    /**
     * 补充产品完工图片类型
     */
    public List<ProductCompletePicItem> mergeAllItems(List<ProductCompletePicItem> items, List<Dict> dicts) {
        Set<ProductCompletePicItem> hasSet = null;
        if (items == null) {
            items = Lists.newArrayList();
            hasSet = Sets.newHashSet();
        } else {
            hasSet = items.stream().collect(Collectors.toSet());
        }

        if (dicts != null && !dicts.isEmpty()) {
            Set<ProductCompletePicItem> dictSet = Mappers.getMapper(ProductCompletePicItemMapper.class).listToPicItem(dicts).stream().collect(Collectors.toSet());
            hasSet.addAll(dictSet);
            //hasSet = Sets.union(hasSet, dictSet);
        }
        items.clear();
        items = hasSet.stream().sorted(Comparator.comparing(ProductCompletePicItem::getSort)).collect(Collectors.toList());
        return items;
    }

    /**
     * 物理删除
     * @param entity
     */
    public void deleteById(ProductCompletePic entity){
        //customerPicDao.deleteById(entity.getId());  // mark on 2020-1-11
        //调用微服务 2019-9-25 start
        MSErrorCode msErrorCode = msCustomerProductPicMappingService.delete(entity);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除客户完工信息失败.失败原因:"+msErrorCode.getMsg());
        }
        //end 2019-9-25
        /*
        // mark on 2020-1-11
        String key = String.format(RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC,entity.getCustomer().getId());
        if(redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, key, entity.getProduct().getId(), entity.getProduct().getId()) > 0){
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, key, entity.getProduct().getId(), entity.getProduct().getId());
        }
         */
    }


    /**
     *获取已配置产品图片规格
     * @return
     */
    public ProductCompletePic getCompletePicItem(Long customerId,Long productId){
        ProductCompletePic productCompletePic = getFromCache(productId,customerId);
        if(productCompletePic == null || productCompletePic.getId() == null){
            productCompletePic = completePicService.getFromCache(productId);
        }
        if(productCompletePic == null){
            productCompletePic = new ProductCompletePic();
        }
        List<Dict> dicts = MSDictUtils.getDictList(ProductCompletePic.DICTTYPE);
        if (dicts == null || dicts.isEmpty()) {
            return productCompletePic;
        }
        List<ProductCompletePicItem> items = Mappers.getMapper(ProductCompletePicItemMapper.class).listToPicItem(dicts);
        Map<String, ProductCompletePicItem> itemMaps = new HashMap<>();
        if(productCompletePic.getItems() != null) {
            itemMaps = productCompletePic.getItems().stream().collect(Collectors.toMap(ProductCompletePicItem::getPictureCode, item -> item));
        }
        productCompletePic.parseItemsFromJson();
        ProductCompletePicItem item;

        for (ProductCompletePicItem itm : items) {
            if (itemMaps.containsKey(itm.getPictureCode())) {
                item = itemMaps.get(itm.getPictureCode());
                itm.setChecked(item.getChecked());
                itm.setTitle(item.getTitle());
                itm.setMustFlag(item.getMustFlag());
                itm.setRemarks(item.getRemarks());
                itm.setSort(item.getSort());
            }
        }
        productCompletePic.setItems(items.stream().sorted(Comparator.comparing(ProductCompletePicItem::getSort)).collect(Collectors.toList()));

        return productCompletePic;
    }


    /****************************************************************************
     * redis操作
     ****************************************************************************/

    /**
     * 从数据库读取信息至缓存
     *
     * @return
     */
    /*
    // mark on 2020-1-11 begin
    private List<ProductCompletePic> loadDataFromDB2Cache(Long customerId) {
        List<ProductCompletePic> list = customerPicDao.getAllList(customerId);
        if(list!=null && list.size()>0){
            String customerKey = String.format(RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC,customerId);
            syncItemInfoFromDict(list);
            Set<RedisCommands.Tuple> set = list.stream().map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getProduct().getId().doubleValue())).collect(Collectors.toSet());
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, customerKey, set, -1);
        }
        return list;
    }
    // mark on 2020-1-11 end
    */

    /**
     * 取出客户所有数据
     *
     * @param customerId
     * @return
     */
    /*
    // mark on 2020-1-11 begin
    public List<ProductCompletePic> findAllList(long customerId) {
        String key = String.format(RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC,customerId);
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, key);
        if (!isExistsCache) {
            return null;
        }
        List<ProductCompletePic> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, key, 0, -1, ProductCompletePic.class);
        return list;
    }
    // mark on 2020-1-11 end
    */

    /**
     * 优先从缓存中按id获得对象
     *
     * @param productId 产品id
     * @param customerId
     * @return
     */
    public ProductCompletePic getFromCache(long productId,long customerId) {
        //调用微服 2019-9-25 start
        ProductCompletePic productCompletePic = msCustomerProductPicMappingService.getCustomerProductPicByProductAndCustomer(customerId,productId);
        if(productCompletePic !=null){
            return productCompletePic;
        }
        return null;
        //end 2019-9-25

        /*
        // mark on 2020-1-11 begin
        String key = String.format(RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC,customerId);
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, key);
        if (!isExistsCache) {
            return null;
        }
        try {
            ProductCompletePic entity = (ProductCompletePic) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, key, productId, productId, ProductCompletePic.class);
            syncItem(entity);
            return entity;

        } catch (Exception e) {
            return null;
        }
        // mark on 2020-1-11 end
        */
    }

    /**
     * 从数据库读取信息至缓存
     *
     * @return
     */
    /*
    // mark on 2020-1-11 begin
    public void loadDataFromDB2Cache() {
        //List<ProductCompletePic> list = super.findAllList();
        //调用微服务 2019-9-26
        List<ProductCompletePic> list = msCustomerProductPicMappingService.findAllList();
        Map<Long,List<ProductCompletePic>> map = new HashMap<>();
        List<ProductCompletePic> completePics =null;
        if (list != null && !list.isEmpty()) {
            syncItemInfoFromDict(list);
            for(ProductCompletePic entity:list){
                if(map.containsKey(entity.getCustomer().getId())){
                    map.get(entity.getCustomer().getId()).add(entity);
                }else{
                    completePics = new ArrayList<>();
                    completePics.add(entity);
                    map.put(entity.getCustomer().getId(),completePics);
                }
            }
            for (Map.Entry<Long, List<ProductCompletePic>> entry : map.entrySet()) {
                String customerKey = String.format(RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC,entry.getKey());
                Set<RedisCommands.Tuple> set = entry.getValue().stream().map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getProduct().getId().doubleValue())).collect(Collectors.toSet());
                redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, customerKey, 0, -1);
                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, customerKey, set, -1);
            }
        }
    }
    // mark on 2020-1-11 end
    */

    public Map<Long, ProductCompletePic> getProductCompletePicMap(List<Long> productIds, Long customerId) {
        if (productIds == null || productIds.isEmpty() || customerId == null) {
            return Maps.newHashMap();
        }
        //调用微服务 2019-9-25 start
        Map<Long,ProductCompletePic> map = msCustomerProductPicMappingService.findCustomerProductPicMap(productIds,customerId);
        if(map!=null && map.size()>0){
            return map;
        }
        return Maps.newHashMap();

        /*
        //mark on 2020-1-11 begin
        String key = String.format(RedisConstant.MD_CUSTOMER_PRODUCT_COMPLETE_PIC,customerId);
        if (!redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, key)) {
            return Maps.newHashMap();
        }
        List<ProductCompletePic> list = redisUtils.getObjFromZSetByIds(RedisConstant.RedisDBType.REDIS_MD_DB, key, productIds, ProductCompletePic.class);
        Map<Long, ProductCompletePic> productCompletePicMap = list.stream().collect(Collectors.toMap(i -> i.getProduct().getId(), i -> i));
        return productCompletePicMap;
        //mark on 2020-1-11 end
        */
    }

}

package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.md.dao.ProductCompletePicDao;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.md.utils.ProductCompletePicItemMapper;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.providermd.service.MSProductPicMappingService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 品牌Service
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ProductCompletePicService extends LongIDCrudService<ProductCompletePicDao, ProductCompletePic> {

    /*@Autowired
    private RedisUtils redisUtils;
*/
    @Autowired
    private ProductService productService;

    @Autowired
    private MSProductPicMappingService msProductPicMappingService;

    @Override
    public ProductCompletePic get(long id) {
        // add on 2019-9-3
        return msProductPicMappingService.get(id);
    }

    @Override
    @Transactional(readOnly = false)
    public void save(ProductCompletePic model) {
        model.toJsonInfo();
        boolean isNew = model.getIsNewRecord();  // add on 2019-9-3
        //super.save(model); //mark on 2020-1-10
        // add on 2019-9-3 begin
        // ProductPicMapping微服务
        MSErrorCode msErrorCode = msProductPicMappingService.save(model, isNew);
        if (msErrorCode.getCode() >0) {
            throw  new RuntimeException("保存完工图片失败。失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-3 end
        /*
        // mark on 2020-1-10
        if (redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL)) {
            redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL, model, model.getProduct().getId(), 0);
        }
        */
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(ProductCompletePic model) {
        //super.delete(model);  //mark on 2020-1-10
        // add on 2019-9-3 begin
        // ProductPicMapping微服务
        MSErrorCode msErrorCode = msProductPicMappingService.delete(model);
        if (msErrorCode.getCode() >0) {
            throw  new RuntimeException("删除完工图片失败。失败原因:"+msErrorCode.getMsg());
        }
        // add on 2019-9-3 end
        /*
        // mark on 2020-1-10
        if (redisUtils.zCount(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL, model.getProduct().getId(), model.getProduct().getId()) > 0) {
            redisUtils.zRemRangeByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL, model.getProduct().getId(), model.getProduct().getId());
        }
        */
    }

    /**
     * 分页查询
     */
    public Page<ProductCompletePic> findPage(Page<ProductCompletePic> page, ProductCompletePic entity) {

        // mark on 2019-9-3
        /*entity.setPage(page);
        List<ProductCompletePic> list = dao.findList(entity);
        if (list == null) {
            list = Lists.newArrayList();
        }
        if (list.isEmpty()) {
            page.setList(list);
            return page;
        }
        syncItemInfoFromDict(list);
        page.setList(list);
        return page;*/

        // add on 2019-9-3 begin
        // ProductCompletePic微服务
        Page<ProductCompletePic> picPage = msProductPicMappingService.findList(page, entity);
        List<ProductCompletePic> list = Lists.newArrayList();
        if (picPage != null && picPage.getList() != null && !picPage.getList().isEmpty()) {
            list = picPage.getList();
            syncItemInfoFromDict(list);
            picPage.setList(list);
        }
        return picPage;
        // add on 2019-9-3 end
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
        ProductCompletePic m;
        ProductCompletePicItem item;
        Product product;
        for (int i = 0, size = list.size(); i < size; i++) {
            m = list.get(i);
            m.parseItemsFromJson();//json to list
            /*
            // mark on 2020-4-29
            product = productService.getProductByIdFromCache(m.getProduct().getId());
            if (product != null) {
                m.setProduct(product);
            }
            */
            if (!m.getItems().isEmpty()) {
                for (ProductCompletePicItem itm : m.getItems()) {
                    if (itemMaps.containsKey(itm.getPictureCode())) {
                        item = itemMaps.get(itm.getPictureCode());
                        itm.setTitle(item.getTitle());
                        itm.setRemarks(item.getRemarks());
                        itm.setSort(item.getSort());
                    }
                }
                m.setItems(m.getItems().stream().sorted(Comparator.comparing(ProductCompletePicItem::getSort)).collect(Collectors.toList()));
            }
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

    /****************************************************************************
     * redis操作
     ****************************************************************************/

    /**
     * 从数据库读取信息至缓存
     *
     * @return
     */
    /*
    // mark on 2020-1-10 begin
    private List<ProductCompletePic> loadDataFromDB2Cache() {
//        List<ProductCompletePic> list = super.findAllList(); //mark on 2019-9-3
        List<ProductCompletePic> list = msProductPicMappingService.findAllList(); //add on 2019-9-3 //ProductPicMapping
        if (list != null && !list.isEmpty()) {
            syncItemInfoFromDict(list);
            Set<RedisCommands.Tuple> set = list.stream().map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getProduct().getId().doubleValue())).collect(Collectors.toSet());
            redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL, set, -1);
        }
        return list;
    }
    // mark on 2020-1-10 end
    */

    /**
     * 加载所有，当缓存未命中则从数据库装载至缓存
     *
     * @return
     */
    @Override
    public List<ProductCompletePic> findAllList() {
        /*
        // mark on 2020-1-10 begin
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL);
        if (!isExistsCache) {
            return loadDataFromDB2Cache();
        }
        List<ProductCompletePic> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL, 0, -1, ProductCompletePic.class);
        return list;
        // mark on 2020-1-10 end
        */

        List<ProductCompletePic> list = msProductPicMappingService.findAllList(); //add on 2019-9-3 //ProductPicMapping
        if (list != null && !list.isEmpty()) {
            syncItemInfoFromDict(list);
        }
        return list;
    }

    /**
     * 优先从缓存中按id获得对象
     *
     * @param prouctId 产品id
     * @return
     */
    public ProductCompletePic getFromCache(long prouctId) {
        /*
        // mark on 2020-1-10 begin
        boolean isExistsCache = redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL);
        if (!isExistsCache) {
            loadDataFromDB2Cache();
        }
        try {
            return (ProductCompletePic) redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL, prouctId, prouctId, ProductCompletePic.class);
        } catch (Exception e) {
            return null;
        }
        // mark on 2020-1-10 end
        */
        ProductCompletePic productCompletePic =  msProductPicMappingService.getByProductId(prouctId);
        if(productCompletePic == null){
            return productCompletePic;
        }
        List<ProductCompletePic> lists = Lists.newArrayList();
        lists.add(productCompletePic);
        syncItemInfoFromDict(lists);
        if (lists != null && !lists.isEmpty()) {
            return lists.get(0);
        }
        return null;
    }

    public Map<Long, ProductCompletePic> getProductCompletePicMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Maps.newHashMap();
        }
        /*
        // mark on 2020-1-10 begin
        if (!redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL)) {
            loadDataFromDB2Cache();
        }
        List<ProductCompletePic> list = redisUtils.getObjFromZSetByIds(RedisConstant.RedisDBType.REDIS_MD_DB, RedisConstant.MD_PRODUCT_COMPLETE_PIC_ALL, productIds, ProductCompletePic.class);
        Map<Long, ProductCompletePic> productCompletePicMap = list.stream().collect(Collectors.toMap(i -> i.getProduct().getId(), i -> i));
        return productCompletePicMap;
        // mark on 2020-1-10 end
        */
        List<ProductCompletePic> list = findAllList();
        if (list != null && !list.isEmpty()) {
            list = list.stream().filter(x -> productIds.contains(x.getProduct().getId())).collect(Collectors.toList());
        }
        if (list != null && !list.isEmpty()) {
            Map<Long, ProductCompletePic> productCompletePicMap = list.stream().collect(Collectors.toMap(i -> i.getProduct().getId(), i -> i));
            return productCompletePicMap;
        }
        return Maps.newHashMap();
    }

}

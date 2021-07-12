package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.ProductCategoryDao;
import com.wolfking.jeesite.modules.md.dao.ProductDao;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryService;
import com.wolfking.jeesite.ms.providermd.service.MSProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Jeff on 2017/4/24.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ProductCategoryService extends LongIDCrudService<ProductCategoryDao, ProductCategory> {

    @Autowired
    private ProductService productService;

//    @Autowired
//    private MSProductCategoryService msProductCategoryService;

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;

    @Autowired
    private MSProductService msProductService;

    /*
    // mark on 2020-3-16 begin
    @Override
    public ProductCategory get(long id) {
        // return super.get(id);   // mark on 2019-8-12
        return msProductCategoryService.getById(id);  // add on 2019-8-12
    }
    // mark on 2020-3-16 end
    */

    @Override
    @Transactional()
    public void save(ProductCategory productCategory){
        boolean isNew = productCategory.getIsNewRecord();  // add on 2019-8-12
        // add on 2019-8-12 begin
        // mark on 2020-4-1 begin
//        MSErrorCode msErrorCode = msProductCategoryService.save(productCategory, isNew);
//        if (msErrorCode.getCode() >0 ) {
//            throw new RuntimeException("调用产品类别微服务保存数据异常。异常原因:" + msErrorCode.getMsg());
//        }
        // mark on 2020-4-1 end
        // add on 2019-8-12 end
        msProductCategoryNewService.save(productCategory,isNew); //add on 2020-4-1
    }

    @Override
    @Transactional()
    public void delete(ProductCategory productCategory){
        // add on 2019-8-12 begin
        // mark on 2020-4-1 begin
//        MSErrorCode msErrorCode = msProductCategoryService.delete(productCategory);
//        if (msErrorCode.getCode() >0 ) {
//            throw new RuntimeException("调用产品类别微服务删除数据异常。异常原因:" + msErrorCode.getMsg());
//        }
        // mark on 2020-4-1 end
        // add on 2019-8-12 end
        msProductCategoryNewService.delete(productCategory);
    }

    /**
     * 根据产品分类编码获取产品分类ID，最多一条，用于判断产品分类编码是否存在于数据库中
     * @param productCategory
     * @return
     */
    public Boolean isExistProductCategoryCode(ProductCategory productCategory){
        //Long id = msProductCategoryService.getIdByCode(productCategory.getCode());  //mark on 2020-4-1
        Long id = msProductCategoryNewService.getIdByCodeForMD(productCategory.getCode()); //add on 2020-4-1
        return id==null?false:(id.equals(productCategory.getId())?false:true);
    }

    /**
     * 根据产品分类名称获取配件ID，最多一条，用于判断产品分类名称是否存在于数据库中
     * @param productCategory
     * @return
     */
    public Boolean isExistProductCategoryName(ProductCategory productCategory){
        //Long id = msProductCategoryService.getIdByName(productCategory.getName());  //mark on 2020-4-1
        Long id = msProductCategoryNewService.getIdByNameForMD(productCategory.getName());  //add on 2020-4-1
        return id==null?false:(id.equals(productCategory.getId())?false:true);
    }

    /**
     * 产品分类下是否存在产品
     * @param productCategoryId
     * @return
     */
    public boolean isExistProductByCategoryId(Long productCategoryId){
        return msProductService.getIdByProductCategoryId(productCategoryId) != null;  // add on 2019-8-16 // 调用Product微服务
    }

    /**
     * 加载产品目录，当缓存未命中则从数据库装载至缓存
     * @return
     */
    @Override
    public List<ProductCategory> findAllList(){
        // mark on 2020-4-1 begin
//        List<ProductCategory> list = msProductCategoryService.findAllList();
//        if (!ObjectUtils.isEmpty(list)) {
//            return list;
//        }
//        return Lists.newArrayList();
        // mark on 2020-4-1 end

        return msProductCategoryNewService.findAllListForMDWithEntity();  // add on 2020-4-1
    }

    public List<ProductCategory> findAllListForRPT() {
        List<ProductCategory> list = msProductCategoryNewService.findAllListForRPTWithEntity();
        if (!ObjectUtils.isEmpty(list)) {
            return list;
        }
        return Lists.newArrayList();
    }
    /**
     * 优先从缓存读取
     * @param id
     * @return  id,name
     */
    public ProductCategory getFromCache(Long id) {
        //return msProductCategoryService.getFromCache(id);  //add on 2020-1-6
        return msProductCategoryNewService.getFromCacheForMDWithEntity(id);
    }

    /**
     * 按Id读取
     * @param id
     * @return  ProductCategory.class
     */
    public ProductCategory getByIdForMD(Long id) {
        return msProductCategoryNewService.getByIdForMD(id);
    }

    //获取客户的产品分类列表
    public HashSet getListByCustomer(Long customerId){
        List<Product> productList = productService.getCustomerProductList(customerId);
        HashSet<HashMap<String,Object>> productCategoryHashSet = new HashSet<>();

        for (Product product: productList) {
            HashMap<String,Object> map=new HashMap<>();
            map.put("id",product.getCategory().getId());
            map.put("name",product.getCategory().getName());
            productCategoryHashSet.add(map);
        }
        return productCategoryHashSet;
    }

    /**
     * 根据客户获取品类
     * @param customerId
     * @return
     */
    public List<ProductCategory> findIdAndNameListByCustomerId(Long customerId) {
        List<ProductCategory> list = msProductCategoryNewService.findIdAndNameListByCustomerIdWithEntity(customerId);
        if (!ObjectUtils.isEmpty(list)) {
            return list;
        }
        return Lists.newArrayList();
    }
}

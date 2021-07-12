package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.GlobalMappingSyncTypeEnum;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.CustomerProduct;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.ms.globalmapping.service.ProductCategoryProductMappingService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductService;
import com.wolfking.jeesite.ms.providermd.service.MSProductCategoryNewService;
import com.wolfking.jeesite.ms.providermd.service.MSProductVerSecondService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class ProductVerSecondService {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private MSProductVerSecondService msProductVerSecondService;

    @Autowired
    private MSProductCategoryNewService msProductCategoryNewService;

    @Autowired
    private ProductCategoryProductMappingService productCategoryProductMappingService;

    @Autowired
    private MSCustomerProductService msCustomerProductService;
    /**
     * 根据ID获取产品名称
     * @param id
     * @return
     */
    public String getNameByIdForMD(Long id){
        return msProductVerSecondService.getNameByIdForMD(id);
    }

    /**
     * 根据id 获取产品
     * @param
     * @return id,name,product_category_id,model,pinYin,sort,flag,remarks
     */
    public Product getSpecColumnByIdForMD (Long id){
        Product product = msProductVerSecondService.getSpecColumnByIdForMD(id);
        if (product != null) {
            String strName = msProductCategoryNewService.getFromCacheForMD(product.getCategory().getId());
            product.getCategory().setName(strName);
        }
        return product;
    }

    /**
     * 分页获取产品信息
     * @param page
     * @param entity
     * @return
     */
    public Page<Product> findPage (Page<Product> page, Product entity){
        if (entity.getCustomerId() != null) {
            List<CustomerProduct> customerProductList = customerService.getListByCustomer(entity.getCustomerId());
            String productIds = "";
            if (customerProductList != null && !customerProductList.isEmpty()) {
                productIds = customerProductList.stream().map(r->r.getProduct().getId().toString()).collect(Collectors.joining(","));
            }
            entity.setProductIds(productIds);
        }
        Page<Product> productPage = msProductVerSecondService.findListForMD(page, entity);
        List<Product> list = productPage.getList();
        if(list != null && list.size()>0) {
            handleProductCategory(list);
        }
        return productPage;
    }

    /**
     * 获取所有的已审核产品数据
     * @return
     */
    public List<Product> findAllList(){
        List<Product> productList = msProductVerSecondService.findAllProductListForMD();
        return handleProductCategory(productList);
    }

    /**
     * 保存产品
     * @param product
     */
    public void saveForMD(Product product){
        boolean isNew = product.getIsNewRecord();
        if (isNew){
            product.preInsert();
        }else{
            product.preUpdate();
        }
        MSErrorCode msErrorCode = msProductVerSecondService.saveForMD(product, isNew);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用产品微服务保存数据失败。失败原因:"+msErrorCode.getMsg());
        }
        productCategoryProductMappingService.saveProductCategoryProductMapping(isNew? GlobalMappingSyncTypeEnum.ADD: GlobalMappingSyncTypeEnum.UPDATE,
                product.getId(), product.getCategory().getId());
    }
    /**
     * 更新排序
     */
    public void updateSortForMD(List<Product> products){
        List<Product> productList = Lists.newArrayList();
        Product product;
        for (Product value : products) {
            product = value;
            if (product == null) {
                continue;
            }
            if (product.getId() != null && product.getSort() >= 0) {
                productList.add(product);
            }
        }
        if (!productList.isEmpty()) {
            MSErrorCode msErrorCode = msProductVerSecondService.updateSortForMD(productList);
            if (msErrorCode.getCode() >0) {
                throw  new RuntimeException("调用产品微服务更新排序失败.失败原因:" + msErrorCode.getMsg());
            }
        }
        // add on 2020-11-26 end
    }

    /**
     * 删除产品
     * @param product
     */
    public void deleteForMD(Product product){
        product.preUpdate();
        MSErrorCode msErrorCode = msProductVerSecondService.deleteForMD(product);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用产品微服务删除数据失败。失败原因:"+msErrorCode.getMsg());
        }
        productCategoryProductMappingService.deleteProductCategoryProductMapping(product.getId());
    }

    /**
     * 处理产品中的产品分类
     * @param productList
     * @return
     */
    private List<Product> handleProductCategory(List<Product> productList) {
        if (productList == null) {
            return Lists.newArrayList();
        }
        List<Long> ids = !productList.isEmpty() ? productList.stream().map(x->x.getCategory().getId()).distinct().collect(Collectors.toList()) : Lists.newArrayList();
        List<ProductCategory> productCategoryList = !ids.isEmpty() ? msProductCategoryNewService.findListByIdsForMDWithEntity(ids):Lists.newArrayList(); //add on 2020-4-1
        Map<Long,ProductCategory> productCategoryMap = Maps.newHashMap();
        if (productCategoryList != null && !productCategoryList.isEmpty()) {
            productCategoryMap = productCategoryList.stream().collect(Collectors.toMap(ProductCategory::getId, r->r));
        }
        Map<Long,ProductCategory> finalProductCategoryMap = productCategoryMap;
        productList.forEach(product -> {
            ProductCategory productCategory = finalProductCategoryMap.get(product.getCategory().getId());
            product.getCategory().setName(productCategory==null?"":productCategory.getName());
        });

        return productList;
    }

    /**
     * 加载非套组产品，当缓存未命中则从数据库装载至缓存
     * @return
     */
    public List<Product> getSingleProductListForMD(){
        List<Product> productListAll = msProductVerSecondService.findAllProductListForMD();
        if(productListAll !=null && productListAll.size()>0){
            List<Product> singleProductList = productListAll.stream().filter(t->t.getSetFlag()==0).collect(Collectors.toList());
            return handleProductCategory(singleProductList);
        }else{
            return Lists.newArrayList();
        }
    }


    /**
     * 获取客户下面的所有产品，当缓存未命中则从数据库装载至缓存
     */
    public List<Product> getCustomerProductList(Long customerId){
        List<Product> productList = msCustomerProductService.findProductByCustomerIdFromCache(customerId);
        if(productList !=null && productList.size()>0){
            return productList;
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 判断是否有套组产品包含此产品
     * @param productId
     * @return
     */
    public HashMap<String, Object> getSetProductByProductId(Long productId){
        HashMap<String, Object> map = null;
        Product product = msProductVerSecondService.getSetProductByProductIdForMD(productId);
        if (product != null) {
            map = Maps.newHashMap();
            map.put("id", product.getId());
            map.put("name", product.getName());
        }
        return map;
    }

    /**
     * 根据产品名称获取产品，最多一条，用于判断产品名称是否存在于数据库中
     * @param product
     * @return
     */
    public Boolean isExistProductName(Product product){
        Long id = msProductVerSecondService.getIdByNameForMD(product.getName());
        return id != null && (!id.equals(product.getId()));
        // add on 2019-8-15 end
    }
}

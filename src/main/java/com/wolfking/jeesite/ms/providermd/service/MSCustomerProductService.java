package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDBrand;
import com.kkl.kklplus.entity.md.MDCustomerProduct;
import com.kkl.kklplus.entity.md.MDProduct;
import com.kkl.kklplus.entity.md.dto.MDCustomerDto;
import com.kkl.kklplus.entity.md.dto.MDCustomerProductDto;
import com.kkl.kklplus.entity.md.dto.MDProductDto;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.CustomerDao;
import com.wolfking.jeesite.modules.md.dao.CustomerProductDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerProductFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MSCustomerProductService {

    @Autowired
    private MSCustomerProductFeign msCustomerProductFeign;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private RedisUtils redisUtils;

//    @Autowired
//    private CustomerDao customerDao;  //mark on 2020-2-11

    /**
     * 从缓存中获取安装规范 （API）
     * @param customerId
     * @param productId
     * @return
     */
    public CustomerProduct getFixSpecFromCache(Long customerId, Long productId) {
        //return MDUtils.getEntity(CustomerProduct.class, ()->msCustomerProductFeign.getFixSpecFromCache(customerId, productId));
        return MDUtils.getObjNecessaryConvertType(CustomerProduct.class, ()->msCustomerProductFeign.getFixSpecFromCache(customerId, productId));
    }

    /**
     * 根据客户id和产品id获取客户产品
     * @param customerId
     * @param productId
     * @return
     */
    public CustomerProduct getByCustomerIdAndProductId(Long customerId, Long productId) {
        return MDUtils.getObjNecessaryConvertType(CustomerProduct.class, ()->msCustomerProductFeign.getByCustomerIdAndProductId(customerId, productId));
    }

    /**
     * 根据客户id获取
     * @param customerId
     * @return
     */
    public List<CustomerProduct> getByCustomer(Long customerId) {
        MSResponse<List<MDCustomerProduct>> msResponse = msCustomerProductFeign.findByCustomer(customerId);
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerProduct> list = mapper.mapAsList(msResponse.getData(),CustomerProduct.class);
            return list;
        }else{
            return Lists.newArrayList();
        }
    }


    /**
     * 根据客户id获取客户Id集合
     * @param customerId
     * @return
     */
    public List<Long> getProductIdsById(Long customerId) {
        MSResponse<List<Long>> msResponse = msCustomerProductFeign.findProductIdsById(customerId);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 根据客户Id,或者产品id，或者产品id集合获取客户产品
     * @param customerProduct
     * @param productIds
     * @return
     */
    public Page<CustomerProduct> findCustomerProductsByIdsWithoutCustomerAndProduct(CustomerProduct customerProduct, List<Long> productIds){
         MDCustomerProduct mdCustomerProduct = mapper.map(customerProduct,MDCustomerProduct.class);
         mdCustomerProduct.setPage(new MSPage<>(customerProduct.getPage().getPageNo(),customerProduct.getPage().getPageSize()));
         MSResponse<MSPage<MDCustomerProduct>> msResponse = msCustomerProductFeign.findCustomerProductsByIdsWithoutCustomerAndProduct(mdCustomerProduct,productIds);
        Page<CustomerProduct> page = new Page<>();
         if(MSResponse.isSuccess(msResponse)){
             List<CustomerProduct> list = mapper.mapAsList(msResponse.getData().getList(),CustomerProduct.class);
             page.setList(list);
             page.setCount(msResponse.getData().getRowCount());
             return page;
         }else{
             page.setList(Lists.newArrayList());
             page.setCount(0);
             return page;
         }
    }


    /**
     * 批量添加
     * @param customerId
     * @param productIds
     * @return
     */
    public MSErrorCode batchInsert(Long customerId,List<Long> productIds) {
        MSResponse<Integer> msResponse = msCustomerProductFeign.batchInsert(customerId,productIds);
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }

    /**
     * 根据客户id删除
     * @param customerId
     * @return
     */
    public MSErrorCode deleteByCustomer(Long customerId) {
       MSResponse<Integer> msResponse = msCustomerProductFeign.deleteByCustomer(customerId);
       return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }


    /**
     * 分页获取客户和产品相关信息(返回客户id，name，code和产品id,name,sort,product_category_id),根据产品sort排序
     * @param customerPrice
     * @return
     */
    public Page<CustomerProduct> findCustomerProductList(CustomerPrice customerPrice){
        MDCustomerProductDto mdCustomerProductDto = new MDCustomerProductDto();
        MDCustomerDto mdCustomerDto = new MDCustomerDto();
        MDProductDto mdProductDto = new MDProductDto();
        mdCustomerProductDto.setPage(new MSPage<>(customerPrice.getPage().getPageNo(),customerPrice.getPage().getPageSize()));
        if(customerPrice.getCustomer()!=null){
            Customer customer = customerPrice.getCustomer();
            if(customer.getId()!=null && customer.getId()>0){
                mdCustomerDto.setId(customer.getId());
            }
            if(customer.getSales()!=null && customer.getSales().getId()!=null && customer.getSales().getId()>0){
                mdCustomerDto.setSalesId(customer.getSales().getId());
            }
            if(customer.getMerchandiser()!=null && customer.getMerchandiser().getId()!=null && customer.getMerchandiser().getId()>0){
                mdCustomerDto.setMerchandiserId(customer.getMerchandiser().getId());
            }
        }
        if(customerPrice.getProduct()!=null){
            Product product = customerPrice.getProduct();
            if(product.getId()!=null && product.getId()>0){
                mdProductDto.setId(product.getId());
            }
        }
        if(customerPrice.getProductCategory() !=null && customerPrice.getProductCategory().getId() !=null && customerPrice.getProductCategory().getId() >0){
            mdProductDto.setProductCategoryId(customerPrice.getProductCategory().getId());
        }
        mdCustomerProductDto.setCustomerDto(mdCustomerDto);
        mdCustomerProductDto.setProductDto(mdProductDto);
        MSResponse<MSPage<MDCustomerProductDto>> msResponse = msCustomerProductFeign.findCustomerProductList(mdCustomerProductDto);
        Page<CustomerProduct> page = new Page<>();
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerProduct> list = mapper.mapAsList(msResponse.getData().getList(),CustomerProduct.class);
            page.setList(list);
            page.setCount(msResponse.getData().getRowCount());
            return page;
        }else{
            page.setList(Lists.newArrayList());
            page.setCount(0);
            return page;
        }
    }


    /**
     * 分页查询
     * @param page
     * @param customerProduct
     * @return
     */
    public Page<MDCustomerProductDto> findList(Page<CustomerProduct> page,CustomerProduct customerProduct){
        MDCustomerProduct mdCustomerProduct = mapper.map(customerProduct,MDCustomerProduct.class);
        // 增加业务员查询
        User user = UserUtils.getUser();
        if (user != null) {
            if (user.isSaleman()) {
                mdCustomerProduct.setSalesId(user.getId());
            }
        }
        Page<MDCustomerProductDto> customerProductPage = new Page<>();
        customerProductPage.setPageSize(page.getPageSize());
        customerProductPage.setPageNo(page.getPageNo());
        mdCustomerProduct.setPage(new MSPage<>(customerProductPage.getPageNo(), customerProductPage.getPageSize()));
        MSResponse<MSPage<MDCustomerProductDto>> mdCustomerProductDto = msCustomerProductFeign.findList(mdCustomerProduct);
        if (MSResponse.isSuccess(mdCustomerProductDto)) {
            MSPage<MDCustomerProductDto> data = mdCustomerProductDto.getData();
            customerProductPage.setCount(data.getRowCount());
            customerProductPage.setList(data.getList());
        } else {
            customerProductPage.setCount(0);
            customerProductPage.setList(Lists.newArrayList());
        }
        return customerProductPage;
    }

    /**
     * 根据id获取数据
     * @param id
     * @return
     */
    public MDCustomerProductDto getById(Long id){
         MSResponse<MDCustomerProductDto> msResponse = msCustomerProductFeign.getById(id);
         if(MSResponse.isSuccess(msResponse)){
             return msResponse.getData();
         }else {
             return null;
         }
    }

    /**
     * 保存单条数据
     * @param customerProduct
     * @return
     */
     @Transactional()
     public void save(CustomerProduct customerProduct){
         customerProduct.preInsert();
         MDCustomerProduct mdCustomerProduct = mapper.map(customerProduct,MDCustomerProduct.class);
         if(customerProduct.getId() !=null && customerProduct.getId()>0){
             MSResponse<Integer> msResponse = msCustomerProductFeign.update(mdCustomerProduct);
             if(msResponse.getCode()>0){
                 throw new RuntimeException("修改客户产品失败.失败原因:" + msResponse.getMsg());
             }
         }else {
             MSResponse<MDCustomerProduct> response = msCustomerProductFeign.checkExistWithCustomerProduct(customerProduct.getCustomer().getId(),customerProduct.getProduct().getId());
             if(MSResponse.isSuccess(response)){
                 if(response.getData()!=null){
                     throw new RuntimeException("该客户产品已经存在");
                 }
             }
             MSResponse<Integer> msResponse = msCustomerProductFeign.insert(mdCustomerProduct);
             if(msResponse.getCode()>0){
                 throw new RuntimeException("添加客户产品失败.失败原因:" + msResponse.getMsg());
             }
         }
     }

    /**
     * 删除单条数据
     * @param customerProduct
     * @return
     */
    @Transactional()
     public void delete(CustomerProduct customerProduct){
        customerProduct.preUpdate();
        MDCustomerProduct mdCustomerProduct = mapper.map(customerProduct,MDCustomerProduct.class);
        //customerDao.deletePricesByCustomerAndProducts(customerProduct.getCustomer().getId(), Lists.newArrayList(customerProduct.getProduct().getId()));//mark on 2020-2-11
        MSResponse<Integer> msResponse = msCustomerProductFeign.delete(mdCustomerProduct);
        if(msResponse.getCode()>0){
            throw new RuntimeException("添加客户产品失败.失败原因:" + msResponse.getMsg());
        }
     }


    /**
     * 批量添加
     * @param customerProduct
     * @param productIds
     * @return
     */
    public MSErrorCode newBatchInsert(CustomerProduct customerProduct,List<Long> productIds){
       MDCustomerProduct mdCustomerProduct = mapper.map(customerProduct,MDCustomerProduct.class);
       MSResponse<Integer> msResponse = msCustomerProductFeign.newBatchInsert(mdCustomerProduct,productIds);
       return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }


    /**
     * 批量删除
     * @param customerProduct
     * @param productIds
     * @return
     */
    public MSErrorCode batchDelete(CustomerProduct customerProduct,List<Long> productIds){
        MDCustomerProduct mdCustomerProduct = mapper.map(customerProduct,MDCustomerProduct.class);
        MSResponse<Integer> msResponse = msCustomerProductFeign.batchDelete(mdCustomerProduct,productIds);
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }


    /**
     * 根据客户Id重缓存获取产品
     * @param customerId
     * @return
     */
    public List<Product> findProductByCustomerIdFromCache(Long customerId){
        MSResponse<List<MDProduct>> msResponse = msCustomerProductFeign.findProductByCustomerIdFromCache(customerId);
        if(MSResponse.isSuccess(msResponse)){
            return mapper.mapAsList(msResponse.getData(),Product.class);
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 查询客户的产品列表，供“客户信息”报表使用
     * 结果集中的MDProductDto对象使用name属性存储客户的产品列表
     * @param customerId
     * @param paymentType
     * @return
     */
    public List<MDProductDto> getCustomerProducts(Long customerId,Integer paymentType){
      MSResponse<List<MDProductDto>> msResponse = msCustomerProductFeign.getCustomerProducts(customerId,paymentType);
      if(MSResponse.isSuccess(msResponse)){
          return msResponse.getData();
      }else{
          return Lists.newArrayList();
      }
    }

    public Integer getRemoteFeeFlag(Long customerId,List<Long> productIds){
        MSResponse<Integer> msResponse = msCustomerProductFeign.getRemoteFeeFlag(customerId,productIds);
        if(MSResponse.isSuccessCode(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }
    /**
     * 根据客户id+二级分类id获取产品规格，属性及产品，服务
     * @param customerId
     * @param productTypeItemId
     * @return
     */
    public MSResponse<String> getProductSpecAndInfoForCreateOrder(Long customerId,Long productTypeItemId){
        return msCustomerProductFeign.getProductSpecAndInfoForCreateOrder(customerId,productTypeItemId);
    }

    /**
     * 读取客户下品类ID集合
     * @param customerId
     * @return
     */
    public MSResponse<List<Long>> getCustomerCategories(Long customerId){
        return msCustomerProductFeign.getCustomerCategories(customerId);
    }

    /**
     * 根据客户id及产品id列表返回对应的产品列表是否有安装规范
     *
     * @param customerId
     * @param productIds
     * @return
     */
    public Map<Long, Integer> findFixSpecByCustomerIdAndProductIdsFromCacheForSD(Long customerId, List<Long> productIds) {
        MSResponse<Map<Long, Integer>> msResponse = msCustomerProductFeign.findFixSpecByCustomerIdAndProductIdsFromCacheForSD(customerId, productIds);
        if(MSResponse.isSuccessCode(msResponse)){
            return msResponse.getData();
        }else{
            return Maps.newHashMap();
        }
    }

    /**
     * 清空安装规范
     * @param customerProduct
     */
    public void removeFixSpec(CustomerProduct customerProduct) {
        MDCustomerProduct mdCustomerProduct = mapper.map(customerProduct,MDCustomerProduct.class);
        MSErrorCode msErrorCode = MDUtils.customSave(()->msCustomerProductFeign.emptyFixSpecById(mdCustomerProduct));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("添加客户产品失败.失败原因:" + msErrorCode.getMsg());
        }
    }
}

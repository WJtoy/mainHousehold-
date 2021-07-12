package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDCustomerPrice;
import com.kkl.kklplus.entity.md.dto.MDCustomerPriceDto;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.CustomerPrice;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerPriceFeign;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerPriceNewFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class MSCustomerPriceService {

    @Autowired
    private MSCustomerPriceFeign msCustomerPriceFeign;

    @Autowired
    private MSCustomerPriceNewFeign msCustomerPriceNewFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 新增客户价格
     * @param customerPrice @return
     */
      public MSErrorCode insert(CustomerPrice customerPrice){
          return MDUtils.genericSave(customerPrice, MDCustomerPrice.class, true, msCustomerPriceNewFeign::insert);
      }
    /**
     * 修改or新增客户价格
     * @param customerPrice
     * @return
     */
    public MSErrorCode updatePrice(CustomerPrice customerPrice, boolean isNew){
        // update 2020-06-04
        return MDUtils.genericSave(customerPrice, MDCustomerPrice.class, isNew, isNew ? msCustomerPriceNewFeign::insert : msCustomerPriceNewFeign::update);
    }

    /**
     * 修改客户价格为标准价
     * @return
     */
    public MSErrorCode updateCustomizePriceFlag(Long customerId, Long productId, List<Long> serviceTypeId, Long userId, String date){
        MSResponse<Integer> msResponse = msCustomerPriceNewFeign.updateCustomizePriceFlag(customerId,productId,serviceTypeId,userId,date);
        MSErrorCode msErrorCode = new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
        return msErrorCode;
    }

    /**
     * 根据客户和产品id集合删除客户价格
     * @param customerId 客户id
     * @param productIds 产品id集合
     * @return
     */
    public MSErrorCode deletePricesByCustomerAndProducts(Long customerId,List<Long> productIds){
        int num = 10;
        List<List<Long>> list = fixedGrouping(productIds,num); //一次传10个产品id,防止调用微服务超时
        MSResponse<Integer> msResponse = new MSResponse<>(MSErrorCode.FAILURE);
        for(List<Long> item:list){
            if(item !=null && item.size()>0){
                msResponse = msCustomerPriceFeign.deletePricesByCustomerAndProducts(customerId,item);
            }
        }
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }

    /**
     * 获得某客户的所有价格清单
     * @param customerId 客户id
     * @param delFlag 0:启用的价格 1:停用的价格 2:待审核的价格 null:所有
     * @return
     */
    /*public List<CustomerPrice> findPrices(Long customerId,Integer delFlag){
      MSResponse<List<MDCustomerPriceDto>> msResponse = msCustomerPriceFeign.findPrices(customerId,delFlag);
      return mdCustomerPriceToCustomer(msResponse);
    }*/

    public List<CustomerPrice> findPricesNew(Long customerId,Integer delFlag){
        MSResponse<List<MDCustomerPriceDto>> msResponse = msCustomerPriceNewFeign.findPricesNew(customerId,delFlag);
        return mdCustomerPriceToCustomer(msResponse);
    }

    /**
     * 获得某客户的生效价格清单用于缓存
     * @param customerId 客户id
     * @return
     */
    public List<CustomerPrice> findPricesForCache(Long customerId){
       MSResponse<List<MDCustomerPriceDto>> msResponse = msCustomerPriceFeign.findCustomerPriceWithAssociated(customerId);
        return mdCustomerPriceToCustomer(msResponse);
    }

    /**
     * 按id列表获得价格列表(for cache)
     * @param ids 客户价格id列表
     * @return
     */
    public List<CustomerPrice> findPricesByPriceIds(List<Long> ids){
      MSResponse<List<MDCustomerPriceDto>> msResponse = msCustomerPriceFeign.findPricesByPriceIds(ids);
      return mdCustomerPriceToCustomer(msResponse);
    }

    /**
     * 按多个id获得客户下价格
     * @param customerIds 客户id列表
     * @param productId 产品id
     * @param serviceTypeId 服务类型id
     * @return
     */
    /*public List<CustomerPrice> findPricesByCustomers(List<Long> customerIds,Long productId,Long serviceTypeId){
       MSResponse<List<MDCustomerPrice>> msResponse = msCustomerPriceFeign.findPricesByCustomers(customerIds,productId,serviceTypeId);
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerPrice> list = mapper.mapAsList(msResponse.getData(),CustomerPrice.class);
            if(list!=null && list.size()>0){
                return list;
            }else{
                return Lists.newArrayList();
            }
        }else{
            return Lists.newArrayList();
        }
    }*/

    public List<CustomerPrice> findPricesByCustomersNew(List<Long> customerIds,Long productId,Long serviceTypeId){
        MSResponse<List<MDCustomerPrice>> msResponse = msCustomerPriceNewFeign.findPricesByCustomersNew(customerIds,productId,serviceTypeId);
        if (MSResponse.isSuccess(msResponse)) {
            List<CustomerPrice> list = mapper.mapAsList(msResponse.getData(),CustomerPrice.class);
            if (!CollectionUtils.isEmpty(list)) {
                return list;
            } else {
                return Lists.newArrayList();
            }
        } else {
            return Lists.newArrayList();
        }
    }

    /**
     * 获得待审核价格清单
     * @param customerPrice 查询条件
     * @param customerPricePage
     * @return
     */
    public Page<CustomerPrice> findApprovePriceList(Page<CustomerPrice> customerPricePage,CustomerPrice customerPrice){
         //return MDUtils.findListForPage(customerPricePage, customerPrice, CustomerPrice.class, MDCustomerPriceDto.class, msCustomerPriceFeign::findApprovePriceList);
        return MDUtils.findListForPage(customerPricePage, customerPrice, CustomerPrice.class, MDCustomerPriceDto.class, msCustomerPriceNewFeign::findApprovePriceList);
    }


    /**
     * 审核价格
     * @param ids 客户价格id
     * @param updateBy 审核人
     * @param updateDate 审核时间,时间戳
     * @return
     */
    public MSErrorCode approvePrices(List<Long> ids,Long updateBy,Long updateDate){
      /*
      // mark on 2020-9-10
      MSResponse<Integer> msResponse = msCustomerPriceFeign.approvePrices(ids,updateBy,updateDate);
      MSErrorCode msErrorCode = new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
      return msErrorCode;*/

      // add on 2020-9-10
      MSResponse<Integer> msResponse = msCustomerPriceNewFeign.approvePrices(ids,updateBy,updateDate);
      MSErrorCode msErrorCode = new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
      return msErrorCode;
    }


    /**
     * 获得某客户的所有价格清单
     * @param id 客户价格id
     * @param delFlag 0:启用的价格 1:停用的价格 2:待审核的价格 null:所有
     * @return
     */
     /*public CustomerPrice getPrice(Long id,Integer delFlag){
         MSResponse<MDCustomerPriceDto> msResponse = msCustomerPriceFeign.getPrice(id,delFlag);
         if(MSResponse.isSuccess(msResponse)){
             return mapper.map(msResponse.getData(),CustomerPrice.class);
         }else{
             return null;
         }
     }*/

    public CustomerPrice getPriceNew(Long id,Integer delFlag){
        MSResponse<MDCustomerPriceDto> msResponse = msCustomerPriceNewFeign.getPriceNew(id,delFlag);
        if (MSResponse.isSuccess(msResponse)) {
            return mapper.map(msResponse.getData(),CustomerPrice.class);
        } else {
            return null;
        }
    }

    /**
     * 修改价格,停用或者启用
     * @param map
     * @return
     */
    /*public MSErrorCode updatePriceByMap(HashMap<String,Object> map){
        MSResponse<Integer> msResponse = msCustomerPriceFeign.updatePriceByMap(map);
        MSErrorCode msErrorCode = new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
        return msErrorCode;
    }*/

    /**
     * 停用或者启用价格new
     * @param map
     * @return
     */
    public MSErrorCode updatePriceByMapNew(HashMap<String,Object> map){
        MSResponse<Integer> msResponse = msCustomerPriceNewFeign.updatePriceByMapNew(map);
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }

    /**
     * 批量添加或者修改
     * @param list
     */
    /*public MSErrorCode insertOrUpdateBatch(List<CustomerPrice> list){
        List<MDCustomerPrice> mdCustomerPriceList = mapper.mapAsList(list,MDCustomerPrice.class);
        if(mdCustomerPriceList==null || mdCustomerPriceList.size()<=0){
            return new MSErrorCode(10000,"参数不能为空");
        }
        MSResponse<Integer> msResponse = msCustomerPriceFeign.insertOrUpdateBatch(mdCustomerPriceList);
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }*/

    public MSErrorCode insertOrUpdateBatchNew(List<CustomerPrice> list){
        List<MDCustomerPrice> mdCustomerPriceList = mapper.mapAsList(list,MDCustomerPrice.class);
        if(mdCustomerPriceList==null || mdCustomerPriceList.size()<=0){
            return new MSErrorCode(10000,"参数不能为空");
        }
        MSResponse<Integer> msResponse = msCustomerPriceNewFeign.insertOrUpdateBatchNew(mdCustomerPriceList);
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }

    public MSErrorCode insertOrUpdateBatchNewTwo(List<CustomerPrice> list, boolean isNew){
        List<MDCustomerPrice> mdCustomerPriceList = mapper.mapAsList(list,MDCustomerPrice.class);
        if(mdCustomerPriceList==null || mdCustomerPriceList.size()<=0){
            if (isNew) {
                return new MSErrorCode(10000,"保内价格不能为0");
            } else {
                return new MSErrorCode(10000,"参数不能为空");
            }
        }
        MSResponse<Integer> msResponse = msCustomerPriceNewFeign.insertOrUpdateBatchNew(mdCustomerPriceList);
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }

    /**
     * 批量添加
     * @param list
     */
    public MSErrorCode batchInsert(List<CustomerPrice> list){
        List<MDCustomerPrice> mdCustomerPriceList = mapper.mapAsList(list,MDCustomerPrice.class);
        List<List<MDCustomerPrice>> insertList = Lists.newArrayList();
        if(mdCustomerPriceList.size()<=20){
            insertList.add(mdCustomerPriceList);
        }else{
            insertList = Lists.partition(mdCustomerPriceList,20);
        }
        MSResponse<Integer> msResponse = new MSResponse<>(MSErrorCode.SUCCESS);
        long start = System.currentTimeMillis();
        if(insertList!=null && insertList.size()>0){
            for(List<MDCustomerPrice> item : insertList){
                msResponse = msCustomerPriceFeign.batchInsert(item);
                if(msResponse.getCode()>0){
                    return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
                }
            }
        }else{
            return new MSErrorCode(1000000,"调用微服务添加的价格数据为空");
        }
        long end = System.currentTimeMillis();
        log.warn("耗时：{}",end-start);
        return new MSErrorCode(msResponse.getCode(),msResponse.getMsg());
    }


    /**
     * 读取某客户的价格清单
     * 先从缓存读取，缓存不存在从数据库读取，并更新缓存
     *
     * @param customerId 客户id
     * @return
     */
    public List<CustomerPrice> getPricesFromCache(Long customerId){
      MSResponse<List<MDCustomerPriceDto>> msResponse =  msCustomerPriceFeign.findCustomerPriceWithAssociatedFromCache(customerId);
      return mdCustomerPriceToCustomer(msResponse);
    }

    /**
     * 读取某客户的价格清单
     * 先从缓存读取，缓存不存在从数据库读取，并更新缓存
     *
     * @param customerId 客户id
     * @return
     */
    public List<CustomerPrice> getPricesFromCacheNew(Long customerId){
        MSResponse<List<MDCustomerPriceDto>> msResponse =  msCustomerPriceNewFeign.findCustomerPriceWithAssociatedFromCache(customerId);
        return mdCustomerPriceToCustomer(msResponse);
    }

    /**
     * 根据产品和服务类型获取客户的服务价格
     *
     * @param customerId 客户id
     * @param paramMap   key 为产品id ,value为服务类型id
     * @return
     */
    public List<CustomerPrice> findPricesByProductsAndServiceTypesFromCache(Long customerId, HashMap<Long, Long> paramMap) {
        MSResponse<List<MDCustomerPriceDto>> msResponse = msCustomerPriceNewFeign.findPricesByProductsAndServiceTypesFromCache(customerId, paramMap);
        if (msResponse.getCode() >0) {
            return null;
        } else {
            List<CustomerPrice> list = mapper.mapAsList(msResponse.getData(),CustomerPrice.class);
            if( list != null && list.size()>0){
                return list;
            }else{
                return Lists.newArrayList();
            }
        }
    }

    /**
     * MDCustomerPrice 转 CustomerPrice
     * @param msResponse
     * @return
     */
    private List<CustomerPrice> mdCustomerPriceToCustomer(MSResponse<List<MDCustomerPriceDto>> msResponse){
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerPrice> list = mapper.mapAsList(msResponse.getData(),CustomerPrice.class);
            if(list!=null && list.size()>0){
                return list;
            }else{
                return Lists.newArrayList();
            }
        }else{
            return Lists.newArrayList();
        }
    }


    /**
     * 将一组数据固定分组，每组n个元素
     *
     * @param source 要分组的数据源
     * @param n      每组n个元素
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> fixedGrouping(List<T> source, int n) {

        if (null == source || source.size() == 0 || n <= 0)
            return Lists.newArrayList();
        List<List<T>> result = new ArrayList<List<T>>();
        int remainder = source.size() % n;
        int size = (source.size() / n);
        for (int i = 0; i < size; i++) {
            List<T> subset = null;
            subset = source.subList(i * n, (i + 1) * n);
            result.add(subset);
        }
        if (remainder > 0) {
            List<T> subset = null;
            subset = source.subList(size * n, size * n + remainder);
            result.add(subset);
        }
        return result;
    }
}

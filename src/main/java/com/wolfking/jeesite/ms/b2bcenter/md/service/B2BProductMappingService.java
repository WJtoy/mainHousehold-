package com.wolfking.jeesite.ms.b2bcenter.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BProductMapping;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.Page;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.b2bcenter.md.feign.B2BProductMappingFeign;
import com.wolfking.jeesite.ms.common.config.MicroServicesProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BProductMappingService {

    @Autowired
    private B2BProductMappingFeign productMappingFeign;

    @Autowired
    private MicroServicesProperties msProperties;

    /**
     * 一键同步,每次调用微服务同步数据的大小
     */
    private static final int SYNC_SIZE = 12;



    /**
     * 根据id获取
     * @param id
     * @return
     */
    public B2BProductMapping getById(Long id){
        MSResponse<B2BProductMapping> msResponse = productMappingFeign.getProductMappingById(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }


    /**
     * 查询数据源中所有B2B产品与工单系统产品的映射关系
     *
     * @param dataSource B2BDataSourceEnum
     * @return
     */
    public List<B2BProductMapping> getListByDataSource(B2BDataSourceEnum dataSource) {
        List<B2BProductMapping> list = Lists.newArrayList();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (dataSource != null) {
                MSResponse<List<B2BProductMapping>> responseEntity = productMappingFeign.getListByDataSource(dataSource.id);
                if (MSResponse.isSuccess(responseEntity)) {
                    list = responseEntity.getData();
                }
            }
        }
        return list;
    }

    public List<B2BProductMapping> getListByCustomerCategoryIds(B2BDataSourceEnum dataSource, List<String> customerCategoryIds) {
        List<B2BProductMapping> list = Lists.newArrayList();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (dataSource != null && customerCategoryIds != null && !customerCategoryIds.isEmpty()) {
                MSResponse<List<B2BProductMapping>> responseEntity = productMappingFeign.getListByCustomerCategoryIds(dataSource.id, customerCategoryIds);
                if (MSResponse.isSuccess(responseEntity)) {
                    list = responseEntity.getData();
                }
            }
        }
        return list;
    }

    public List<B2BProductMapping> getListByCustomerCategoryIds(B2BDataSourceEnum dataSource, String shopId, List<String> customerCategoryIds) {
        List<B2BProductMapping> list = Lists.newArrayList();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (dataSource != null && customerCategoryIds != null && StringUtils.isNotBlank(shopId) && !customerCategoryIds.isEmpty()) {
                MSResponse<List<B2BProductMapping>> responseEntity = productMappingFeign.getListByCustomerCategoryIds(dataSource.id, customerCategoryIds);
                if (MSResponse.isSuccess(responseEntity)) {
                    list = responseEntity.getData();
                    list = list.stream().filter(i -> StringUtils.isNotBlank(i.getShopId()) && i.getShopId().equals(shopId)).collect(Collectors.toList());
                }
            }
        }
        return list;
    }

    /**
     * 获取多个店铺指定类目对应的产品
     */
    public Map<String, List<B2BProductMapping>> getListByCustomerCategoryIds(B2BDataSourceEnum dataSource, List<String> shopIds, List<String> customerCategoryIds) {
        Map<String, List<B2BProductMapping>> result = Maps.newHashMap();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (dataSource != null && customerCategoryIds != null && shopIds != null) {
                customerCategoryIds = customerCategoryIds.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
                shopIds = shopIds.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
                if (!customerCategoryIds.isEmpty() && !shopIds.isEmpty()) {
                    MSResponse<List<B2BProductMapping>> responseEntity = productMappingFeign.getListByCustomerCategoryIds(dataSource.id, customerCategoryIds);
                    if (MSResponse.isSuccess(responseEntity)) {
                        List<B2BProductMapping> list;
                        for (String sId : shopIds) {
                            list = responseEntity.getData().stream().filter(i -> StringUtils.isNotBlank(i.getShopId()) && i.getShopId().equals(sId)).collect(Collectors.toList());
                            if (!list.isEmpty()) {
                                result.put(sId, list);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public Map<Long, List<String>> getListByProductIds(B2BDataSourceEnum dataSource, List<Long> productIds) {
        Map<Long, List<String>> map = Maps.newHashMap();
        if (msProperties.getB2bcenter().getEnabled()) {
            if (dataSource != null && productIds != null && !productIds.isEmpty()) {
                MSResponse<Map<Long, List<String>>> responseEntity = productMappingFeign.getListByProductIds(dataSource.id, productIds);
                if (MSResponse.isSuccess(responseEntity)) {
                    map = responseEntity.getData();
                }
            }
        }
        return map;
    }

    public List<String> getB2BProductCodesByProductId(Integer dataSourceId, Long productId) {
        List<String> list = Lists.newArrayList();
        Map<Long, List<String>> map = getListByProductIds(B2BDataSourceEnum.valueOf(dataSourceId), Lists.newArrayList(productId));
        if (!map.isEmpty()) {
            list = map.get(productId);
        }
        return list;
    }

    /**
     * 分页查询
     *
     * @param page,b2BProductMapping
     * @return
     */
    public Page<B2BProductMapping> getList(Page<B2BProductMapping> page, B2BProductMapping b2BProductMapping) {
        Page<B2BProductMapping> b2BProductMappingPage = new Page<>();
        b2BProductMappingPage.setPageSize(page.getPageSize());
        b2BProductMappingPage.setPageNo(page.getPageNo());
        b2BProductMapping.setPage(new MSPage<>(b2BProductMappingPage.getPageNo(), b2BProductMappingPage.getPageSize()));
        MSResponse<MSPage<B2BProductMapping>> returnCustomerMapping = productMappingFeign.getProductMappingList(b2BProductMapping);
        if (MSResponse.isSuccess(returnCustomerMapping)) {
            MSPage<B2BProductMapping> data = returnCustomerMapping.getData();
            b2BProductMappingPage.setCount(data.getRowCount());
            b2BProductMappingPage.setList(data.getList());
        }
        return b2BProductMappingPage;
    }

    /**
     * 批量添加
     *
     * @param productMapping
     * @return public MSResponse<List<B2BProductMapping>> insertBatch(ProductMapping productMapping, User user) {
    List<B2BProductMapping> list = new ArrayList<>();

    if (StringUtils.isNotBlank(productMapping.getCustomerCategoryId())) {
    List<String> customerCategoryId = arrayDuplicate(productMapping.getCustomerCategoryId().split(","));
    for (String str : customerCategoryId) {
    if (str != null && StringUtils.isNotBlank(str)) {
    B2BProductMapping b2bProductMapping = new B2BProductMapping();
    b2bProductMapping.setCustomerCategoryId(str);
    b2bProductMapping.setDataSource(productMapping.getDataSource());
    b2bProductMapping.setProductId(productMapping.getProductId());
    b2bProductMapping.setRemarks(productMapping.getRemarks());
    b2bProductMapping.preInsert();
    b2bProductMapping.setCreateById(user.getId());
    b2bProductMapping.setUpdateById(user.getId());
    list.add(b2bProductMapping);
    }
    }
    }
    if (list != null && list.size() > 0) {
    Gson gson = new Gson();
    String json = gson.toJson(list);
    return productMappingFeign.insertBatch(json);
    } else {
    return new MSResponse<>(MSErrorCode.FAILURE, list);
    }
    }
     */

    /**
     * 添加
     *
     * @param b2BProductMapping
     * @return
     */
    public MSErrorCode save(B2BProductMapping b2BProductMapping) {
        if (b2BProductMapping.getId() != null && b2BProductMapping.getId() > 0) {
            b2BProductMapping.preUpdate();
            MSResponse<Integer> msResponse = productMappingFeign.updateProductMapping(b2BProductMapping);
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        } else {
            b2BProductMapping.preInsert();
            MSResponse<B2BProductMapping> msResponse = productMappingFeign.insertProductMapping(b2BProductMapping);
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        }
    }

    /**
     * 根据数据源查询
     *
     * @param dataSource
     * @return
     */
    public List<B2BProductMapping> getListByDataSource(Integer dataSource) {
        MSResponse<List<B2BProductMapping>> returnCustomerMapping = productMappingFeign.getListByDataSource(dataSource);
        List<B2BProductMapping> list = returnCustomerMapping.getData();
        return list;
    }

    /**
     * 删除数据
     *
     * @param b2BProductMapping
     * @return
     */
    public MSResponse<Integer> delete(B2BProductMapping b2BProductMapping) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            b2BProductMapping.setUpdateById(user.getId());
        }
        b2BProductMapping.preUpdate();
        MSResponse<Integer> msResponse = productMappingFeign.deleteProductMapping(b2BProductMapping);
        return msResponse;
    }

    /**
     * 利用list数组去重
     */
    public List<String> arrayDuplicate(String[] arr) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < arr.length; i++) {
            if (!list.contains(arr[i])) {
                list.add(arr[i]);
            }
        }
        return list;
    }

    /**
     * 一键同步b2b配置路由
     *
     * @param productMapping
     * @return
     */
    public MSResponse<Integer> syncByDataSource(B2BProductMapping productMapping, Long createById) {
        MSResponse<List<B2BProductMapping>> msResponse = productMappingFeign.getListByDataSource(productMapping.getDataSource());
        List<B2BProductMapping> list = new ArrayList<>();
        if (MSResponse.isSuccess(msResponse)) {
            list = msResponse.getData();
            MSResponse<Integer> response = new MSResponse<>(MSErrorCode.SUCCESS);
            List<List<B2BProductMapping>> groupingList = fixedGrouping(list, SYNC_SIZE);
            for (int i = 0; i < groupingList.size(); i++) {
                if (groupingList.get(i) != null && groupingList.get(i).size() > 0) {
                    response = productMappingFeign.syncByDataSource(groupingList.get(i), createById);
                }
            }
            return response;
        } else {
            return new MSResponse<>(MSErrorCode.FAILURE);
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
    public <T> List<List<T>> fixedGrouping(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();
        if (null == source || source.size() == 0 || n <= 0) {
            return result;
        }
        int sourceSize = source.size();
        int size = (source.size() / n) + 1;
        for (int i = 0; i < size; i++) {
            List<T> subset = new ArrayList<T>();
            for (int j = i * n; j < (i + 1) * n; j++) {
                if (j < sourceSize) {
                    subset.add(source.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }

}

package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.CustomerProductModel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.CustomerProductModelFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户产品型号服务
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class ProductModelService {

    @Autowired
    private CustomerProductModelFeign customerProductModelFeign;

    /**
     * 分页查询
     *
     * @param page,b2BServiceTypeMapping
     * @return
     */
    public Page<CustomerProductModel> getList(Page<CustomerProductModel> page, CustomerProductModel customerProductModel) {
        if (customerProductModel.getPage() == null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<CustomerProductModel> customerProductModelPage = new Page<>();
        customerProductModelPage.setPageSize(page.getPageSize());
        customerProductModelPage.setPageNo(page.getPageNo());
        customerProductModel.setPage(new MSPage<>(customerProductModelPage.getPageNo(), customerProductModelPage.getPageSize()));
        MSResponse<MSPage<CustomerProductModel>> returnCustomerProductModel = customerProductModelFeign.getList(customerProductModel);
        if (MSResponse.isSuccess(returnCustomerProductModel)) {
            MSPage<CustomerProductModel> data = returnCustomerProductModel.getData();
            customerProductModelPage.setCount(data.getRowCount());
            customerProductModelPage.setList(data.getList());
        }else{
            customerProductModelPage.setCount(0);
            customerProductModelPage.setList(new ArrayList<>());
        }
        return customerProductModelPage;
    }


    /**
     * 保存
     *
     * @param customerProductModel
     * @return
     */
    public MSErrorCode save(CustomerProductModel customerProductModel) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerProductModel.setCreateById(user.getId());
            customerProductModel.setUpdateById(user.getId());
        }
        if (customerProductModel.getId() != null && customerProductModel.getId() > 0) {
            customerProductModel.preUpdate();
            MSResponse<Integer> msResponse = customerProductModelFeign.update(customerProductModel);
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        } else {
            customerProductModel.preInsert();
            MSResponse<CustomerProductModel> msResponse = customerProductModelFeign.insert(customerProductModel);
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        }
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public MSResponse<CustomerProductModel> getById(Long id) {
        MSResponse<CustomerProductModel> msResponse = customerProductModelFeign.getById(id);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse;
        }else{
            return new MSResponse<>(MSErrorCode.SUCCESS,new CustomerProductModel());
        }
    }

    /**
     * 删除
     *
     * @param customerProductModel
     * @return
     */
    public MSResponse<Integer> delete(CustomerProductModel customerProductModel) {
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            customerProductModel.setUpdateById(user.getId());
        }
        customerProductModel.preUpdate();
        MSResponse<Integer> msResponse = customerProductModelFeign.delete(customerProductModel);
        return msResponse;
    }

    public List<CustomerProductModel> getFromCache(Long customerId,Long ProductId){
        List<CustomerProductModel> list = new ArrayList<>();
        MSResponse<List<CustomerProductModel>> msResponse = customerProductModelFeign.getListByField(customerId,ProductId);
        if(MSResponse.isSuccess(msResponse)){
            list =  msResponse.getData();
        }
        return list;
    }

    public List<String> getModelNamesFromCache(Long customerId,Long productId){
        List<CustomerProductModel> list = Lists.newArrayList();
        MSResponse<List<CustomerProductModel>> msResponse = customerProductModelFeign.getListByField(customerId,productId);
        if(MSResponse.isSuccess(msResponse)){
            list =  msResponse.getData();
        }
        if(list == null || list.isEmpty()){
            return Lists.newArrayList();
        }else{
            return list.stream().map(CustomerProductModel::getCustomerModel).distinct().collect(Collectors.toList());
        }
    }

    public List<CustomerProductModel> getModelListFromCache(Long customerId,Long productId){
        List<CustomerProductModel> list = Lists.newArrayList();
        MSResponse<List<CustomerProductModel>> msResponse = customerProductModelFeign.getListByField(customerId,productId);
        if(MSResponse.isSuccess(msResponse)){
            list =  msResponse.getData();
        }
        if(list == null){
            return Lists.newArrayList();
        }else{
            //return list.stream().map(CustomerProductModel::getCustomerModel).distinct().collect(Collectors.toList());
            return list;
        }
    }

    /**
     * 根据客户,产品,品牌获取数据
     * @param customerId,ProductId,brandId
     * @return
     */
    public List<CustomerProductModel> getListFromCacheByField(Long customerId,Long productId,Long brandId){
        List<CustomerProductModel> list = new ArrayList<>();
        MSResponse<List<CustomerProductModel>> msResponse = customerProductModelFeign.getListByField(customerId,productId);
        if(MSResponse.isSuccess(msResponse)){
            list =  msResponse.getData();
        }
        if(list == null || list.isEmpty()){
            return Lists.newArrayList();
        }else{
            List<CustomerProductModel> filterList = list.stream().filter(i-> i!=null && i.getBrandId()!=null && i.getBrandId().equals(brandId)).collect(Collectors.toList());
            return filterList;
        }
    }

    /**
     * 根据客户,产品获取 客户产品名称，型号，产品id（如云米的69码）
     * @param customerId
     * @param productId
     * @return
     */
    public List<CustomerProductModel> findListByCustomerAndProduct(Long customerId,Long productId){
        List<CustomerProductModel> list = new ArrayList<>();
        MSResponse<List<CustomerProductModel>> msResponse = customerProductModelFeign.findListByCustomerAndProduct(customerId,productId);
        if(MSResponse.isSuccess(msResponse)){
            list =  msResponse.getData();
        }
        if(list == null || list.isEmpty()){
            return Lists.newArrayList();
        } else {
            return list;
        }
    }
}

package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerAddress;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerNewFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MSCustomerNewService {

    @Autowired
    private MSCustomerNewFeign msCustomerNewFeign;

    @Autowired
    private MapperFacade mapper;

    public MDCustomerAddress getCustomerAddress(Long customerId, Integer addressType) {
        return MDUtils.getObjUnnecessaryConvertType(() -> msCustomerNewFeign.getByCustomerIdAndType(customerId, addressType));
    }

    public MDCustomerAddress getCustomerAddress(Long addressId) {
        return MDUtils.getObjUnnecessaryConvertType(() -> msCustomerNewFeign.getById(addressId));
    }

    public Integer deleteCustomerAddress(Long addressId) {
        return MDUtils.getObjUnnecessaryConvertType(() -> msCustomerNewFeign.delete(addressId));
    }
    public List<MDCustomerAddress> getCustomerAllAddress(Long customerId) {
        return MDUtils.getObjUnnecessaryConvertType(() -> msCustomerNewFeign.findListByCustomerId(customerId));
    }

        public MSResponse<Integer> updateCustomerAddress(MDCustomerAddress mdCustomerAddress) {
        return  msCustomerNewFeign.update(mdCustomerAddress);
   }

    public MSResponse<Long> insertCustomerAddress(MDCustomerAddress mdCustomerAddress) {
        return  msCustomerNewFeign.insert(mdCustomerAddress);
    }


    public MSResponse<NameValuePair<Long, Long>> insertCustomerUnion(Customer customer) {
        MDCustomer mdCustomer = mapper.map(customer, MDCustomer.class);
        return msCustomerNewFeign.insertCustomerUnion(mdCustomer);
    }

    public MSResponse<Integer> updateCustomerUnion(Customer customer) {
        MDCustomer mdCustomer = mapper.map(customer, MDCustomer.class);
        return msCustomerNewFeign.updateCustomerUnion(mdCustomer);
    }

    public MSResponse<Long> existByName(String customerName) {
        return msCustomerNewFeign.existByName(customerName);
    }


    /**
     * 根据客户id和地址类型从缓存中获取地址信息
     * @param customerId
     * @param addressType
     */
    public MDCustomerAddress getByCustomerIdAndTypeFromCache(Long customerId,Integer addressType){
        MSResponse<MDCustomerAddress> msResponse = msCustomerNewFeign.getByCustomerIdAndTypeFromCache(customerId,addressType);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }else{
            return null;
        }
    }

    public Customer getCustomerByIdFromCache(Long id) {
        return MDUtils.getById(id, Customer.class, msCustomerNewFeign::getCustomerByIdFromCache);
    }

    public MSResponse reloadCustomerCacheById(Long id){
        return msCustomerNewFeign.reloadCustomerCacheById(id);
    }

}

package com.wolfking.jeesite.modules.customer.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CtCustomerMaterialService {
    @Autowired
    private MSCustomerMaterialService msCustomerMaterialService;

    /**
     * 添加数据
     * @param  customerMaterial
     * @return Long
    */
    public void save(CustomerMaterial customerMaterial){
        Customer customer = customerMaterial.getCustomer();
        Product product = customerMaterial.getProduct();
        //dao.deleteByCustomerAndProduct(customer.getId(),product.getId()); //mark on 2020-1-10
        Map<String,Object> map = Maps.newHashMap();
        List<CustomerMaterial> list = Lists.newArrayList();
        CustomerMaterial entity;
        for(CustomerMaterialItem item:customerMaterial.getItemList()){
            entity = new CustomerMaterial();
            Material material = new Material();
            material.setId(item.getMaterialId());
            entity.setCustomer(customer);
            entity.setProduct(product);
            entity.setMaterial(material);
            entity.setIsReturn(item.getIsReturn());
            entity.setCustomerPartCode(item.getCustomerPartCode()==null?"":item.getCustomerPartCode());
            entity.setCustomerPartName(item.getCustomerPartName()==null?"":item.getCustomerPartName());
            entity.setWarrantyDay(item.getWarrantyDay()==null? 0:item.getWarrantyDay());
            entity.setPrice(item.getPrice());
            entity.setRecycleFlag(item.getRecycleFlag());
            if(item.getRecycleFlag() == 0){
                entity.setRecyclePrice(0.0D);
            }else {
                entity.setRecyclePrice(item.getRecyclePrice());
            }
            entity.setRemarks(item.getRemarks());
            entity.preInsert();
            //super.save(entity);  //mark on 2020-1-10
            map.put(item.getMaterialId().toString(),entity);
            list.add(entity);
        }
        //调用微服务 2019-9-21
        if (list.isEmpty()) {
            if(customer.getId() != null && product.getId() != null) {
                msCustomerMaterialService.deleteByCustomerAndProduct(customer.getId(), product.getId());
            }
        } else {
            MSErrorCode msErrorCode = msCustomerMaterialService.batchInsert(list);
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("保存客户配件.失败原因:" + msErrorCode.getMsg());
            }
        }
    }


    /**
     * 删除数据
     * @param  customerMaterial
     * @return Long
    */
    @Transactional()
    public void delete(CustomerMaterial customerMaterial){
        //调用微服务 2019-9-21
        MSErrorCode msErrorCode = msCustomerMaterialService.delete(customerMaterial);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("删除客户配件.失败原因:"+msErrorCode.getMsg());
        }
    }
}

package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.md.MDCustomerMaterial;
import com.kkl.kklplus.entity.md.MDProductMaterial;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.CustomerMaterial;
import com.wolfking.jeesite.modules.md.entity.Material;
import com.wolfking.jeesite.modules.md.entity.ProductMaterial;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerMaterialFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

;

@Service
public class MSCustomerMaterialService {

    @Autowired
    private MSCustomerMaterialFeign mdCustomerMaterialFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取品牌信息
     * @param id
     * @return
     */
    public CustomerMaterial getById(Long id) {
        return MDUtils.getById(id, CustomerMaterial.class, mdCustomerMaterialFeign::getById);
    }


    /**
     * 获取分页数据
     * @param customerMaterialPage
     * @param customerMaterial
     * @return
     */
    public Page<CustomerMaterial> findList(Page<CustomerMaterial> customerMaterialPage, CustomerMaterial customerMaterial) {
        return MDUtils.findListForPage(customerMaterialPage, customerMaterial, CustomerMaterial.class, MDCustomerMaterial.class, mdCustomerMaterialFeign::findList);
    }
    /**
     * 根据客户品类产品分页获取分页数据
     * @param customerMaterialPage
     * @param customerMaterial
     * @return
     */
    public Page<CustomerMaterial> findAllList(Page<CustomerMaterial> customerMaterialPage, CustomerMaterial customerMaterial) {
        return MDUtils.findListForPage(customerMaterialPage, customerMaterial, CustomerMaterial.class, MDCustomerMaterial.class, mdCustomerMaterialFeign::findListByProductAndCustomerIdAndMaterial);
    }

    /**
     * 批量插入客户配件
     * @param customerMaterialList
     * @return
     */
    public MSErrorCode batchInsert(List<CustomerMaterial> customerMaterialList) {
        return MDUtils.genericBatchSave(customerMaterialList, MDCustomerMaterial.class, mdCustomerMaterialFeign::batchInsert);
    }

    /**
     * 删除
     * @param customerMaterial
     * @return
     */
    public MSErrorCode delete(CustomerMaterial customerMaterial) {
        return MDUtils.genericSave(customerMaterial, MDCustomerMaterial.class, false, mdCustomerMaterialFeign::delete);
    }

    /**
     * 根据客户id,产品id,配件id获取客户配件
     * @param customerId,productId,materialId
     * @return
     */
    public CustomerMaterial getCustomerMaterialByCustomerAndProductAndMaterial(Long customerId,Long productId,Long materialId){
        MSResponse<MDCustomerMaterial> msResponse = mdCustomerMaterialFeign.getCustomerMaterialByCustomerAndProductAndMaterial(customerId,productId,materialId);
        if(MSResponse.isSuccess(msResponse)){
            CustomerMaterial customerMaterial =mapper.map(msResponse.getData(),CustomerMaterial.class);
            return customerMaterial;
        }else{
            return null;
        }
    }

    /**
     * 根据客户id,产品id获取客户配件
     * @param customerId,productId
     * @return
     */
    public List<CustomerMaterial> findListByCustomerAndProduct(Long customerId,Long productId){
        MSResponse<List<MDCustomerMaterial>> msResponse = mdCustomerMaterialFeign.findListByCustomerAndProduct(customerId,productId);
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerMaterial> list = mapper.mapAsList(msResponse.getData(),CustomerMaterial.class);
            return list;
        }else{
            return Lists.newArrayList();
        }
    }

    /**
     * 据客户ID、产品ID、配件IDS获取配件列表信息
     * @param customerId,productId
     * @return
     */
    public List<CustomerMaterial> findListByCustomerAndProductAndMaterialIds(Long customerId, List<NameValuePair<Long, Long>> pmIds){
        MSResponse<List<MDCustomerMaterial>> msResponse = mdCustomerMaterialFeign.findListByCustomerIdAndMaterialIdsFromCache(customerId,pmIds);
        if(MSResponse.isSuccess(msResponse)){
            List<CustomerMaterial> list = mapper.mapAsList(msResponse.getData(),CustomerMaterial.class);
            return list;
        }else{
            return Lists.newArrayList();
        }
    }
    /**
     * 根据客户id和产品id删除客户配件信息
     * @param customerId  客户id
     * @param productId   产品id
     */
    public void deleteByCustomerAndProduct(Long customerId, Long productId) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->mdCustomerMaterialFeign.deleteByCustomerAndProduct(customerId, productId));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务删除客户配件信息失败，异常信息：" + msErrorCode.getMsg());
        }
    }

    /**
     * 批量更新客户配件
     * @param customerMaterials
     * @return
     */
    public String updateCustomerMaterials(List<MDCustomerMaterial> customerMaterials) {
       MSResponse<String> msResponse = mdCustomerMaterialFeign.updateCustomerMaterials(customerMaterials);
       if (!MSResponse.isSuccess(msResponse)) {
           throw new RuntimeException("调用微服务删除客户配件信息失败，异常信息：" + msResponse.getMsg());
       }
       return msResponse.getData();
    }

    public void insertCustomerMaterial(MDCustomerMaterial mdCustomerMaterial){
        MSResponse<Integer> msResponse = mdCustomerMaterialFeign.insert(mdCustomerMaterial);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("调用微服务添加客户配件信息失败，异常信息：" + msResponse.getMsg());
        }
    }

    public void updateCustomerMaterial(MDCustomerMaterial mdCustomerMaterial){
        MSResponse<Integer> msResponse = mdCustomerMaterialFeign.update(mdCustomerMaterial);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("调用微服务修改客户配件信息失败，异常信息：" + msResponse.getMsg());
        }
    }

    public void updateCustomerMaterialId(MDCustomerMaterial mdCustomerMaterial){
        MSResponse<Integer> msResponse = mdCustomerMaterialFeign.updateMaterialId(mdCustomerMaterial);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("调用微服务修改客户配件信息失败，异常信息：" + msResponse.getMsg());
        }
    }

    public Long  getIdByCustomerAndProductAndMaterial(Long customerId,Long productId,Long materialId){
        MSResponse<Long> msResponse = mdCustomerMaterialFeign.getIdByCustomerAndProductAndMaterial(customerId,productId,materialId);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("调用微服务查询客户配件信息失败，异常信息：" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    /**
     * 根据客户id和产品id， 客户型号从客户配件中获取产品id及配件id列表
     *
     * @param customerId
     * @param productId
     * @param customerModel  客户型号
     * @return
     */
    public List<ProductMaterial> findProductMaterialByCustomerAndProduct(Long customerId, Long productId, String customerModel) {
       MSResponse<List<MDProductMaterial>> msResponse = mdCustomerMaterialFeign.findProductMaterialByCustomerAndProduct(customerId, productId, customerModel);
       if (!MSResponse.isSuccess(msResponse)) {
           return Lists.newArrayList();
       } else {
           List<ProductMaterial> productMaterials =  mapper.mapAsList(msResponse.getData(), ProductMaterial.class);
           return productMaterials;
       }
    }

    /**
     * 根据客户id和产品id、客户型号列表从客户配件中获取产品id及配件id列表
     *
     * @param customerId
     * @param nameValuePairs long-产品id，String-customerModel(客户型号)
     * @return
     */
    public List<ProductMaterial> findProductMaterialByCustomerAndProductIds(Long customerId, List<NameValuePair<Long,String>> nameValuePairs) {
        MSResponse<List<MDProductMaterial>> msResponse = mdCustomerMaterialFeign.findProductMaterialByCustomerAndProductIds(customerId, nameValuePairs);
        if (!MSResponse.isSuccess(msResponse)) {
            return Lists.newArrayList();
        } else {
            List<ProductMaterial> productMaterials =  mapper.mapAsList(msResponse.getData(), ProductMaterial.class);
            return productMaterials;
        }
    }

    /**
     * 根据客户id，产品id，配件id，客户型号(可为空)列表获取客户配件信息
     *
     * @param customerMaterials
     * @return
     */
    public List<CustomerMaterial> findListByCustomerMaterial(List<CustomerMaterial> customerMaterials) {
        List<MDCustomerMaterial> mdCustomerMaterialList = mapper.mapAsList(customerMaterials, MDCustomerMaterial.class);
        return MDUtils.findListNecessaryConvertType(CustomerMaterial.class, ()->mdCustomerMaterialFeign.findListByCustomerMaterial(mdCustomerMaterialList));
    }
}

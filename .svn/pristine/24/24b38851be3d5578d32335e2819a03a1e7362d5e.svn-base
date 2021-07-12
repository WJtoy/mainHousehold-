package com.wolfking.jeesite.ms.providermd.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerAction;
import com.kkl.kklplus.entity.md.MDCustomerProductType;
import com.kkl.kklplus.entity.md.MDCustomerProductTypeMapping;
import com.kkl.kklplus.entity.md.dto.MDCustomerActionDto;
import com.kkl.kklplus.entity.viomi.sd.FaultType;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerProductTypeFeign;
import com.wolfking.jeesite.ms.viomi.sd.feign.VioMiOrderFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MSCustomerProductTypeService {

    @Autowired
    private VioMiOrderFeign vioMiOrderFeign;

    @Autowired
    private MSCustomerProductTypeFeign msCustomerProductTypeFeign;

    /**
     * 获取客户故障类别
     *
     * @return
     */
    public List<FaultType> getProductParts() {
        MSResponse<List<FaultType>> msResponse = vioMiOrderFeign.getFaultType();
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("调用云米微服务获取故障类别失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public void updateCustomerActionList(List<MDCustomerAction> mdCustomerActionList, Integer beginFlag, Integer endFlag) {
        MSResponse<Integer> msResponse = msCustomerProductTypeFeign.updateCustomerActionList(mdCustomerActionList, beginFlag, endFlag);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("更新微服务故障类别失败,失败原因:" + msResponse.getMsg());
        }
    }

    public List<Long> findProductIds(Long customerId, Long customerProductTypeId) {
        MSResponse<List<Long>> msResponse = msCustomerProductTypeFeign.findProductIds(customerId, customerProductTypeId);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("获取微服务客户产品分类关联的产品失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public List<MDCustomerProductType> getCustomerProductTypeList(Long customerId) {
        MSResponse<List<MDCustomerProductType>> msResponse = msCustomerProductTypeFeign.findCustomerProductTypeListByCustomerId(customerId);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("根据客户获取客户产品分类列表,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public List<MDCustomerAction> findIdAndNameByCustomerId(Long customerId, Long customerProductTypeId) {
        MSResponse<List<MDCustomerAction>> msResponse = msCustomerProductTypeFeign.findIdAndNameByCustomerId(customerId, customerProductTypeId);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("获取微服务客户产品分类故障详情失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public List<MDCustomerProductTypeMapping> findProductTypeMappingByCustomerId(Long customerId) {
        MSResponse<List<MDCustomerProductTypeMapping>> msResponse = msCustomerProductTypeFeign.findProductTypeMappingByCustomerId(customerId);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("获取微服务客户产品分类列表失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public Integer batchInsert(Long customerId, List<Long> productIds) {
        MSResponse<Integer> msResponse = msCustomerProductTypeFeign.batchInsert(customerId, productIds);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("更新微服务客户产品分类关联的产品失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public List<MDCustomerProductTypeMapping> findListByCustomerId(Long customerId, Long customerProductTypeId) {
        MSResponse<List<MDCustomerProductTypeMapping>> msResponse = msCustomerProductTypeFeign.findListByCustomerId(customerId, customerProductTypeId);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("获取微服务客户产品分类关联产品失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public MSPage<MDCustomerActionDto> findCustomerActionDtoList(MDCustomerActionDto mdCustomerActionDto) {
        MSResponse<MSPage<MDCustomerActionDto>> msResponse = msCustomerProductTypeFeign.findCustomerActionDtoList(mdCustomerActionDto);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("获取微服务客户产品分类故障列表失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public Integer deleteCustomerAction(MDCustomerAction mdCustomerAction) {
        MSResponse<Integer> msResponse = msCustomerProductTypeFeign.deleteAction(mdCustomerAction);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("删除微服务客户产品分类故障列表失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public MDCustomerActionDto getCustomerActionDto(MDCustomerActionDto mdCustomerActionDto) {
        MSResponse<MDCustomerActionDto> msResponse = msCustomerProductTypeFeign.getCustomerActionDto(mdCustomerActionDto);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("根据客户Id,客户产品分类ID,一级,二级获取故障详情失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public Integer delete(MDCustomerAction mdCustomerAction) {
        MSResponse<Integer> msResponse = msCustomerProductTypeFeign.delete(mdCustomerAction);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("删除客户故障分析和故障处理失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public Integer saveCustomerActionDto(MDCustomerAction mdCustomerAction) {
        MSResponse<Integer> msResponse = msCustomerProductTypeFeign.saveCustomerActionDto(mdCustomerAction);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("保存客户故障分析和故障处理失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public List<String> findErrorTypeNameList(Long customerId, Long productId) {
        MSResponse<List<String>> msResponse = msCustomerProductTypeFeign.findErrorTypeNameList(customerId, productId);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("获取故障分类列表失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }


    public Long customerProductTypeInsert(MDCustomerProductType mdCustomerProductType) {
        MSResponse<Long> msResponse = msCustomerProductTypeFeign.customerProductTypeInsert(mdCustomerProductType);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("新增客户产品分类失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public Integer customerProductTypeUpdate(MDCustomerProductType mdCustomerProductType) {
        MSResponse<Integer> msResponse = msCustomerProductTypeFeign.customerProductTypeUpdate(mdCustomerProductType);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("修改客户产品分类失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public MSPage<MDCustomerProductType> customerProductTypeFindList(Long customerId, Integer pageNo, Integer pageSize) {
        MSResponse<MSPage<MDCustomerProductType>> msResponse = msCustomerProductTypeFeign.customerProductTypeFindList(customerId, pageNo, pageSize);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("获取客户产品分类列表失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public MDCustomerProductType customerProductTypeGetById(Long id) {
        MSResponse<MDCustomerProductType> msResponse = msCustomerProductTypeFeign.customerProductTypeGetById(id);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("根据ID获取客户产品分类详情失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public Integer customerProductTypeDelete(MDCustomerProductType mdCustomerProductType) {
        MSResponse<Integer> msResponse = msCustomerProductTypeFeign.customerProductTypeDelete(mdCustomerProductType);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("删除客户产品分类失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }

    public MDCustomerProductType getByCustomerIdAndName(Long customerId, String name) {
        MSResponse<MDCustomerProductType> msResponse = msCustomerProductTypeFeign.getByCustomerIdAndName(customerId, name);
        if (!MSResponse.isSuccess(msResponse)) {
            throw new RuntimeException("判断客户产品分类名称是否存在失败,失败原因:" + msResponse.getMsg());
        }
        return msResponse.getData();
    }
}

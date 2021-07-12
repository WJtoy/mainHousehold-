package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerAction;
import com.kkl.kklplus.entity.md.MDCustomerProductType;
import com.kkl.kklplus.entity.md.MDCustomerProductTypeMapping;
import com.kkl.kklplus.entity.md.dto.MDCustomerActionDto;
import com.kkl.kklplus.entity.viomi.sd.FaultType;
import com.kkl.kklplus.entity.viomi.sd.FaultTypeLevel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerProductTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerProductTypeService {

    @Autowired
    private MSCustomerProductTypeService msCustomerProductTypeService;


    /**
     * 更新客户故障列表
     *
     * @param customerId
     */
    public Integer updateProductTypeList(Long customerId) {
        List<FaultType> typeList = msCustomerProductTypeService.getProductParts();
        User user = UserUtils.getUser();
        Long userId = Optional.ofNullable(user).map(r -> r.getId()).orElse(0L);
        List<MDCustomerAction> mdCustomerActionList = Lists.newArrayList();
        MDCustomerAction mdCustomerAction;
        Date currentDate;
        if (!ObjectUtils.isEmpty(typeList)) {
            for (FaultType faultType : typeList) {
                for (FaultTypeLevel faultTypeLevel : faultType.getData()) {
                    for (FaultTypeLevel faultTypeLevelToo : faultTypeLevel.getData()) {
                        for (FaultTypeLevel faultTypeLevelThree : faultTypeLevelToo.getData()) {
                            mdCustomerAction = new MDCustomerAction();
                            currentDate = new Date();
                            mdCustomerAction.setCustomerId(customerId);
                            mdCustomerAction.setErrorTypeName(faultTypeLevel.getName());
                            mdCustomerAction.setErrorAppearanceName(faultTypeLevelToo.getName());
                            mdCustomerAction.setCustomerProductTypeName(faultType.getName());
                            mdCustomerAction.setCustomerProductTypeAlias(faultType.getAlias());
                            mdCustomerAction.setErrorAnalysisName(faultTypeLevelThree.getName());
                            mdCustomerAction.setErrorProcess(faultTypeLevelThree.getServiceMeasures());
                            mdCustomerAction.setServiceLevel(faultTypeLevelThree.getServiceLevel());
                            mdCustomerAction.setCreateById(userId);
                            mdCustomerAction.setCreateDate(currentDate);
                            mdCustomerAction.setUpdateById(userId);
                            mdCustomerAction.setUpdateDate(currentDate);
                            mdCustomerAction.setAutomaticFlag(1);
                            mdCustomerActionList.add(mdCustomerAction);
                        }
                    }
                }
            }
            AtomicInteger beginFlag = new AtomicInteger(1);
            Lists.partition(mdCustomerActionList, 500).forEach(list -> {
                int endFlag = 0;
                if (list.size() < 500) {
                    endFlag = 1;
                }
                msCustomerProductTypeService.updateCustomerActionList(list, beginFlag.get(), endFlag);
                beginFlag.set(0);

            });

        }
        return typeList.size();
    }

    public List<Long> findProductIds(Long customerId, Long customerProductTypeId) {
        return msCustomerProductTypeService.findProductIds(customerId, customerProductTypeId);
    }

    public List<MDCustomerProductType> getCustomerProductTypeList(Long customerId) {
        return msCustomerProductTypeService.getCustomerProductTypeList(customerId);
    }

    public List<MDCustomerAction> findIdAndNameByCustomerId(Long customerId, Long customerProductTypeId) {
        return msCustomerProductTypeService.findIdAndNameByCustomerId(customerId, customerProductTypeId);
    }

    public List<MDCustomerProductTypeMapping> findProductTypeMappingByCustomerId(Long customerId) {
        return msCustomerProductTypeService.findProductTypeMappingByCustomerId(customerId);
    }

    public void batchInsert(Long customerProductTypeId, List<Long> productIds) {
        msCustomerProductTypeService.batchInsert(customerProductTypeId, productIds);
    }

    public List<MDCustomerProductTypeMapping> findListByCustomerId(Long customerId, Long customerProductId) {
        return msCustomerProductTypeService.findListByCustomerId(customerId, customerProductId);
    }

    public Page<MDCustomerActionDto> findCustomerActionDtoList(Page page, MDCustomerActionDto mdCustomerActionDto) {
        MSPage<MDCustomerAction> dtoPage = new MSPage<>();
        dtoPage.setPageNo(page.getPageNo());
        dtoPage.setPageSize(page.getPageSize());
        mdCustomerActionDto.setPage(dtoPage);
        MSPage<MDCustomerActionDto> msPage = msCustomerProductTypeService.findCustomerActionDtoList(mdCustomerActionDto);
        Page<MDCustomerActionDto> entityPage = new Page<>();
        entityPage.setPageNo(msPage.getPageNo());
        entityPage.setPageSize(msPage.getPageSize());
        entityPage.setCount(msPage.getRowCount());
        entityPage.setList(msPage.getList());
        return entityPage;
    }

    public Integer deleteCustomerAction(MDCustomerAction mdCustomerAction) {
        return msCustomerProductTypeService.deleteCustomerAction(mdCustomerAction);
    }

    public MDCustomerActionDto getCustomerActionDto(MDCustomerActionDto mdCustomerActionDto) {
        return msCustomerProductTypeService.getCustomerActionDto(mdCustomerActionDto);
    }

    public Integer delete(MDCustomerAction mdCustomerAction) {
        return msCustomerProductTypeService.delete(mdCustomerAction);
    }

    public Integer saveCustomerActionDto(MDCustomerAction mdCustomerAction) {
        return msCustomerProductTypeService.saveCustomerActionDto(mdCustomerAction);
    }

    public List<String> findErrorTypeNameList(Long customerId, Long productId) {
        return msCustomerProductTypeService.findErrorTypeNameList(customerId, productId);
    }


    public Long customerProductTypeInsert(MDCustomerProductType mdCustomerProductType) {
        return msCustomerProductTypeService.customerProductTypeInsert(mdCustomerProductType);
    }

    public Integer customerProductTypeUpdate(MDCustomerProductType mdCustomerProductType) {
        return msCustomerProductTypeService.customerProductTypeUpdate(mdCustomerProductType);
    }

    public Page<MDCustomerProductType> customerProductTypeFindList(Long customerId, Integer pageNo, Integer pageSize) {
        MSPage<MDCustomerProductType> msPage = msCustomerProductTypeService.customerProductTypeFindList(customerId, pageNo, pageSize);
        Page<MDCustomerProductType> entityPage = new Page<>();
        entityPage.setPageNo(msPage.getPageNo());
        entityPage.setPageSize(msPage.getPageSize());
        entityPage.setCount(msPage.getRowCount());
        entityPage.setList(msPage.getList());
        return entityPage;
    }

    public MDCustomerProductType customerProductTypeGetById(Long id) {
        return msCustomerProductTypeService.customerProductTypeGetById(id);
    }

    public Integer customerProductTypeDelete(MDCustomerProductType mdCustomerProductType) {
        return msCustomerProductTypeService.customerProductTypeDelete(mdCustomerProductType);
    }

    public MDCustomerProductType getByCustomerIdAndName(Long customerId, String name) {
        return msCustomerProductTypeService.getByCustomerIdAndName(customerId, name);
    }
}

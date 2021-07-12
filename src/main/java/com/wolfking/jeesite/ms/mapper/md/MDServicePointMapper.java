package com.wolfking.jeesite.ms.mapper.md;

import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.kkl.kklplus.entity.md.MDServicePoint;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MDServicePointMapper extends CustomMapper<MDServicePoint, ServicePoint> {
    @Override
    public void mapAtoB(MDServicePoint a, ServicePoint b, MappingContext context) {
        b.setId(a.getId());
        b.setServicePointNo(a.getServicePointNo());
        b.setName(a.getName());
        b.setContactInfo1(a.getContactInfo1());
        b.setContactInfo2(a.getContactInfo2());
        b.setContractDate(a.getContractDate());
        b.setDeveloper(a.getDeveloper());
        b.setAddress(a.getAddress());
        b.setGrade(Optional.ofNullable(a.getGrade()).map(Long::intValue).orElse(0));
        b.setLevel(new Dict(Optional.ofNullable(a.getLevel()).orElse(0),""));
        b.setSignFlag(Optional.ofNullable(a.getSignFlag()).orElse(0));
        b.setOrderCount(Optional.ofNullable(a.getOrderCount()).map(Long::intValue).orElse(0));
        b.setUnfinishedOrderCount(Optional.ofNullable(a.getUnfinishedOrderCount()).orElse(0));
        b.setPlanCount(Optional.ofNullable(a.getPlanCount()).map(Long::intValue).orElse(0));
        b.setBreakCount(Optional.ofNullable(a.getBreakCount()).map(Long::intValue).orElse(0));
        b.setLongitude(a.getLongitude());
        b.setLatitude(a.getLatitude());
        b.setQq(a.getQq());
        b.setAttachment1(a.getAttachment1());
        b.setAttachment2(a.getAttachment2());
        b.setAttachment3(a.getAttachment3());
        b.setAttachment4(a.getAttachment4());
        b.setAttachment5(a.getAttachment5());
        b.setUseDefaultPrice(Optional.ofNullable(a.getUseDefaultPrice()).orElse(0));
        b.setShortMessageFlag(Optional.ofNullable(a.getShortMessageFlag()).orElse(0));
        b.setSubAddress(a.getSubAddress());
        b.setPrimary(Optional.ofNullable(a.getPrimaryId()).map(r->new Engineer(r)).orElse(null));
        b.setScale(Optional.ofNullable(a.getScale()).orElse(0));
        b.setProperty(Optional.ofNullable(a.getProperty()).orElse(0));
        b.setDescription(a.getDescription());
        b.setCreateBy(Optional.ofNullable(a.getCreateById()).map(r->new User(r)).orElse(null));
        b.setCreateDate(a.getCreateDate());
        b.setUpdateBy(Optional.ofNullable(a.getUpdateById()).map(r->new User(r)).orElse(null));
        b.setUpdateDate(a.getUpdateDate());
        b.setRemarks(a.getRemarks());
        b.setDelFlag(Optional.ofNullable(a.getDelFlag()).orElse(0));
        b.setArea(Optional.ofNullable(a.getAreaId()).map(r->new Area(r)).orElse(null));
        b.setSubEngineerCount(Optional.ofNullable(a.getSubEngineerCount()).map(Long::intValue).orElse(0));
        b.setAutoCompleteOrder(Optional.ofNullable(a.getAutoCompleteOrder()).orElse(0));
        b.setForTmall(Optional.ofNullable(a.getForTmall()).orElse(0));
        b.setCapacity(Optional.ofNullable(a.getCapacity()).map(Long::intValue).orElse(0));
        b.setPlanRemark(a.getPlanRemark());
        b.setPlanRemarks(a.getPlanRemarks());
        b.setInsuranceFlag(a.getInsuranceFlag());
        b.setAppInsuranceFlag(a.getAppInsuranceFlag());
        b.setTimeLinessFlag(a.getTimeLinessFlag());
        b.setInvoiceFlag(a.getInvoiceFlag());
        b.setDiscountFlag(a.getDiscountFlag());
        b.setPaymentType(new Dict(Optional.ofNullable(a.getPaymentType()).orElse(0),""));
        b.setBank(new Dict(Optional.ofNullable(a.getBank()).orElse(0), ""));
        b.setBankNo(a.getBankNo());
        b.setBankOwner(a.getBankOwner());
        b.setBankIssue(new Dict(Optional.ofNullable(a.getBankIssue()).orElse(0), ""));
        b.setStatus(new Dict(Optional.ofNullable(a.getStatus()).orElse(0),""));
        b.setAutoPlanFlag(Optional.ofNullable(a.getAutoPlanFlag()).orElse(0));
        b.setProductCategoryIds(a.getProductCategoryIds());
        b.setCustomizePriceFlag(a.getCustomizePriceFlag());
        b.setDegree(a.getDegree());
        b.setPraiseFeeFlag(a.getPraiseFeeFlag());
        b.setCustomerTimeLinessFlag(a.getCustomerTimeLinessFlag());
        b.setAppFlag(a.getAppFlag());
        b.setBankOwnerIdNo(a.getBankOwnerIdNo());
        b.setBankOwnerPhone(a.getBankOwnerPhone());
        b.setNeedAuthFlag(a.getNeedAuthFlag());
        b.setCompleteAuthFlag(a.getCompleteAuthFlag());
        b.setPaymentChannel(a.getPaymentChannel());
        b.setAutoPaymentFlag(a.getAutoPaymentFlag());
        b.setPlanContactFlag(a.getPlanContactFlag());
        if(b.getMdDepositLevel() == null){
            b.setMdDepositLevel(new MDDepositLevel());
        }
        b.getMdDepositLevel().setId(a.getDepositLevelId());
        b.setDepositFromOrderFlag(a.getDepositFromOrderFlag());
        b.setDeposit(a.getDeposit());
        b.setRemotePriceFlag(a.getRemotePriceFlag());
        b.setRemotePriceType(a.getRemotePriceType());
        b.setRemotePriceEnabledFlag(a.getRemotePriceEnabledFlag());
    }

    @Override
    public void mapBtoA(ServicePoint b, MDServicePoint a, MappingContext context) {
        a.setId(b.getId());
        a.setServicePointNo(b.getServicePointNo());
        a.setName(b.getName());
        a.setContactInfo1(b.getContactInfo1());
        a.setContactInfo2(b.getContactInfo2());
        a.setContractDate(b.getContractDate());
        a.setDeveloper(b.getDeveloper());
        a.setAddress(b.getAddress());
        a.setGrade(Long.valueOf(b.getGrade()));
        a.setLevel(Optional.ofNullable(b.getLevel()).map(Dict::getValue).map(Integer::valueOf).orElse(null));
        a.setSignFlag(b.getSignFlag());
        a.setOrderCount(Long.valueOf(b.getOrderCount()));
        a.setUnfinishedOrderCount(b.getUnfinishedOrderCount());
        a.setPlanCount(Long.valueOf(b.getPlanCount()));
        a.setBreakCount(Long.valueOf(b.getBreakCount()));
        a.setLongitude(b.getLongitude());
        a.setLatitude(b.getLatitude());
        a.setQq(b.getQq());
        a.setAttachment1(b.getAttachment1());
        a.setAttachment2(b.getAttachment2());
        a.setAttachment3(b.getAttachment3());
        a.setAttachment4(b.getAttachment4());
        a.setAttachment5(b.getAttachment5());
        a.setUseDefaultPrice(b.getUseDefaultPrice());
        a.setShortMessageFlag(b.getShortMessageFlag());
        a.setSubAddress(b.getSubAddress());
        a.setPrimaryId(Optional.ofNullable(b.getPrimary()).map(Engineer::getId).orElse(null));
        a.setScale(b.getScale());
        a.setProperty(b.getProperty());
        a.setDescription(b.getDescription());
        a.setCreateById(Optional.ofNullable(b.getCreateBy()).map(User::getId).orElse(null));
        a.setCreateDate(b.getCreateDate());
        a.setUpdateById(Optional.ofNullable(b.getUpdateBy()).map(User::getId).orElse(null));
        a.setUpdateDate(b.getUpdateDate());
        a.setRemarks(b.getRemarks());
        a.setDelFlag(b.getDelFlag());
        a.setAreaId(Optional.ofNullable(b.getArea()).map(Area::getId).orElse(null));
        a.setSubEngineerCount(Long.valueOf(b.getSubEngineerCount()));
        a.setAutoCompleteOrder(b.getAutoCompleteOrder());
        a.setForTmall(b.getForTmall());
        a.setCapacity(Optional.ofNullable(b.getCapacity()).map(Long::valueOf).orElse(null));
        a.setPlanRemark(Optional.ofNullable(b.getPlanRemark()).orElse(""));
        a.setPlanRemarks(b.getPlanRemarks());
        a.setInsuranceFlag(b.getInsuranceFlag());
        a.setAppInsuranceFlag(b.getAppInsuranceFlag());
        a.setTimeLinessFlag(b.getTimeLinessFlag());
        a.setInvoiceFlag(b.getInvoiceFlag());
        a.setDiscountFlag(b.getDiscountFlag());
        a.setPaymentType(Optional.ofNullable(b.getPaymentType()).map(Dict::getValue).map(Integer::valueOf).orElse(null));
        a.setBank(Optional.ofNullable(b.getBank()).map(Dict::getValue).map(Integer::valueOf).orElse(null));
        a.setBankNo(b.getBankNo());
        a.setBankOwner(b.getBankOwner());
        a.setBankIssue(Optional.ofNullable(b.getBankIssue()).map(Dict::getValue).map(Integer::valueOf).orElse(null));
        a.setStatus(Optional.ofNullable(b.getStatus()).map(Dict::getValue).map(Integer::valueOf).orElse(null));
        a.setAutoPlanFlag(b.getAutoPlanFlag());
        a.setProductCategoryIds(b.getProductCategoryIds());
        a.setCustomizePriceFlag(b.getCustomizePriceFlag());
        a.setDegree(b.getDegree());
        a.setPraiseFeeFlag(b.getPraiseFeeFlag());
        a.setCustomerTimeLinessFlag(b.getCustomerTimeLinessFlag());
        a.setAppFlag(b.getAppFlag());
        a.setBankOwnerIdNo(b.getBankOwnerIdNo());
        a.setBankOwnerPhone(b.getBankOwnerPhone());
        a.setNeedAuthFlag(b.getNeedAuthFlag());
        a.setCompleteAuthFlag(b.getCompleteAuthFlag());
        a.setPaymentChannel(b.getPaymentChannel());
        a.setAutoPaymentFlag(b.getAutoPaymentFlag());
        a.setPlanContactFlag(b.getPlanContactFlag());
        if(b.getMdDepositLevel() != null && b.getMdDepositLevel().getId() != null){
            a.setDepositLevelId(b.getMdDepositLevel().getId());
        }
        a.setDepositFromOrderFlag(b.getDepositFromOrderFlag());
        a.setDeposit(b.getDeposit());
        a.setRemotePriceFlag(b.getRemotePriceFlag());
        a.setRemotePriceType(b.getRemotePriceType());
        a.setRemotePriceEnabledFlag(b.getRemotePriceEnabledFlag());
    }
}

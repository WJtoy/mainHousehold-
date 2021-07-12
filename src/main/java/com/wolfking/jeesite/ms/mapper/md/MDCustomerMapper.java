package com.wolfking.jeesite.ms.mapper.md;

import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.MdAttachment;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MDCustomerMapper  extends CustomMapper<MDCustomer,Customer> {
    @Override
    public void mapAtoB(MDCustomer a, Customer b, MappingContext context) {
        b.setId(a.getId());
        b.setCode(a.getCode());
        b.setName(a.getName());
        b.setFullName(a.getFullName());
        b.setAddress(a.getAddress());
        b.setSales(a.getSalesId()==null?null:new User(a.getSalesId()));
        Dict paymentType = a.getPaymentType()==null ? null:MSDictUtils.getDictByValue(a.getPaymentType().toString(), "PaymentType");
        if (paymentType != null) {
            b.setPaymentType(paymentType);
        } else {
            b.setPaymentType(null);
        }
        b.setZipCode(a.getZipCode());
        b.setMaster(a.getMaster());
        b.setMobile(a.getMobile());
        b.setPhone(a.getPhone());
        b.setFax(a.getFax());
        b.setEmail(a.getEmail());
        b.setContractDate(a.getContractDate());
        b.setProjectOwner(a.getProjectOwner());
        b.setProjectOwnerPhone(a.getProjectOwnerPhone());
        b.setProjectOwnerQq(a.getProjectOwnerQq());
        b.setServiceOwner(a.getServiceOwner());
        b.setServiceOwnerPhone(a.getServiceOwnerPhone());
        b.setServiceOwnerQq(a.getServiceOwnerQq());
        b.setFinanceOwner(a.getFinanceOwner());
        b.setFinanceOwnerPhone(a.getFinanceOwnerPhone());
        b.setFinanceOwnerQq(a.getFinanceOwnerQq());
        b.setTechnologyOwner(a.getTechnologyOwner());
        b.setTechnologyOwnerPhone(a.getTechnologyOwnerPhone());
        b.setTechnologyOwnerQq(a.getTechnologyOwnerQq());
        b.setDefaultBrand(a.getDefaultBrand());
        b.setEffectFlag(a.getEffectFlag());
        b.setLogo(a.getLogoId()==null?null:new MdAttachment(a.getLogoId()));
        b.setAttachment1(a.getAttachment1Id()==null?null:new MdAttachment(a.getAttachment1Id()));
        b.setAttachment2(a.getAttachment2Id()==null?null:new MdAttachment(a.getAttachment2Id()));
        b.setAttachment3(a.getAttachment3Id()==null?null:new MdAttachment(a.getAttachment3Id()));
        b.setAttachment4(a.getAttachment4Id()==null?null:new MdAttachment(a.getAttachment4Id()));
        b.setIsFrontShow(a.getIsFrontShow());
        b.setSort(a.getSort());
        b.setDescription(a.getDescription());
        b.setMinUploadNumber(a.getMinUploadNumber());
        b.setMaxUploadNumber(a.getMaxUploadNumber());
        b.setReturnAddress(a.getReturnAddress());
        b.setOrderApproveFlag(a.getOrderApproveFlag());
        b.setTimeLinessFlag(a.getTimeLinessFlag());
        b.setUrgentFlag(a.getUrgentFlag());
        b.setShortMessageFlag(a.getShortMessageFlag());
        b.setRemarks(a.getRemarks());
        b.setReminderFlag(a.getReminderFlag()==null?0:a.getReminderFlag().intValue());
        b.setMerchandiser(new User(Optional.ofNullable(a.getMerchandiserId()).orElse(0L),""));
        b.setVipFlag(a.getVipFlag());
        b.setMaterialFlag(a.getMaterialFlag());
        b.setErrorFlag(a.getErrorFlag());
        b.setRemoteFeeFlag(a.getRemoteFeeFlag());
        b.setCustomerAddresses(a.getCustomerAddressList());
        if(!b.getProductIds().isEmpty()){
            b.setProductIds(StringUtils.join(a.getProducts(), ","));
        }
        b.setUseDefaultPrice(Optional.ofNullable(a.getUseDefaultPrice()).orElse(0));
        b.setUpdateBy(new User(a.getUpdateById()));
        b.setUpdateDate(a.getUpdateDate());
        b.setCreateBy(new User(a.getCreateById()));
        b.setCreateDate(a.getCreateDate());
        b.setContractFlag(a.getContractFlag());
        b.setCustomizePriceFlag(a.getCustomizePriceFlag());
        if(a.getVip()==null){
            b.setVip(0);
        }else{
            b.setVip(a.getVip());
        }
        if(a.getVipName() == null){
            b.setVipName("");
        }else {
            b.setVipName(a.getVipName());
        }
        b.setOfflineOrderFlag(a.getOfflineOrderFlag());
        b.setAutoCompleteOrder(a.getAutoCompleteOrder());

    }

    @Override
    public void mapBtoA(Customer b, MDCustomer a, MappingContext context) {
        a.setId(b.getId());
        a.setCode(b.getCode());
        a.setName(b.getName());
        a.setFullName(b.getFullName());
        a.setAddress(b.getAddress());
        a.setSalesId(b.getSales()==null?null:b.getSales().getId());
        a.setPaymentType(b.getPaymentType() != null?b.getPaymentType().getIntValue():null);
        a.setZipCode(b.getZipCode());
        a.setMaster(b.getMaster());
        a.setMobile(b.getMobile());
        a.setPhone(b.getPhone());
        a.setFax(b.getFax());
        a.setEmail(b.getEmail());
        a.setContractDate(b.getContractDate());
        a.setProjectOwner(b.getProjectOwner());
        a.setProjectOwnerPhone(b.getProjectOwnerPhone());
        a.setProjectOwnerQq(b.getProjectOwnerQq());
        a.setServiceOwner(b.getServiceOwner());
        a.setServiceOwnerPhone(b.getServiceOwnerPhone());
        a.setServiceOwnerQq(b.getServiceOwnerQq());
        a.setFinanceOwner(b.getFinanceOwner());
        a.setFinanceOwnerPhone(b.getFinanceOwnerPhone());
        a.setFinanceOwnerQq(b.getFinanceOwnerQq());
        a.setTechnologyOwner(b.getTechnologyOwner());
        a.setTechnologyOwnerPhone(b.getTechnologyOwnerPhone());
        a.setTechnologyOwnerQq(b.getTechnologyOwnerQq());
        a.setDefaultBrand(b.getDefaultBrand());
        a.setEffectFlag(b.getEffectFlag());
        a.setLogoId(b.getLogo()==null?null:b.getLogo().getId());
        a.setAttachment1Id(b.getAttachment1()==null?null:b.getAttachment1().getId());
        a.setAttachment2Id(b.getAttachment2()==null?null:b.getAttachment2().getId());
        a.setAttachment3Id(b.getAttachment3()==null?null:b.getAttachment3().getId());
        a.setAttachment4Id(b.getAttachment4()==null?null:b.getAttachment4().getId());
        a.setIsFrontShow(b.getIsFrontShow());
        a.setSort(b.getSort());
        a.setDescription(b.getDescription());
        a.setMinUploadNumber(b.getMinUploadNumber());
        a.setMaxUploadNumber(b.getMaxUploadNumber());
        a.setReturnAddress(b.getReturnAddress());
        a.setOrderApproveFlag(b.getOrderApproveFlag());
        a.setTimeLinessFlag(b.getTimeLinessFlag());
        a.setUrgentFlag(b.getUrgentFlag());
        a.setShortMessageFlag(b.getShortMessageFlag());
        a.setRemarks(b.getRemarks());
        a.setReminderFlag(b.getReminderFlag());
        a.setMerchandiserId(Optional.ofNullable(b.getMerchandiser()).map(User::getId).orElse(null));
        a.setVipFlag(b.getVipFlag());
        a.setMaterialFlag(b.getMaterialFlag());
        a.setErrorFlag(b.getErrorFlag());
        a.setRemoteFeeFlag(b.getRemoteFeeFlag());
        a.setCustomerAddressList(b.getCustomerAddresses());
        if (StringUtils.isNotBlank(b.getProductIds())) {
            a.setProducts(Arrays.stream(b.getProductIds().split(",")).map(Long::valueOf).collect(Collectors.toList()));
        }
        a.setUpdateById(Optional.ofNullable(b.getUpdateBy()).map(User::getId).orElse(null));
        a.setUpdateDate(b.getUpdateDate());
        a.setCreateById(Optional.ofNullable(b.getCreateBy()).map(User::getId).orElse(null));
        a.setCreateDate(b.getCreateDate());
        a.setUseDefaultPrice(b.getUseDefaultPrice());
        a.setContractFlag(b.getContractFlag());
        a.setCustomizePriceFlag(b.getCustomizePriceFlag());
        a.setVip(b.getVip());
        a.setVipName(b.getVipName());
        a.setOfflineOrderFlag(b.getOfflineOrderFlag());
        a.setAutoCompleteOrder(b.getAutoCompleteOrder());
    }
}

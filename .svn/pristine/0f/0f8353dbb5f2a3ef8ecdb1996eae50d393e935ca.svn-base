package com.wolfking.jeesite.ms.recharge.entity.mapper;


import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.ms.recharge.entity.CustomerOfflineRechargeModel;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.mapstruct.*;

import java.util.List;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)

public abstract class CustomerOffineRechargeModelMapper {

    private static String DateFormat = "yyyy-MM-dd HH:mm:ss";

    private static CustomerService customerService = SpringContextHolder.getBean(CustomerService.class);

    /**
     * 数据转视图模型
     */
    public abstract CustomerOfflineRechargeModel toViewModel(CustomerOfflineRecharge model);

    @AfterMapping
    protected void after(@MappingTarget final CustomerOfflineRechargeModel viewModel, CustomerOfflineRecharge model){
        Customer customer = customerService.getFromCache(model.getCustomerId());
        if(customer!=null){
            viewModel.setCustomerName(customer.getName());
        }
        if(model.getCreateAt()!= null && model.getCreateAt()>0){
            viewModel.setRechargeTime(DateFormatUtils.format(model.getCreateAt(), DateFormat));
        }
        if(model.getUpdateAt()!= null && model.getUpdateAt()>0){
            viewModel.setPendingTime(DateFormatUtils.format(model.getUpdateAt(), DateFormat));
        }
    }
    /**
     * 转List
     */
    public abstract List<CustomerOfflineRechargeModel> toViewModels(List<CustomerOfflineRecharge> models);
}

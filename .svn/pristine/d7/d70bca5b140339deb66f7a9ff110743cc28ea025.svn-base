package com.wolfking.jeesite.modules.md.service;

import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.md.dao.CustomerAccountProfileDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerAccountProfileService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created on 2017-05-03.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerAccountProfileService extends LongIDCrudService<CustomerAccountProfileDao, CustomerAccountProfile> {
    @Autowired
    private MSCustomerAccountProfileService msCustomerAccountProfileService;

    @Autowired
    private MSCustomerService msCustomerService;

    /**
     * 获取单条数据
     *
     * @param id
     * @return
     */
    @Override
    public CustomerAccountProfile get(long id) {
        CustomerAccountProfile customerAccountProfile =  msCustomerAccountProfileService.getById(id);
        if (customerAccountProfile != null) {
            if (customerAccountProfile.getCustomer() != null && customerAccountProfile.getCustomer().getId() != null) {
                Customer customer = msCustomerService.getByIdToCustomer(customerAccountProfile.getCustomer().getId());
                if (customer != null) {
                    customerAccountProfile.setCustomer(customer);
                }
            }
        }
        return customerAccountProfile;
    }
}

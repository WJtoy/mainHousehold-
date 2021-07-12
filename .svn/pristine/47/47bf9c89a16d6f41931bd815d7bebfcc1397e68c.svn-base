package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerPrice;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpCustomerService {


    @Autowired
    private CustomerService customerService;


    /**
     * 读取某客户的价格清单
     * 先从缓存读取，缓存不存在从数据库读取，并更新缓存
     *
     * @param id 客户id
     */
    public List<CustomerPrice> getPricesFromCache(Long id) {
        return customerService.getPricesFromCache(id);
    }

    /**
     * 从缓存读取客户信息
     * 只包含基本信息
     */
    public Customer getFromCache(long id) {
        return customerService.getFromCache(id);
    }
}

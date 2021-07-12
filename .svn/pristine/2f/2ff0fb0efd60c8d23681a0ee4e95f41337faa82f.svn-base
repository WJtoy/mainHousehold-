package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.modules.md.service.CustomerProductCompletePicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpCustomerProductCompletePicService {


    @Autowired
    private CustomerProductCompletePicService completePicService;

    /**
     * 优先从缓存中按id获得对象
     */
    public ProductCompletePic getFromCache(long productId, long customerId) {
        return completePicService.getFromCache(productId, customerId);
    }
}

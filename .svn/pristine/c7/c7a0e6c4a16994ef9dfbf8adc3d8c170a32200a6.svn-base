package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.wolfking.jeesite.modules.md.entity.ProductCompletePic;
import com.wolfking.jeesite.modules.md.service.ProductCompletePicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpProductCompletePicService {


    @Autowired
    private ProductCompletePicService productCompletePicService;


    /**
     * 优先从缓存中按id获得对象
     *
     * @param prouctId 产品id
     */
    public ProductCompletePic getFromCache(long prouctId) {
        return productCompletePicService.getFromCache(prouctId);
    }

}

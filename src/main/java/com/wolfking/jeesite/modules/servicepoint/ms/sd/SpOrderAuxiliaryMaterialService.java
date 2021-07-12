package com.wolfking.jeesite.modules.servicepoint.ms.sd;

import com.wolfking.jeesite.modules.sd.service.OrderAuxiliaryMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpOrderAuxiliaryMaterialService {


    @Autowired
    private OrderAuxiliaryMaterialService auxiliaryMaterialService;


    /**
     * 是否有附加费用(辅材)
     */
    public boolean hasAuxiliaryMaterials(Long orderId, String quarter) {
        return auxiliaryMaterialService.hasAuxiliaryMaterials(orderId, quarter);
    }
}

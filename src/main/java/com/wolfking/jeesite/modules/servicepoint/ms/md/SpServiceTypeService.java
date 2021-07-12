package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpServiceTypeService {


    @Autowired
    private ServiceTypeService serviceTypeService;


    /**
     * 获取所有服务类型
     */
    public Map<Long, ServiceType> getAllServiceTypeMap() {
        return serviceTypeService.getAllServiceTypeMap();
    }
}

package com.wolfking.jeesite.modules.servicepoint.ms.sys;

import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpSystemService {


    @Autowired
    private SystemService systemService;


    /**
     * 按安维id获得帐号信息
     */
    public User getUserByEngineerId(Long engineerId) {
        return systemService.getUserByEngineerId(engineerId);
    }
}

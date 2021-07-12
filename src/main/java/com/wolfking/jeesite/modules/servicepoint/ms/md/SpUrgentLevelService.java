package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpUrgentLevelService {


    @Autowired
    private UrgentLevelService urgentLevelService;

    /**
     * 加载所有加急等级，当缓存未命中则从数据库装载至缓存
     */
    public Map<Long, UrgentLevel> findAllMap() {
        return urgentLevelService.findAllMap();
    }
}

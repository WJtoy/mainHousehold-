package com.wolfking.jeesite.modules.servicepoint.ms.md;

import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.ms.providermd.service.MSEngineerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpEngineerService {


    @Autowired
    private MSEngineerService engineerService;


    /**
     * 根据id列表获取安维人员map对象
     */
    public Map<Long, Engineer> findEngineersByIdsToMap(List<Long> ids, List<String> fields) {
        return engineerService.findEngineersByIdsToMap(ids, fields);
    }

    /**
     * 更新安维人员单数与评分
     */
    public void updateEngineerByMap(Map<String, Object> paramMap) {
        engineerService.updateEngineerByMap(paramMap);
    }
}

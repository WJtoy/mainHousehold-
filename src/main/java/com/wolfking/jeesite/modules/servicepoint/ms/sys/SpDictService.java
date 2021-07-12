package com.wolfking.jeesite.modules.servicepoint.ms.sys;

import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SpDictService {


    @Autowired
    private MSDictService dictService;


    /**
     * 根据类型查询字典项
     */
    public List<Dict> findListByType(String type) {
        return dictService.findListByType(type);
    }
}

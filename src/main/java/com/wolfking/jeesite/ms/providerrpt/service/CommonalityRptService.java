package com.wolfking.jeesite.ms.providerrpt.service;

import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CommonalityRptService {

    /**
     * 读取区域
     *
     * @param fromType 结束区域层级
     * @return
     */
    public List<Area> findAll(Integer fromType) {
        List<Area> list = UserUtils.getAreaList();
        if (fromType == null) {
            return list;
        } else {
            return list.stream().filter(t -> t.getType() <= fromType)
                    .sorted(Comparator.comparingInt(Area::getSort)).collect(Collectors.toList());
        }
    }
}

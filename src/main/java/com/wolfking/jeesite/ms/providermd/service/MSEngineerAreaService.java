package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDEngineerArea;
import com.wolfking.jeesite.ms.providermd.feign.MSEngineerAreaFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MSEngineerAreaService {
    @Autowired
    private MSEngineerAreaFeign msEngineerAreaFeign;

    /**
     * 通过安维id获取安维对应的区域id
     *
     * @param engineerId
     * @return
     */
    public List<Long> findEngineerAreaIds(Long engineerId) {
        MSResponse<List<Long>> msResponse = msEngineerAreaFeign.findEngineerAreaIds(engineerId);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return Lists.newArrayList();
    }

    /**
     * 通过安维id获取安维区域id列表
     *
     * @param engineerIds
     * @return
     */
    public List<MDEngineerArea> findEngineerAreasWithIds(List<Long> engineerIds) {
        MSResponse<List<MDEngineerArea>> msResponse = msEngineerAreaFeign.findEngineerAreasWithIds(engineerIds);
        if (MSResponse.isSuccess(msResponse)) {
            return msResponse.getData();
        }
        return Lists.newArrayList();
    }

    /**
     * 给安维人员分配区域id
     *
     * @param engineerId
     * @param areas
     * @return
     */
    public void assignEngineerAreas(List<Long> areas, Long engineerId) {
        MSErrorCode msErrorCode = MDUtils.customSave(()-> msEngineerAreaFeign.assignEngineerAreas(areas, engineerId));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用安维区域微服务保存数据错误，出错原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 根据安维人员id删除其对应的区域信息
     *
     * @param engineerId
     * @return
     */
    public void removeEnigineerAreas(Long engineerId) {
        MSErrorCode msErrorCode = MDUtils.genericCustomConditionSave(engineerId, msEngineerAreaFeign::removeEnigineerAreas);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用安维区域微服务删除数据错误，出错原因:" + msErrorCode.getMsg());
        }
    }


    /**
     * 根据安维人员id删除其对应的区域信息
     *
     * @param servicePointId
     * @return
     */
    public void deleteEnigineerAreas(Long servicePointId, List<Long> areas) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msEngineerAreaFeign.deleteEnigineerAreas(servicePointId, areas));
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用安维区域微服务删除失效安维区域数据错误，出错原因:" + msErrorCode.getMsg());
        }
    }


}

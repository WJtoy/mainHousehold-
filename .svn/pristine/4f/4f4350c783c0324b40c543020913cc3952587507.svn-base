package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.wolfking.jeesite.ms.providermd.feign.MSDepositLevelFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MSDepositLevelService {

    @Autowired
    private MSDepositLevelFeign msDepositLevelFeign;

    public MDDepositLevel getById(Long id){
        MDDepositLevel mdDepositLevel = new MDDepositLevel();
        MSResponse<MDDepositLevel> msResponse = msDepositLevelFeign.getById(id);
        if (MSResponse.isSuccess(msResponse)) {
            mdDepositLevel = msResponse.getData();
        }
        return mdDepositLevel;
    }

    public List<MDDepositLevel> findAllLevelList(){
        List<MDDepositLevel> list = Lists.newArrayList();
        MSResponse<List<MDDepositLevel>> msResponse = msDepositLevelFeign.findAllLevelList();
        if (MSResponse.isSuccess(msResponse)) {
            list = msResponse.getData();
        }
        return list;
    }

    /**
     * 读取所有质保金等级信息
     *
     * @return
     */
    public Map<Long,MDDepositLevel> getAllLevelMap(){
        try {
            MSResponse<List<MDDepositLevel>> msResponse = msDepositLevelFeign.findAllLevelList();
            if (!MSResponse.isSuccess(msResponse)) {
                return Maps.newHashMap();
            }
            return msResponse.getData().stream().collect(Collectors.toMap(MDDepositLevel::getId, t -> t));
        }catch (Exception e){
            return Maps.newHashMap();
        }
    }
}

package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDDepositLevel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSDepositLevelFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DepositLevelService {

    @Autowired
    private MSDepositLevelFeign msDepositLevelFeign;

    public Page<MDDepositLevel> findList(Page<MDDepositLevel> page) {
        int pageNo =  page.getPageNo();
        int pageSize =  page.getPageSize();
        MSResponse<MSPage<MDDepositLevel>> returnResponse = msDepositLevelFeign.findList(pageNo,pageSize);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<MDDepositLevel> msPage = returnResponse.getData();
            List<MDDepositLevel> list = msPage.getList();
            page.setList(list);
            page.setCount(msPage.getRowCount());
        } else {
            page.setCount(0);
            page.setList(Lists.newArrayList());
        }
        return page;
    }


    public MDDepositLevel getById(Long id){
        MDDepositLevel mdDepositLevel = new MDDepositLevel();
        MSResponse<MDDepositLevel> msResponse = msDepositLevelFeign.getById(id);
        if (MSResponse.isSuccess(msResponse)) {
            mdDepositLevel = msResponse.getData();
        }
        return mdDepositLevel;
    }

    public void save(MDDepositLevel mdDepositLevel){
        User user = UserUtils.getUser();
        if(mdDepositLevel != null){
            if(mdDepositLevel.getId() != null && mdDepositLevel.getId() > 0){
                mdDepositLevel.setUpdateById(user.getId());
                mdDepositLevel.setUpdateDate(new Date());
                msDepositLevelFeign.update(mdDepositLevel);
            }else {
                mdDepositLevel.setCreateById(user.getId());
                mdDepositLevel.setCreateDate(new Date());
                msDepositLevelFeign.insert(mdDepositLevel);
            }
        }
    }

    public Long getByName(String name){
        MSResponse<Long> msResponse = msDepositLevelFeign.getByName(name);
        return msResponse.getData();
    }

    public Long getByCode(String code){
        MSResponse<Long> msResponse = msDepositLevelFeign.getByCode(code);
        return msResponse.getData();
    }

    public void   delete(Long id){
        User user = UserUtils.getUser();
        MDDepositLevel mdDepositLevel = new MDDepositLevel();
        mdDepositLevel.setId(id);
        mdDepositLevel.setUpdateById(user.getId());
        mdDepositLevel.setUpdateDate(new Date());
        MSResponse<Integer> msResponse = msDepositLevelFeign.delete(mdDepositLevel);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }
    }

    public Integer isDepositLevel(Long id){
        MSResponse<Integer> msResponse = msDepositLevelFeign.getStatusByDepositLevelId(id);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }

        return  msResponse.getData();
    }

    /**
     * 根据网点编号ID列表，返回对应的质保金等级信息
     * @param servicePointIds
     * @return
     */
    public Map<Long,Map<String, Object>> getDepositLevelByServicePointIdsForFI(List<Long> servicePointIds){
        Map<Long,Map<String, Object>> result = new HashMap<>();
        MSResponse<Map<Long,Map<String, Object>>> msResponse = msDepositLevelFeign.getDepositLevelByServicePointIdsForFI(servicePointIds);
        if (MSResponse.isSuccess(msResponse)) {
            result = msResponse.getData();
        }
        return result;
    }
}

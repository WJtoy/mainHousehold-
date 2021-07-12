package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerVipLevel;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerVipLevelFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MSCustomerVipLevelService {

    @Autowired
    private MSCustomerVipLevelFeign msCustomerVipLevelFeign;

    public Page<MDCustomerVipLevel> findList(Page<MDCustomerVipLevel> page, MDCustomerVipLevel mdCustomerVipLevel) {
        MSPage<MDCustomerVipLevel> dtoPage = new MSPage<>();
        dtoPage.setPageNo(page.getPageNo());
        dtoPage.setPageSize(page.getPageSize());
        mdCustomerVipLevel.setPage(dtoPage);
        MSResponse<MSPage<MDCustomerVipLevel>> returnResponse = msCustomerVipLevelFeign.findList(mdCustomerVipLevel);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<MDCustomerVipLevel> msPage = returnResponse.getData();
            List<MDCustomerVipLevel> list = msPage.getList();
            page.setList(list);
            page.setCount(msPage.getRowCount());
        } else {
            page.setCount(0);
            page.setList(Lists.newArrayList());
        }
        return page;
    }

    public List<MDCustomerVipLevel> findAllIdAndNameList() {
        List<MDCustomerVipLevel> list = Lists.newArrayList();
        MSResponse<List<MDCustomerVipLevel>> returnResponse = msCustomerVipLevelFeign.findAllIdAndNameList();
        if (MSResponse.isSuccess(returnResponse)) {
            list = returnResponse.getData();
        }
        return list;
    }

    public MDCustomerVipLevel getById(Long id){
        MDCustomerVipLevel mdCustomerVipLevel = new MDCustomerVipLevel();
        MSResponse<MDCustomerVipLevel> msResponse = msCustomerVipLevelFeign.getById(id);
        if (MSResponse.isSuccess(msResponse)) {
            mdCustomerVipLevel = msResponse.getData();
        }
        return mdCustomerVipLevel;
    }

    public void save(MDCustomerVipLevel mdCustomerVipLevel){
        User user = UserUtils.getUser();
        if(mdCustomerVipLevel != null){
            if(mdCustomerVipLevel.getId() != null && mdCustomerVipLevel.getId() > 0){
                mdCustomerVipLevel.setUpdateById(user.getId());
                mdCustomerVipLevel.setUpdateDate(new Date());
                msCustomerVipLevelFeign.update(mdCustomerVipLevel);
            }else {
                mdCustomerVipLevel.setCreateById(user.getId());
                mdCustomerVipLevel.setCreateDate(new Date());
                msCustomerVipLevelFeign.insert(mdCustomerVipLevel);
            }
        }
    }

    public Long getByName(String name){
        MSResponse<Long> msResponse = msCustomerVipLevelFeign.getByName(name);
        return msResponse.getData();
    }

    public Long getByValue(Integer value){
        MSResponse<Long> msResponse = msCustomerVipLevelFeign.getByValue(value);
        return msResponse.getData();
    }

    public void delete(Long id){
        User user = UserUtils.getUser();
        MDCustomerVipLevel mdCustomerVipLevel = new MDCustomerVipLevel();
        mdCustomerVipLevel.setId(id);
        mdCustomerVipLevel.setUpdateById(user.getId());
        mdCustomerVipLevel.setUpdateDate(new Date());
        msCustomerVipLevelFeign.delete(mdCustomerVipLevel);
    }


}

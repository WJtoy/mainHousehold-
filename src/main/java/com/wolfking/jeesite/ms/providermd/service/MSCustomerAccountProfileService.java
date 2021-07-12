package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.md.MDCustomer;
import com.kkl.kklplus.entity.md.MDCustomerAccountProfile;
import com.netflix.discovery.converters.Auto;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerAccountProfile;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerAccountProfileFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MSCustomerAccountProfileService {
    @Autowired
    private MSCustomerAccountProfileFeign msCustomerAccountProfileFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据id获取客户账号信息
     * @param id
     * @return
     * id
     * customerId
     * orderApproveFlag
     * createBy
     * createDate
     * updateBy
     * updateDate
     * remarks
     * delFlag
     */
    public CustomerAccountProfile getById(Long id) {
        /*
        MSResponse<MDCustomerAccountProfile> msResponse = msCustomerAccountProfileFeign.getById(id);
        CustomerAccountProfile customerAccountProfile = null;
        if (MSResponse.isSuccess(msResponse)) {
            log.warn("CustomerAccountProfile微服务getById方法返回:{}",msResponse.getData());
            customerAccountProfile = mapper.map(msResponse.getData(), CustomerAccountProfile.class);
        }
        return customerAccountProfile;
        */

        return MDUtils.getById(id, CustomerAccountProfile.class, msCustomerAccountProfileFeign::getById);
    }

    /**
     * 根据customerId和OrderApproveFlag返回客户账号信息列表
     * @param customerAccountProfile
     * @return
     */
    public List<CustomerAccountProfile> findByCustomerIdAndOrderApproveFlag(CustomerAccountProfile customerAccountProfile) {
        /*
        MDCustomerAccountProfile mdCustomerAccountProfile = mapper.map(customerAccountProfile, MDCustomerAccountProfile.class);
        MSResponse<List<MDCustomerAccountProfile>> msResponse = msCustomerAccountProfileFeign.findByCustomerIdAndOrderApproveFlag(mdCustomerAccountProfile);
        List<CustomerAccountProfile> customerAccountProfileList = Lists.newArrayList();
        if (MSResponse.isSuccess(msResponse)) {
            log.warn("CustomerAccountProfile微服务findByCustomerIdAndOrderApproveFlag方法返回:{}",msResponse.getData());
            customerAccountProfileList = mapper.mapAsList(msResponse.getData(), CustomerAccountProfile.class);
        }
        return customerAccountProfileList;
        */

        return MDUtils.findList(customerAccountProfile, CustomerAccountProfile.class, MDCustomerAccountProfile.class, msCustomerAccountProfileFeign::findByCustomerIdAndOrderApproveFlag);
    }

    /**
     * 保存客户账号信息
     * @param customerAccountProfile
     * @param isNew
     * @return
     */
    public MSErrorCode save(CustomerAccountProfile customerAccountProfile, boolean isNew) {
        MDCustomerAccountProfile mdCustomerAccountProfile = mapper.map(customerAccountProfile,MDCustomerAccountProfile.class);
        // 新增/修改
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdCustomerAccountProfile.setCreateById(user.getId());
            mdCustomerAccountProfile.setUpdateById(user.getId());
        }

        if (!isNew) {
            mdCustomerAccountProfile.preUpdate();
            MSResponse<Integer> msResponse = msCustomerAccountProfileFeign.update(mdCustomerAccountProfile);
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        } else {
            mdCustomerAccountProfile.preInsert();
            MSResponse<Integer> msResponse = msCustomerAccountProfileFeign.insert(mdCustomerAccountProfile);
            
            if (MSResponse.isSuccess(msResponse)) {
                customerAccountProfile.setId(msResponse.getData().longValue());
            }
            return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        }

        //由于在添加数据需要返回id，所有mark如下代码 //2020-1-11
        //return MDUtils.genericSave(customerAccountProfile, MDCustomerAccountProfile.class, isNew, isNew?msCustomerAccountProfileFeign::insert:msCustomerAccountProfileFeign::update);
    }

    /**
     * 删除
     *
     * @param customerAccountProfile
     * @return
     */
    public MSErrorCode delete(CustomerAccountProfile customerAccountProfile) {
        /*
        User user = UserUtils.getUser();
        if (user.getId() != null) {
            mdCustomerAccountProfile.setUpdateById(user.getId());
        }
        mdCustomerAccountProfile.preUpdate();
        MSResponse<Integer> msResponse = msCustomerAccountProfileFeign.delete(mdCustomerAccountProfile);
        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
        */
        return MDUtils.genericSave(customerAccountProfile, MDCustomerAccountProfile.class, false, msCustomerAccountProfileFeign::delete);
    }
}

/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.api.service.sd;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 子账号的工单列表
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AppBaseService extends LongIDBaseService {

    /**
     * 检查工单编号是否有效

    protected boolean checkOrderNo(OrderServicePointSearchModel searchModel) {
        boolean result = true;
        if (searchModel != null) {
            if (StringUtils.isNotBlank(searchModel.getOrderNo()) && searchModel.getOrderNoSearchType() != 1){
                result = false;
            }
        }
        return result;
    } */

    /**
     * 检查用户电话是否有效

    protected boolean checkServicePhone(OrderServicePointSearchModel searchModel) {
        boolean result = true;
        if (searchModel != null) {
            if (StringUtils.isNotBlank(searchModel.getUserPhone()) && searchModel.getIsPhone() != 1) {
                result = false;
            }
        }
        return result;
    }*/

    /**
     * 获取区县乡镇名称

    protected String getAreaAndCountyName(String fullAreaName) {
        String result = "";
        if (StringUtils.isNotBlank(fullAreaName)) {
            String[] addressArr = fullAreaName.split(" ");
            if (addressArr.length > 2) {
                List<String> temp = Lists.newArrayList();
                for (int i = 2; i < addressArr.length; i++) {
                    temp.add(addressArr[i]);
                }
                result = StringUtils.join(temp, "");
            }
        }
        return result;
    }*/

    /**
     * 获取实际上门地址

    protected String getAppServiceAddress(String fullAreaName, String serviceAddress) {
        StringBuilder address = new StringBuilder();
        address.append(getAreaAndCountyName(fullAreaName));
        if (StringUtils.isNotBlank(serviceAddress)) {
            address.append(serviceAddress);
        }
        return address.toString();
    }*/


}

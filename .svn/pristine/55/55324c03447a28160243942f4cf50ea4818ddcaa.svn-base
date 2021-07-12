package com.wolfking.jeesite.modules.sys.service;

import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.sys.dao.UserSubDao;
import com.wolfking.jeesite.modules.sys.entity.UserSub;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserSubService {
    @Resource
    UserSubDao userSubDao;

    /**
     * 通过用户id及type获取客户id列表
     * @param userId
     * @param type
     * @return
     */
    public List<Long> findCustomerIdListByUserId(Long userId, Integer type) {
        List<Long> customerIdList = Lists.newArrayList();

        int pageNo = 1;
        Page<UserSub> userSubPage = new Page<UserSub>();
        userSubPage.setPageNo(pageNo);
        userSubPage.setPageSize(500);
        customerIdList.addAll(userSubDao.findCustomerIdListByUserId(userId, type, userSubPage));
        while(pageNo < userSubPage.getPageCount()) {
            pageNo++;
            userSubPage.setPageNo(pageNo);
            customerIdList.addAll(userSubDao.findCustomerIdListByUserId(userId, type, userSubPage));
        }
        return customerIdList;
    }
}

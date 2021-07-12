package com.wolfking.jeesite.modules.customer.md.service;

import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.b2bcenter.md.feign.B2BCustomerMappingFeign;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CtCustomerShopService {
    @Autowired
    private B2BCustomerMappingFeign customerMappingFeign;

    /**
     * 分页获取->基础资料
     * @param page
     * @param b2BCustomerMapping
     * @return
     */
    public Page<B2BCustomerMapping> findList(Page<B2BCustomerMapping> page, B2BCustomerMapping b2BCustomerMapping) {
        if (b2BCustomerMapping.getPage() == null) {
            MSPage msPage = PageMapper.INSTANCE.toMSPage(page);
        }
        Page<B2BCustomerMapping> b2BCustomerMappingPage = new Page<>();
        b2BCustomerMappingPage.setPageSize(page.getPageSize());
        b2BCustomerMappingPage.setPageNo(page.getPageNo());
        b2BCustomerMapping.setPage(new MSPage<>(b2BCustomerMappingPage.getPageNo(), b2BCustomerMappingPage.getPageSize()));
        MSResponse<MSPage<B2BCustomerMapping>> returnCustomerMapping = customerMappingFeign.findList(b2BCustomerMapping);
        if (MSResponse.isSuccess(returnCustomerMapping)) {
            MSPage<B2BCustomerMapping> data = returnCustomerMapping.getData();
            b2BCustomerMappingPage.setList(data.getList());
            b2BCustomerMappingPage.setCount(data.getRowCount());
        }
        return b2BCustomerMappingPage;
    }
}

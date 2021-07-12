package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerTimeliness;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerTimeliness;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerTimelinessFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class MSCustomerTimelinessService {
    @Autowired
    private MSCustomerTimelinessFeign msCustomerTimelinessFeign;
    @Autowired
    private MapperFacade mapper;

    /**
     * 根据customerId，areaId获取时效列表
     * @param customerTimeliness
     * @return
     */
    public List<CustomerTimeliness> findListByCustomerId(CustomerTimeliness customerTimeliness) {
        return MDUtils.findListByCustomCondition(customerTimeliness.getCustomer().getId(), CustomerTimeliness.class, msCustomerTimelinessFeign::findListByCustomerId);
    }

    /**
     * 时效列表
     * @param page
     * @param customerTimeliness
     * @return
     */
    public Page<CustomerTimeliness> findList(Page<CustomerTimeliness> page, CustomerTimeliness customerTimeliness) {
        MDCustomerTimeliness mdCustomerTimeliness = mapper.map(customerTimeliness, MDCustomerTimeliness.class);
        if (mdCustomerTimeliness.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(page);
        }
        Page<MDCustomerTimeliness> mdCustomerTimelinessPage = new Page<>();
        mdCustomerTimelinessPage.setPageSize(page.getPageSize());
        mdCustomerTimelinessPage.setPageNo(page.getPageNo());
        mdCustomerTimeliness.setPage(new MSPage<>(mdCustomerTimelinessPage.getPageNo(), mdCustomerTimelinessPage.getPageSize()));
        MSResponse<MSPage<Long>> returnResponse = msCustomerTimelinessFeign.findList(mdCustomerTimeliness);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<Long>  msPage = returnResponse.getData();
            List<CustomerTimeliness> customerTimelinessList = Lists.newArrayList();
            msPage.getList().forEach(r->{
                CustomerTimeliness customerTimelinessEntity = new CustomerTimeliness();
                customerTimelinessEntity.setCustomer(new Customer(r));
                customerTimelinessList.add(customerTimelinessEntity);
            });
            page.setList(customerTimelinessList);
            page.setCount(msPage.getRowCount());
            log.warn("findList返回的数据:{}", msPage.getList());
        } else {
            page.setCount(0);
            page.setList(new ArrayList<>());
            log.warn("findList返回无数据返回,参数customerTimeliness:{}", customerTimeliness);
        }
        return page;
    }


    /**
     * 添加客户时效
     * @param customerTimelinessList
     * @return
     */
    public MSErrorCode  batchInsert(List<CustomerTimeliness> customerTimelinessList) {
        List<MDCustomerTimeliness> mdCustomerTimelinessList = Lists.newArrayList();

        User user = UserUtils.getUser();
        Long userId = null;
        if (user.getId() != null) {
            userId = user.getId();
        }
        final Long finalUserId = userId;
        customerTimelinessList.stream().forEach(customerTimeliness -> {
            MDCustomerTimeliness mdCustomerTimeliness = mapper.map(customerTimeliness,MDCustomerTimeliness.class);
            mdCustomerTimeliness.setCreateById(finalUserId);
            mdCustomerTimeliness.setUpdateById(finalUserId);
            mdCustomerTimeliness.preInsert();
            mdCustomerTimelinessList.add(mdCustomerTimeliness);
        });

        // 新增
        long lStart = System.currentTimeMillis();
        MSResponse<Integer> msResponse = msCustomerTimelinessFeign.batchInsert(mdCustomerTimelinessList);
        long lEnd = System.currentTimeMillis();
        log.warn("msCustomerTimelinessFeign.batchInsert {},耗时:{} 毫秒。", mdCustomerTimelinessList, (lEnd-lStart));

        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
    }

    public MSErrorCode delete(CustomerTimeliness customerTimeliness) {
        return MDUtils.genericSave(customerTimeliness, MDCustomerTimeliness.class, false, msCustomerTimelinessFeign::delete);
    }
}

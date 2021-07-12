package com.wolfking.jeesite.ms.providermd.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.md.MDCustomerUrgent;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.mapper.common.PageMapper;
import com.wolfking.jeesite.ms.providermd.feign.MSCustomerUrgentFeign;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MSCustomerUrgentService {
    @Autowired
    private MSCustomerUrgentFeign msCustomerUrgentFeign;

    @Autowired
    private MapperFacade mapper;

    /**
     * 根据customerId，areaId获取加急列表
     * @param urgentCustomer
     * @return
     */
    public List<UrgentCustomer> findListByCustomerId(UrgentCustomer urgentCustomer) {
        return MDUtils.findListByCustomCondition(urgentCustomer.getCustomer().getId(), UrgentCustomer.class, msCustomerUrgentFeign::findListByCustomerId);
    }

    /**
     * 加急列表
     * @param page
     * @param urgentCustomer
     * @return
     */
    public Page<UrgentCustomer> findList(Page<UrgentCustomer> page, UrgentCustomer urgentCustomer) {
        MDCustomerUrgent mdCustomerUrgent = mapper.map(urgentCustomer, MDCustomerUrgent.class);
        if (mdCustomerUrgent.getPage() == null) {
            PageMapper.INSTANCE.toMSPage(page);
        }
        Page<MDCustomerUrgent> mdCustomerUrgentPage = new Page<>();
        mdCustomerUrgentPage.setPageSize(page.getPageSize());
        mdCustomerUrgentPage.setPageNo(page.getPageNo());
        mdCustomerUrgent.setPage(new MSPage<>(mdCustomerUrgentPage.getPageNo(), mdCustomerUrgentPage.getPageSize()));
        MSResponse<MSPage<Long>> returnResponse = msCustomerUrgentFeign.findList(mdCustomerUrgent);
        if (MSResponse.isSuccess(returnResponse)) {
            MSPage<Long>  msPage = returnResponse.getData();
            List<UrgentCustomer> urgentCustomerList = Lists.newArrayList();
            msPage.getList().forEach(r->{
               UrgentCustomer urgentCustomerEntity = new UrgentCustomer();
               urgentCustomerEntity.setCustomer(new Customer(r));

               urgentCustomerList.add(urgentCustomerEntity);
            });
            page.setList(urgentCustomerList);
            page.setCount(msPage.getRowCount());
            log.warn("findList返回的数据:{}", msPage.getList());
        } else {
            page.setCount(0);
            page.setList(new ArrayList<>());
            log.warn("findList返回无数据返回,参数urgentCustomer:{}", urgentCustomer);
        }
        return page;
    }

    /**
     * 添加客户时效
     * @param urgentCustomerList
     * @return
     */
    public MSErrorCode  batchInsert(List<UrgentCustomer> urgentCustomerList) {
        List<MDCustomerUrgent> mdCustomerUrgentList = Lists.newArrayList();

        User user = UserUtils.getUser();
        Long userId = null;
        if (user.getId() != null) {
            userId = user.getId();
        }
        final Long finalUserId = userId;
        urgentCustomerList.stream().forEach(urgentCustomer -> {
            MDCustomerUrgent mdCustomerUrgent = mapper.map(urgentCustomer,MDCustomerUrgent.class);
            mdCustomerUrgent.setCreateById(finalUserId);
            mdCustomerUrgent.setUpdateById(finalUserId);

            mdCustomerUrgent.preInsert();
            mdCustomerUrgentList.add(mdCustomerUrgent);
        });

        // 新增
        long lStart = System.currentTimeMillis();
        MSResponse<Integer> msResponse = msCustomerUrgentFeign.batchInsert(mdCustomerUrgentList);
        long lEnd = System.currentTimeMillis();
        log.warn("msCustomerUrgentFeign.batchInsert {},耗时:{} 毫秒。", mdCustomerUrgentList, (lEnd-lStart));

        return new MSErrorCode(msResponse.getCode(), msResponse.getMsg());
    }


    public MSErrorCode delete(UrgentCustomer urgengCustomer) {
        return MDUtils.genericSave(urgengCustomer, MDCustomerUrgent.class, false, msCustomerUrgentFeign::delete);
    }
}

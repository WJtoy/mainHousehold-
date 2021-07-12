package com.wolfking.jeesite.ms.cc.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.vm.ReminderListModel;
import com.kkl.kklplus.entity.cc.vm.ReminderPageSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.feign.CCCustomerReminderFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



/**
 * 催单服务层
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerReminderService {

    @Autowired
    private CCCustomerReminderFeign feign;

   @Autowired
   private KefuReminderService kefuReminderService;

    //region 查询

    /**
     * 待回复列表
     */
    public Page<ReminderModel> waitReplyList(Page<ReminderModel> page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.waitReplyList(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = kefuReminderService.toList(data);
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * 分页查询所有催单
     */
    public Page<ReminderModel> getPage(Page<ReminderModel> page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.getReminderPage(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = kefuReminderService.toList(data);
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    /**
     * 查询已回复列表
     */
    public Page<ReminderModel> haveRepliedList(Page<ReminderModel> page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.haveRepliedList(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = kefuReminderService.toList(data);
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return page;
    }

    //endregion

}

package com.wolfking.jeesite.ms.cc.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.cc.ReminderStatus;
import com.kkl.kklplus.entity.cc.vm.*;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderModelMapper;
import com.wolfking.jeesite.ms.cc.feign.CCKefuReminderFeign;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 催单服务层
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class KefuReminderService {

    @Autowired
    private CCKefuReminderFeign feign;

    @Autowired
    private ProductCategoryService productCategoryService;

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
                List<ReminderModel> list = toList(data);
                Date date = new Date();
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long startDt = twoTuple.getAElement();
                long endDt = twoTuple.getBElement();
                for(ReminderModel item:list){
                    double praiseTimeliness = DateUtils.calculateTimeliness(date,item.getTimeoutAt(),startDt,endDt);
                    item.setProcessTimeLiness(praiseTimeliness);
                    item.setCutOffLabel(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,60));
                }
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
                List<ReminderModel> list = toList(data);
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long startDt = twoTuple.getAElement();
                long endDt = twoTuple.getBElement();
                Date date = new Date();
                for(ReminderModel model:list){
                    if(model.getStatus()== ReminderStatus.WaitReply.getCode()){
                        double praiseTimeliness = DateUtils.calculateTimeliness(date,model.getTimeoutAt(),startDt,endDt);
                        model.setProcessTimeLiness(praiseTimeliness);
                        model.setCutOffLabel(PraiseUtils.getCutOffTimelinessLabel(praiseTimeliness,60));
                    }else if(model.getStatus() == ReminderStatus.Replied.getCode()){
                        int minutes = (int) (60*model.getProcessTimeLiness());
                        model.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                        if(model.getOrderCompleteTimeoutAt()>0){
                            double completeAtTimeliness = DateUtils.calculateTimeliness(date,model.getOrderCompleteTimeoutAt(),startDt,endDt);
                            model.setOrderCompleteTimeliness(completeAtTimeliness);
                            model.setCompleteTimelinessLabel(PraiseUtils.getCutOffTimelinessLabel(completeAtTimeliness,60));
                        }
                    }else if(model.getStatus() == ReminderStatus.Confirmed.getCode()){
                        int minutes = (int) (60*model.getProcessTimeLiness());
                        model.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                        int handleTimeliness = (int) (60*model.getHandleTimeLiness());
                        model.setHandleTimeLinessLabel(DateUtils.minuteToTimeString(handleTimeliness,"小时","分钟"));
                        if(model.getOrderCompleteTimeoutAt()>0){
                            double completeAtTimeliness = DateUtils.calculateTimeliness(date,model.getOrderCompleteTimeoutAt(),startDt,endDt);
                            model.setOrderCompleteTimeliness(completeAtTimeliness);
                            model.setCompleteTimelinessLabel(PraiseUtils.getCutOffTimelinessLabel(completeAtTimeliness,60));
                        }
                    }else if(model.getStatus() == ReminderStatus.Completed.getCode()){
                        int minutes = (int) (60*model.getProcessTimeLiness());
                        model.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                        int handleTimeliness = (int) (60*model.getHandleTimeLiness());
                        model.setHandleTimeLinessLabel(DateUtils.minuteToTimeString(handleTimeliness,"小时","分钟"));
                        int completeTimeliness = (int) (60*model.getOrderTimeLiness());
                        model.setCompleteTimelinessLabel(DateUtils.minuteToTimeString(completeTimeliness,"小时","分钟"));
                    }
                }
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
                List<ReminderModel> list = toList(data);
                Date date = new Date();
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long startDt = twoTuple.getAElement();
                long endDt = twoTuple.getBElement();
                for(ReminderModel item:list){
                    int minutes = (int) (60*item.getProcessTimeLiness());
                    item.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                    if(item.getStatus()==ReminderStatus.Completed.getCode()){
                        int completeTimeliness = (int) (60*item.getOrderTimeLiness());
                        item.setOrderCompleteTimeliness(item.getOrderTimeLiness());
                        item.setCompleteTimelinessLabel("用时："+DateUtils.minuteToTimeString(completeTimeliness,"小时","分钟"));
                    }else{
                        if(item.getOrderCompleteTimeoutAt()>0){
                            double completeAtTimeliness = DateUtils.calculateTimeliness(date,item.getOrderCompleteTimeoutAt(),startDt,endDt);
                            item.setOrderCompleteTimeliness(completeAtTimeliness);
                            item.setCompleteTimelinessLabel(PraiseUtils.getCutOffTimelinessLabel(completeAtTimeliness,60));
                        }
                    }
                }
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
     * 超过24小时未完成(工单未完成)催单
     */
    public Page<ReminderModel> moreThan24HoursUnfinishedList(Page<ReminderModel> page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.moreThan24HoursUnfinishedList(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = toList(data);
                Date date = new Date();
                for(ReminderModel item:list){
                    int minutes = (int) (60*item.getProcessTimeLiness());
                    item.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                    double completeTimeliness = DateUtils.dateDiff(date.getTime(),item.getCreateAt());
                    completeTimeliness = completeTimeliness - 24;
                    int completeTimelinessMinutes = (int) (60*completeTimeliness);
                    item.setCompleteTimelinessLabel(DateUtils.minuteToTimeString(completeTimelinessMinutes,"小时","分钟"));
                }
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
     * 超过24小时未完成(工单未完成)催单
     */
    public Page<ReminderModel> moreThan48HoursUnfinishedList(Page<ReminderModel> page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.moreThan48HoursUnfinishedList(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = toList(data);
                Date date = new Date();
                for(ReminderModel item:list){
                    int minutes = (int) (60*item.getProcessTimeLiness());
                    item.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                    double completeTimeliness = DateUtils.dateDiff(date.getTime(),item.getCreateAt());
                    completeTimeliness = completeTimeliness -24;
                    int completeTimelinessMinutes = (int) (60*completeTimeliness);
                    item.setCompleteTimelinessLabel(DateUtils.minuteToTimeString(completeTimelinessMinutes,"小时","分钟"));
                }
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


    public List<ReminderModel> toList(MSPage<ReminderListModel> data){
        if(CollectionUtils.isEmpty(data.getList())){
            return Lists.newArrayList();
        }
        int size = data.getList().size();
        Map<Long, ServicePoint> spMap = Maps.newHashMapWithExpectedSize(size);
        Map<Long, Customer> customerMap = Maps.newHashMapWithExpectedSize(size);
        Map<Integer, String> dataSourceMap = Maps.newHashMapWithExpectedSize(size);
        Map<Long, Area> areaMap = Maps.newHashMapWithExpectedSize(size*2);
        List<ReminderModel> list = Lists.newArrayListWithCapacity(size);
        ReminderModelMapper mapper = Mappers.getMapper(ReminderModelMapper.class);
        ReminderModel model;
        List<ProductCategory> allCategories = productCategoryService.findAllList();
        Map<Long,String> categoryMap = Maps.newHashMap();
        if(allCategories!=null && allCategories.size()>0){
            categoryMap = allCategories
                    .stream()
                    .collect(Collectors.toMap(ProductCategory::getId, s -> s.getName()));
        }
        for(ReminderListModel form:data.getList()){
            model = mapper.toViewModel(form,spMap,customerMap,dataSourceMap,areaMap);
            model.setItemId(form.getItemId());
            if(categoryMap.containsKey(form.getProductCategoryId())){
                model.setProductCategoryName(categoryMap.get(form.getProductCategoryId()));
            }
            list.add(model);
        }
        //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList(),spMap,customerMap,userMap,categoryMap);
        spMap = null;
        customerMap = null;
        dataSourceMap = null;
        areaMap = null;
        categoryMap = null;
        return list;
    }

    //endregion

}

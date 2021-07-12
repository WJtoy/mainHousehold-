package com.wolfking.jeesite.ms.cc.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderReminderProcessMessage;
import com.kkl.kklplus.entity.cc.*;
import com.kkl.kklplus.entity.cc.vm.*;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.utils.PraiseUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.service.B2BOrderReminderService;
import com.wolfking.jeesite.ms.cc.entity.ReminderAutoCloseTypeEnum;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import com.wolfking.jeesite.ms.cc.entity.mapper.ReminderModelMapper;
import com.wolfking.jeesite.ms.cc.feign.CCReminderFeign;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 催单服务层
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ReminderService {

    @Autowired
    private CCReminderFeign feign;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private B2BOrderReminderService b2BOrderReminderService;

    //region 查询

    /**
     * 待回复列表
     */
    public Page<ReminderListModel> waitReplyList(Page page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.waitReplyList(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = toList(data);
                //List list = Mappers.getMapper(ReminderModelMapper.class).toViewModels(data.getList());
                loadProductCategory(list);
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
                //List<ReminderModel> list = Mappers.getMapper(ReminderModelMapper.class).toViewModels(data.getList());
                loadProductCategory(list);
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long startDt = twoTuple.getAElement();
                long endDt = twoTuple.getBElement();
                Date date = new Date();
                for(ReminderModel model:list){
                    if(model.getStatus()==ReminderStatus.WaitReply.getCode()){
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
                        if(model.getHandleTimeLiness()>0){
                            int handleTimeliness = (int) (60*model.getHandleTimeLiness());
                            model.setHandleTimeLinessLabel(DateUtils.minuteToTimeString(handleTimeliness,"小时","分钟"));
                        }
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
                //List<ReminderModel> list = Mappers.getMapper(ReminderModelMapper.class).toViewModels(data.getList());
                loadProductCategory(list);
                TwoTuple<Long,Long> twoTuple = DateUtils.getTwoTupleDate(9,18);
                long startDt = twoTuple.getAElement();
                long endDt = twoTuple.getBElement();
                Date date = new Date();
                for(ReminderModel model:list){
                    int minutes = (int) (60*model.getProcessTimeLiness());
                    model.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                    if(model.getStatus()==ReminderStatus.Completed.getCode()){
                        int completeTimeliness = (int) (60*model.getOrderTimeLiness());
                        model.setCompleteTimelinessLabel("用时："+DateUtils.minuteToTimeString(completeTimeliness,"小时","分钟"));
                    }else{
                        if(model.getOrderCompleteTimeoutAt()>0){
                            double completeAtTimeliness = DateUtils.calculateTimeliness(date,model.getOrderCompleteTimeoutAt(),startDt,endDt);
                            model.setOrderCompleteTimeliness(completeAtTimeliness);
                            model.setCompleteTimelinessLabel(PraiseUtils.getCutOffTimelinessLabel(completeAtTimeliness,60));
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
            //page.setMessage(MSErrorCode.FALLBACK_FAILURE.msg);
        }
        return page;
    }

    /**
     * 查询已处理列表
     */
    public Page<ReminderModel> haveConfirmedList(Page<ReminderModel> page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.processedList(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = toList(data);
                //List<ReminderModel> list = Mappers.getMapper(ReminderModelMapper.class).toViewModels(data.getList());
                loadProductCategory(list);
                for(ReminderModel model:list){
                    int minutes = (int) (60*model.getProcessTimeLiness());
                    model.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                    if(model.getHandleTimeLiness()>0){
                        int handleTimeliness = (int) (60*model.getHandleTimeLiness());
                        model.setHandleTimeLinessLabel(DateUtils.minuteToTimeString(handleTimeliness,"小时","分钟"));
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
     * 查询工单完成的催单列表
     */
    public Page<ReminderModel> finishList(Page<ReminderModel> page, ReminderPageSearchModel searchModel){
        searchModel.setPage(new MSPage<>(page.getPageNo(), page.getPageSize()));
        try {
            MSResponse<MSPage<ReminderListModel>> msResponse = feign.finishList(searchModel);
            if (MSResponse.isSuccess(msResponse)) {
                MSPage<ReminderListModel> data = msResponse.getData();
                page.setCount(data.getRowCount());
                List<ReminderModel> list = toList(data);
                //List<ReminderModel> list = Mappers.getMapper(ReminderModelMapper.class).toViewModels(data.getList());
                loadProductCategory(list);
                for(ReminderModel model:list){
                    int minutes = (int) (60*model.getProcessTimeLiness());
                    model.setCutOffLabel(DateUtils.minuteToTimeString(minutes,"小时","分钟"));
                    if(model.getHandleTimeLiness()>0){
                        int handleTimeliness = (int) (60*model.getHandleTimeLiness());
                        model.setHandleTimeLinessLabel(DateUtils.minuteToTimeString(handleTimeliness,"小时","分钟"));
                    }
                    int completeTimeliness = (int) (60*model.getOrderTimeLiness());
                    model.setCompleteTimelinessLabel(DateUtils.minuteToTimeString(completeTimeliness,"小时","分钟"));
                }
                page.setList(list);
            } else {
                page.setCount(0);
                page.setList(Lists.newArrayList());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
            //page.setMessage(MSErrorCode.FALLBACK_FAILURE.msg);
        }
        return page;
    }

    /**
     * 读取产品列表名称
     * @param list
     */
    private void loadProductCategory(List<ReminderModel> list){
        if(ObjectUtils.isEmpty(list)){
            return;
        }
        List<ProductCategory> allCategories = null;
        try {
            allCategories = productCategoryService.findAllList();
            if (ObjectUtils.isEmpty(allCategories)) {
                return;
            }
        }catch (Exception e){
            log.error("读取服务列表失败",e);
            return;
        }
        Map<Long,String> categoryMap = allCategories
                .stream()
                .collect(Collectors.toMap(ProductCategory::getId,s -> s.getName()));
        allCategories = null;
        ReminderModel item;
        for(int i=0,size=list.size();i<size;i++){
            item = list.get(i);
            if(categoryMap.containsKey(item.getProductCategoryId())){
                item.setProductCategoryName(categoryMap.get(item.getProductCategoryId()));
            }
        }
        categoryMap = null;
    }

    //endregion

    //region 催单

    public List<Reminder> getListByOrderId(Long orderId,String quarter,int pageSize,String orderBy){
        ReminderSearchModel searchModel = ReminderSearchModel.builder()
                .orderId(orderId)
                .quarter(quarter)
                .build();
        MSPage<ReminderSearchModel> page = new MSPage<>(1, pageSize);
        page.setOrderBy(StringUtils.isBlank(orderBy)?"create_date desc":orderBy);
        searchModel.setPage(page);
        try {
            MSResponse<MSPage<Reminder>> msResponse = feign.getListByOrderId(searchModel);
            if (MSResponse.isSuccessCode(msResponse)) {
                MSPage<Reminder> data = msResponse.getData();
                if(data != null) {
                    return data.getList();
                }else{
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 根据Id+quarter读取催单信息
     * @param itemFlag 是否读取催单项目列表 1:是
     */
    public Reminder getReminderById(Long id,String quarter,Integer itemFlag){
        MSResponse<Reminder> mSResponse = feign.getReminderById(quarter,id,itemFlag);
        if(MSResponse.isSuccess(mSResponse)){
            return mSResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 根据订单Id+quarter读取待回复催单信息
     */
    public Reminder getWaitReplyReminder(Long orderId,String quarter){
        MSResponse<Reminder> mSResponse = feign.getWaitReplyReminder(quarter,orderId);
        if(MSResponse.isSuccess(mSResponse)){
            return mSResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 根据订单Id+quarter读取最后一次催单信息
     */
    public Reminder getLastReminderByOrderId(Long orderId,String quarter){
        MSResponse<Reminder> mSResponse = feign.getLast(quarter,orderId);
        if(MSResponse.isSuccess(mSResponse)){
            return mSResponse.getData();
        }else{
            return null;
        }
    }

    /**
     * 批量检查订单是否可以再次催单
     * reminder.delFlag: 0-可以催单 1-不能催单，催单未关闭 2-不能催单，时间不满足或达到催单次数上限
     * reminder.remkars: 提示内容
     */
    public Map<Long,Reminder> bulkRereminderCheck(BulkRereminderCheckModel searchModel){
        MSResponse<Map<Long,Reminder>> msResponse = feign.bulkRereminderCheck(searchModel);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 检查订单是否可以再次催单
     * reminder.delFlag: 0-可以催单 1-不能催单，催单未关闭 2-不能催单，时间不满足或达到催单次数上限
     * reminder.remkars: 提示内容
     */
    public RereminderCheckRespone reReminderCheck(Long orderId,String quarter,Long createAt){
        MSResponse<RereminderCheckRespone> msResponse = feign.reReminderCheck(quarter,orderId,createAt);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }
        return RereminderCheckRespone.builder().code(2).msg(msResponse.getMsg()).build();
    }


    /**
     * 按订单id批量读取订单催单时效信息
     * processTimeLiness:时效 ，创建或再次催单距离现在的时效
     * createDt: 创建日期时间戳
     */
    public Map<Long, ReminderTimeLinessModel> bulkGetReminderTimeLinessByOrders(BulkRereminderCheckModel searchModel){
        MSResponse<Map<Long,ReminderTimeLinessModel>> msResponse = feign.bulkGetReminderTimeLinessByOrders(searchModel);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }
        return null;
    }

    /**
     * 按订单id批量读取订单催单时效信息
     * processTimeLiness:时效 ，创建或再次催单距离现在的时效
     * createDt: 创建日期时间戳
     */
    public Map<Long, ReminderTimeLinessModel> findReminderTimelinessByOrderIds(BulkRereminderCheckModel searchModel){
        MSResponse<Map<Long,ReminderTimeLinessModel>> msResponse = feign.findReminderTimelinessByOrderIds(searchModel);
        if(MSResponse.isSuccess(msResponse)){
            return msResponse.getData();
        }
        return Maps.newHashMap();
    }


    /**
     * 新建催单
     */
    @Transactional()
    public Long newReminder(Reminder reminder){
        long id = sequenceIdService.nextId();//2020/05/24
        reminder.setId(id);
        reminder.setItemId(sequenceIdService.nextId());
        //reminder.setReminderType(ReminderType.Manual.getCode());
        ReminderLog log = ReminderLog.builder()
                .quarter(reminder.getQuarter())
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left("新增催单,催单意见：" + reminder.getReminderRemark(), 250))
                .createName(StringUtils.left(reminder.getCreateBy().toString(),30))
                .build();
        log.setId(sequenceIdService.nextId());//2020/05/24
        log.setCreateDt(reminder.getCreateDt());
        reminder.setReminderLog(log);
        MSResponse<Reminder> msResponse = feign.newReminder(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }else {
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
            /* reminderFlag -> orderStatus.reminderStatus 2019-08-13
            map.put("orderId", reminder.getOrderId());
            map.put("quarter", reminder.getQuarter());
            map.put("reminderFlag", reminder.getStatus());
            orderService.updateOrderCondition(map);
             */
            //map.clear();
            map.put("orderId", reminder.getOrderId());
            map.put("quarter", reminder.getQuarter());
            map.put("reminderCreateBy", reminder.getCreateById());
            map.put("reminderCreateAt", reminder.getCreateDt());
            map.put("reminderStatus", reminder.getStatus());
            map.put("servicePointId",reminder.getServicepointId());
            orderService.updateReminderInfo(map);
        }
        return reminder.getItemId();
    }

    /**
     * 新建催单项
     */
    @Transactional()
    public Long newReminderItem(Reminder reminder){
        Long reminderItemId=0L;
        ReminderLog log = ReminderLog.builder()
                .quarter(reminder.getQuarter())
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left("新增催单,催单意见：" + reminder.getReminderRemark(), 250))
                .createName(StringUtils.left(reminder.getCreateBy().toString(),30))
                .build();
        log.setId(sequenceIdService.nextId());//2020/05/24
        log.setCreateDt(reminder.getCreateDt());
        reminder.setReminderLog(log);
        MSResponse<Long> msResponse = feign.insertReminderItem(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }else {
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
            map.put("orderId", reminder.getOrderId());
            map.put("quarter", reminder.getQuarter());
            map.put("reminderCreateBy", reminder.getCreateById());
            map.put("reminderCreateAt", reminder.getCreateDt());
            map.put("reminderStatus", reminder.getStatus());
            map.put("servicePointId",reminder.getServicepointId());
            orderService.updateReminderInfo(map);
            reminderItemId = msResponse.getData();
        }
        return reminderItemId;
    }

    /**
     * 回复催单
     */
    @Transactional()
    public void replyReminder(Long id,Long orderId,String quarter,String remark,User user,Long servicePointId,Long itemId){
        long time = System.currentTimeMillis();
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setOrderId(orderId);
        reminder.setQuarter(quarter);
        reminder.setProcessAt(time);
        reminder.setProcessRemark(remark);
        reminder.setProcessBy(user.getName());
        reminder.setOperatorType(getReminderCreatorType(user).getCode());
        reminder.setStatus(ReminderStatus.Replied.getCode());
        reminder.setPreStatus(ReminderStatus.WaitReply.getCode());
        reminder.setUpdateById(user.getId());
        reminder.setUpdateDt(time);
        reminder.setItemId(itemId);
        ReminderLog reminderLog = ReminderLog.builder()
                .quarter(quarter)
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left("回复催单,回复内容：" + remark, 250))
                .createName(user.getName())
                .creatorType(reminder.getOperatorType())
                .build();
        reminderLog.setCreateDt(reminder.getProcessAt());
        reminderLog.setId(sequenceIdService.nextId());//2020/05/24
        reminder.setReminderLog(reminderLog);

        MSResponse<Integer> msResponse = feign.replyReminder(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }else {
            /*Integer status = getStatusById(id,quarter);
            if(status!=null && status>0 && status!=reminder.getStatus()){
                reminder.setStatus(status);
            }*/
            Integer status = msResponse.getData();
            if(status!=null && status>0){
                reminder.setStatus(status);
            }
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
            map.put("orderId", orderId);
            map.put("quarter", quarter);
            map.put("reminderStatus", reminder.getStatus());
            map.put("reminderCreateBy", reminder.getUpdateById());
            map.put("reminderCreateAt", reminder.getUpdateDt());
            map.put("servicePointId",servicePointId);
            orderService.updateReminderInfo(map);
            ReminderItem reminderItem = null;
            try {
                MSResponse<ReminderItem> msItemResponse = feign.getReminderItemById(itemId,quarter);
                if(MSResponse.isSuccess(msItemResponse)){
                    reminderItem = msItemResponse.getData();
                    if(isSendProcessToB2b(reminderItem)){
                        MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage message = MQB2BOrderReminderProcessMessage.B2BOrderReminderProcessMessage.newBuilder()
                                .setDataSource(reminderItem.getDataSource())
                                .setKklReminderId(reminderItem.getId())
                                .setOperationType(20)
                                .setContent(remark)
                                .setB2BReminderNo(reminderItem.getB2bReminderNo()==null?"":reminderItem.getB2bReminderNo())
                                .setB2BReminderId(reminderItem.getB2bReminderId())
                                .setCreateDate(time)
                                .setOperatorId(user.getId())
                                .build();
                        b2BOrderReminderService.sendReminderProcess(message);
                    }
                }
            }catch (Exception e){
                log.error("ReminderService.replyReminder:{}", reminderItem,e.getMessage());
                //LogUtils.saveLog("回复催单发送给B2B失败","ReminderService.replyReminder","",e, null);
            }
        }
    }

    /**
     * 确认催单
     * 客户或跟单处理
     */
    @Transactional()
    public void confirmReminder(Long id,Long orderId,String quarter,String remark,User user,Long servicePointId,Long itemId){
        long time = System.currentTimeMillis();
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setOrderId(orderId);
        reminder.setQuarter(quarter);
        reminder.setUpdateById(user.getId());
        reminder.setUpdateDt(time);
        reminder.setOperatorType(getReminderCreatorType(user).getCode());
        reminder.setStatus(ReminderStatus.Confirmed.getCode());
        reminder.setPreStatus(ReminderStatus.Replied.getCode());
        reminder.setItemId(itemId);

        ReminderLog log = ReminderLog.builder()
                .quarter(reminder.getQuarter())
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left("确认催单,确认意见：" + remark, 250))
                .createName(user.getName())
                .creatorType(reminder.getOperatorType())
                .build();
        log.setCreateDt(reminder.getProcessAt());
        log.setId(sequenceIdService.nextId());//2020/05/24
        reminder.setReminderLog(log);

        MSResponse<Integer> msResponse = feign.confirmProcessed(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }else {
            /*Integer status = getStatusById(id,quarter);
            if(status!=null && status>0 && status!=reminder.getStatus()){
                reminder.setStatus(status);
            }*/
            Integer status = msResponse.getData();
            if(status!=null && status>0){
                reminder.setStatus(status);
            }
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
            map.put("orderId", reminder.getOrderId());
            map.put("quarter", reminder.getQuarter());
            map.put("reminderStatus", reminder.getStatus());
            map.put("reminderCreateBy", reminder.getUpdateById());
            map.put("reminderCreateAt", reminder.getUpdateDt());
            map.put("servicePointId",servicePointId);
            orderService.updateReminderInfo(map);
        }
    }

    /**
     * 再次催单
     * 客户或跟单驳回回复的意见
     */
    @Transactional()
    public void rejectReminder(Long id,Long orderId,String quarter,String remark,User user,int preStatus,Long servicePointId,NameValuePair reminderReason){
        long time = System.currentTimeMillis();
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setOrderId(orderId);
        reminder.setQuarter(quarter);
        reminder.setProcessAt(time);
        reminder.setProcessBy(user.getName());
        reminder.setProcessRemark(remark);
        reminder.setUpdateById(user.getId());
        reminder.setUpdateDt(time);
        reminder.setOperatorType(getReminderCreatorType(user).getCode());
        reminder.setStatus(ReminderStatus.WaitReply.getCode());
        reminder.setPreStatus(preStatus);
        reminder.setReminderType(ReminderType.Manual.code);
        reminder.setReminderReason(reminderReason);
        //reminder.setPreStatus(ReminderStatus.Replied.getCode());

        ReminderLog log = ReminderLog.builder()
                .quarter(reminder.getQuarter())
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left("再次催单,催单意见：" + remark, 250))
                .createName(reminder.getProcessBy())
                .creatorType(reminder.getOperatorType())
                .build();
        log.setCreateDt(time);
        log.setId(sequenceIdService.nextId());//2020/05/24
        reminder.setReminderLog(log);

        MSResponse<Integer> msResponse = feign.rejectProcessed(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }else {
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
            map.put("orderId", reminder.getOrderId());
            map.put("quarter", reminder.getQuarter());
            map.put("reminderStatus", reminder.getStatus());
            map.put("reminderCreateBy", reminder.getUpdateById());
            map.put("reminderCreateAt", reminder.getUpdateDt());
            map.put("servicePointId",servicePointId);
            orderService.updateReminderInfo(map);
        }
    }

    /**
     * 自动关闭催单
     * 工单取消/退单审核/客评时自动关闭催单
     * @param orderId   订单id
     * @param quarter   分片
     * @param user 完成人
     * @param completeDate 完成时间
     * @param remarks 备注
     * @param closeType 自动关闭类型
     */
    @Transactional()
    public void completeReminder(long orderId, String quarter,User user, Date completeDate,String remarks,
                                 ReminderAutoCloseTypeEnum closeType,Integer orderStatus,Long servicePointId){
        if ( orderId <= 0 || StringUtils.isBlank(quarter)
                 || user == null || user.getId() == null || user.getId()<=0 || StringUtils.isBlank(user.getName())
                 || completeDate == null || closeType == null){
            throw new RuntimeException("完成催单失败，参数错误!");
        }
        StringBuilder sRemarks = new StringBuilder(300);
        sRemarks.append("完成催单：")
                .append(closeType.getMsg());
        if (StringUtils.isNotBlank(remarks)) {
            sRemarks.append(",")
                    .append(remarks);
        }
        Reminder reminder = Reminder.builder()
                .orderId(orderId)
                .quarter(quarter)
                .completeBy(StringUtils.left(user.getName(),30))
                .status(ReminderStatus.Completed.getCode())
                .completeAt(completeDate.getTime())
                .completeRemark(StringUtils.left(sRemarks.toString(),250))
                .build();
        reminder.setOrderCloseAt(completeDate.getTime());
        reminder.setOrderStatus(orderStatus);
        //complete
        reminder.setCompleteAt(reminder.getCompleteAt());
        reminder.setCompleteBy(reminder.getCompleteBy());
        reminder.setCompleteRemark(reminder.getCompleteRemark());
        reminder.setOperatorType(getReminderCreatorType(user).getCode());
        //update
        reminder.setUpdateById(user.getId());
        reminder.setUpdateDate(completeDate);
        reminder.setUpdateDt(completeDate.getTime());
        ReminderLog log = ReminderLog.builder()
                .quarter(quarter)
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(reminder.getCompleteRemark())
                .createName(reminder.getCompleteBy())
                .build();
        log.setCreateDt(reminder.getCompleteAt());
        log.setId(sequenceIdService.nextId());//2020/05/24
        reminder.setReminderLog(log);
        MSResponse<Integer> msResponse = feign.completeByOrder(reminder);
        if (!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        } else {
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
            map.put("orderId", orderId);
            map.put("quarter", quarter);
            map.put("reminderStatus", reminder.getStatus());
            map.put("reminderCreateBy", reminder.getUpdateById());
            map.put("reminderCreateAt", reminder.getUpdateDt());
            map.put("servicePointId",servicePointId);
            orderService.updateReminderInfo(map);
        }
    }


    /**
     * 第三方系统驳回
     * @param reminder
     * @return
     */
    public void rejectByB2B(Reminder reminder){
        reminder.setStatus(ReminderStatus.WaitReply.getCode());
        ReminderLog log = ReminderLog.builder()
                .quarter(reminder.getQuarter())
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left("第三方系统催单异常日志："+reminder.getReminderRemark(),255))
                .createName(reminder.getCreateBy().toString())
                .creatorType(2)
                .build();
        log.setId(sequenceIdService.nextId());//2020/05/24
        reminder.setReminderLog(log);
        MSResponse<Integer> msResponse = feign.rejectReminderForB2B(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException("第三方系统驳回调用微服务失败:"+msResponse.getMsg());
        }
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
        map.put("orderId", reminder.getOrderId());
        map.put("quarter", reminder.getQuarter());
        map.put("reminderStatus", reminder.getStatus());
        map.put("reminderCreateBy", reminder.getCreateById());
        map.put("reminderCreateAt", reminder.getCreateDt());
        map.put("servicePointId",reminder.getServicepointId());
        orderService.updateReminderInfo(map);
    }


    /**
     * 第三方系统关闭催单
     * @param reminder
     * @return
     */
    public void closeReminderItemByB2B(Reminder reminder){
        reminder.setStatus(ReminderStatus.Completed.code);
        reminder.setOperatorType(getReminderCreatorType(new User(reminder.getCreateById())).getCode());
        B2BDataSourceEnum dataSourceEnum = B2BDataSourceEnum.get(reminder.getDataSource());
        String dataSource = dataSourceEnum==null?"第三方系统通知关闭催单项：":dataSourceEnum.name+"通知关闭催单项：";
        ReminderLog log = ReminderLog.builder()
                .quarter(reminder.getQuarter())
                .status(reminder.getStatus())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left(dataSource+reminder.getReminderRemark(),255))
                .createName(reminder.getCreateBy().toString())
                .creatorType(2)
                .build();
        reminder.setReminderLog(log);
        MSResponse<Integer> msResponse = feign.closeReminderItemForB2B(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException("第三方系统关闭催单调用微服务失败:"+msResponse.getMsg());
        }else{
            Integer status = msResponse.getData();
            if(status!=null && status>0){
                reminder.setStatus(status);
            }
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(6);
            map.put("orderId", reminder.getOrderId());
            map.put("quarter", reminder.getQuarter());
            map.put("reminderStatus", reminder.getStatus());
            map.put("reminderCreateBy", reminder.getCreateById());
            map.put("reminderCreateAt", reminder.getCreateDt());
            map.put("servicePointId",reminder.getServicepointId());
            orderService.updateReminderInfo(map);
        }
    }

    public void insertReminderLogByB2B(Reminder reminder){
        ReminderLog log = ReminderLog.builder()
                .quarter(reminder.getQuarter())
                .visibilityFlag(ReminderModel.VISIBILITY_FLAG_ALL)
                .content(StringUtils.left("第三方系统发送普通催单日志："+reminder.getReminderRemark(),255))
                .createName(reminder.getCreateBy().toString())
                .creatorType(2)
                .build();
        reminder.setReminderLog(log);
        MSResponse<Integer> msResponse = feign.insertReminderLogByB2B(reminder);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException("第三方系统发送普通催单日志调用微服务失败:"+msResponse.getMsg());
        }
    }

    /**
     * 判断是否发送b2b
     */
    private boolean isSendProcessToB2b(ReminderItem reminderItem){
        boolean result = false;
        if(reminderItem.getDataSource()==B2BDataSourceEnum.JOYOUNG.id && reminderItem.getCreateType()==ReminderType.B2B.code
                && reminderItem.getId()>0){
           return true;
        }
        if(reminderItem.getDataSource()==B2BDataSourceEnum.MQI.id && reminderItem.getCreateType()==ReminderType.B2B.code
                && reminderItem.getId()>0){
            return true;
        }

        return result;
    }

    /**
     * 工单关闭更新相关信息(工单状态,工单关闭时间,工单关闭时效)
     * 工单关闭(工单取消/退单审核/客评时)
     * @param orderId   订单id
     * @param quarter   分片
     * @param orderCloseDate 工单关闭时间
     * @param orderStatus 工单状态
     */
    @Transactional()
    public void updateOrderCloseInfo(long orderId, String quarter,Date orderCloseDate,Integer orderStatus){
        /*
        Reminder reminder = new Reminder();
        reminder.setOrderId(orderId);
        reminder.setQuarter(quarter);
        reminder.setOrderCloseAt(orderCloseDate.getTime());
        reminder.setOrderStatus(orderStatus);
        feign.updateOrderCloseInfo(reminder);
        */
    }

    //endregion 催单

    //region 日志

    /**
     * 新建日志
     */
    @Transactional
    public void newLog(ReminderLog log){
        //2020/05/24
        if(log.getId() == null || log.getId() <=0){
            log.setId(sequenceIdService.nextId());
        }
        MSResponse<Long> msResponse = feign.newLog(log);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }
    }

    /**
     * 按催单id查询所有日志列表(按时间排序)
     */
    public List<ReminderLog> getLogs(String quarter,Long reminderId){
        MSResponse<MSPage<ReminderLog>> msResponse = feign.getReminderLogs(quarter,reminderId.toString());
        if(!MSResponse.isSuccessCode(msResponse)){
            return null;
        }
        MSPage<ReminderLog> msPage = msResponse.getData();
        if(msPage == null){
            return null;
        }

        return msPage.getList();
    }

    //endregion

    //region 公共
    /**
     * 判断用户的类型
     * @param user  帐号信息
     * @return 用户类型(ReminderCreatorType枚举)
     */
    public ReminderCreatorType getReminderCreatorType(User user){
        if(user == null || user.getId() == null || user.getId()<=0){
            return ReminderCreatorType.None;
        }
        if(user.isSaleman()){
            return ReminderCreatorType.FollowUp;
        }
        if(user.isCustomer()){
            return ReminderCreatorType.Customer;
        }
        if(user.isEngineer()){
            return ReminderCreatorType.Engineer;
        }
        return ReminderCreatorType.Kefu;
    }

    private List<ReminderModel> toList(MSPage<ReminderListModel> data){
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
        for(ReminderListModel form:data.getList()){
            model = mapper.toViewModel(form,spMap,customerMap,dataSourceMap,areaMap);
            model.setItemId(form.getItemId());
            list.add(model);
        }
        //List<AbnormalFormModel> list = Mappers.getMapper(AbnormalFormModelMapper.class).toViewModels(data.getList(),spMap,customerMap,userMap,categoryMap);
        spMap = null;
        customerMap = null;
        dataSourceMap = null;
        areaMap = null;
        return list;
    }

    public Integer getStatusById(Long id,String quarter){
        Integer status = 0;
        MSResponse<Integer> msResponse = feign.getStatusById(id,quarter);
        if(msResponse.getData()!=null){
            status = msResponse.getData();
        }
        return status;
    }
    //endregion
}

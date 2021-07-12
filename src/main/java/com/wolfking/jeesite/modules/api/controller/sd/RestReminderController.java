/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.api.controller.sd;

import com.kkl.kklplus.entity.cc.Reminder;
import com.kkl.kklplus.entity.cc.ReminderCreatorType;
import com.kkl.kklplus.entity.cc.ReminderItem;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.controller.RestBaseController;
import com.wolfking.jeesite.modules.api.entity.md.RestLoginUserInfo;
import com.wolfking.jeesite.modules.api.entity.sd.RestReminder;
import com.wolfking.jeesite.modules.api.entity.sd.RestReminderItem;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestGetReminderRequest;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestReplyReminderRequest;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.api.util.RestSessionUtils;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.cc.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;


/**
 * 催单处理
 *
 * @author Ryan
 * @version 2019-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/reminder/")
public class RestReminderController extends RestBaseController {

    private static String ENGINEE_RREPLY_CONTENT = "收到，立即处理";

    @Autowired
    private OrderService orderService;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private ReminderService reminderService;

    @Bean
    public MapperFactory getFactory() {
        return new DefaultMapperFactory.Builder().build();
    }

    //region 催单

    /**
     * 按订单id读取催单项目明细
     * isWaitReply 1:待回复
     */
    @RequestMapping(value = "getItems", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> list(HttpServletRequest request, HttpServletResponse response,
                                   @RequestBody RestGetReminderRequest reminderRequest) {
        RestLoginUserInfo userInfo = null;
        try {
            userInfo = RestSessionUtils.getLoginUserInfoFromRestSession(request.getAttribute("sessionUserId").toString());
            if (userInfo == null || userInfo.getUserId() == null || userInfo.getServicePointId() == null) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            if (reminderRequest == null || reminderRequest.getOrderId() == null || reminderRequest.getOrderId() == 0 || StringUtils.isBlank(reminderRequest.getQuarter())) {
                return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
            }
            Reminder reminder = reminderService.getLastReminderByOrderId(reminderRequest.getOrderId(),reminderRequest.getQuarter());
            if(reminder == null){
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, ErrorCode.DATA_PROCESS_ERROR.message);
            }
            if(CollectionUtils.isEmpty(reminder.getItems())){
                return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, "读取催单失败");
            }
            List<RestReminderItem> reminderItems = Lists.newArrayList();
            List<ReminderItem> items = reminder.getItems();
            ReminderItem item;
            RestReminderItem.RestReminderItemBuilder builder = RestReminderItem.builder();
            int itemNo = 0;
            ReminderCreatorType userType;
            RestReminder.RestReminderBuilder reminderBuilder = RestReminder.builder()
                    .id(reminder.getId())
                    .orderId(reminder.getOrderId())
                    .quarter(reminder.getQuarter())
                    .isWaitReply(reminder.getStatus()==1?1:0);
            //items
            for(int i=0,size=items.size();i<size;i++){
                item = items.get(i);
                itemNo++;
                userType = ReminderCreatorType.fromCode(item.getCreatorType());
                builder = builder.itemNo(itemNo).processor(item.getCreateName())
                        .processorTypeId(userType==null?ReminderCreatorType.Kefu.getCode():userType.getCode())
                        .processorType(userType==null?ReminderCreatorType.Kefu.getMsg():userType.getMsg())
                        .processAt(item.getCreateAt())
                        .processRemark(item.getCreateRemark());
                reminderItems.add(builder.build());
                if(item.getProcessAt()>0){
                    itemNo++;
                    userType = ReminderCreatorType.fromCode(item.getProcessorType());
                    builder = builder.itemNo(itemNo).processor(item.getCreateName())
                            .processorTypeId(userType==null?ReminderCreatorType.Kefu.getCode():userType.getCode())
                            .processorType(userType==null?ReminderCreatorType.Kefu.getMsg():userType.getMsg())
                            .processAt(item.getProcessAt())
                            .processRemark(item.getProcessRemark());
                    reminderItems.add(builder.build());
                }
            }
            return RestResultGenerator.success(reminderBuilder.items(reminderItems).build());
        } catch (Exception e) {
            try {
                log.error("[getItems] user:{} ,json:{}", userInfo.getUserId(), GsonUtils.toGsonString(reminderRequest), e);
            } catch (Exception e1) {
                log.error("[getItems] user:{}", userInfo.getUserId(), e);
            }
            return RestResultGenerator.custom(ErrorCode.DATA_PROCESS_ERROR.code, ErrorCode.DATA_PROCESS_ERROR.message);
        }
    }

    /**
     * 安维快速回复
     */
    @RequestMapping(value = "quickReply", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public RestResult<Object> quickReply(HttpServletRequest request, HttpServletResponse response,
                                          @RequestBody RestReplyReminderRequest reminderRequest) {
        //检查输入
        if(reminderRequest == null || StringUtils.isBlank(reminderRequest.getId()) || StringUtils.isBlank(reminderRequest.getOrderId())
                || StringUtils.isBlank(reminderRequest.getQuarter()) || StringUtils.isBlank(reminderRequest.getContent())
                || StringUtils.isBlank(reminderRequest.getUserPhone()) || StringUtils.isBlank(reminderRequest.getDate())
                ){
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        long id = StringUtils.toLong(reminderRequest.getId());
        long orderId = StringUtils.toLong(reminderRequest.getOrderId());
        if(id <= 0 || orderId <= 0){
            return RestResultGenerator.custom(ErrorCode.WRONG_REQUEST_FORMAT.code, ErrorCode.WRONG_REQUEST_FORMAT.message);
        }
        //帐号
        RestResult loginUserInfo = getLoginUserInfo(request);
        if(!loginUserInfo.getCode().equals(ErrorCode.NO_ERROR.code)){
            return loginUserInfo;
        }
        User user = (User)loginUserInfo.getData();
        if(user == null){
            return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
        }
        try {
            long servicePointId = Optional.ofNullable(user.getServicePointId()).orElse(0l);
            String replyContent = String.format("已与【%s】联系，沟通方案为【%s】，预计完成时间【%s】",reminderRequest.getUserPhone(),reminderRequest.getContent(),reminderRequest.getDate());
            reminderService.replyReminder(id,orderId,reminderRequest.getQuarter(),StringUtils.left(replyContent,250),user,servicePointId,0L);
            return RestResultGenerator.success();
        }catch (Exception e){
            return RestResultGenerator.exception(e.getMessage());
        }
    }

    //endregion 催单

    //region 公共

    /**
     * 读取当前登录帐号信息
     * @param request
     * @return RestResult.data > User
     */
    private RestResult getLoginUserInfo(HttpServletRequest request){
        try {
            RestLoginUserInfo userInfo = RestSessionUtils.getLoginUserInfoFromRestSession(request.getAttribute("sessionUserId").toString());
            if (userInfo == null || userInfo.getUserId() == null) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            long userId = userInfo.getUserId();
            User user = UserUtils.getAcount(userId);
            if (user == null) {
                return RestResultGenerator.custom(ErrorCode.LOGIN_INFO_MISSING.code, ErrorCode.LOGIN_INFO_MISSING.message);
            }
            return RestResultGenerator.success(user);
        }catch (Exception e){
            return RestResultGenerator.exception(e.getMessage());
        }
    }

    //endregion
}

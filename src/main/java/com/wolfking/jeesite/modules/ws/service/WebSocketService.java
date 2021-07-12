package com.wolfking.jeesite.modules.ws.service;

/**
 * Created by Ryan Lu
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.entity.WSMessage;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.NoticeService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.modules.ws.entity.WSResponse;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class WebSocketService extends LongIDBaseService {

    @Autowired
    //使用SimpMessagingTemplate 向浏览器发送消息
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private OrderService orderService;
//    @Autowired
//    private NoticeService noticeService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MapperFacade mapper;

    public void sendMessage(WSMessage message){
        WSResponse response = new WSResponse();
        response.setTitle(message.getTitle());
        response.setSender(message.getTriggerBy().getName());
        response.setContext(message.getContext());
        response.setNoticeType(message.getMessageType());
        response.setReferId(message.getOrderId().toString());
        response.setReferNo(message.getOrderNo());
        response.setQuarter(message.getQuarter());
        response.setSender(message.getTriggerBy().getName());
        response.setSendDate(DateUtils.formatDate(message.getTriggerDate(),"MM-dd HH:mm"));
        Notice notice = new Notice();
        Long nid;
        try {
            if (WSMessage.isSendToUser(message.getMessageType())) {
                //点对点
                List<String> users = getReceiver(message);
                if(users !=null && users.size()>0) {
                    //BeanMapper.copy(response, notice);
                    notice = mapper.map(response,Notice.class);
                    notice.setCreateBy(message.getTriggerBy());
                    notice.setCreateDate(message.getTriggerDate());
                    for(int i=0,size=users.size();i<size;i++) {
                        notice.setUserId(Long.valueOf(users.get(i)));
                        //同用户，同订单，同类型有未读的不发送 - 2017/10/08
                        // mark on 2020-11-23 begin
                        /*nid = noticeService.getNotReadLastId(notice.getReferId(),notice.getUserId(),notice.getNoticeType());
                        if(nid == null || nid ==0) {
                            noticeService.insert(notice);
                            response.setId(notice.getId().toString());
                            messagingTemplate.convertAndSendToUser(users.get(i), message.getDistination(), response);
                        }*/
                        // mark on 2020-11-23 end
                    }
                }
            } else {
                //群发
                messagingTemplate.convertAndSend(message.getDistination(), response);
            }
            /*
            message.setStatus(30);
            message.setCreateDate(new Date());
            if(message.getId()==null || message.getId()==0) {
                message.setId(SeqUtils.NextID());
                messageService.insert(wsMessage);
            }else{
                messageService.update(wsMessage);
                //失败
                //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }*/
        }catch (Exception e){
            log.error("[WebSocketService.sendMessage]",e);
        }
    }

    /**
     * 返回收件人
     * @param message
     * @return
     */
    private List<String> getReceiver(WSMessage message){
//        Map<String,String> maps = Maps.newHashMap();
        Set<String> ids = Sets.newHashSet();
        switch (message.getMessageType()){
            case Notice.NOTICE_TYPE_FEEDBACK:
                if(message.getTriggerBy().getUserType()== 3 || message.getTriggerBy().getUserType()== 4 || message.getTriggerBy().getUserType()==7){
                    //3，4：customer ->客服，客服主管
                    //7：业务 -> 客服，客服主管
//                Order order = orderService.getOrderById(message.getOrderId(),message.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
                    if(message.getKefuId()==null || message.getKefuId()==0){
                        //无客服
//                      List<String> leaders = UserUtils.getOfficeLeaderIds(message.getTriggerBy().getOfficeId(),2);
                        //1.kefu
                        List<Long> users = UserUtils.getKefuIdListByOrder(message.getCustomerId(),message.getAreaId());
                        if(users != null && users.size()>0){
                            //leader
                            List<String> leaders = UserUtils.getOfficeLeaderIds(users.get(0));
                            if(leaders != null && leaders.size()>0){
                                ids = Sets.union(ids,leaders.stream().collect(Collectors.toSet()));
                            }
                            ids = Sets.union(ids,users.stream().map(t->t.toString()).collect(Collectors.toSet()));
                        }
                    }else{
                        Order order = orderService.getOrderById(message.getOrderId(),message.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
                        if(order == null || order.getOrderCondition() == null){
                            return Lists.newArrayList();
                        }
                        if(order.getOrderCondition()!=null && order.getOrderCondition().getKefu() !=null){
                            ids=Sets.union(ids,Sets.newHashSet(message.getKefuId().toString()));
                            //leader
                            List<String> leaders = UserUtils.getOfficeLeaderIds(message.getKefuId());
                            if(leaders != null && leaders.size()>0){
                                ids = Sets.union(ids,leaders.stream().collect(Collectors.toSet()));
                            }
                        }
                    }
                }else if(message.getTriggerBy().getUserType()<=2){
                    //1:客服主管，管理员，其它人员 -> 客户，业务，业务主管
                    //2:客服 -> 客户，业务，业务主管
                    //Order order = orderService.getOrderById(message.getOrderId(),message.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
                    //Long cid = order.getOrderCondition().getCustomer().getId();
                    List<String> leaders;
                    //客户
                    if(!Objects.equals(message.getCreateId(),message.getTriggerBy().getId())){
                        ids=Sets.union(ids,Sets.newHashSet(message.getCreateId().toString()));
                    }
                    //客户主帐号
                    leaders = customerService.getAccountMasters(message.getCustomerId());
                    if(leaders !=null && leaders.size()>0){
                        ids = Sets.union(ids,leaders.stream().collect(Collectors.toSet()));
                    }

                    //业务
                    if(!Objects.equals(message.getSalesId(),message.getTriggerBy().getId())) {
                        ids=Sets.union(ids,Sets.newHashSet(message.getSalesId().toString()));
                    }
                    //业务主管
                    User u = UserUtils.get(message.getSalesId(),null);
                    leaders = UserUtils.getOfficeLeaderIds(u.getOffice().getId(),u.getUserType());
                    if(leaders !=null &&leaders.size()>0){
                        if(leaders.contains(message.getTriggerBy().getId().toString())){
                            leaders.remove(message.getTriggerBy().getId().toString());//触发者是业务主管(user_type=1,role:业务主管)
                        }
                        ids = Sets.union(ids,leaders.stream().collect(Collectors.toSet()));
                    }
                }
                break;
            case Notice.NOTICE_TYPE_APPABNORMALY://app异常，只通知客服
                if(message.getKefuId()==null || message.getKefuId()==0){
                    //无客服
                    //1.kefu
                    List<Long> users = UserUtils.getKefuIdListByOrder(message.getCustomerId(),message.getAreaId());
                    if(users != null && users.size()>0){
                        //leader
                        List<String> leaders = UserUtils.getOfficeLeaderIds(users.get(0));
                        if(leaders != null && leaders.size()>0){
                            ids = Sets.union(ids,leaders.stream().collect(Collectors.toSet()));
                        }
                        ids = Sets.union(ids,users.stream().map(t->t.toString()).collect(Collectors.toSet()));
                    }
                }else{
                    Order order = orderService.getOrderById(message.getOrderId(),message.getQuarter(), OrderUtils.OrderDataLevel.CONDITION,true);
                    if(order == null || order.getOrderCondition() == null){
                        return Lists.newArrayList();
                    }
                    if(order.getOrderCondition()!=null && order.getOrderCondition().getKefu() !=null){
                        ids=Sets.union(ids,Sets.newHashSet(message.getKefuId().toString()));
                        //leader
                        List<String> leaders = UserUtils.getOfficeLeaderIds(message.getKefuId());
                        if(leaders != null && leaders.size()>0){
                            ids = Sets.union(ids,leaders.stream().collect(Collectors.toSet()));
                        }
                    }
                }
                break;
        }

        return ids.stream().collect(Collectors.toList());
    }

    /**
     * 群发消息
     * @param subject
     * @param content
     */
    public void sendMessage(String subject,String content){
        WSResponse response = new WSResponse(subject,content);
        // 发送消息给订阅 "/topic/notice" 且在线的用户
        messagingTemplate.convertAndSend("/topic/notice", response);
    }

    /**
     * 群发消息
     * @param subject
     * @param content
     */
    public void sendMessageToUser(String toUser,String subject,String content){
        WSResponse response = new WSResponse(subject,content);
        // 发送消息给订阅 "/user/topic/chat" 且用户名为toUser的用户
        messagingTemplate.convertAndSendToUser(toUser, "/topic/point", response);
    }

//    public void sendMessage() throws Exception{
//        for(int i=0;i<10;i++)
//        {
//            Thread.sleep(1000);
//            template.convertAndSend("/topic/getAlarm",new Response(1,"后台发送","Welcome,yangyibo !"));
//            System.out.println("----------------------yangyibo"+i);
//        }
//    }

}

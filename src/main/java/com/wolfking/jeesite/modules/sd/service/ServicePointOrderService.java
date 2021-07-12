/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.persistence.LongIDBaseEntity;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDBaseService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.md.service.UrgentLevelService;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.sd.dao.*;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderServicePointSearchModel;
import com.wolfking.jeesite.modules.sd.utils.OrderItemUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 网点工单列表
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointOrderService extends LongIDBaseService {

    /**
     * 持久层对象
     */
    @Resource
    protected OrderDao dao;


    @Resource
    protected CustomerFinanceDao customerFinanceDao;

    @Resource
    protected CustomerCurrencyDao customerCurrencyDao;

    @Resource
    protected ServicePointDao servicePointDao;

    @Resource
    protected OrderAttachmentDao attachmentDao;

    @Resource
    protected OrderComplainDao complainDao;

    @Resource
    protected UserDao userDao;

    @Resource
    protected FeedbackDao feedbackDao;

    @Autowired
    private ServicePointService servicePointService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    public RedisTemplate redisTemplate;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;


    /**
     * 设置工单对象的部分属性
     *
     * @param orderList
     */
    private void setOrderProperties(List<Order> orderList) {
        if (orderList != null && orderList.size() > 0) {
            Map<String, Dict> statusMap = MSDictUtils.getDictMap("order_status");
            Map<String, Dict> pendingTypeMap = MSDictUtils.getDictMap("PendingType");
            Map<Long, UrgentLevel> urgentLevelMap = urgentLevelService.findAllMap();
            List<Long> kefuIds = orderList.stream().filter(i->i.getOrderCondition()!=null&&i.getOrderCondition().getKefu()!=null&&i.getOrderCondition().getKefu().getId()!=null)
                    .map(i->i.getOrderCondition().getKefu().getId()).collect(Collectors.toList());
            Map<Long, User> kefuMap = MSUserUtils.getMapByUserIds(kefuIds);
//            Map<Long, User> kefuMap = MSUserUtils.getMapByUserType(User.USER_TYPE_SERVICE);
            List<OrderItem> orderItemList = Lists.newArrayList();
            for (Order item : orderList) {
                item.setItems(OrderItemUtils.fromOrderItemsJson(item.getOrderItemJson()));
                orderItemList.addAll(item.getItems());

                if (item.getOrderCondition() != null) {
                    if (item.getOrderCondition().getServicePoint() != null && item.getOrderCondition().getServicePoint().getId() != null) {
                        ServicePoint servicePoint = servicePointService.getFromCache(item.getOrderCondition().getServicePoint().getId());
                        if (servicePoint != null) {
                            item.getOrderCondition().setServicePoint(servicePoint);
                        }
                        if (item.getOrderCondition().getEngineer() != null && item.getOrderCondition().getEngineer().getId() != null) {
                            Engineer engineer = servicePointService.getEngineerFromCache(item.getOrderCondition().getServicePoint().getId(), item.getOrderCondition().getEngineer().getId());
                            if (engineer != null) {
                                item.getOrderCondition().setEngineer(new User(engineer.getId(), engineer.getName(), engineer.getContactInfo()));
                            }
                        }
                    }
                    if (item.getOrderCondition().getKefu() != null && item.getOrderCondition().getKefu().getId() != null) {
                        User kefu = kefuMap.get(item.getOrderCondition().getKefu().getId());
                        if (kefu != null) {
                            item.getOrderCondition().setKefu(kefu);
                        }
                    }
                }
                Dict statusDict = statusMap.get(item.getOrderCondition().getStatus().getValue());
                if (statusDict != null) {
                    item.getOrderCondition().setStatus(statusDict);
                }
                Dict pendingTypeDict = pendingTypeMap.get(item.getOrderCondition().getPendingType().getValue());
                if (pendingTypeDict != null) {
                    item.getOrderCondition().setPendingType(pendingTypeDict);
                }
                UrgentLevel urgentLevel = urgentLevelMap.get(item.getOrderCondition().getUrgentLevel().getId());
                if (urgentLevel != null) {
                    item.getOrderCondition().setUrgentLevel(urgentLevel);
                }
            }
            OrderItemUtils.setOrderItemProperties(orderItemList, Sets.newHashSet(CacheDataTypeEnum.SERVICETYPE, CacheDataTypeEnum.PRODUCT));
        }
    }



    //endregion 网点

    /**
     * 判断网点订单是否有投诉
     *
     * @param servicePointId 网点ID
     * @param orderIds       订单ID列表
     * @return
     */
    public Set<Long> getServicePointComplainOrderIdSet(Long servicePointId, List<Long> orderIds) {
        List<Long> ids = complainDao.getOrderIdList(servicePointId, orderIds);
        if (ids == null || ids.size() == 0) {
            return Sets.newHashSet();
        }
        return Sets.newHashSet(ids);
    }
}

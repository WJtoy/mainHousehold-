package com.wolfking.jeesite.ms.providersys.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SyncTypeEnum;
import com.kkl.kklplus.entity.sys.SysUserCustomer;
import com.kkl.kklplus.entity.sys.mq.MQSysUserCustomerMessage;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.mq.sender.SysUserCustomerSender;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.utils.MDUtils;
import com.wolfking.jeesite.ms.providersys.feign.MSSysUserCustomerFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MSSysUserCustomerService {
    @Autowired
    private MSSysUserCustomerFeign msSysUserCustomerFeign;

    @Autowired
    private SysUserCustomerSender sysUserCustomerSender;

    /**
     * 根据客户id获取用户id列表
     * @param customerId
     * @return
     */
    public List<Long> findUserIdListByCustomerId(Long customerId) {
        return MDUtils.findListUnnecessaryConvertType(()->msSysUserCustomerFeign.findUserIdListByCustomerId(customerId));
    }

    /**
     * 根据userId或(同时）customerId获取客户id列表
     * @param paramMap
     * @return
     */
    public List<Long> findCustomerIdList(Map<String, Object> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return Lists.newArrayList();
        }
        SysUserCustomer sysUserCustomer = new SysUserCustomer();
        Optional.ofNullable(paramMap.get("userId")).ifPresent(r->sysUserCustomer.setUserId(Long.valueOf(r.toString())));
        Optional.ofNullable(paramMap.get("customerId")).ifPresent(r->sysUserCustomer.setCustomerId(Long.valueOf(r.toString())));

        return MDUtils.findListUnnecessaryConvertType(()->msSysUserCustomerFeign.findCustomerIdList(sysUserCustomer));
    }

    public List<Long> findCustomerIdListByUserId(Long userId) {
        Map<String,Object> paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
        return findCustomerIdList(paramMap);
    }


    /**
     * 获取所有的customerId列表
     *
     * @return
     */
    public List<Long> findAllCustomerIdList() {
        return MDUtils.findListUnnecessaryConvertType(()->msSysUserCustomerFeign.findAllCustomerIdList());
    }


    /**
     * 根据userId删除用户客户
     * @param userId
     */
    public void deleteByUserId(Long userId) {
        MSErrorCode msErrorCode = MDUtils.customSave(()->msSysUserCustomerFeign.deleteByUserId(userId));
        if (msErrorCode.getCode() > 0) {
            throw new RuntimeException("调用微服务删除用户客户失败,失败原因:"+ msErrorCode.getMsg());
        }
    }

    /**
     * 批量添加用户客户列表
     * @param user
     */
    public void batchInsert(User user) {
        List<Long> customerIds = Lists.newArrayList();

        if (user != null && user.getId() != null) {
            List<Customer> customerList = user.getCustomerList();
            if (!ObjectUtils.isEmpty(customerList)) {
                for(int i=0; i < customerList.size();i++) {
                    Customer customer = customerList.get(i);
                    if (customer.getId() != null) {
                        customerIds.add(customer.getId());
                    }
                }
            }
        }
        if (ObjectUtils.isEmpty(customerIds)) {
            return;
        }
        if (customerIds.size() < 100) {
            MSErrorCode msErrorCode = MDUtils.customSave(() -> msSysUserCustomerFeign.batchInsert(user.getId(), customerIds));
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("调用微服务批量添加用户客户失败,失败原因:" + msErrorCode.getMsg());
            }
        } else {
            Lists.partition(customerIds, 100).forEach(partList->{
                MSErrorCode msErrorCode = MDUtils.customSave(() -> msSysUserCustomerFeign.batchInsert(user.getId(), partList));
                if (msErrorCode.getCode() > 0) {
                    throw new RuntimeException("调用微服务分批添加用户客户失败,失败原因:" + msErrorCode.getMsg());
                }
            });
        }
        try {
            MQSysUserCustomerMessage.SysUserCustomerMessage message = MQSysUserCustomerMessage.SysUserCustomerMessage.newBuilder()
                    .setUserId(user.getId())
                    .addAllCustomerIds(customerIds)
                    .setSyncType(SyncTypeEnum.ADD.getValue())
                    .build();
            sysUserCustomerSender.send(message);
        }catch (Exception e){
           log.error("发送消息队列同步用户客户关联到报表数据库失败.MSSysUserCustomerService.batchInsert:{}",user.getId(),e.getMessage());
        }
    }

    @Deprecated   // 辅助方法，后续要删除
    public void compareCollectionData(List<Long> dbList, List<Long> msList, String param, String methodName) {
        try{
            String msg = "";
            if (org.springframework.util.ObjectUtils.isEmpty(msList)) {
                if (!org.springframework.util.ObjectUtils.isEmpty(dbList)) {
                    List<Long> dbIds = dbList.stream().sorted().collect(Collectors.toList());
                    msg = "db:" + dbIds.toString() + ",ms为空.";
                }
            } else {
                boolean isEquals = true;
                List<Long> msIds = msList.stream().sorted().collect(Collectors.toList());
                if (!org.springframework.util.ObjectUtils.isEmpty(dbList)) {
                    List<Long> dbIds = dbList.stream().collect(Collectors.toList());

                    if (dbIds.size() != msIds.size()) {
                        isEquals = false;
                    }
                    if (isEquals) {
                        if (dbIds.stream().filter(x->!msIds.contains(x)).count() >0) {
                            isEquals = false;
                        }
                    }
                    if (isEquals) {
                        if (msIds.stream().filter(x->!dbIds.contains(x)).count() >0) {
                            isEquals = false;
                        }
                    }
                    if (!isEquals) {
                        msg = "ms:" + msIds.toString()+",DB："+ dbIds.toString();
                    }
                } else {
                    msg = "ms:" + msIds.toString()+",DB为空";
                }
            }
            if (msg != "") {
                msg = "网点日志DB与MS返回不一致," + msg;
                //LogUtils.saveLog("基础资料_用户客户", methodName, "参数："+param+",信息:"+msg, null, UserUtils.getUser());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Deprecated
    public void compareFeedbackData(List<Map<String,Object>> dbList, List<Map<String,Object>> msList, String id , String param, String methodName) {
        try {
            String msg = "";
            if (org.springframework.util.ObjectUtils.isEmpty(msList)) {
                if (!org.springframework.util.ObjectUtils.isEmpty(dbList)) {
                    msg = "db:" + dbList.toString() + ",ms为空.";
                }
            } else {
                boolean isEquals = true;
                List<Long> msIds = msList.stream().map(r->r.get(id).toString()).map(Long::valueOf).collect(Collectors.toList());
                if (!org.springframework.util.ObjectUtils.isEmpty(dbList)) {
                    List<Long> dbIds = dbList.stream().map(r->r.get(id).toString()).map(Long::valueOf).collect(Collectors.toList());

                    if (dbIds.size() != msIds.size()) {
                        isEquals = false;
                    }
                    if (isEquals) {
                        if (dbIds.stream().filter(x->!msIds.contains(x)).count() >0) {
                            isEquals = false;
                        }
                    }
                    if (isEquals) {
                        if (msIds.stream().filter(x->!dbIds.contains(x)).count() >0) {
                            isEquals = false;
                        }
                    }
                    if (!isEquals) {
                        msg = "ms:" + msIds.toString()+",DB："+ dbIds.toString();
                    }
                } else {
                    msg = "ms:" + msIds.toString()+",DB为空";
                }
            }
            if (msg != "") {
                msg = "网点日志DB与MS返回不一致," + msg;
                LogUtils.saveLog("基础资料_用户客户", methodName, "参数："+param+",信息:"+msg, null, UserUtils.getUser());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}

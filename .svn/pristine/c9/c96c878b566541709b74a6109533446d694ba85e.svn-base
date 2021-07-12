package com.wolfking.jeesite.modules.fi.service;

import cn.hutool.core.util.StrUtil;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.fi.mq.MQCreateCustomerChargeMessage;
import com.kkl.kklplus.entity.fi.mq.MQCreateCustomerCurrencyMessage;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.modules.fi.dao.CustomerBlockCurrencyDao;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.mq.sender.CreateCustomerBlockCurrencySender;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 冻结流水服务
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class CustomerBlockCurrencyService extends LongIDCrudService<CustomerCurrencyDao, CustomerCurrency> {

    @Resource
    private CustomerBlockCurrencyDao customerBlockCurrencyDao;

    @Autowired
    private SequenceIdService sequenceIdService;
    @Autowired
    private CreateCustomerBlockCurrencySender createCustomerBlockCurrencySender;

    //region 读数据

    public List<CustomerCurrency> getCustomerBlockCurrencyList(Long customerId, Long salesId, Integer currencyType, String currencyNo,
                                                               Date beginDate, Date endDate, Page<CustomerCurrency> page) {
        return customerBlockCurrencyDao.getCustomerBlockCurrencyList(beginDate, endDate, customerId, currencyType, currencyNo, page);
    }

    //endregion 读数据

    //region 写数据

    /**
     * 下单冻结流水
     */
    public void saveOrderCreatedBlockCurrency(Long customerId, String orderNo,
                                              Double beforeBalance, Double blockedCharge, Double expectCharge, Double customerUrgentCharge,
                                              String quarter, User user, Date createDate) {
        try {
            String remarks;
            if (customerUrgentCharge > 0) {
                remarks = String.format("下单冻结 %.2f元,其中加急费：%.2f. 相关单号为 %s", (blockedCharge + expectCharge), customerUrgentCharge, orderNo);
            } else {
                remarks = String.format("下单冻结 %.2f元 相关单号为 %s", (blockedCharge + expectCharge), orderNo);
            }
            MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.Builder builder = MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.newBuilder()
                    .setMessageId(getBlockCurrencyId())
                    .setCustomerId(customerId)
                    .setCurrencyType(CustomerCurrency.CURRENCY_TYPE_IN)
                    .setCurrencyNo(orderNo)
                    .setBeforeBalance(beforeBalance)
                    .setBalance(beforeBalance + blockedCharge + expectCharge)
                    .setAmount(blockedCharge + expectCharge)
                    .setCreateById(user.getId())
                    .setCreateDate(createDate.getTime())
                    .setRemarks(remarks)
                    .setQuarter(quarter);
            sendCustomerBlockCurrency(builder);
        } catch (Exception e) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("orderNo:").append(StrUtil.isNotEmpty(orderNo) ? orderNo : "").append(";")
                    .append("createDate:").append(createDate == null ? 0 : createDate.getTime());
            LogUtils.saveLog("客户冻结.创建消息失败", "CustomerBlockCurrencyService#saveOrderCreatedBlockCurrency", logBuilder.toString(), e, null);
        }
    }

    /**
     * 工单对账的解冻流水
     */
    public void saveOrderChargedBlockCurrency(MQCreateCustomerChargeMessage.CreateCustomerChargeMessage messageObj) {
        try {
            MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.Builder builder = MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.newBuilder()
                    .setMessageId(messageObj.getCustomerBlockCurrencyId())
                    .setCustomerId(messageObj.getCustomerId())
                    .setCurrencyType(CustomerCurrency.CURRENCY_TYPE_OUT)
                    .setCurrencyNo(messageObj.getOrderNo())
                    .setBeforeBalance(messageObj.getBeforeBlockBalance())
                    .setBalance(messageObj.getBlockBalance())
                    .setAmount(messageObj.getBlockAmount())
                    .setCreateById(messageObj.getCreateById())
                    .setCreateDate(messageObj.getCreateDate())
                    .setRemarks(String.format("订单完结解冻:%.2f元,相关单号为:%s", (0 - messageObj.getBlockAmount()), messageObj.getOrderNo()))
                    .setQuarter(messageObj.getQuarter());
            sendCustomerBlockCurrency(builder);
        } catch (Exception e) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("orderNo:").append(StrUtil.isNotEmpty(messageObj.getOrderNo()) ? messageObj.getOrderNo() : "").append(";")
                    .append("createDate:").append(messageObj.getCreateDate());
            LogUtils.saveLog("客户冻结.创建消息失败", "CustomerBlockCurrencyService#saveOrderChargedBlockCurrency", logBuilder.toString(), e, null);
        }
    }

    /**
     * 客户冻结流水
     */
    public void saveCustomerBlockCurrency(CustomerCurrency currency) {
        try {
            MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.Builder builder = MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.newBuilder()
                    .setCustomerId(currency.getCustomer().getId())
                    .setCurrencyType(currency.getCurrencyType())
                    .setCurrencyNo(currency.getCurrencyNo())
                    .setBeforeBalance(currency.getBeforeBalance())
                    .setBalance(currency.getBalance())
                    .setAmount(currency.getAmount())
                    .setCreateById(currency.getCreateBy().getId())
                    .setCreateDate(currency.getCreateDate().getTime())
                    .setRemarks(currency.getRemarks())
                    .setQuarter(currency.getQuarter());
            if (currency.getId() != null && currency.getId() > 0) {
                builder.setMessageId(currency.getId());
            }
            sendCustomerBlockCurrency(builder);
        } catch (Exception e) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("orderNo:").append(currency != null && StrUtil.isNotEmpty(currency.getCurrencyNo()) ? currency.getCurrencyNo() : "").append(";")
                    .append("createDate:").append(currency != null && currency.getCreateDate() != null ? currency.getCreateDate().getTime() : 0);
            LogUtils.saveLog("客户冻结.创建消息失败", "CustomerBlockCurrencyService#saveCustomerBlockCurrency", logBuilder.toString(), e, null);
        }
    }


    private void sendCustomerBlockCurrency(MQCreateCustomerCurrencyMessage.CreateCustomerCurrencyMessage.Builder builder) {
        try {
            builder.setActionType(CustomerCurrency.ACTION_TYPE_BLOCK)
                    .setPaymentType(CustomerCurrency.PAYMENT_TYPE_CASH);
            createCustomerBlockCurrencySender.send(builder.build());
        } catch (Exception e) {
            LogUtils.saveLog("客户冻结.发送消息失败", "CustomerBlockCurrencyService#sendCustomerBlockCurrency", new JsonFormat().printToString(builder.build()), e, null);
        }
    }

    private Long getBlockCurrencyId() {
        Long currencyId = null;
        try {
            currencyId = SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency);
        } catch (Exception e) {
            log.error("[CustomerBlockCurrencyService.getBlockCurrencyId]获得id错误，key:{}", SeqUtils.TableName.CustomerCurrency, e);
        }
        if (currencyId == null || currencyId == 0) {
            currencyId = sequenceIdService.nextId();
        }
        return currencyId;
    }

    //endregion 写数据
}

package com.wolfking.jeesite.ms.recharge.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRechargeSearch;
import com.kkl.kklplus.entity.rpt.common.RPTSystemCodeEnum;
import com.kkl.kklplus.entity.sys.SysSMSTypeEnum;
import com.kkl.kklplus.utils.NumberUtils;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.modules.fi.dao.CustomerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.CustomerCurrency;
import com.wolfking.jeesite.modules.md.dao.CustomerFinanceDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsMQSender;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.SeqUtils;
import com.wolfking.jeesite.ms.recharge.entity.CustomerOfflineRechargeModel;
import com.wolfking.jeesite.ms.recharge.entity.mapper.CustomerOffineRechargeModelMapper;
import com.wolfking.jeesite.ms.recharge.feign.CustomerOfflineRechargeFeign;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerOfflineRechargeService {

    @Autowired
    private CustomerOfflineRechargeFeign customerOfflineRechargeFeign;

    @Autowired
    private CustomerFinanceDao customerFinanceDao;

    @Autowired
    private CustomerCurrencyDao customerCurrencyDao;

    @Autowired
    private SmsMQSender smsMQSender;

    @Autowired
    private CustomerService customerService;

    //财务手机号
    public static final String FINANCE_PHONE = "13620893003";

    /**
     * 添加客户线下充值
     * */
    public void save(CustomerOfflineRecharge customerOfflineRecharge){
      MSResponse<Integer> msResponse = customerOfflineRechargeFeign.save(customerOfflineRecharge);
      if(!MSResponse.isSuccessCode(msResponse)){
        throw new RuntimeException(msResponse.getMsg());
      }
      try {
          //给财务发送短信
          String strPayType="";
          if(customerOfflineRecharge.getPayType().equals(CustomerOfflineRecharge.ALIPAY_TYPE)){
              strPayType="支付宝";
          }else if(customerOfflineRecharge.getPayType().equals(CustomerOfflineRecharge.WECHAT_TYPE)){
              strPayType="微信";
          }
          String systemName="";
          RPTSystemCodeEnum systemCodeEnum = RPTSystemCodeEnum.get(customerOfflineRecharge.getSite());
          if(systemCodeEnum!=null){
              systemName = systemCodeEnum.name;
          }
          String customerName = "";
          Customer customer = customerService.getFromCache(customerOfflineRecharge.getCustomerId());
          if(customer!=null){
              customerName = customer.getName();
          }
          //String content ="【快可立】您有新的".concat("线下充值申请单需要审核,客户【").concat(strPayType).concat("】，充值金额【").concat(systemName).concat("系统后台进行审核");
          String content ="【快可立】您有新的线下充值申请单需要审核,客户【" +customerName+"】,"+ String.format("充值金额【%.2f元】",customerOfflineRecharge.getPendingAmount())+
                          ",交易单号【"+customerOfflineRecharge.getTransferNo()+"】,充值方式【"+strPayType+"】,请及时登录"+systemName+"系统后台进行审核";
          smsMQSender.sendNew(FINANCE_PHONE,content,"",customerOfflineRecharge.getCreateById(),customerOfflineRecharge.getCreateAt(), SysSMSTypeEnum.OFFLINE_RECHARGE);
      }catch (Exception e){
          log.error("CustomerOfflineRechargeService.save:客户线下充值短信通知财务失败:{}",e.getMessage());
      }
    }

    /**
     * 查看客户线下待审核充值明细
     * @param offlineRechargePage
     * @param offlineRechargeSearch
     * */
    public Page<CustomerOfflineRechargeModel> findPendingList(Page<CustomerOfflineRechargeModel> offlineRechargePage, CustomerOfflineRechargeSearch offlineRechargeSearch){
        offlineRechargeSearch.setPage(new MSPage<>(offlineRechargePage.getPageNo(), offlineRechargePage.getPageSize()));
        MSResponse<MSPage<CustomerOfflineRecharge>> msResponse = customerOfflineRechargeFeign.findPendingList(offlineRechargeSearch);
        if (MSResponse.isSuccess(msResponse)) {
            MSPage<CustomerOfflineRecharge> data = msResponse.getData();
            offlineRechargePage.setCount(data.getRowCount());
            List<CustomerOfflineRechargeModel> list = Mappers.getMapper(CustomerOffineRechargeModelMapper.class).toViewModels(data.getList());
            List<Dict> dictList = MSDictUtils.getDictList("offline_recharge_back_ratio");
            double backAmount = 0.0;
            for(CustomerOfflineRechargeModel item:list){
                backAmount = getMoneyBack(item.getPendingAmount(),dictList);
                if(backAmount>0){
                    item.setFinallyAmount(item.getPendingAmount()+backAmount);
                }else{
                    item.setFinallyAmount(item.getPendingAmount());
                }
                item.setStrPendingAmount(rvZeroAndDot(item.getPendingAmount()));
                item.setStrFinallyAmount(rvZeroAndDot(item.getFinallyAmount()));
            }
            offlineRechargePage.setList(list);
        } else {
            offlineRechargePage.setCount(0);
            offlineRechargePage.setList(Lists.newArrayList());
        }
        return offlineRechargePage;
    }

    /**
     * 查看客户线下已审核充值明细
     * @param offlineRechargePage
     * @param offlineRechargeSearch
     * */
    public Page<CustomerOfflineRechargeModel> findHasReviewList(Page<CustomerOfflineRechargeModel> offlineRechargePage, CustomerOfflineRechargeSearch offlineRechargeSearch){
        offlineRechargeSearch.setPage(new MSPage<>(offlineRechargePage.getPageNo(), offlineRechargePage.getPageSize()));
        MSResponse<MSPage<CustomerOfflineRecharge>> msResponse = customerOfflineRechargeFeign.findHasReviewList(offlineRechargeSearch);
        if (MSResponse.isSuccess(msResponse)) {
            MSPage<CustomerOfflineRecharge> data = msResponse.getData();
            offlineRechargePage.setCount(data.getRowCount());
            List<CustomerOfflineRechargeModel> list = Mappers.getMapper(CustomerOffineRechargeModelMapper.class).toViewModels(data.getList());
            offlineRechargePage.setList(list);
        } else {
            offlineRechargePage.setCount(0);
            offlineRechargePage.setList(Lists.newArrayList());
        }
        return offlineRechargePage;
    }

    /**
     *  审核通过
     * @param customerOfflineRecharge
     * */
    @Transactional()
    public void approved(CustomerOfflineRecharge customerOfflineRecharge,User user){
        double backAmount = 0.0;
        Date date = new Date();
        customerOfflineRecharge.setUpdateAt(date.getTime());
        List<Dict> dictList = MSDictUtils.getDictList("offline_recharge_back_ratio");//返现比率
        backAmount = getMoneyBack(customerOfflineRecharge.getActualAmount(),dictList);
        if(backAmount>0){
            customerOfflineRecharge.setFinallyAmount(customerOfflineRecharge.getActualAmount()+backAmount);
        }else{
            customerOfflineRecharge.setFinallyAmount(customerOfflineRecharge.getActualAmount());
        }
        customerOfflineRecharge.setBackAmount(backAmount);
        MSResponse<Integer> msResponse = customerOfflineRechargeFeign.approved(customerOfflineRecharge);
        if(!MSResponse.isSuccessCode(msResponse)){
            throw new RuntimeException(msResponse.getMsg());
        }
        //生成客户流水记录,更改客户余额
        CustomerFinance customerFinance = customerFinanceDao.get(customerOfflineRecharge.getCustomerId());
        //切换为微服务
        if (customerFinance.getPaymentType() != null && Integer.parseInt(customerFinance.getPaymentType().getValue()) > 0) {
            String paymentTypeLabel = MSDictUtils.getDictLabel(customerFinance.getPaymentType().getValue(), "PaymentType", "");
            customerFinance.getPaymentType().setLabel(paymentTypeLabel);
        }
        String strPayType="";
        if(customerOfflineRecharge.getPayType().equals(CustomerOfflineRecharge.ALIPAY_TYPE)){
            strPayType="支付宝";
        }else if(customerOfflineRecharge.getPayType().equals(CustomerOfflineRecharge.WECHAT_TYPE)){
            strPayType="微信";
        }
        CustomerCurrency entity = new CustomerCurrency();
        //充值成功新增一笔流水
        entity.setId(SeqUtils.NextIDValue(SeqUtils.TableName.CustomerCurrency));
        entity.setCurrencyNo(customerOfflineRecharge.getTransferNo());
        entity.setCustomer(new Customer(customerOfflineRecharge.getCustomerId()));
        entity.setBeforeBalance(customerFinance.getBalance());
        entity.setBalance(entity.getBeforeBalance() + customerOfflineRecharge.getFinallyAmount());
        entity.setAmount(customerOfflineRecharge.getFinallyAmount());
        entity.setCurrencyType(CustomerCurrency.CURRENCY_TYPE_IN);
        entity.setActionType(CustomerCurrency.ACTION_TYPE_CHARGEOFFLINE); //线下充值
        entity.setPaymentType(CustomerCurrency.PAYMENT_TYPE_TRANSFER_ACCOUNT);  //20 转帐
        String strQuarter = DateUtils.getYear() + DateUtils.getSeason();
        entity.setQuarter(strQuarter);
        entity.setCreateBy(user);
        entity.setUpdateDate(date);
        entity.setUpdateBy(user);
        entity.setRemarks(strPayType+"线下充值");
        entity.setCreateDate(date);
        entity.setCreateBy(user);
        customerCurrencyDao.insert(entity);
        customerFinance.setTransactionAmount(customerOfflineRecharge.getFinallyAmount());
        customerFinance.setUpdateBy(user);
        customerFinance.setUpdateDate(date);
        customerFinanceDao.updateBalance(customerFinance);
        try {
            String strDate = DateUtils.formatDate(new Date(customerOfflineRecharge.getCreateAt()),"M月d日HH:mm");
            StringBuffer strContent = new StringBuffer();
            strContent.append("【快可立】您在");
            strContent.append(strDate+strPayType+"扫码充值"+customerOfflineRecharge.getPendingAmount()+"元审核通过");
            strContent.append("，实际到账"+customerOfflineRecharge.getActualAmount()+"元，已充值到您的账户金额为：" + customerOfflineRecharge.getFinallyAmount()+"元");
            smsMQSender.sendNew(customerOfflineRecharge.getPhone(),strContent.toString(),"",user.getId(),date.getTime(), SysSMSTypeEnum.OFFLINE_RECHARGE);
        }catch (Exception e){
            log.error("CustomerOfflineRechargeService.approved:客户下单审核通过发送短信失败:{}",e.getMessage());
        }
    }

    /**
     *  审核无效
     * @param customerOfflineRecharge
     * @param user
     * */
    public void invalid(CustomerOfflineRecharge customerOfflineRecharge,User user){
       MSResponse<Integer> msResponse = customerOfflineRechargeFeign.invalid(customerOfflineRecharge);
       if(!MSResponse.isSuccessCode(msResponse)){
           throw new RuntimeException("审核无效失败：" + msResponse.getMsg());
       }
        try {
            String strPayType="";
            if(customerOfflineRecharge.getPayType().equals(CustomerOfflineRecharge.ALIPAY_TYPE)){
                strPayType="使用支付宝";
            }else if(customerOfflineRecharge.getPayType().equals(CustomerOfflineRecharge.WECHAT_TYPE)){
                strPayType="使用微信";
            }
            StringBuffer strContent = new StringBuffer();
            strContent.append("【快可立】您在");
            String strDate = DateUtils.formatDate(new Date(customerOfflineRecharge.getCreateAt()),"M月d日HH:mm");
            strContent.append(strDate+strPayType+"扫码充值"+customerOfflineRecharge.getPendingAmount()+"元审核无效");
            if(customerOfflineRecharge.getInvalidType()!=20){
                Dict dict = MSDictUtils.getDictByValue(customerOfflineRecharge.getInvalidType().toString(),"recharge_invalid_type");
                if(dict!=null){
                    strContent.append("，"+dict.getLabel());
                }
            }
            if(StringUtils.isNotBlank(customerOfflineRecharge.getInvalidReason())){
                strContent.append("，"+customerOfflineRecharge.getInvalidReason());
            }
            smsMQSender.sendNew(customerOfflineRecharge.getPhone(),strContent.toString(),"",user.getId(),new Date().getTime(), SysSMSTypeEnum.OFFLINE_RECHARGE);
        }catch (Exception e){
            log.error("CustomerOfflineRechargeService.invalid:客户下单审核无效发送短信失败:{}",e.getMessage());
        }
    }

    /**
     * 计算返现金额
     * @param amounts 金额
     * @param dictList 返现比率集合
     * */
    public double getMoneyBack(double amounts,List<Dict> dictList){
        double money = 0.0;
        dictList = dictList.stream().sorted(Comparator.comparing(t->Double.valueOf(t.getValue()))).collect(Collectors.toList());
        for(int i=0;i<dictList.size();i++){
            if(i==dictList.size()-1 && Double.valueOf(dictList.get(i).getValue())<=amounts){
                Dict dict = dictList.get(i);
                if(dict!=null){
                    //money = CurrencyUtil.round2(Double.valueOf(dict.getLabel())*amounts);
                    money = NumberUtils.doubleScale(Double.valueOf(dict.getLabel())*amounts,2);
                }
                break;
            }else if (Double.valueOf(dictList.get(i).getValue())<=amounts && amounts<Double.valueOf(dictList.get(i+1).getValue())){
                Dict dict =dictList.get(i);
                if(dict!=null){
                    //money = CurrencyUtil.round2(Double.valueOf(dict.getLabel())*amounts);
                    money = NumberUtils.doubleScale(Double.valueOf(dict.getLabel())*amounts,2);
                }
                break;
            }
        }
        return money;
    }

    /**
    * 小数点后为零显示整数，否则保留
    */
    public static String rvZeroAndDot(double num){
        if(num % 1.0 == 0)
        {
            return String.valueOf((long)num);
        }

        return String.valueOf(num);
    }

}

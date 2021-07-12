package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BCustomerMapping;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sd.service.OrderMQService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.b2bcenter.exception.B2BOrderExistsException;
import com.wolfking.jeesite.ms.b2bcenter.md.utils.B2BMDUtils;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class B2BCenterOrderTransferService extends B2BOrderAutoBaseService {

    @Autowired
    private B2BCenterOrderService b2BCenterOrderService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMQService orderMQService;


    public B2BOrderVModel getB2BOrderVModel(MQB2BOrderMessage.B2BOrderMessage b2BOrderMsg) {
        B2BOrderVModel b2BOrderVModel = new B2BOrderVModel();
        b2BOrderVModel.setId(b2BOrderMsg.getId());
        b2BOrderVModel.setB2bOrderId(b2BOrderMsg.getId());
        List<TwoTuple<String, String>> b2bProductCodeAndSpes = b2BOrderMsg.getB2BOrderItemList().stream()
                .filter(i -> StringUtils.isNotBlank(i.getProductCode()))
                .map(i->new TwoTuple<>(i.getProductCode(), i.getProductSpec())).collect(Collectors.toList());
        String shopId = getShopId(b2BOrderMsg.getDataSource(), b2BOrderMsg.getShopId(), b2bProductCodeAndSpes);//京东的店铺需要从产品映射中获取
        if (b2BOrderMsg.getProcessFlag() > 0) {
            b2BOrderVModel.setProcessFlag(b2BOrderMsg.getProcessFlag());
        }
        if (b2BOrderMsg.getDataSource() > 0) {
            b2BOrderVModel.setDataSource(b2BOrderMsg.getDataSource());
            Map<String, B2BCustomerMapping> map = B2BMDUtils.getCustomerMappingMap(B2BDataSourceEnum.get(b2BOrderMsg.getDataSource()));
            if (map != null && map.size() > 0) {
                B2BCustomerMapping b2bCustomerMapping = map.get(shopId);
                if (b2bCustomerMapping != null) {
                    Customer customer = customerService.getFromCache(b2bCustomerMapping.getCustomerId());
                    b2BOrderVModel.setCustomer(customer);
                    b2BOrderVModel.setCustomerMapping(b2bCustomerMapping);
                }
            }
        }

        String defaultShopId = B2BMDUtils.getDefaultShopId(b2BOrderMsg.getDataSource());
        b2BOrderVModel.setDefaultShopId(defaultShopId);

        if (StringUtils.isNotBlank(b2BOrderMsg.getOrderNo())) {
            b2BOrderVModel.setOrderNo(b2BOrderMsg.getOrderNo());
        }
        //add by ryan at 2018/09/21
        //parentBizOrderId,消息中没有值，和orderNo相同
        if (StringUtils.isNotBlank(b2BOrderMsg.getParentBizOrderId())) {
            b2BOrderVModel.setParentBizOrderId(b2BOrderMsg.getParentBizOrderId());
        } else {
            b2BOrderVModel.setParentBizOrderId(b2BOrderVModel.getOrderNo());
        }

        if (StringUtils.isNotBlank(shopId)) {
            b2BOrderVModel.setShopId(shopId);
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserName())) {
            b2BOrderVModel.setUserName(b2BOrderMsg.getUserName());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserMobile())) {
            b2BOrderVModel.setUserMobile(b2BOrderMsg.getUserMobile());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserPhone())) {
            b2BOrderVModel.setUserPhone(b2BOrderMsg.getUserPhone());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserAddress())) {
            b2BOrderVModel.setUserAddress(b2BOrderMsg.getUserAddress());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserProvince())) {
            b2BOrderVModel.setUserProvince(b2BOrderMsg.getUserProvince());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserCity())) {
            b2BOrderVModel.setUserCity(b2BOrderMsg.getUserCity());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserCounty())) {
            b2BOrderVModel.setUserCounty(b2BOrderMsg.getUserCounty());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getUserStreet())) {
            b2BOrderVModel.setUserStreet(b2BOrderMsg.getUserStreet());
        }
        if (b2BOrderMsg.getReceiveDate() > 0) {
            b2BOrderVModel.setReceiveDate(b2BOrderMsg.getReceiveDate());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getBrand())) {
            b2BOrderVModel.setBrand(b2BOrderMsg.getBrand());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getServiceType())) {
            b2BOrderVModel.setServiceType(b2BOrderMsg.getServiceType());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getWarrantyType())) {
            b2BOrderVModel.setWarrantyType(b2BOrderMsg.getWarrantyType());
        }
        b2BOrderVModel.setStatus(b2BOrderMsg.getStatus());
        if (b2BOrderMsg.getB2BOrderItemList() != null && b2BOrderMsg.getB2BOrderItemList().size() > 0) {
            B2BOrderVModel.B2BOrderItem b2BOrderItem = null;
            List<B2BOrderVModel.B2BOrderItem> b2BOrderItemList = new ArrayList<>();
            for (MQB2BOrderMessage.B2BOrderItem item : b2BOrderMsg.getB2BOrderItemList()) {
                b2BOrderItem = new B2BOrderVModel.B2BOrderItem();
                b2BOrderItem.setProductName(item.getProductName());
                //TODO: 客户产品型号转换成大写
//                b2BOrderItem.setProductSpec(item.getProductSpec());
                b2BOrderItem.setProductSpec(StringUtils.toString(item.getProductSpec()).toUpperCase());
                b2BOrderItem.setProductCode(item.getProductCode());
                b2BOrderItem.setQty(item.getQty());
                b2BOrderItem.setServiceType(item.getServiceType());
                b2BOrderItem.setWarrantyType(item.getWarrantyType());
                b2BOrderItem.setB2bWarrantyCode(item.getB2BWarrantyCode());
                b2BOrderItem.setBrand(item.getBrand());
                //图片 2020-08-06
                if(item.getPicsCount()>0){
                    b2BOrderItem.setPics(item.getPicsList());
                }
                b2BOrderItemList.add(b2BOrderItem);
                b2BOrderItem.setExpressCompany(item.getExpressCompany());
                b2BOrderItem.setExpressNo(item.getExpressNo());
            }
            b2BOrderVModel.setItems(b2BOrderItemList);
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getDescription())) {
            b2BOrderVModel.setDescription(b2BOrderMsg.getDescription());
        }
        b2BOrderVModel.setProcessTime(b2BOrderMsg.getProcessTime());
        if (StringUtils.isNotBlank(b2BOrderMsg.getProcessComment())) {
            b2BOrderVModel.setProcessComment(b2BOrderMsg.getProcessComment());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getQuarter())) {
            b2BOrderVModel.setQuarter(b2BOrderMsg.getQuarter());
        }
        if (StringUtils.isNotBlank(b2BOrderMsg.getIssueBy())) {
            b2BOrderVModel.setIssueBy(b2BOrderMsg.getIssueBy());
        } else {
            b2BOrderVModel.setIssueBy("");
        }
        b2BOrderVModel.setEstimatedReceiveDate(StringUtils.toString(b2BOrderMsg.getEstimatedReceiveDate()));
        b2BOrderVModel.setBuyDate(b2BOrderMsg.getBuyDate());
        b2BOrderVModel.setExpectServiceTime(StringUtils.toString(b2BOrderMsg.getExpectServiceTime()));
        //TODO: B2B下单人
//        b2BOrderVModel.setCreateById(b2BOrderMsg.getCreateById());
        Long b2bCreateById = getB2BOrderCreateBy(b2BOrderMsg.getDataSource(), b2BOrderVModel.getShopId(), b2BOrderMsg.getCreateById());
        b2BOrderVModel.setCreateById(b2bCreateById);
        if (StringUtils.isNotBlank(b2BOrderMsg.getKklOrderNo())) {
            b2BOrderVModel.setKklOrderNo(b2BOrderMsg.getKklOrderNo());
        }
        b2BOrderVModel.setCreateDt(b2BOrderMsg.getCreateDt());
        b2BOrderVModel.setSaleChannel(b2BOrderMsg.getSaleChannel());

        b2BOrderVModel.setSiteCode(b2BOrderMsg.getSiteCode());
        b2BOrderVModel.setSiteName(b2BOrderMsg.getSiteName());
        b2BOrderVModel.setEngineerName(b2BOrderMsg.getEngineerName());
        b2BOrderVModel.setEngineerMobile(b2BOrderMsg.getEngineerMobile());
        b2BOrderVModel.setOrderDataSource(b2BOrderMsg.getOrderDataSource());

        return b2BOrderVModel;
    }


    /**
     * 处理新单消息
     */
    public void processB2BOrderMessage(MQB2BOrderMessage.B2BOrderMessage b2BOrderMsg, User user) {
        if (B2BDataSourceEnum.isB2BDataSource(b2BOrderMsg.getDataSource())) {
            B2BOrderTransferResult conversionProgress = new B2BOrderTransferResult();
            B2BOrderVModel orderVModel = getB2BOrderVModel(b2BOrderMsg);
            conversionProgress.setId(orderVModel.getId());
            conversionProgress.setB2bOrderId(orderVModel.getB2bOrderId());
            Order order = null;
            try {
                order = toOrderAuto(orderVModel, user);
                OrderCondition orderCondition = order.getOrderCondition();
                String repeatedOrderNo = orderService.getRepeateOrderNo(orderCondition.getCustomer().getId(), orderCondition.getPhone1());
                if (StringUtils.isNotBlank(repeatedOrderNo)) {
                    order.setRepeateNo(repeatedOrderNo);
                }
                orderService.createOrder_v2_1(order, null);
                if (order.getCreateBy() == null || order.getCreateBy().getId() == null || order.getCreateBy().getId() <= 0) {
                    order.setCreateBy(user);
                }
                orderMQService.sendCreateOrderMessage(order, "B2BCenterOrderTransferService.processB2BOrderMessage");
                conversionProgress.setOrderId(order.getId());
                conversionProgress.setKklOrderNo(order.getOrderNo());
                conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                conversionProgress.setProcessComment("转单成功(自动转单)");
            } catch (B2BOrderExistsException e1) {
                conversionProgress.setOrderId(e1.getOrderId());
                conversionProgress.setKklOrderNo(e1.getOrderNo());
                conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                conversionProgress.setProcessComment(e1.getLocalizedMessage());
            } catch (Exception e2) {
                conversionProgress.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                conversionProgress.setProcessComment(StringUtils.left(e2.getLocalizedMessage(), 200));
            }
            conversionProgress.setDataSource(b2BOrderMsg.getDataSource());
            conversionProgress.setB2bOrderNo(orderVModel.getOrderNo());
            conversionProgress.setB2bQuarter(orderVModel.getQuarter());
            conversionProgress.setUpdater(user.getName());
            conversionProgress.setUpdateDt(System.currentTimeMillis());
            List<B2BOrderTransferResult> conversionProgressList = Lists.newArrayList(conversionProgress);
            b2BCenterOrderService.updateB2BOrderConversionProgress(B2BDataSourceEnum.get(b2BOrderMsg.getDataSource()), conversionProgressList, user);
            if (order == null || order.getId() == null) {
                String msgJson = new JsonFormat().printToString(b2BOrderMsg);
                LogUtils.saveLog("B2B自动转单更新进度失败", "B2BCenterOrderTransferService.processB2BOrderMessage", msgJson, null, user);
            }
        }
    }

}

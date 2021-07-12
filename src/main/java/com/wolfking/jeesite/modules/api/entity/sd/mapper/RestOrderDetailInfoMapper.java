package com.wolfking.jeesite.modules.api.entity.sd.mapper;

import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderDetail;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderDetailInfo;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrderItem;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.*;
import com.wolfking.jeesite.modules.sd.utils.OrderPicUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 订单与API订单详情模型转换
 */
@Component
public class RestOrderDetailInfoMapper extends CustomMapper<RestOrderDetailInfo, Order>{

    @Autowired
    private ServiceTypeService serviceTypeService;

    @Override
    public void mapAtoB(RestOrderDetailInfo a, Order b, MappingContext context) {

    }

    @Override
    public void mapBtoA(Order b, RestOrderDetailInfo a, MappingContext context) {
        a.setDataSource(b.getDataSourceId());
        OrderCondition condition = b.getOrderCondition();
        a.setOrderId(condition.getOrderId());
        a.setQuarter(condition.getQuarter());
        a.setOrderNo(condition.getOrderNo());
        a.setUserName(condition.getUserName());
        a.setServicePhone(condition.getServicePhone());
        //a.setServiceAddress(condition.getArea().getName().concat(" ").concat(condition.getServiceAddress()));
        a.setServiceAddress(AreaUtils.getCountyName(condition.getArea().getId()) + condition.getServiceAddress());
        a.setAreaName(AreaUtils.getCountyFullName(condition.getArea().getId()));
        a.setSubAddress(condition.getServiceAddress());
//        a.setServiceAddress(AreaUtils.getCountyName(condition.getArea().getId()) + AreaUtils.getTownName(condition.getArea().getId(), condition.getSubArea().getId()) + condition.getServiceAddress());
        a.setAcceptDate(b.getOrderStatus().getAcceptDate());
        a.setAppointDate(condition.getAppointmentDate());
        a.setStatus(condition.getStatus());
        a.setEngineer(condition.getEngineer());
        a.setRemarks("");
        if(condition.getKefu() != null && StringUtils.isNotBlank(condition.getKefu().getPhone())) {
            a.setKefuPhone(condition.getKefu().getPhone());//2018/01/12
        }
        a.setAreaId(condition.getArea().getId().toString());
        a.setAppCompleteType(condition.getAppCompleteType());
        a.setOrderServiceType(condition.getOrderServiceType());
        a.setAppAbnormalyFlag(condition.getAppAbnormalyFlag());
        //a.setIsComplained(condition.getIsComplained()>0?1:0);//2018/04/13
        // 2019-08-29 投诉标识转移到orderStatus
        if(b.getOrderStatus() != null && b.getOrderStatus().getComplainFlag() != null){
            a.setIsComplained(b.getOrderStatus().getComplainFlag()>0?1:0);
        }
        //a.setReminderFlag(condition.getReminderFlag());//催单标识 0：无催单 19/07/09
        if(b.getOrderStatus() != null && b.getOrderStatus().getReminderStatus() != null) {
            a.setReminderFlag(b.getOrderStatus().getReminderStatus());//催单标识 2019/08/15
        }
        Set<String> sets = Sets.newHashSet("0","2","3");
        if(condition.getPendingType() !=null
                && StringUtils.isNotBlank(condition.getPendingType().getValue())
                && !sets.contains(condition.getPendingType().getValue())
                && condition.getAppointmentDate() != null
                && DateUtils.pastMinutes(condition.getAppointmentDate())<0 ){
            a.setPendingFlag(1);
        }else{
            a.setPendingFlag(0);
        }
        try {
            Dict orderServiceType = MSDictUtils.getDictByValue(String.valueOf(condition.getOrderServiceType()), "order_service_type");//切换为微服务
            if(orderServiceType != null){
                a.setOrderServiceTypeName(orderServiceType.getLabel());
            }
        }catch (Exception e){
            LogUtils.saveLog("读取订单服务类型错误","RestOrderDetailInfoMapper.mapBtoA",String.valueOf(condition.getOrderServiceType()),null,null,2);
        }
        if (condition.getUrgentLevel() != null && condition.getUrgentLevel().getId() != null) {
            a.setUrgentLevelId(condition.getUrgentLevel().getId());
        }
        //remarks
        //int idx = 0;
        //StringBuffer buf = new StringBuffer(255);
        //for (OrderItem item:b.getItems()){
            //if(idx>0){
            //    buf.append(" ,");
            //}
            //buf.append(String.format("%s  %s x%d\r\n",item.getProduct().getName(),item.getServiceType().getName(),item.getQty()));
        //}
        //a.setRemarks(buf.toString());
        //buf.setLength(0);
        a.setDescription(Encodes.unescapeHtml(b.getDescription()));//服务描述
        a.setRemarks(Encodes.unescapeHtml(b.getOrderCondition().getCustomer().getRemarks()));//厂商备注
        a.setPartsFlag(condition.getPartsFlag());
        a.setFinishPhotoQty(condition.getFinishPhotoQty());
        a.setOrderServiceType(condition.getOrderServiceType());
        a.setServiceTimes(condition.getServiceTimes());
        List<MaterialMaster> materials = b.getMaterials();
        if(null == materials || 0 == materials.size()){
            a.setPartsStatus(0);
        }else{
            //long count = materials.stream().filter(t->t.getApplyId().longValue()==0 && StringUtils.inString(t.getStatus().getValue(),new String[]{"1","2"})).count();//返件单独立前
            //配件与返件单资料分开存取
            //long count = materials.stream().filter(t->StringUtils.inString(t.getStatus().getValue(),new String[]{"1","2"})).count();
            long count = materials.stream().filter(t-> ArrayUtil.contains(new String[]{"1","2"},t.getStatus().getValue())).count();
            if(count>0){
                a.setPartsStatus(1);
            }else{
                //返件
                count = materials.stream().filter(t->t.getApplyId()>0 && t.getExpressCompany().getValue() == "").count();
                if(count>0){
                    a.setPartsStatus(1);
                }else {
                    a.setPartsStatus(2);
                }
            }
        }
        //完成照片
        List<OrderAttachment> photos = Lists.newArrayList();
        if(b.getAttachments() != null && b.getAttachments().size()>0){
            String host = Global.getConfig("userfiles.host")+"/";
            b.getAttachments().forEach(t->{
                t.setFilePath(host+t.getFilePath());
                photos.add(t);
            });
        }
        a.setPhotos(photos);
        a.setPhotoMaxQty(condition.getCustomer().getMaxUploadNumber());//前提读订单时要单独取次客户
        a.setPhotoMinQty(condition.getCustomer().getMinUploadNumber());//前提读订单时要单独取次客户
        //items
        RestOrderItem ritem;
        //mark on 2019-10-12
        //List<ServiceType> serviceTypes = serviceTypeService.findAllList();
        //调用微服务获取所有服务类型 对象返回 id,code，name warrantyStatus值 start 2019-10-12
        List<ServiceType> serviceTypes = serviceTypeService.findAllListIdsAndNamesAndCodes();
        // end
        if(serviceTypes != null && serviceTypes.size()==0){
            serviceTypes = null;
        }
        ServiceType serviceType;
        for(OrderItem item:b.getItems()){
            ritem = new RestOrderItem();
            ritem.setItemNo(item.getItemNo().toString());
            ritem.setProductId(item.getProduct().getId());
            ritem.setProductName(item.getProduct().getName());
            ritem.setProductSpec(item.getProductSpec());
            ritem.setBrand(item.getBrand());
            ritem.setQty(item.getQty());
            if(serviceTypes !=null){
                serviceType = serviceTypes.stream().filter(t->t.getId().equals(item.getServiceType().getId())).findFirst().orElse(item.getServiceType());
                ritem.setServiceTypeId(serviceType.getId());
                ritem.setServiceTypeName(serviceType.getName());
                if(serviceType.getWarrantyStatus() == null || StringUtils.isBlank(serviceType.getWarrantyStatus().getValue())){
                    ritem.setWarrantyStatus("读取错误");
                }else {
                    if (serviceType.getWarrantyStatus().getValue().equalsIgnoreCase("IW")) {
                        ritem.setWarrantyStatus("保内");
                    } else {
                        ritem.setWarrantyStatus("保外");
                    }
                }
            }else {
                ritem.setServiceTypeId(item.getServiceType().getId());
                ritem.setServiceTypeName(item.getServiceType().getName());
                ritem.setWarrantyStatus("读取错误");
            }

            ritem.setUnit(item.getProduct().getSetFlag()==1?"套":"台");
            ritem.setRemarks(item.getRemarks());
            List<RestOrderItem.PicItem> picItems = Lists.newArrayList();
            if (item.getPics() != null && !item.getPics().isEmpty()) {
                for (String picUrl : item.getPics()) {
                    if (StringUtils.isNotBlank(picUrl)) {
                        picItems.add(new RestOrderItem.PicItem(OrderPicUtils.getPicUrl(picUrl)));
                    }
                }
                ritem.setPics(picItems);
            }
            a.getItems().add(ritem);
        }
        //services
        if(b.getDetailList()!=null && b.getDetailList().size()>0){
            a.setServiceFlag(1);//有上门服务
            RestOrderDetail detail;
            for(OrderDetail m:b.getDetailList()){
                if(m.getDelFlag() != 0){
                    continue;
                }
                detail = new RestOrderDetail()
                        .setId(String.valueOf(m.getId()))
                        .setQuarter(b.getQuarter())
                        .setServiceTimes(m.getServiceTimes())
                        .setOrderId(String.valueOf(m.getOrderId()))
                        .setProductId(String.valueOf(m.getProduct().getId()))
                        .setProductName(m.getProduct().getName())
                        .setQty(m.getQty())
                        .setUnit(m.getProduct().getSetFlag()==1?"套":"台")
                        .setServiceTypeName(m.getServiceType().getName())
                        .setServicePointId(m.getServicePoint().getId())
                        .setEngineerId(m.getEngineer().getId())
                        .setEngineerExpressCharge(m.getEngineerExpressCharge())
                        .setEngineerMaterialCharge(m.getEngineerMaterialCharge())
                        .setEngineerTravelCharge(m.getEngineerTravelCharge())
                        .setTravelNo(m.getTravelNo())
                        .setEngineerServiceCharge(m.getEngineerServiceCharge())
                        .setEngineerChage(m.getEngineerChage())
                        .setEngineerOtherCharge(m.getEngineerOtherCharge())
                        .setRemarks(m.getRemarks());
                //维修
                //服务类型
                Dict serviceCategory = Optional.ofNullable(m.getServiceCategory())
                        .filter(t->StringUtils.isNotBlank(t.getValue()))
                        .orElseGet(() -> {
                            return new Dict("0","");
                        });
                detail.setServiceCategoryId(StringUtils.toLong(serviceCategory.getValue()));
                detail.setServiceCategoryName(StringUtils.toString(serviceCategory.getLabel()));
                //故障类型，故障现象，故障处理
                detail.setErrorTypeName(Optional.ofNullable(m.getErrorType()).map(t->t.getName()).orElse(StringUtils.EMPTY));
                detail.setErrorCodeName(Optional.ofNullable(m.getErrorCode()).map(t->t.getName()).orElse(StringUtils.EMPTY));
                detail.setActionCodeName(Optional.ofNullable(m.getActionCode()).map(t->t.getName()).orElse(StringUtils.EMPTY));
                //}
                detail.setOtherActionRemark(StringUtils.toString(m.getOtherActionRemark()));
                //判断是否已添加维修故障信息
                detail.setHasRepaired(0);
                //维修
                if(serviceCategory.getIntValue() == 2){
                    if(m.getErrorType().getId() > 0) {
                        detail.setHasRepaired(1);
                    }else{
                        if(StringUtils.isNotBlank(detail.getOtherActionRemark())){
                            detail.setHasRepaired(1);
                        }
                    }
                }
                a.getServices().add(detail);
            }
            //涉及多个网点，费用统计放在外层处理
        }
        //products
        a.setProducts(b.getProducts());
        if (b.getOrderAdditionalInfo() != null && StringUtils.isNotBlank(b.getOrderAdditionalInfo().getEstimatedReceiveDate())) {
            a.setEstimatedReceiveDate(b.getOrderAdditionalInfo().getEstimatedReceiveDate());
            a.setExpectServiceTime(StringUtils.toString(b.getOrderAdditionalInfo().getExpectServiceTime()));
        }
        a.setArrivalDate(condition.getArrivalDate() == null ? 0 : condition.getArrivalDate().getTime());
        a.setSuspendFlag(condition.getSuspendFlag() == null ? 0 : condition.getSuspendFlag());
        a.setSuspendType(condition.getSuspendType() == null ? 0 : condition.getSuspendType());
    }
}

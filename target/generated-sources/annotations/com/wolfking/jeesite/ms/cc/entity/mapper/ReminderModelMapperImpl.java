package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.kkl.kklplus.entity.cc.vm.ReminderListModel;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import java.util.Map;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-06-11T10:55:25+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class ReminderModelMapperImpl extends ReminderModelMapper {

    @Override
    public ReminderModel toViewModel(ReminderListModel model, Map<Long, ServicePoint> spMap, Map<Long, Customer> customerMap, Map<Integer, String> dataSourceMap, Map<Long, Area> areaMap) {
        if ( model == null && spMap == null && customerMap == null && dataSourceMap == null && areaMap == null ) {
            return null;
        }

        ReminderModel reminderModel = new ReminderModel();

        if ( model != null ) {
            reminderModel.setId( model.getId() );
            reminderModel.setB2bReminderId( model.getB2bReminderId() );
            if ( model.getB2bReminderNo() != null ) {
                reminderModel.setB2bReminderNo( model.getB2bReminderNo() );
            }
            if ( model.getReminderNo() != null ) {
                reminderModel.setReminderNo( model.getReminderNo() );
            }
            reminderModel.setReminderTimes( model.getReminderTimes() );
            reminderModel.setOrderId( model.getOrderId() );
            if ( model.getQuarter() != null ) {
                reminderModel.setQuarter( model.getQuarter() );
            }
            reminderModel.setDataSource( model.getDataSource() );
            reminderModel.setReminderType( model.getReminderType() );
            if ( model.getReminderReason() != null ) {
                reminderModel.setReminderReason( model.getReminderReason() );
            }
            if ( model.getOrderNo() != null ) {
                reminderModel.setOrderNo( model.getOrderNo() );
            }
            reminderModel.setStatus( model.getStatus() );
            reminderModel.setProvinceId( model.getProvinceId() );
            reminderModel.setCityId( model.getCityId() );
            reminderModel.setAreaId( model.getAreaId() );
            reminderModel.setSubAreaId( model.getSubAreaId() );
            reminderModel.setCustomerId( model.getCustomerId() );
            reminderModel.setServicepointId( model.getServicepointId() );
            if ( model.getUserName() != null ) {
                reminderModel.setUserName( model.getUserName() );
            }
            if ( model.getUserPhone() != null ) {
                reminderModel.setUserPhone( model.getUserPhone() );
            }
            if ( model.getUserAddress() != null ) {
                reminderModel.setUserAddress( model.getUserAddress() );
            }
            reminderModel.setProductCategoryId( model.getProductCategoryId() );
            reminderModel.setOrderStatus( model.getOrderStatus() );
            reminderModel.setOrderCloseAt( model.getOrderCloseAt() );
            reminderModel.setCompleteAt( model.getCompleteAt() );
            if ( model.getCompleteBy() != null ) {
                reminderModel.setCompleteBy( model.getCompleteBy() );
            }
            if ( model.getCompleteRemark() != null ) {
                reminderModel.setCompleteRemark( model.getCompleteRemark() );
            }
            reminderModel.setOrderTimeLiness( model.getOrderTimeLiness() );
            reminderModel.setItemNo( model.getItemNo() );
            reminderModel.setTimeoutAt( model.getTimeoutAt() );
            reminderModel.setTimeoutFlag( model.getTimeoutFlag() );
            reminderModel.setCreateAt( model.getCreateAt() );
            if ( model.getCreateName() != null ) {
                reminderModel.setCreateName( model.getCreateName() );
            }
            if ( model.getCreateRemark() != null ) {
                reminderModel.setCreateRemark( model.getCreateRemark() );
            }
            reminderModel.setProcessAt( model.getProcessAt() );
            if ( model.getProcessName() != null ) {
                reminderModel.setProcessName( model.getProcessName() );
            }
            if ( model.getProcessRemark() != null ) {
                reminderModel.setProcessRemark( model.getProcessRemark() );
            }
            reminderModel.setProcessTimeLiness( model.getProcessTimeLiness() );
            reminderModel.setItemId( model.getItemId() );
            reminderModel.setItemStatus( model.getItemStatus() );
            reminderModel.setOrderCompleteTimeoutAt( model.getOrderCompleteTimeoutAt() );
            reminderModel.setHandleTimeLiness( model.getHandleTimeLiness() );
        }

        after( reminderModel, model, spMap, customerMap, dataSourceMap, areaMap );

        return reminderModel;
    }
}

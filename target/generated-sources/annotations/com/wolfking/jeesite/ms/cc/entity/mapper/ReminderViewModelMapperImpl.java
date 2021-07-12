package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.kkl.kklplus.entity.cc.Reminder;
import com.wolfking.jeesite.ms.cc.entity.ReminderModel;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-06-11T10:55:25+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class ReminderViewModelMapperImpl extends ReminderViewModelMapper {

    @Override
    public ReminderModel toViewModel(Reminder model) {
        if ( model == null ) {
            return null;
        }

        ReminderModel reminderModel = new ReminderModel();

        if ( model.getReminderRemark() != null ) {
            reminderModel.setCreateRemark( model.getReminderRemark() );
        }
        if ( model.getCreateDt() != null ) {
            reminderModel.setCreateAt( model.getCreateDt() );
        }
        if ( model.getProcessBy() != null ) {
            reminderModel.setProcessName( model.getProcessBy() );
        }
        if ( model.getId() != null ) {
            reminderModel.setId( model.getId() );
        }
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
        reminderModel.setTimeoutAt( model.getTimeoutAt() );
        reminderModel.setTimeoutFlag( model.getTimeoutFlag() );
        reminderModel.setProcessAt( model.getProcessAt() );
        if ( model.getProcessRemark() != null ) {
            reminderModel.setProcessRemark( model.getProcessRemark() );
        }
        reminderModel.setProcessTimeLiness( model.getProcessTimeLiness() );
        reminderModel.setItemId( model.getItemId() );
        reminderModel.setOrderCompleteTimeoutAt( model.getOrderCompleteTimeoutAt() );
        reminderModel.setHandleTimeLiness( model.getHandleTimeLiness() );

        after( reminderModel, model );

        return reminderModel;
    }

    @Override
    public List<ReminderModel> reminderToViewModels(List<Reminder> reminders) {
        if ( reminders == null ) {
            return null;
        }

        List<ReminderModel> list = new ArrayList<ReminderModel>( reminders.size() );
        for ( Reminder reminder : reminders ) {
            list.add( toViewModel( reminder ) );
        }

        return list;
    }
}

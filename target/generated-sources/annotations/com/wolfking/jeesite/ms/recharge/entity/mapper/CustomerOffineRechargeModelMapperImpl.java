package com.wolfking.jeesite.ms.recharge.entity.mapper;

import com.kkl.kklplus.entity.fi.recharge.CustomerOfflineRecharge;
import com.wolfking.jeesite.ms.recharge.entity.CustomerOfflineRechargeModel;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:41+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class CustomerOffineRechargeModelMapperImpl extends CustomerOffineRechargeModelMapper {

    @Override
    public CustomerOfflineRechargeModel toViewModel(CustomerOfflineRecharge model) {
        if ( model == null ) {
            return null;
        }

        CustomerOfflineRechargeModel customerOfflineRechargeModel = new CustomerOfflineRechargeModel();

        if ( model.getId() != null ) {
            customerOfflineRechargeModel.setId( model.getId() );
        }
        if ( model.getRemarks() != null ) {
            customerOfflineRechargeModel.setRemarks( model.getRemarks() );
        }
        if ( model.getCreateBy() != null ) {
            customerOfflineRechargeModel.setCreateBy( model.getCreateBy() );
        }
        if ( model.getCreateById() != null ) {
            customerOfflineRechargeModel.setCreateById( model.getCreateById() );
        }
        if ( model.getCreateDate() != null ) {
            customerOfflineRechargeModel.setCreateDate( model.getCreateDate() );
        }
        if ( model.getUpdateBy() != null ) {
            customerOfflineRechargeModel.setUpdateBy( model.getUpdateBy() );
        }
        if ( model.getUpdateById() != null ) {
            customerOfflineRechargeModel.setUpdateById( model.getUpdateById() );
        }
        if ( model.getUpdateDate() != null ) {
            customerOfflineRechargeModel.setUpdateDate( model.getUpdateDate() );
        }
        if ( model.getDelFlag() != null ) {
            customerOfflineRechargeModel.setDelFlag( model.getDelFlag() );
        }
        if ( model.getPage() != null ) {
            customerOfflineRechargeModel.setPage( model.getPage() );
        }
        if ( model.getCustomerId() != null ) {
            customerOfflineRechargeModel.setCustomerId( model.getCustomerId() );
        }
        if ( model.getPayType() != null ) {
            customerOfflineRechargeModel.setPayType( model.getPayType() );
        }
        if ( model.getAlipayAccount() != null ) {
            customerOfflineRechargeModel.setAlipayAccount( model.getAlipayAccount() );
        }
        if ( model.getTransferNo() != null ) {
            customerOfflineRechargeModel.setTransferNo( model.getTransferNo() );
        }
        if ( model.getPendingAmount() != null ) {
            customerOfflineRechargeModel.setPendingAmount( model.getPendingAmount() );
        }
        if ( model.getActualAmount() != null ) {
            customerOfflineRechargeModel.setActualAmount( model.getActualAmount() );
        }
        if ( model.getFinallyAmount() != null ) {
            customerOfflineRechargeModel.setFinallyAmount( model.getFinallyAmount() );
        }
        if ( model.getBackAmount() != null ) {
            customerOfflineRechargeModel.setBackAmount( model.getBackAmount() );
        }
        if ( model.getStatus() != null ) {
            customerOfflineRechargeModel.setStatus( model.getStatus() );
        }
        if ( model.getInvalidType() != null ) {
            customerOfflineRechargeModel.setInvalidType( model.getInvalidType() );
        }
        if ( model.getInvalidReason() != null ) {
            customerOfflineRechargeModel.setInvalidReason( model.getInvalidReason() );
        }
        if ( model.getPhone() != null ) {
            customerOfflineRechargeModel.setPhone( model.getPhone() );
        }
        if ( model.getSite() != null ) {
            customerOfflineRechargeModel.setSite( model.getSite() );
        }
        if ( model.getCreateAt() != null ) {
            customerOfflineRechargeModel.setCreateAt( model.getCreateAt() );
        }
        if ( model.getUpdateAt() != null ) {
            customerOfflineRechargeModel.setUpdateAt( model.getUpdateAt() );
        }

        after( customerOfflineRechargeModel, model );

        return customerOfflineRechargeModel;
    }

    @Override
    public List<CustomerOfflineRechargeModel> toViewModels(List<CustomerOfflineRecharge> models) {
        if ( models == null ) {
            return null;
        }

        List<CustomerOfflineRechargeModel> list = new ArrayList<CustomerOfflineRechargeModel>( models.size() );
        for ( CustomerOfflineRecharge customerOfflineRecharge : models ) {
            list.add( toViewModel( customerOfflineRecharge ) );
        }

        return list;
    }
}

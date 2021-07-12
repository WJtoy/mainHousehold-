package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.sd.entity.MaterialMaster;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.entity.OrderAttachment;
import com.wolfking.jeesite.modules.sd.entity.OrderDetail;
import com.wolfking.jeesite.modules.sd.entity.OrderGrade;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.OrderProcessLog;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePointFee;
import com.wolfking.jeesite.ms.cc.entity.OrderReminderVM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:40+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class OrderReminderVMMapperImpl implements OrderReminderVMMapper {

    @Override
    public OrderReminderVM toReminderModel(Order model) {
        if ( model == null ) {
            return null;
        }

        OrderReminderVM orderReminderVM = new OrderReminderVM();

        if ( model.getId() != null ) {
            orderReminderVM.setId( model.getId() );
        }
        if ( model.getRemarks() != null ) {
            orderReminderVM.setRemarks( model.getRemarks() );
        }
        if ( model.getCreateBy() != null ) {
            orderReminderVM.setCreateBy( model.getCreateBy() );
        }
        if ( model.getCreateDate() != null ) {
            orderReminderVM.setCreateDate( model.getCreateDate() );
        }
        if ( model.getCurrentUser() != null ) {
            orderReminderVM.setCurrentUser( model.getCurrentUser() );
        }
        if ( model.getPage() != null ) {
            orderReminderVM.setPage( model.getPage() );
        }
        Map<String, String> map = model.getSqlMap();
        if ( map != null ) {
            orderReminderVM.setSqlMap( new HashMap<String, String>( map ) );
        }
        else {
            orderReminderVM.setSqlMap( null );
        }
        orderReminderVM.setIsNewRecord( model.getIsNewRecord() );
        if ( model.getOrderNo() != null ) {
            orderReminderVM.setOrderNo( model.getOrderNo() );
        }
        if ( model.getOrderType() != null ) {
            orderReminderVM.setOrderType( model.getOrderType() );
        }
        if ( model.getDataSource() != null ) {
            orderReminderVM.setDataSource( model.getDataSource() );
        }
        if ( model.getOrderChannel() != null ) {
            orderReminderVM.setOrderChannel( model.getOrderChannel() );
        }
        if ( model.getTotalQty() != null ) {
            orderReminderVM.setTotalQty( model.getTotalQty() );
        }
        if ( model.getVerificationCode() != null ) {
            orderReminderVM.setVerificationCode( model.getVerificationCode() );
        }
        if ( model.getConfirmDoor() != null ) {
            orderReminderVM.setConfirmDoor( model.getConfirmDoor() );
        }
        if ( model.getServiceTimes() != null ) {
            orderReminderVM.setServiceTimes( model.getServiceTimes() );
        }
        if ( model.getDescription() != null ) {
            orderReminderVM.setDescription( model.getDescription() );
        }
        if ( model.getOrderInfo() != null ) {
            orderReminderVM.setOrderInfo( model.getOrderInfo() );
        }
        if ( model.getOrderStatus() != null ) {
            orderReminderVM.setOrderStatus( model.getOrderStatus() );
        }
        if ( model.getOrderStatusFlag() != null ) {
            orderReminderVM.setOrderStatusFlag( model.getOrderStatusFlag() );
        }
        if ( model.getOrderCondition() != null ) {
            orderReminderVM.setOrderCondition( model.getOrderCondition() );
        }
        if ( model.getOrderFee() != null ) {
            orderReminderVM.setOrderFee( model.getOrderFee() );
        }
        List<OrderItem> list = model.getItems();
        if ( list != null ) {
            orderReminderVM.setItems( new ArrayList<OrderItem>( list ) );
        }
        else {
            orderReminderVM.setItems( null );
        }
        if ( model.getOrderItemJson() != null ) {
            orderReminderVM.setOrderItemJson( model.getOrderItemJson() );
        }
        List<OrderGrade> list1 = model.getGradeList();
        if ( list1 != null ) {
            orderReminderVM.setGradeList( new ArrayList<OrderGrade>( list1 ) );
        }
        else {
            orderReminderVM.setGradeList( null );
        }
        List<OrderDetail> list2 = model.getDetailList();
        if ( list2 != null ) {
            orderReminderVM.setDetailList( new ArrayList<OrderDetail>( list2 ) );
        }
        else {
            orderReminderVM.setDetailList( null );
        }
        List<OrderProcessLog> list3 = model.getLogList();
        if ( list3 != null ) {
            orderReminderVM.setLogList( new ArrayList<OrderProcessLog>( list3 ) );
        }
        else {
            orderReminderVM.setLogList( null );
        }
        List<OrderAttachment> list4 = model.getAttachments();
        if ( list4 != null ) {
            orderReminderVM.setAttachments( new ArrayList<OrderAttachment>( list4 ) );
        }
        else {
            orderReminderVM.setAttachments( null );
        }
        if ( model.getQuarter() != null ) {
            orderReminderVM.setQuarter( model.getQuarter() );
        }
        if ( model.getOrderLocation() != null ) {
            orderReminderVM.setOrderLocation( model.getOrderLocation() );
        }
        if ( model.getSendUserMessageFlag() != null ) {
            orderReminderVM.setSendUserMessageFlag( model.getSendUserMessageFlag() );
        }
        if ( model.getSendEngineerMessageFlag() != null ) {
            orderReminderVM.setSendEngineerMessageFlag( model.getSendEngineerMessageFlag() );
        }
        if ( model.getTrackingDate() != null ) {
            orderReminderVM.setTrackingDate( model.getTrackingDate() );
        }
        if ( model.getIsCustomerSame() != null ) {
            orderReminderVM.setIsCustomerSame( model.getIsCustomerSame() );
        }
        if ( model.getCrushPlanFlag() != null ) {
            orderReminderVM.setCrushPlanFlag( model.getCrushPlanFlag() );
        }
        if ( model.getOldServicePointId() != null ) {
            orderReminderVM.setOldServicePointId( model.getOldServicePointId() );
        }
        List<Product> list5 = model.getProducts();
        if ( list5 != null ) {
            orderReminderVM.setProducts( new ArrayList<Product>( list5 ) );
        }
        else {
            orderReminderVM.setProducts( null );
        }
        List<MaterialMaster> list6 = model.getMaterials();
        if ( list6 != null ) {
            orderReminderVM.setMaterials( new ArrayList<MaterialMaster>( list6 ) );
        }
        else {
            orderReminderVM.setMaterials( null );
        }
        if ( model.getRepeateNo() != null ) {
            orderReminderVM.setRepeateNo( model.getRepeateNo() );
        }
        if ( model.getWriteOff() != null ) {
            orderReminderVM.setWriteOff( model.getWriteOff() );
        }
        if ( model.getComplainFormStatus() != null ) {
            orderReminderVM.setComplainFormStatus( model.getComplainFormStatus() );
        }
        List<OrderServicePointFee> list7 = model.getServicePointFees();
        if ( list7 != null ) {
            orderReminderVM.setServicePointFees( new ArrayList<OrderServicePointFee>( list7 ) );
        }
        else {
            orderReminderVM.setServicePointFees( null );
        }
        if ( model.getWorkCardId() != null ) {
            orderReminderVM.setWorkCardId( model.getWorkCardId() );
        }
        if ( model.getB2bOrderId() != null ) {
            orderReminderVM.setB2bOrderId( model.getB2bOrderId() );
        }
        if ( model.getB2bShop() != null ) {
            orderReminderVM.setB2bShop( model.getB2bShop() );
        }
        if ( model.getAppMessage() != null ) {
            orderReminderVM.setAppMessage( model.getAppMessage() );
        }
        if ( model.getParentBizOrderId() != null ) {
            orderReminderVM.setParentBizOrderId( model.getParentBizOrderId() );
        }
        if ( model.getOrderAdditionalInfo() != null ) {
            orderReminderVM.setOrderAdditionalInfo( model.getOrderAdditionalInfo() );
        }
        byte[] itemsPb = model.getItemsPb();
        if ( itemsPb != null ) {
            orderReminderVM.setItemsPb( Arrays.copyOf( itemsPb, itemsPb.length ) );
        }
        byte[] additionalInfoPb = model.getAdditionalInfoPb();
        if ( additionalInfoPb != null ) {
            orderReminderVM.setAdditionalInfoPb( Arrays.copyOf( additionalInfoPb, additionalInfoPb.length ) );
        }

        return orderReminderVM;
    }
}

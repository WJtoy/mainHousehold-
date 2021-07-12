package com.wolfking.jeesite.modules.mq.entity.mapper;

import com.wolfking.jeesite.modules.md.entity.Engineer;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage.OrderInfo;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage.ServicePointInfo;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage.ServicePointMessage;
import com.wolfking.jeesite.modules.mq.dto.MQOrderServicePointMessage.UserInfo;
import com.wolfking.jeesite.modules.sd.entity.OrderServicePoint;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:40+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class OrderServicePointMessageMapperImpl extends OrderServicePointMessageMapper {

    @Override
    public OrderServicePoint mqToModel(ServicePointMessage message) {
        if ( message == null ) {
            return null;
        }

        OrderServicePoint orderServicePoint = new OrderServicePoint();

        if ( message.hasOrderInfo() ) {
            orderServicePoint.setArea( orderInfoToArea( message.getOrderInfo() ) );
        }
        orderServicePoint.setCreateBy( servicePointMessageToUser( message ) );
        if ( message.hasServicePointInfo() ) {
            orderServicePoint.setEngineer( servicePointInfoToEngineer( message.getServicePointInfo() ) );
        }
        if ( message.hasOrderInfo() ) {
            orderServicePoint.setStatus( orderInfoToDict( message.getOrderInfo() ) );
        }
        orderServicePoint.setUpdateBy( servicePointMessageToUser1( message ) );
        orderServicePoint.setMasterFlag( message.getMasterFlag() );
        orderServicePoint.setPlanAt( message.getPlanDate() );
        orderServicePoint.setOrderId( message.getOrderId() );
        Long servicePointId = messageServicePointInfoServicePointId( message );
        if ( message.hasServicePointInfo() ) {
            orderServicePoint.setServicePointId( servicePointId );
        }
        int planOrder = messageServicePointInfoPlanOrder( message );
        if ( message.hasServicePointInfo() ) {
            orderServicePoint.setPlanOrder( planOrder );
        }
        orderServicePoint.setReservationAt( message.getReservationDate() );
        if ( message.getAppCompleteType() != null ) {
            orderServicePoint.setAppCompleteType( message.getAppCompleteType() );
        }
        orderServicePoint.setOrderChannel( message.getOrderChannel() );
        orderServicePoint.setAbnormalyFlag( message.getAbnormalyFlag() );
        orderServicePoint.setId( message.getId() );
        orderServicePoint.setComplainFlag( message.getComplainFlag() );
        orderServicePoint.setAppointmentAt( message.getAppointmentDate() );
        orderServicePoint.setChargeAt( message.getChargeDate() );
        orderServicePoint.setReminderFlag( message.getReminderFlag() );
        String orderNo = messageOrderInfoOrderNo( message );
        if ( message.hasOrderInfo() ) {
            orderServicePoint.setOrderNo( orderNo );
        }
        int planType = messageServicePointInfoPlanType( message );
        if ( message.hasServicePointInfo() ) {
            orderServicePoint.setPlanType( planType );
        }
        String phone = messageUserInfoPhone( message );
        if ( message.hasUserInfo() ) {
            orderServicePoint.setServicePhone( phone );
        }
        String address = messageUserInfoAddress( message );
        if ( message.hasUserInfo() ) {
            orderServicePoint.setServiceAddress( address );
        }
        orderServicePoint.setPendingType( message.getPendingType() );
        String userName = messageUserInfoUserName( message );
        if ( message.hasUserInfo() ) {
            orderServicePoint.setUserName( userName );
        }
        orderServicePoint.setCloseAt( message.getCloseDate() );
        orderServicePoint.setSubStatus( message.getSubStatus() );
        orderServicePoint.setUrgentLevelId( message.getUrgentLevelId() );
        int orderServiceType = messageOrderInfoOrderServiceType( message );
        if ( message.hasOrderInfo() ) {
            orderServicePoint.setOrderServiceType( orderServiceType );
        }
        orderServicePoint.setDataSource( message.getDataSource() );
        if ( message.getQuarter() != null ) {
            orderServicePoint.setQuarter( message.getQuarter() );
        }
        orderServicePoint.setServiceFlag( message.getServiceFlag() );

        orderServicePoint.setUpdateDate( com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getOperationAt()) );
        orderServicePoint.setReservationDate( com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getReservationDate()) );
        orderServicePoint.setCreateDate( com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getOperationAt()) );
        orderServicePoint.setPlanDate( com.wolfking.jeesite.common.utils.DateUtils.longToDate(message.getPlanDate()) );

        return orderServicePoint;
    }

    protected Area orderInfoToArea(OrderInfo orderInfo) {
        if ( orderInfo == null ) {
            return null;
        }

        Area area = new Area();

        if ( orderInfo.getAreaName() != null ) {
            area.setName( orderInfo.getAreaName() );
        }
        area.setId( orderInfo.getAreaId() );

        return area;
    }

    protected User servicePointMessageToUser(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return null;
        }

        User user = new User();

        user.setId( servicePointMessage.getOperationBy() );

        return user;
    }

    protected Engineer servicePointInfoToEngineer(ServicePointInfo servicePointInfo) {
        if ( servicePointInfo == null ) {
            return null;
        }

        Engineer engineer = new Engineer();

        engineer.setId( servicePointInfo.getEngineerId() );

        return engineer;
    }

    protected Dict orderInfoToDict(OrderInfo orderInfo) {
        if ( orderInfo == null ) {
            return null;
        }

        Dict dict = new Dict();

        dict.setValue( String.valueOf( orderInfo.getStatus() ) );

        return dict;
    }

    protected User servicePointMessageToUser1(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return null;
        }

        User user = new User();

        user.setId( servicePointMessage.getOperationBy() );

        return user;
    }

    private Long messageServicePointInfoServicePointId(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return null;
        }
        if ( !servicePointMessage.hasServicePointInfo() ) {
            return null;
        }
        ServicePointInfo servicePointInfo = servicePointMessage.getServicePointInfo();
        long servicePointId = servicePointInfo.getServicePointId();
        return servicePointId;
    }

    private int messageServicePointInfoPlanOrder(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return 0;
        }
        if ( !servicePointMessage.hasServicePointInfo() ) {
            return 0;
        }
        ServicePointInfo servicePointInfo = servicePointMessage.getServicePointInfo();
        int planOrder = servicePointInfo.getPlanOrder();
        return planOrder;
    }

    private String messageOrderInfoOrderNo(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return null;
        }
        if ( !servicePointMessage.hasOrderInfo() ) {
            return null;
        }
        OrderInfo orderInfo = servicePointMessage.getOrderInfo();
        String orderNo = orderInfo.getOrderNo();
        if ( orderNo == null ) {
            return null;
        }
        return orderNo;
    }

    private int messageServicePointInfoPlanType(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return 0;
        }
        if ( !servicePointMessage.hasServicePointInfo() ) {
            return 0;
        }
        ServicePointInfo servicePointInfo = servicePointMessage.getServicePointInfo();
        int planType = servicePointInfo.getPlanType();
        return planType;
    }

    private String messageUserInfoPhone(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return null;
        }
        if ( !servicePointMessage.hasUserInfo() ) {
            return null;
        }
        UserInfo userInfo = servicePointMessage.getUserInfo();
        String phone = userInfo.getPhone();
        if ( phone == null ) {
            return null;
        }
        return phone;
    }

    private String messageUserInfoAddress(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return null;
        }
        if ( !servicePointMessage.hasUserInfo() ) {
            return null;
        }
        UserInfo userInfo = servicePointMessage.getUserInfo();
        String address = userInfo.getAddress();
        if ( address == null ) {
            return null;
        }
        return address;
    }

    private String messageUserInfoUserName(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return null;
        }
        if ( !servicePointMessage.hasUserInfo() ) {
            return null;
        }
        UserInfo userInfo = servicePointMessage.getUserInfo();
        String userName = userInfo.getUserName();
        if ( userName == null ) {
            return null;
        }
        return userName;
    }

    private int messageOrderInfoOrderServiceType(ServicePointMessage servicePointMessage) {
        if ( servicePointMessage == null ) {
            return 0;
        }
        if ( !servicePointMessage.hasOrderInfo() ) {
            return 0;
        }
        OrderInfo orderInfo = servicePointMessage.getOrderInfo();
        int orderServiceType = orderInfo.getOrderServiceType();
        return orderServiceType;
    }
}

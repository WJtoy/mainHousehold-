package com.wolfking.jeesite.ms.tmall.sd.mapper;

import com.kkl.kklplus.entity.b2b.order.WorkcardInfo;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.common.MSPage;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.tmall.sd.entity.WorkcardInfoModel;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:41+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class B2BOrderModelMapperImpl extends B2BOrderModelMapper {

    @Override
    public WorkcardInfoModel toTMallModel(B2BOrderVModel entity) {
        if ( entity == null ) {
            return null;
        }

        WorkcardInfoModel workcardInfoModel = new WorkcardInfoModel();

        workcardInfoModel.setServiceCode( entity.getServiceType() );
        workcardInfoModel.setProcessComment( entity.getProcessComment() );
        workcardInfoModel.setBuyerName( entity.getUserName() );
        workcardInfoModel.setProcessTime( entity.getProcessTime() );
        if ( entity.getOrderNo() != null ) {
            workcardInfoModel.setWorkcardId( Long.parseLong( entity.getOrderNo() ) );
        }
        workcardInfoModel.setProcessFlag( entity.getProcessFlag() );
        workcardInfoModel.setBuyerAddress( entity.getUserAddress() );
        workcardInfoModel.setBuyerPhone( entity.getUserPhone() );
        workcardInfoModel.setTaskMemo( entity.getDescription() );
        workcardInfoModel.setBuyerMobile( entity.getUserMobile() );
        workcardInfoModel.setBrand( entity.getBrand() );
        workcardInfoModel.setTaskStatus( entity.getStatus() );
        workcardInfoModel.setCustomer( entity.getCustomer() );
        if ( entity.getShopId() != null ) {
            workcardInfoModel.setSellerShopId( Long.parseLong( entity.getShopId() ) );
        }
        workcardInfoModel.setQuarter( entity.getQuarter() );
        workcardInfoModel.setId( entity.getId() );
        workcardInfoModel.setRemarks( entity.getRemarks() );
        workcardInfoModel.setCreateBy( entity.getCreateBy() );
        workcardInfoModel.setCreateById( entity.getCreateById() );
        workcardInfoModel.setCreateDate( entity.getCreateDate() );
        workcardInfoModel.setUpdateBy( entity.getUpdateBy() );
        workcardInfoModel.setUpdateById( entity.getUpdateById() );
        workcardInfoModel.setUpdateDate( entity.getUpdateDate() );
        workcardInfoModel.setDelFlag( entity.getDelFlag() );
        workcardInfoModel.setPage( b2BOrderMSPageToWorkcardInfoMSPage( entity.getPage() ) );
        workcardInfoModel.setCreateDt( entity.getCreateDt() );
        workcardInfoModel.setUpdateDt( entity.getUpdateDt() );
        workcardInfoModel.setUniqueId( entity.getUniqueId() );
        workcardInfoModel.setB2bOrderId( entity.getB2bOrderId() );
        if ( entity.getParentBizOrderId() != null ) {
            workcardInfoModel.setParentBizOrderId( Long.parseLong( entity.getParentBizOrderId() ) );
        }
        workcardInfoModel.setShopName( entity.getShopName() );
        workcardInfoModel.setAbnormalOrderFlag( entity.getAbnormalOrderFlag() );

        workcardInfoModel.setAuctionName( entity.getItems() != null && !entity.getItems().isEmpty() ? entity.getItems().get(0).getProductName() : "" );
        workcardInfoModel.setServiceCount( entity.getItems() != null && !entity.getItems().isEmpty() ? entity.getItems().get(0).getQty() : 0 );
        workcardInfoModel.setModelNumber( entity.getItems() != null && !entity.getItems().isEmpty() ? entity.getItems().get(0).getProductSpec() : "" );
        workcardInfoModel.setCategoryId( entity.getItems() != null && !entity.getItems().isEmpty() ? Long.valueOf(entity.getItems().get(0).getProductCode()) : 0 );

        return workcardInfoModel;
    }

    @Override
    public List<WorkcardInfoModel> listToTMallModel(List<B2BOrderVModel> order) {
        if ( order == null ) {
            return null;
        }

        List<WorkcardInfoModel> list = new ArrayList<WorkcardInfoModel>( order.size() );
        for ( B2BOrderVModel b2BOrderVModel : order ) {
            list.add( toTMallModel( b2BOrderVModel ) );
        }

        return list;
    }

    protected WorkcardInfo b2BOrderToWorkcardInfo(B2BOrder b2BOrder) {
        if ( b2BOrder == null ) {
            return null;
        }

        WorkcardInfo workcardInfo = new WorkcardInfo();

        workcardInfo.setId( b2BOrder.getId() );
        workcardInfo.setRemarks( b2BOrder.getRemarks() );
        workcardInfo.setCreateBy( b2BOrder.getCreateBy() );
        workcardInfo.setCreateById( b2BOrder.getCreateById() );
        workcardInfo.setCreateDate( b2BOrder.getCreateDate() );
        workcardInfo.setUpdateBy( b2BOrder.getUpdateBy() );
        workcardInfo.setUpdateById( b2BOrder.getUpdateById() );
        workcardInfo.setUpdateDate( b2BOrder.getUpdateDate() );
        workcardInfo.setDelFlag( b2BOrder.getDelFlag() );
        workcardInfo.setPage( b2BOrderMSPageToWorkcardInfoMSPage( b2BOrder.getPage() ) );
        workcardInfo.setCreateDt( b2BOrder.getCreateDt() );
        workcardInfo.setUpdateDt( b2BOrder.getUpdateDt() );
        workcardInfo.setProcessFlag( b2BOrder.getProcessFlag() );
        workcardInfo.setProcessTime( b2BOrder.getProcessTime() );
        workcardInfo.setProcessComment( b2BOrder.getProcessComment() );
        workcardInfo.setQuarter( b2BOrder.getQuarter() );
        workcardInfo.setDataSource( b2BOrder.getDataSource() );
        workcardInfo.setUniqueId( b2BOrder.getUniqueId() );
        workcardInfo.setB2bOrderId( b2BOrder.getB2bOrderId() );
        workcardInfo.setBrand( b2BOrder.getBrand() );
        if ( b2BOrder.getParentBizOrderId() != null ) {
            workcardInfo.setParentBizOrderId( Long.parseLong( b2BOrder.getParentBizOrderId() ) );
        }
        workcardInfo.setShopName( b2BOrder.getShopName() );
        workcardInfo.setAbnormalOrderFlag( b2BOrder.getAbnormalOrderFlag() );

        return workcardInfo;
    }

    protected List<WorkcardInfo> b2BOrderListToWorkcardInfoList(List<B2BOrder> list) {
        if ( list == null ) {
            return null;
        }

        List<WorkcardInfo> list1 = new ArrayList<WorkcardInfo>( list.size() );
        for ( B2BOrder b2BOrder : list ) {
            list1.add( b2BOrderToWorkcardInfo( b2BOrder ) );
        }

        return list1;
    }

    protected MSPage<WorkcardInfo> b2BOrderMSPageToWorkcardInfoMSPage(MSPage<B2BOrder> mSPage) {
        if ( mSPage == null ) {
            return null;
        }

        MSPage<WorkcardInfo> mSPage1 = new MSPage<WorkcardInfo>();

        mSPage1.setPageNo( mSPage.getPageNo() );
        mSPage1.setPageSize( mSPage.getPageSize() );
        mSPage1.setPageCount( mSPage.getPageCount() );
        mSPage1.setRowCount( mSPage.getRowCount() );
        mSPage1.setList( b2BOrderListToWorkcardInfoList( mSPage.getList() ) );
        mSPage1.setOrderBy( mSPage.getOrderBy() );

        return mSPage1;
    }
}

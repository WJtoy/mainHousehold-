package com.wolfking.jeesite.ms.cc.entity.mapper;

import com.kkl.kklplus.entity.cc.AbnormalForm;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.ms.cc.entity.AbnormalFormModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-18T17:10:41+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class AbnormalFormModelMapperImpl extends AbnormalFormModelMapper {

    @Override
    public AbnormalFormModel toViewModel(AbnormalForm model, Map<Long, User> userMap) {
        if ( model == null && userMap == null ) {
            return null;
        }

        AbnormalFormModel abnormalFormModel = new AbnormalFormModel();

        if ( model != null ) {
            if ( model.getId() != null ) {
                abnormalFormModel.setId( model.getId() );
            }
            if ( model.getRemarks() != null ) {
                abnormalFormModel.setRemarks( model.getRemarks() );
            }
            if ( model.getCreateBy() != null ) {
                abnormalFormModel.setCreateBy( model.getCreateBy() );
            }
            if ( model.getCreateById() != null ) {
                abnormalFormModel.setCreateById( model.getCreateById() );
            }
            if ( model.getCreateDate() != null ) {
                abnormalFormModel.setCreateDate( model.getCreateDate() );
            }
            if ( model.getUpdateBy() != null ) {
                abnormalFormModel.setUpdateBy( model.getUpdateBy() );
            }
            if ( model.getUpdateById() != null ) {
                abnormalFormModel.setUpdateById( model.getUpdateById() );
            }
            if ( model.getUpdateDate() != null ) {
                abnormalFormModel.setUpdateDate( model.getUpdateDate() );
            }
            if ( model.getDelFlag() != null ) {
                abnormalFormModel.setDelFlag( model.getDelFlag() );
            }
            if ( model.getPage() != null ) {
                abnormalFormModel.setPage( model.getPage() );
            }
            if ( model.getCreateDt() != null ) {
                abnormalFormModel.setCreateDt( model.getCreateDt() );
            }
            if ( model.getUpdateDt() != null ) {
                abnormalFormModel.setUpdateDt( model.getUpdateDt() );
            }
            if ( model.getOrderId() != null ) {
                abnormalFormModel.setOrderId( model.getOrderId() );
            }
            if ( model.getOpinionLogId() != null ) {
                abnormalFormModel.setOpinionLogId( model.getOpinionLogId() );
            }
            if ( model.getOrderNo() != null ) {
                abnormalFormModel.setOrderNo( model.getOrderNo() );
            }
            if ( model.getQuarter() != null ) {
                abnormalFormModel.setQuarter( model.getQuarter() );
            }
            if ( model.getAbnormalNo() != null ) {
                abnormalFormModel.setAbnormalNo( model.getAbnormalNo() );
            }
            if ( model.getChannel() != null ) {
                abnormalFormModel.setChannel( model.getChannel() );
            }
            if ( model.getFormType() != null ) {
                abnormalFormModel.setFormType( model.getFormType() );
            }
            if ( model.getSubType() != null ) {
                abnormalFormModel.setSubType( model.getSubType() );
            }
            if ( model.getCustomerId() != null ) {
                abnormalFormModel.setCustomerId( model.getCustomerId() );
            }
            if ( model.getServicepointId() != null ) {
                abnormalFormModel.setServicepointId( model.getServicepointId() );
            }
            if ( model.getEngineerId() != null ) {
                abnormalFormModel.setEngineerId( model.getEngineerId() );
            }
            if ( model.getProductCategoryId() != null ) {
                abnormalFormModel.setProductCategoryId( model.getProductCategoryId() );
            }
            if ( model.getKefuId() != null ) {
                abnormalFormModel.setKefuId( model.getKefuId() );
            }
            if ( model.getStatus() != null ) {
                abnormalFormModel.setStatus( model.getStatus() );
            }
            if ( model.getReason() != null ) {
                abnormalFormModel.setReason( model.getReason() );
            }
            if ( model.getDescription() != null ) {
                abnormalFormModel.setDescription( model.getDescription() );
            }
            if ( model.getProvinceId() != null ) {
                abnormalFormModel.setProvinceId( model.getProvinceId() );
            }
            if ( model.getCityId() != null ) {
                abnormalFormModel.setCityId( model.getCityId() );
            }
            if ( model.getAreaId() != null ) {
                abnormalFormModel.setAreaId( model.getAreaId() );
            }
            if ( model.getSubAreaId() != null ) {
                abnormalFormModel.setSubAreaId( model.getSubAreaId() );
            }
            if ( model.getUserName() != null ) {
                abnormalFormModel.setUserName( model.getUserName() );
            }
            if ( model.getUserPhone() != null ) {
                abnormalFormModel.setUserPhone( model.getUserPhone() );
            }
            if ( model.getUserAddress() != null ) {
                abnormalFormModel.setUserAddress( model.getUserAddress() );
            }
            if ( model.getCloseComment() != null ) {
                abnormalFormModel.setCloseComment( model.getCloseComment() );
            }
            if ( model.getTimeLiness() != null ) {
                abnormalFormModel.setTimeLiness( model.getTimeLiness() );
            }
            if ( model.getCloseBy() != null ) {
                abnormalFormModel.setCloseBy( model.getCloseBy() );
            }
            if ( model.getCloseAt() != null ) {
                abnormalFormModel.setCloseAt( model.getCloseAt() );
            }
            if ( model.getTimeoutAt() != null ) {
                abnormalFormModel.setTimeoutAt( model.getTimeoutAt() );
            }
            if ( model.getTimeoutFlag() != null ) {
                abnormalFormModel.setTimeoutFlag( model.getTimeoutFlag() );
            }
            if ( model.getCanRush() != null ) {
                abnormalFormModel.setCanRush( model.getCanRush() );
            }
            if ( model.getKefuType() != null ) {
                abnormalFormModel.setKefuType( model.getKefuType() );
            }
        }

        after( abnormalFormModel, model, userMap );

        return abnormalFormModel;
    }

    @Override
    public List<AbnormalFormModel> toViewModels(List<AbnormalForm> models) {
        if ( models == null ) {
            return null;
        }

        List<AbnormalFormModel> list = new ArrayList<AbnormalFormModel>( models.size() );
        for ( AbnormalForm abnormalForm : models ) {
            list.add( abnormalFormToAbnormalFormModel( abnormalForm ) );
        }

        return list;
    }

    protected AbnormalFormModel abnormalFormToAbnormalFormModel(AbnormalForm abnormalForm) {
        if ( abnormalForm == null ) {
            return null;
        }

        AbnormalFormModel abnormalFormModel = new AbnormalFormModel();

        if ( abnormalForm.getId() != null ) {
            abnormalFormModel.setId( abnormalForm.getId() );
        }
        if ( abnormalForm.getRemarks() != null ) {
            abnormalFormModel.setRemarks( abnormalForm.getRemarks() );
        }
        if ( abnormalForm.getCreateBy() != null ) {
            abnormalFormModel.setCreateBy( abnormalForm.getCreateBy() );
        }
        if ( abnormalForm.getCreateById() != null ) {
            abnormalFormModel.setCreateById( abnormalForm.getCreateById() );
        }
        if ( abnormalForm.getCreateDate() != null ) {
            abnormalFormModel.setCreateDate( abnormalForm.getCreateDate() );
        }
        if ( abnormalForm.getUpdateBy() != null ) {
            abnormalFormModel.setUpdateBy( abnormalForm.getUpdateBy() );
        }
        if ( abnormalForm.getUpdateById() != null ) {
            abnormalFormModel.setUpdateById( abnormalForm.getUpdateById() );
        }
        if ( abnormalForm.getUpdateDate() != null ) {
            abnormalFormModel.setUpdateDate( abnormalForm.getUpdateDate() );
        }
        if ( abnormalForm.getDelFlag() != null ) {
            abnormalFormModel.setDelFlag( abnormalForm.getDelFlag() );
        }
        if ( abnormalForm.getPage() != null ) {
            abnormalFormModel.setPage( abnormalForm.getPage() );
        }
        if ( abnormalForm.getCreateDt() != null ) {
            abnormalFormModel.setCreateDt( abnormalForm.getCreateDt() );
        }
        if ( abnormalForm.getUpdateDt() != null ) {
            abnormalFormModel.setUpdateDt( abnormalForm.getUpdateDt() );
        }
        if ( abnormalForm.getOrderId() != null ) {
            abnormalFormModel.setOrderId( abnormalForm.getOrderId() );
        }
        if ( abnormalForm.getOpinionLogId() != null ) {
            abnormalFormModel.setOpinionLogId( abnormalForm.getOpinionLogId() );
        }
        if ( abnormalForm.getOrderNo() != null ) {
            abnormalFormModel.setOrderNo( abnormalForm.getOrderNo() );
        }
        if ( abnormalForm.getQuarter() != null ) {
            abnormalFormModel.setQuarter( abnormalForm.getQuarter() );
        }
        if ( abnormalForm.getAbnormalNo() != null ) {
            abnormalFormModel.setAbnormalNo( abnormalForm.getAbnormalNo() );
        }
        if ( abnormalForm.getChannel() != null ) {
            abnormalFormModel.setChannel( abnormalForm.getChannel() );
        }
        if ( abnormalForm.getFormType() != null ) {
            abnormalFormModel.setFormType( abnormalForm.getFormType() );
        }
        if ( abnormalForm.getSubType() != null ) {
            abnormalFormModel.setSubType( abnormalForm.getSubType() );
        }
        if ( abnormalForm.getCustomerId() != null ) {
            abnormalFormModel.setCustomerId( abnormalForm.getCustomerId() );
        }
        if ( abnormalForm.getServicepointId() != null ) {
            abnormalFormModel.setServicepointId( abnormalForm.getServicepointId() );
        }
        if ( abnormalForm.getEngineerId() != null ) {
            abnormalFormModel.setEngineerId( abnormalForm.getEngineerId() );
        }
        if ( abnormalForm.getProductCategoryId() != null ) {
            abnormalFormModel.setProductCategoryId( abnormalForm.getProductCategoryId() );
        }
        if ( abnormalForm.getKefuId() != null ) {
            abnormalFormModel.setKefuId( abnormalForm.getKefuId() );
        }
        if ( abnormalForm.getStatus() != null ) {
            abnormalFormModel.setStatus( abnormalForm.getStatus() );
        }
        if ( abnormalForm.getReason() != null ) {
            abnormalFormModel.setReason( abnormalForm.getReason() );
        }
        if ( abnormalForm.getDescription() != null ) {
            abnormalFormModel.setDescription( abnormalForm.getDescription() );
        }
        if ( abnormalForm.getProvinceId() != null ) {
            abnormalFormModel.setProvinceId( abnormalForm.getProvinceId() );
        }
        if ( abnormalForm.getCityId() != null ) {
            abnormalFormModel.setCityId( abnormalForm.getCityId() );
        }
        if ( abnormalForm.getAreaId() != null ) {
            abnormalFormModel.setAreaId( abnormalForm.getAreaId() );
        }
        if ( abnormalForm.getSubAreaId() != null ) {
            abnormalFormModel.setSubAreaId( abnormalForm.getSubAreaId() );
        }
        if ( abnormalForm.getUserName() != null ) {
            abnormalFormModel.setUserName( abnormalForm.getUserName() );
        }
        if ( abnormalForm.getUserPhone() != null ) {
            abnormalFormModel.setUserPhone( abnormalForm.getUserPhone() );
        }
        if ( abnormalForm.getUserAddress() != null ) {
            abnormalFormModel.setUserAddress( abnormalForm.getUserAddress() );
        }
        if ( abnormalForm.getCloseComment() != null ) {
            abnormalFormModel.setCloseComment( abnormalForm.getCloseComment() );
        }
        if ( abnormalForm.getTimeLiness() != null ) {
            abnormalFormModel.setTimeLiness( abnormalForm.getTimeLiness() );
        }
        if ( abnormalForm.getCloseBy() != null ) {
            abnormalFormModel.setCloseBy( abnormalForm.getCloseBy() );
        }
        if ( abnormalForm.getCloseAt() != null ) {
            abnormalFormModel.setCloseAt( abnormalForm.getCloseAt() );
        }
        if ( abnormalForm.getTimeoutAt() != null ) {
            abnormalFormModel.setTimeoutAt( abnormalForm.getTimeoutAt() );
        }
        if ( abnormalForm.getTimeoutFlag() != null ) {
            abnormalFormModel.setTimeoutFlag( abnormalForm.getTimeoutFlag() );
        }
        if ( abnormalForm.getCanRush() != null ) {
            abnormalFormModel.setCanRush( abnormalForm.getCanRush() );
        }
        if ( abnormalForm.getKefuType() != null ) {
            abnormalFormModel.setKefuType( abnormalForm.getKefuType() );
        }

        return abnormalFormModel;
    }
}

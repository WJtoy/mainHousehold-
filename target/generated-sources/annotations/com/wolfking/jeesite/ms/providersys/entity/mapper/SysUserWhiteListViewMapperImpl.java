package com.wolfking.jeesite.ms.providersys.entity.mapper;

import com.kkl.kklplus.entity.sys.SysUserWhiteList;
import com.wolfking.jeesite.ms.providersys.entity.SysUserWhiteListView;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-06-28T15:55:09+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_181 (Oracle Corporation)"
)
public class SysUserWhiteListViewMapperImpl extends SysUserWhiteListViewMapper {

    @Override
    public SysUserWhiteListView toViewModel(SysUserWhiteList model) {
        if ( model == null ) {
            return null;
        }

        SysUserWhiteListView sysUserWhiteListView = new SysUserWhiteListView();

        if ( model.getId() != null ) {
            sysUserWhiteListView.setId( model.getId() );
        }
        if ( model.getRemarks() != null ) {
            sysUserWhiteListView.setRemarks( model.getRemarks() );
        }
        if ( model.getCreateBy() != null ) {
            sysUserWhiteListView.setCreateBy( model.getCreateBy() );
        }
        if ( model.getCreateById() != null ) {
            sysUserWhiteListView.setCreateById( model.getCreateById() );
        }
        if ( model.getCreateDate() != null ) {
            sysUserWhiteListView.setCreateDate( model.getCreateDate() );
        }
        if ( model.getUpdateBy() != null ) {
            sysUserWhiteListView.setUpdateBy( model.getUpdateBy() );
        }
        if ( model.getUpdateById() != null ) {
            sysUserWhiteListView.setUpdateById( model.getUpdateById() );
        }
        if ( model.getUpdateDate() != null ) {
            sysUserWhiteListView.setUpdateDate( model.getUpdateDate() );
        }
        if ( model.getDelFlag() != null ) {
            sysUserWhiteListView.setDelFlag( model.getDelFlag() );
        }
        if ( model.getPage() != null ) {
            sysUserWhiteListView.setPage( model.getPage() );
        }
        if ( model.getUserId() != null ) {
            sysUserWhiteListView.setUserId( model.getUserId() );
        }
        if ( model.getEndDate() != null ) {
            sysUserWhiteListView.setEndDate( model.getEndDate() );
        }
        if ( model.getLoginIp() != null ) {
            sysUserWhiteListView.setLoginIp( model.getLoginIp() );
        }
        if ( model.getLoginDate() != null ) {
            sysUserWhiteListView.setLoginDate( model.getLoginDate() );
        }

        after( sysUserWhiteListView, model );

        return sysUserWhiteListView;
    }

    @Override
    public List<SysUserWhiteListView> toViewModels(List<SysUserWhiteList> models) {
        if ( models == null ) {
            return null;
        }

        List<SysUserWhiteListView> list = new ArrayList<SysUserWhiteListView>( models.size() );
        for ( SysUserWhiteList sysUserWhiteList : models ) {
            list.add( toViewModel( sysUserWhiteList ) );
        }

        return list;
    }
}

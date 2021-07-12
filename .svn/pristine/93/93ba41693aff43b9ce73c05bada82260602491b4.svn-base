package com.wolfking.jeesite.ms.providersys.entity.mapper;


import com.kkl.kklplus.entity.sys.SysUserWhiteList;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providersys.entity.SysUserWhiteListView;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.mapstruct.*;

import java.util.List;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)

public abstract class SysUserWhiteListViewMapper {

    /**
     * 数据转视图模型
     */
    public abstract SysUserWhiteListView toViewModel(SysUserWhiteList model);

    @AfterMapping
    protected void after(@MappingTarget final SysUserWhiteListView viewModel, SysUserWhiteList model){
        if(model.getUserId()!=null && model.getUserId()>0){
            User user = UserUtils.get(model.getUserId());
            if(user!=null){
                viewModel.setUserName(user.getName());
                viewModel.setLoginName(user.getLoginName());
                Dict dict = MSDictUtils.getDictByValue(user.getUserType().toString(),"sys_user_type");
                if(dict!=null){
                    viewModel.setUserType(dict.getLabel());
                }
            }
        }
    }

    /**
     * 转List
     */
    public abstract List<SysUserWhiteListView> toViewModels(List<SysUserWhiteList> models);
}

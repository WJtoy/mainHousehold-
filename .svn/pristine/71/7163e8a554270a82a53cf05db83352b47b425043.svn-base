package com.wolfking.jeesite.modules.sd.dao;

import com.wolfking.jeesite.modules.sd.entity.MaterialMaster;
import com.wolfking.jeesite.modules.sd.entity.MaterialReturn;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderMaterialSearchModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客服配件数据访问接口
 *
 * @author wangshoujiang
 * @date 2021-03-22
 */
@Mapper
public interface KefuOrderMaterialDao{


    //region 客服
    List<MaterialMaster> findKefuMaterialList(OrderMaterialSearchModel searchModel);

    //旧件待签收列表
    List<MaterialReturn> waitSignMaterialReturnList(OrderMaterialSearchModel searchModel);


}

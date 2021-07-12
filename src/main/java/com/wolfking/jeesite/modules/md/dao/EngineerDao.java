package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.Engineer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *
 */
@Mapper
public interface EngineerDao extends LongIDCrudDao<Engineer> {

    /**
     * 查询师傅列表
     */
    List<Engineer> getEngineersForKefu(Engineer engineer);

    /**
     * 获取所有能手机派单的主账号安维人员 // add 2019-9-30
     * (去ServicePoint时处理网点应付汇总报表使用)
     * @param engineer
     * @return
     */
    List<Engineer> findMasterEngineer(Engineer engineer);
}

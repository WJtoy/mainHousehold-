package com.wolfking.jeesite.modules.md.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.md.entity.SystemNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 网点通知DAO
 * Created on 2019-03-06
 */
@Mapper
public interface SystemNoticeDao extends LongIDCrudDao<SystemNotice> {

}

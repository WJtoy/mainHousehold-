/**
 * Copyright &copy; 2014-2014 All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sys.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知数据库操作
 */
@Mapper
public interface NoticeDao extends LongIDCrudDao<Notice> {

    /**
     * 读取用户的通知列表
     * @param userId
     * @param delFlag
     * @return
     */
    public List<Notice> getListByUserID(@Param("userId") Long userId, @Param("delFlag") Integer delFlag);

    /**
     * 逻辑删除
     */
    public void deleteById(@Param("id") Long id);

    /**
     * 读取该用户同订单，同类型未读的消息id
     * @param referId
     * @param userId
     * @param noticeType 1:问题反馈 2：app异常
     * @return
     */
    public Long getNotReadLastId(@Param("referId") Long referId,@Param("userId") Long userId,@Param("noticeType") Integer noticeType);

}

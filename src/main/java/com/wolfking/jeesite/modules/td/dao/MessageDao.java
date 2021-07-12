package com.wolfking.jeesite.modules.td.dao;

import com.wolfking.jeesite.common.persistence.LongIDCrudDao;
import com.wolfking.jeesite.modules.td.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户数据访问接口
 * Created on 2017-04-12.
 */
@Mapper
public interface MessageDao extends LongIDCrudDao<Message> {
//    List<Message> Message();
//
//    /**
//     * 读取待重送的列表
//     * @param number    返回的记录数量（null 或<0时，返回所有的记录）
//     * @return
//     */
//    List<Message> getResendList(@Param("num") Integer number);
}

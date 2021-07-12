/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.sys.dao.NoticeDao;
import com.wolfking.jeesite.modules.sys.entity.APPNotice;
import com.wolfking.jeesite.modules.sys.entity.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务层
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class NoticeService extends LongIDCrudService<NoticeDao,Notice> {

	public void insert(Notice entity){
//		dao.insert(entity);
	}

	public void update(Notice entity){
//		dao.update(entity);
	}

	public Notice get(Long id)
	{
//		return dao.get(id);
		return new Notice();
	}

	public List<Notice> getListByUserID(Long userId, Integer delFlag)
	{
//		return dao.getListByUserID(userId,delFlag);
		return Lists.newArrayList();
	}

	/**
	 * 读取该用户同订单，同类型未读的消息id
	 * @param referId
	 * @param userId
	 * @param noticeType 1:问题反馈 2：app异常
	 * @return
	 */
	public Long getNotReadLastId(Long referId,Long userId,Integer noticeType){
//		return dao.getNotReadLastId(referId,userId,noticeType);
		return 0L;
	}

}

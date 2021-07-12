/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.modules.sys.service;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.modules.api.entity.md.RestUploadPushInfo;
import com.wolfking.jeesite.modules.api.util.RestEnum;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.sys.dao.APPNoticeDao;
import com.wolfking.jeesite.modules.sys.entity.APPNotice;
import com.wolfking.jeesite.ms.providersys.service.MSAppNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author F1053038
 * 
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class APPNoticeService extends LongIDCrudService<APPNoticeDao,APPNotice> {

	@Autowired
	private MSAppNoticeService msAppNoticeService;

	/*
	// 以下两方法无处调用，已注释  2020-7-10
	public APPNotice get(Long id){
		return dao.get(id);
	}

	public List<APPNotice> getListByUserID(Long userId, String platform){
		return dao.getListByUserID(userId, platform);
	}
	*/

	//region api functions

	/**
	 * 上传推送相关信息
	 * @param userId
	 * @param phoneType
	 * @param pushInfo
	 * @return
	 */
	public RestResult<Object> uploadPushInfo(Long userId, RestEnum.PhoneType phoneType, RestUploadPushInfo pushInfo){
		//Long oldAppNoticeId = dao.getOneIdByUserId(userId);   // mark on 2020-7-10  APPNotice微服务化
		Long oldAppNoticeId = msAppNoticeService.getOneIdByUserId(userId);  //add on 2020-7-10
		APPNotice appNotice = new APPNotice();
		appNotice.setUserId(userId);
		appNotice.setPlatform(phoneType.ordinal());
		appNotice.setDeviceId(pushInfo.getDeviceId());
		appNotice.setChannelId(pushInfo.getChannelId());
		appNotice.setCreateDate(new Date());
		appNotice.setUpdateDate(new Date());
		if (oldAppNoticeId != null){
			//dao.updateByUserId(appNotice); // mark on 2020-7-10  APPNotice微服务化
			msAppNoticeService.updateByUserId(appNotice); //add on 2020-7-10
		}else{
			//dao.insert(appNotice);  // mark on 2020-7-10  APPNotice微服务化
			msAppNoticeService.insert(appNotice); //add on 2020-7-10
		}
		return RestResultGenerator.success();
	}
	//endregion api functions
}

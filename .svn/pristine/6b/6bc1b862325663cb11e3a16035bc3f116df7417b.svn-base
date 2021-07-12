/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sd.entity;

import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Date;
import java.util.Map;

/**
 * B2B接口微服务调用日志
 * @author Ryan
 * @version 2018-5-21
 */
@Data
public class B2BMsLog extends LongIDDataEntity<B2BMsLog> {

	private static final long serialVersionUID = 1L;
    private String quarter; 	// 季度,分片根据,如20171
    private String title;	// 标题
    private String method;	// 接口
    private String body; //消息体
    private int retryTimes = 0; // 重试次数
    private String exception; 	// 异常信息
    private int status = STATUS_FAILURE;

	// 状态
    public static final Integer STATUS_FAILURE = 0;
    public static final Integer STATUS_SUCCESS = 1;

    public B2BMsLog(){
        this.title = "B2B微服务调用";
        this.createDate = new Date();
        this.quarter = QuarterUtils.getSeasonQuarter(this.createDate);
    }
    public B2BMsLog(User user){
        this();
        this.createBy = user;
    }
}
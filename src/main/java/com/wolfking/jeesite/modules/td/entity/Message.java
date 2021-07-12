package com.wolfking.jeesite.modules.td.entity;


import java.util.Date;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.DateUtils;

/**
 * 短信实体类
 * @author Kody huang
 * @version 2015-10-19
 */

public class Message extends LongIDDataEntity<Message> {

	private static final long serialVersionUID = 1L;
	
	public static final String EXTNO_DEFAULT="";
	
	// Fields
//	private String name="";
//	private String password="";
//	private String sign="";
	private String mobile="";
	private String content="";
	
	/**
	 * 追加发送时间，可为空，为空为即时发送
	 */
	private Date sendTime ;//发送时间　
	private String type="";//类型
	private String extno="";//
	private int retryTimes = 0;//发送重试次数
	private int status = 30;//消息发送状态，10：待处理，20：失败重试中，30：处理成功，40：处理失败
	private Long triggerBy = 0l;//触发者
	private Date triggerDate; //触发日期


	// Constructors
	
	public Message() {
	}
	
	public Message(String mobile,String content,String extno)
	{
		this.mobile=mobile;
		this.content=content;
		this.type="pt";
		this.extno=extno;
	}

	
	public Message(long id) {
		this();
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getExtno() {
		return extno;
	}

	public void setExtno(String extno) {
		this.extno = extno;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Long getTriggerBy() {
		return triggerBy;
	}

	public void setTriggerBy(Long triggerBy) {
		this.triggerBy = triggerBy;
	}

	public Date getTriggerDate() {
		return triggerDate;
	}

	public void setTriggerDate(Date triggerDate) {
		this.triggerDate = triggerDate;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	@Override
	public String toString(){
		return String.format(
				"mobile:%s content:%s sendTime:%s",
				this.mobile==null?"":this.mobile,
				this.content==null?"":this.content,
				this.sendTime==null?"": DateUtils.formatDateTime(this.sendTime)
		);
	}
}
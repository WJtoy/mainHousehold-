package com.wolfking.jeesite.modules.td.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;

import java.util.Date;
import java.util.List;

/**
 * 短信接口返回
 * @author Ryan
 * @version 2017-10-30
 */

public class ShortMessageResponse {

	private String status ="";
	private Long balance;
	private List<ShortMessageResponseItem> list = Lists.newArrayList();


	// Constructors

	public ShortMessageResponse() { }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}

	public List<ShortMessageResponseItem> getList() {
		return list;
	}

	public void setList(List<ShortMessageResponseItem> list) {
		this.list = list;
	}
}
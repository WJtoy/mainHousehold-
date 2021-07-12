package com.wolfking.jeesite.modules.td.entity;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 短信接口返回
 * @author Ryan
 * @version 2017-10-30
 */

public class ShortMessageResponseItem {

	private String mid="";
	private String mobile = "";
	private int result = 0;

	// Constructors

	public ShortMessageResponseItem() { }

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
}
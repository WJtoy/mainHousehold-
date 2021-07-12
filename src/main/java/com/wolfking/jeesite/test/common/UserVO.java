package com.wolfking.jeesite.test.common;

import com.wolfking.jeesite.modules.sys.entity.Office;

public class UserVO {
	Long id;
	String name;
	Office company;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Office getCompany() {
		return company;
	}

	public void setCompany(Office company) {
		this.company = company;
	}
}

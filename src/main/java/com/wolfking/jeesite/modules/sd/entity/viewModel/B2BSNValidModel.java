package com.wolfking.jeesite.modules.sd.entity.viewModel;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 验证B2B条码
 * 
 * @author Ryan
 */
@Data
@NoArgsConstructor
@ToString
public class B2BSNValidModel implements Serializable {

	private static final long serialVersionUID = 1602859682386L;

	/**
	 * 数据源
	 */
	private Integer dataSourceId;
	/**
	 * 第三方单号
	 */
	private String b2bOrderNo;
	/**
	 * SN条码
	 */
	private String sn;
	/**
	 * 条码类型(预留)
	 */
	private Integer snType;

}
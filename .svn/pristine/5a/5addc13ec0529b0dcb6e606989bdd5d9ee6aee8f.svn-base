package com.wolfking.jeesite.modules.sd.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.GsonIgnore;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCompletePicItem;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 退换货工单完工记录
 */
@Slf4j
@Accessors(chain = true)
@Data
@NoArgsConstructor
@ToString
public class OrderReturnComplete implements Serializable {


	/**
	 * ID
	 */
	private Long id;

	/**
	 * 分片
	 */
	private String quarter;

	/**
	 * 订单id
	 */
	private Long orderId;

	/**
	 * 数据源
	 */
	private Integer dataSource;

	/**
	 * 产品
	 */
	private Long productId;

	private String productName;

	/**
	 * 完工项目
	 * 1-拆装项 2-物流
	 */
	private Integer itemType;

	/**
	 * 编号1 拆装项-对应故障货品SN
	 */
	@JsonIgnore
	@GsonIgnore
	private String oldSN = "";

	/**
	 * 编号2 拆装项-对应换货流程的新货品SN
	 */
	private String newSN ="";

	/**
	 * 数据内容
	 */
	private String json;

	/**
	 * 数据上传B2B标志 1-已上传
	 */
	private int uploadFlag = 0;

	/**
	 * 内容对应的对象，根据不同完工项目转换
	 */
	private JsonItem jsonItem;


	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * 创建人
	 */
	private Date createDate;

	/**
	 * 修改人
	 */
	private String updateBy;

	/**
	 * 修改日期
	 */
	private Date updateDate;

	//region 其他类及枚举

	/**
	 * 完工项目类型
	 */
	public enum ItemTypeEnum {
		DISMOUNT(1, "dismount","拆装项"),
		LOGISTICS(2, "logistics","物流");

		private int id;
		private String name;
		private String code;

		ItemTypeEnum(int id, String code,String name) {
			this.id = id;
			this.code = code;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getCode() { return code; }


		private static final Map<Integer, OrderReturnComplete.ItemTypeEnum> MAP = Maps.newHashMap();

		static {
			for (com.wolfking.jeesite.modules.sd.entity.OrderReturnComplete.ItemTypeEnum field : com.wolfking.jeesite.modules.sd.entity.OrderReturnComplete.ItemTypeEnum.values()) {
				MAP.put(field.id, field);
			}
		}

		public static OrderReturnComplete.ItemTypeEnum get(Integer id) {
			OrderReturnComplete.ItemTypeEnum field = null;
			field = MAP.get(id);
			return field;
		}
	}


	/**
	 * 物流项目
	 */
	@Builder
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class JsonItem {
		/**
		 * 物流公司名称
		 */
		private String company;
		private String companyCode = "";
		/**
		 * 物流单号
		 */
		private String number;
		/**
		 * 照片
		 */
		private List<PicSubItem> photos;
	}


	/**
	 * 图片类子项
	 */
	@Builder
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PicSubItem implements Serializable {

		/**
		 * 项目编码
		 */
		private String code;
		/**
		 * 项目名称
		 */
		private String title;
		/**
		 * 是否必填
		 */
		private int required = 0;
		/**
		 * 排序
		 */
		private int sort = 0;
		/**
		 * 图片相对地址
		 */
		private String url;
		/**
		 * 备注
		 */
		private String remarks;

		private String createBy;
		private String createDate;
		private String updateBy;
		private String updateDate;
	}

	/**
	 * 输入类子项
	 */
	@Accessors(chain = true)
	@Data
	@NoArgsConstructor
	public class SNSubItem implements Serializable {
		/**
		 * 项目编码
		 */
		private String code;
		/**
		 * 项目名称
		 */
		private String title;
		/**
		 * 是否必填
		 */
		private int required = 0;
		/**
		 * 排序
		 */
		private int sort = 0;
		/**
		 * 条码
		 */
		private String sn;

		private String createBy;
		private String createDate;
		private String updateBy;
		private String updateDate;
	}

	//endregion 其他类及枚举
}
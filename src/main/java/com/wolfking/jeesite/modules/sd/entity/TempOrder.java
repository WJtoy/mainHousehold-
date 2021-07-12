package com.wolfking.jeesite.modules.sd.entity;

import com.google.gson.annotations.JsonAdapter;
import com.wolfking.jeesite.common.persistence.LongIDDataEntity;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.utils.CustomerSimpleAdapter;
import com.wolfking.jeesite.modules.md.utils.ServiceTypeSimpleAdapter;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.adapter.DictSimpleAdapter;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;

import java.util.List;

/**
 * 临时订单
 * @author Ryan Lu
 */
public class TempOrder extends LongIDDataEntity<TempOrder> {

	private static final long serialVersionUID = 1L;

	@JsonAdapter(CustomerSimpleAdapter.class)
	private Customer customer;// 用户
	private String userName = "";// 用户名
	private String phone = "";//手机号
	private String tel = "";//固话
	private String address = "";// 用户地址
	private Product product;// 产品
	private String brand = "";// 品牌
	private String productSpec = "";//型号/规格
	@JsonAdapter(ServiceTypeSimpleAdapter.class)
	private ServiceType serviceType;// 服务项目
	private String description = "";// 服务描述
	private Integer qty = 1;// 数量
	private String errorMsg = "";// 错误信息
	private Integer successFlag = 0;// 成功标示
	private String orderNo = "";

	@JsonAdapter(DictSimpleAdapter.class)
	private Dict expressCompany;
	private String expressNo = "";
	private String thdNo; //第三方单号

	//导入的时间(毫秒)
	private Long CreateTimeMillis = 0L;
	//是否可保存到导入订单表
    //可忽略的检查错误，可保存，如重单
	private int canSave = 1;
	//行号，导入时的excel中行号
	private int lineNumber = 0;
	//重复订单号
	private String repeateOrderNo = "";
	//失败次数
	private int retryTimes = 0;
	//排序
	private int sort = 0;

	private B2bCustomerMap b2bShop;
	//店铺id清单
	private List<String> shopIds;

	//原始信息
	private String orgServiceType;
	private String orgProduct;
	private String orgProductSpec;
	private String orgExpressCompany;
	private String orgDesription;
	private String define1;
	private String define2;
	private String define3;
	private String orgQty;


	public TempOrder() {
	}


	public TempOrder(Long id) {
		super(id);
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getProductSpec() {
		return productSpec;
	}

	public void setProductSpec(String productSpec) {
		this.productSpec = productSpec;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Integer getSuccessFlag() {
		return successFlag;
	}

	public void setSuccessFlag(Integer successFlag) {
		this.successFlag = successFlag;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Dict getExpressCompany() {
		return expressCompany;
	}

	public void setExpressCompany(Dict expressCompany) {
		this.expressCompany = expressCompany;
	}

	public String getExpressNo() {
		return expressNo;
	}

	public void setExpressNo(String expressNo) {
		this.expressNo = expressNo;
	}

	public Long getCreateTimeMillis() {
		return CreateTimeMillis;
	}

	public void setCreateTimeMillis(Long createTimeMillis) {
		CreateTimeMillis = createTimeMillis;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TempOrder other = (TempOrder) obj;
        if (id != other.id)
            return false;
        if (phone == null) {
            if (other.phone != null)
                return false;
        } else if (!phone.equals(other.phone))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((phone == null) ? 0 : phone.hashCode());
        return result;
    }

	public int getCanSave() {
		return canSave;
	}

	public void setCanSave(int canSave) {
		this.canSave = canSave;
	}

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

	public String getRepeateOrderNo() {
		return repeateOrderNo;
	}

	public void setRepeateOrderNo(String repeateOrderNo) {
		this.repeateOrderNo = repeateOrderNo;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public String getOrgDesription() {
		return orgDesription;
	}

	public void setOrgDesription(String orgDesription) {
		this.orgDesription = orgDesription;
	}

	public String getDefine1() {
		return define1;
	}

	public void setDefine1(String define1) {
		this.define1 = define1;
	}

	public String getDefine2() {
		return define2;
	}

	public void setDefine2(String define2) {
		this.define2 = define2;
	}

	public String getDefine3() {
		return define3;
	}

	public void setDefine3(String define3) {
		this.define3 = define3;
	}

	public String getOrgServiceType() {
		return orgServiceType;
	}

	public void setOrgServiceType(String orgServiceType) {
		this.orgServiceType = orgServiceType;
	}

	public String getOrgProduct() {
		return orgProduct;
	}

	public void setOrgProduct(String orgProduct) {
		this.orgProduct = orgProduct;
	}

	public String getOrgExpressCompany() {
		return orgExpressCompany;
	}

	public void setOrgExpressCompany(String orgExpressCompany) {
		this.orgExpressCompany = orgExpressCompany;
	}

	public String getOrgProductSpec() {
		return orgProductSpec;
	}

	public void setOrgProductSpec(String orgProductSpec) {
		this.orgProductSpec = orgProductSpec;
	}

	public String getOrgQty() {
		return orgQty;
	}

	public void setOrgQty(String orgQty) {
		this.orgQty = orgQty;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getThdNo() {
		return thdNo;
	}

	public void setThdNo(String thdNo) {
		this.thdNo = thdNo;
	}

	public B2bCustomerMap getB2bShop() {
		return b2bShop;
	}

	public void setB2bShop(B2bCustomerMap b2bShop) {
		this.b2bShop = b2bShop;
	}

	public List<String> getShopIds() {
		return shopIds;
	}

	public void setShopIds(List<String> shopIds) {
		this.shopIds = shopIds;
	}
}
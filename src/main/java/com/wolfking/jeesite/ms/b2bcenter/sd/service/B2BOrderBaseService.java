package com.wolfking.jeesite.ms.b2bcenter.sd.service;

import cn.hutool.core.util.StrUtil;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerFinance;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.ProductCategoryService;
import com.wolfking.jeesite.modules.md.service.ProductService;
import com.wolfking.jeesite.modules.md.service.ServiceTypeService;
import com.wolfking.jeesite.modules.sd.entity.Order;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.UserKeFuService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.ms.b2bcenter.exception.*;
import com.wolfking.jeesite.ms.b2bcenter.md.service.B2BCustomerMappingService;
import com.wolfking.jeesite.ms.b2bcenter.sd.dao.B2BOrderDao;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderConvertVModel;
import com.wolfking.jeesite.ms.b2bcenter.sd.entity.B2BOrderVModel;
import com.wolfking.jeesite.ms.providersys.service.MSSysOfficeService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import ma.glasnost.orika.MapperFacade;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class B2BOrderBaseService {

    @Autowired
    protected CustomerService customerService;
    @Autowired
    protected ServiceTypeService serviceTypeService;
    @Autowired
    protected ProductService productService;
    @Autowired
    protected OrderService orderService;
    @Autowired
    protected AreaService areaService;
    @Autowired
    protected MapperFacade mapperFacade;
    @Resource
    private B2BOrderDao b2BOrderDao;
    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private UserKeFuService userKeFuService;

    @Autowired
    private MSSysOfficeService sysOfficeService;
    @Autowired
    private B2BCustomerMappingService customerMappingService;

    /**
     * 查询工单的id, order_no, quarter
     */
    public Order getOrderInfo(int dateSource, String b2bOrderNo, String quarter) {
        Order order = null;
        if (dateSource != 0 && StringUtils.isNotBlank(b2bOrderNo)) {
            order = b2BOrderDao.getOrderInfo(dateSource, b2bOrderNo, quarter);
        }
        return order;
    }

    /**
     * 查询工单的id, order_no, quarter
     */
    public Order getOrderInfoByB2BOrderId(int dateSource, Long b2bOrderId, String quarter) {
        Order order = null;
        if (dateSource != 0 && b2bOrderId != 0) {
            order = b2BOrderDao.getOrderInfoByB2BOrderId(dateSource, b2bOrderId, quarter);
        }
        return order;
    }


    /**
     * 检查B2B工单基本属性
     */
    protected void validateBasicProperties(B2BOrderVModel orderVModel) {
        List<String> errorMsgList = Lists.newArrayList();
        if (StringUtils.isBlank(orderVModel.getOrderNo())) {
            errorMsgList.add("缺少B2B工单ID");
        }
        if (orderVModel.getDataSource() == null || !B2BDataSourceEnum.isB2BDataSource(orderVModel.getDataSource())) {
            errorMsgList.add("数据源设置错误");
        }
        if (StringUtils.isBlank(orderVModel.getUserName())) {
            errorMsgList.add("缺少用户姓名");
        }
        if (StringUtils.isBlank(orderVModel.getUserAddress())) {
            errorMsgList.add("缺少用户地址");
        }
        if (StringUtils.isBlank(orderVModel.getUserMobile()) && StringUtils.isBlank(orderVModel.getUserPhone())) {
            errorMsgList.add("缺少用户电话");
        }
        if (StringUtils.isBlank(orderVModel.getFirstProductCode())) {
            errorMsgList.add("缺少产品编码");
        }
        if (orderVModel.getProductQty() == 0) {
            errorMsgList.add("工单项的产品数量为0");
        }
        if (orderVModel.getOrderItemQty() > 10) {
            errorMsgList.add("工单项数超过10项，不允许自动转单");
        }
        if (StringUtils.isNotBlank(orderVModel.getUserMobile())) {
            String checkMobile = StringUtils.isPhoneWithRelaxed(StringUtils.trim(orderVModel.getUserMobile()));
            if (!checkMobile.isEmpty()) {
                errorMsgList.add(checkMobile);
            }
        }
        if (errorMsgList.size() > 0) {
            String errorMsg = String.join("；", errorMsgList);
            throw new IncompleteB2BOrderException(errorMsg);
        }
    }

    /**
     * 检查B2B工单基本属性
     */
    protected void validateBasicPropertiesForManual(B2BOrderVModel orderVModel) {
        List<String> errorMsgList = Lists.newArrayList();
        if (StringUtils.isBlank(orderVModel.getOrderNo())) {
            errorMsgList.add("缺少B2B工单ID");
        }
        if (orderVModel.getDataSource() == null || !B2BDataSourceEnum.isB2BDataSource(orderVModel.getDataSource())) {
            errorMsgList.add("数据源设置错误");
        }
        if (StringUtils.isBlank(orderVModel.getUserName())) {
            errorMsgList.add("缺少用户姓名");
        }
        if (StringUtils.isBlank(orderVModel.getUserAddress())) {
            errorMsgList.add("缺少用户地址");
        }
        if (StringUtils.isBlank(orderVModel.getUserMobile()) && StringUtils.isBlank(orderVModel.getUserPhone())) {
            errorMsgList.add("缺少用户电话");
        }
        if (StringUtils.isBlank(orderVModel.getFirstProductCode())) {
            errorMsgList.add("缺少产品编码");
        }
        if (orderVModel.getProductQty() == 0) {
            errorMsgList.add("工单项的产品数量为0");
        }
        if (errorMsgList.size() > 0) {
            String errorMsg = String.join("；", errorMsgList);
            throw new IncompleteB2BOrderException(errorMsg);
        }
    }

    /**
     * 解析地址
     */
    protected String[] parseAddress(String userAddress) {
        String[] result;
        try {
            //result = AreaUtils.parseAddress(userAddress.replace(" ", ""));  //mark on 2020-8-5
            result = AreaUtils.parseAddressFromMS(userAddress.replace(" ", ""));    //add on 2020-8-5
        } catch (Exception e) {
            throw new AddressParseFailureException("工单地址解析失败");
        }
        if (result == null || result.length == 0) {
            throw new AddressParseFailureException("工单地址解析失败");
        }
        if (result.length > 2 && result[2].length() > B2BOrderConvertVModel.ADDRESS_MAX_LENGTH) {
            throw new AddressParseFailureException("详细地址长度超过数据库设定:" + String.valueOf(B2BOrderConvertVModel.ADDRESS_MAX_LENGTH));
        }
        return result;
    }

    /**
     * 解析地址
     */
    protected String[] parseAddressNew(String userAddress) {
        String[] result;
        try {
            // result = AreaUtils.parseAddress(userAddress.replace(" ", ""));
            //result = AreaUtils.decodeAddressGaode(userAddress.replace(" ", ""));  //mark on 2020-8-5
            result = AreaUtils.decodeAddressGaodeFromMS(userAddress.replace(" ", ""));  //add on 2020-8-5
        } catch (Exception e) {
            throw new AddressParseFailureException(String.format("工单地址解析失败：%s", e.getLocalizedMessage()));
        }
        if (result != null && result.length > 0) {
            for (int i = 0; i < result.length; i++) {
                if (StrUtil.isBlank(result[i])) {
                    result[i] = "";
                }
            }
        }
        if (result == null || result.length == 0) {
            throw new AddressParseFailureException("工单地址解析失败");
        }
        if (result[0] == null || result[0].equals("") || result[0].equals("0")) {
            throw new AddressParseFailureException("工单地址解析失败");
        }
        String detailAddress = null;
        if (result.length > 3) {
            detailAddress = result[3];
            if (result[3].length() > B2BOrderConvertVModel.ADDRESS_MAX_LENGTH) {
                throw new AddressParseFailureException("详细地址长度超过数据库设定:" + String.valueOf(B2BOrderConvertVModel.ADDRESS_MAX_LENGTH));
            }
        }
        if (StringUtils.isBlank(detailAddress)) {
            throw new AddressParseFailureException("工单地址解析失败：详细地址为空");
        }

        return result;
    }


    /**
     * 检查B2B工单是否已经被转换
     */
    protected void validateB2BOrderIsExists(String b2bOrderNo, int dataSourceId) {
        if (StringUtils.isNotBlank(b2bOrderNo) && B2BDataSourceEnum.isB2BDataSource(dataSourceId)) {
            Order order = getOrderInfo(dataSourceId, b2bOrderNo, null);
            if (order != null && StringUtils.isNotBlank(order.getOrderNo())) {
                throw new B2BOrderExistsException("工单已被转换", order.getId(), order.getOrderNo(), dataSourceId, order.getB2bOrderId(), b2bOrderNo);
            }
        }
    }

    protected void validateB2BOrderIsExistsNew(Long b2bOrderId, String b2bOrderNo, int dataSourceId) {
        if (dataSourceId == B2BDataSourceEnum.VIOMI.id || dataSourceId == B2BDataSourceEnum.INSE.id) {
            Order order = getOrderInfoByB2BOrderId(dataSourceId, b2bOrderId, null);
            if (order != null && StringUtils.isNotBlank(order.getOrderNo())) {
                throw new B2BOrderExistsException("工单已被转换", order.getId(), order.getOrderNo(), dataSourceId, b2bOrderId, order.getWorkCardId());
            }
        } else {
            validateB2BOrderIsExists(b2bOrderNo, dataSourceId);
        }
    }

    /**
     * 检查客户余额
     */
    protected void validateCustomerBalance(B2BOrderVModel orderVModel, double totalCharge, double blockedCharge) {
        CustomerFinance finance = orderVModel.getCustomer().getFinance();
        if (finance.getBalance() + (finance.getCreditFlag() == 1 ? finance.getCredit() : 0) - finance.getBlockAmount() - totalCharge - blockedCharge < 0) {
            throw new B2BOrderInsufficientBalanceException(orderVModel.getCustomer().getName() + " : 账户余额不足");
        }
    }

    /**
     * 获取随机客服
     */
    protected User getRandomKefu(Long customerId, Long areaId, Long productCategoryId, int kefuType, Long cityId, Long provinceId) {
        User kefu = orderService.getRandomKefu(customerId, areaId, productCategoryId, kefuType, cityId, provinceId);
        if (kefu == null) {
            ProductCategory productCategory = productCategoryService.getFromCache(productCategoryId);
            Area area = areaService.get(areaId);
            String tip = "【".concat(area == null ? "" : area.getFullName()).concat("】暂无负责【").concat(productCategory == null ? "" : productCategory.getName()).concat("】客服，请联系");
            Customer customer = customerService.getFromCache(customerId);
            User userSupervisor = userKeFuService.getKefuSupervisor(customer, areaId, productCategoryId, kefuType, cityId, provinceId);
            if (userSupervisor != null) {
                Office office = sysOfficeService.get(userSupervisor.getOfficeId());
                if (office != null) {
                    tip = tip + office.getName() + "主管配置区域后下单。";
                }
            }
            throw new B2BOrderTranserFailureException(tip);
        }
        return kefu;
    }

    /**
     * 生成快可立单号
     */
    protected String generateOrderNo() {
        String orderNo = "";
        try {
            orderNo = orderService.getNewOrderNo();
            if (StringUtils.isBlank(orderNo)) {
                orderNo = orderService.getNewOrderNo();
            }
        } catch (Exception e) {
            throw new B2BOrderTranserFailureException("生成订单号失败，请重试");
        }
        if (StringUtils.isBlank(orderNo)) {
            throw new B2BOrderTranserFailureException("生成订单号失败，请重试");
        }
        return orderNo;
    }

    /**
     * 生成快可立单号
     */
    protected String generateOrderNoNew(Integer dataSourceId, String kklOrderNo) {
        String orderNo = "";
        if (dataSourceId != null && dataSourceId == B2BDataSourceEnum.VIOMI.id) {
            if (StringUtils.isBlank(kklOrderNo)) {
                throw new B2BOrderTranserFailureException(B2BDataSourceEnum.VIOMI.name + "B2B工单缺少快可立单号");
            }
            orderNo = kklOrderNo;
        } else {
            orderNo = generateOrderNo();
        }
        return orderNo;
    }

    /**
     * 生成下单时间
     */
    protected Date generateOrderCreateDate(Integer dataSourceId, Long b2bOrderCreateDt) {
        Date orderCreateDate = null;
        if (dataSourceId != null && dataSourceId == B2BDataSourceEnum.VIOMI.id) {
            if (b2bOrderCreateDt == null || b2bOrderCreateDt == 0) {
                throw new B2BOrderTranserFailureException(B2BDataSourceEnum.VIOMI.name + "B2B工单缺少下单时间");
            }
            orderCreateDate = new Date(b2bOrderCreateDt);
        } else {
            orderCreateDate = new Date();
        }
        return orderCreateDate;
    }

    /**
     * 获取工单状态
     */
    protected Dict getOrderStatus(User user) {
        Dict status;
        if (user.isSystemUser()) {
            status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");
        } else if (user.isCustomer() && user.getCustomerAccountProfile().getOrderApproveFlag() == 0) {
            status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_APPROVED), "order_status");
        } else {
            status = MSDictUtils.getDictByValue(String.valueOf(Order.ORDER_STATUS_NEW), "order_status");
        }
        return status;
    }

    /**
     * 根据数据源与店铺ID获取B2B工单的下单人
     *
     * @param dataSourceId
     * @param shopId
     * @return
     */
    protected Long getB2BOrderCreateBy(Integer dataSourceId, String shopId, Long defaultCreateById) {
        Long createById = defaultCreateById;
        if (dataSourceId != null && StrUtil.isNotEmpty(shopId)) {
            Long accountId = customerMappingService.getCustomerAccountIdByShopIdAndDataSource(dataSourceId, shopId);
            if (accountId != null && accountId > 0) {
                createById = accountId;
            }
        }
        return createById;
    }


}

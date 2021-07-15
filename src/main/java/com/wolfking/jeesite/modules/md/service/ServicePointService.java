package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointMessage;
import com.kkl.kklplus.entity.es.mq.MQSyncServicePointStationMessage;
import com.kkl.kklplus.entity.es.mq.MQSyncType;
import com.kkl.kklplus.entity.fi.mq.MQInserDefaultsMessage;
import com.kkl.kklplus.entity.md.*;
import com.kkl.kklplus.entity.md.dto.*;
import com.kkl.kklplus.entity.md.mq.MQServicePointPriceMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.exception.OrderException;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.service.SequenceIdService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.api.entity.fi.RestServicePointBalance;
import com.wolfking.jeesite.modules.api.entity.md.*;
import com.wolfking.jeesite.modules.api.util.RestEnum;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.fi.dao.EngineerCurrencyDao;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrency;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrencyDeposit;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayCondition;
import com.wolfking.jeesite.modules.fi.entity.ServicePointPayableMonthly;
import com.wolfking.jeesite.modules.md.dao.ServicePointDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointPlanRemarkModel;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.mq.sender.InsertFIDefaultsSender;
import com.wolfking.jeesite.modules.mq.sender.ServicePointPriceSender;
import com.wolfking.jeesite.modules.mq.sender.ServicePointSender;
import com.wolfking.jeesite.modules.mq.sender.ServicePointStationSender;
import com.wolfking.jeesite.modules.rpt.entity.ServicePointServiceArea;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.ThreeTuple;
import com.wolfking.jeesite.modules.sd.entity.TwoTuple;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import com.wolfking.jeesite.modules.sys.utils.DictUtils;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.globalmapping.service.ProductCategoryServicePointMappingService;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ibatis.annotations.Param;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 服务网点
 * Ryan Lu
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ServicePointService extends LongIDCrudService<ServicePointDao, ServicePoint> {

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private UserDao userDao;

    @Autowired
    private ServiceTypeService typeService;

    @Autowired
    private ProductPriceService productPriceService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MapperFacade mapper;

    @Resource
    private EngineerCurrencyDao engineerCurrencyDao;

    @Autowired
    private InsertFIDefaultsSender insertFIDefaultsSender;

    @Autowired
    private MSUserService msUserService;

    @Autowired
    private ServicePointLogService servicePointLogService;

    @Autowired
    private ServicePointSender servicePointSender;

    @Autowired
    private ServicePointStationSender servicePointStationSender;

    @Autowired
    private ServicePointStationService servicePointStationService;

    @Value("${SyncServicePoint2ES}")
    private boolean syncServicePoint2ES;

    @Autowired
    private MSProductService msProductService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVerSecondService productVerSecondService;

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private MSEngineerService msEngineerService;

    @Autowired
    private EngineerService engineerService;

    @Autowired
    private MSEngineerAreaService msEngineerAreaService;

    @Autowired
    private MSServicePointAreaService msServicePointAreaService;

    @Autowired
    private MSServicePointProductService msServicePointProductService;

    @Autowired
    private MSServicePointPriceService msServicePointPriceService;

    @Autowired
    private MSServicePointStationService msServicePointStationService;

    @Autowired
    private MSProductPriceService msProductPriceService;

    @Autowired
    private ServicePointPriceSender servicePointPriceSender;

    @Autowired
    private MSProductCategoryServicePointService msProductCategoryServicePointService;

    private final static int DISCOUNT_FLAG_DISABLED = 0;  //停用扣点标志

    @Autowired
    private ServicePointFinanceService servicePointFinanceService;

    @Autowired
    private MSSysAreaService msSysAreaService;

    @Autowired
    private MSCommonQueryService msCommonQueryService;

    @Autowired
    private MSDepositLevelService msDepositLevelService;

    //region 网点

    public ServicePoint get(Long id) {
        return getWithExtendPropertyFromMaster(id);
    }

    public ServicePoint getWithExtendPropertyFromMaster(long id) {
        ServicePoint servicePoint = msServicePointService.getById(id);
        if (servicePoint != null && servicePoint.getProductCategoryIds() != null && !servicePoint.getProductCategoryIds().isEmpty()) {
            Map<Long, ProductCategory> productCategoryMap = ProductUtils.getAllProductCategoryMap();
            List<ProductCategory> productCategories = Lists.newArrayList();
            ProductCategory productCategory;
            for (Long pId : servicePoint.getProductCategoryIds()) {
                productCategory = productCategoryMap.get(pId);
                if (productCategory != null) {
                    productCategories.add(productCategory);
                }
            }
            servicePoint.setProductCategories(productCategories);
        }
        if (servicePoint != null && servicePoint.getCreateBy() != null && servicePoint.getCreateBy().getId() != null) {
            User user = UserUtils.get(servicePoint.getCreateBy().getId());
            servicePoint.setCreateBy(user);
        }
        if (servicePoint != null && servicePoint.getUpdateBy() != null && servicePoint.getUpdateBy().getId() != null) {
            User user = UserUtils.get(servicePoint.getUpdateBy().getId());
            servicePoint.setUpdateBy(user);
        }
        if(servicePoint != null && servicePoint.getMdDepositLevel() != null && servicePoint.getMdDepositLevel().getId() != null){
            MDDepositLevel depositLevel = msDepositLevelService.getById(servicePoint.getMdDepositLevel().getId());
            servicePoint.setMdDepositLevel(depositLevel);
        }

        return getServicePointExtraProperties(servicePoint, true);
    }

    public ServicePoint getSimple(Long id) {
        return msServicePointService.getSimpleById(id);
    }

    /**
     * 获取网点信息及网点的区，市，省区域名称
     * @param servicePointId
     * @return
     */
    public ServicePoint getServicePointAndTripleAreaById(Long servicePointId) {
        ServicePoint servicePoint = msServicePointService.getById(servicePointId);
        if (servicePoint != null){
            Area area = areaService.getThreeLevelAreaById(servicePoint.getArea().getId());
            servicePoint.setArea(area);
        }
        return servicePoint;
    }

    /**
     * 获取网点其他额外的属性
     * @param servicePoint
     * @return
     */
    public ServicePoint getServicePointExtraProperties(ServicePoint servicePoint, boolean bFromMaster) {
        if (servicePoint == null) {
            return servicePoint;
        }
        // add on 2019-9-10
        // 获取finance
        ServicePointFinance servicePointFinance = null;
        if (bFromMaster) {
            servicePointFinance = dao.getFinanceFromMaster(servicePoint.getId());  //add on 2020-3-18
        } else {
            //servicePointFinance = dao.getFinanceNew(servicePoint.getId());  // mark on 2020-5-5
            servicePointFinance = servicePointFinanceService.getFromCache(servicePoint.getId()); // add on 2020-5-5 //从网点财务缓存中获取
        }
        if (servicePointFinance == null) {
            return null;
        }
        servicePoint.setFinance(servicePointFinance);
        // 获取主账号信息
        if (servicePoint.getPrimary() != null && servicePoint.getPrimary().getId() != null) {
            Engineer engineerFromMS = null;
            if (bFromMaster) {  // 从微服务DB中获取
                engineerFromMS = msEngineerService.getById(servicePoint.getPrimary().getId());
            } else {
                engineerFromMS = msEngineerService.getByIdFromCache(servicePoint.getPrimary().getId());
            }
            if (engineerFromMS != null) {
                servicePoint.setPrimary(engineerFromMS);
            }
            // add on 2019-10-21 end
        }
        // 获取区域信息
        if (servicePoint.getArea() != null && servicePoint.getArea().getId() != null) {
            Area area = areaService.getFromCache(servicePoint.getArea().getId());
            servicePoint.setArea(area);
        }

        //切换为微服务
        if (servicePoint.getFinance().getPaymentType() != null && Integer.parseInt(servicePoint.getFinance().getPaymentType().getValue()) > 0) {
            String paymentTypeLabel = MSDictUtils.getDictLabel(servicePoint.getFinance().getPaymentType().getValue(), "PaymentType", "");
            servicePoint.getFinance().getPaymentType().setLabel(paymentTypeLabel);
        }
        if (servicePoint.getFinance().getBank() != null && Integer.parseInt(servicePoint.getFinance().getBank().getValue()) > 0) {
            String bankName = MSDictUtils.getDictLabel(servicePoint.getFinance().getBank().getValue(), "banktype", "");
            servicePoint.getFinance().getBank().setLabel(bankName);
        }
        if (servicePoint.getLevel() != null && Integer.parseInt(servicePoint.getLevel().getValue()) > 0) {
            //String levelName = MSDictUtils.getDictLabel(servicePoint.getLevel().getValue(), "ServicePointLevel", "");
            //servicePoint.getLevel().setLabel(levelName);
            Dict servicePointLevel = MSDictUtils.getDictByValue(servicePoint.getLevel().getValue(), "ServicePointLevel");
            if(servicePointLevel != null){
                servicePoint.setLevel(servicePointLevel);
            }
        }
        if (servicePoint.getFinance() != null && servicePoint.getFinance().getUnit() != null && StringUtils.isNotBlank(servicePoint.getFinance().getUnit().getValue())) {
            String unitName = MSDictUtils.getDictLabel(servicePoint.getFinance().getUnit().getValue(), "unit", "");
            servicePoint.getFinance().getUnit().setLabel(unitName);
        }
        if (servicePoint.getFinance() != null && servicePoint.getFinance().getBankIssue() != null &&
                StringUtils.toInteger(servicePoint.getFinance().getBankIssue().getValue()) > 0) {
            String bankIssueName = MSDictUtils.getDictLabel(servicePoint.getFinance().getBankIssue().getValue(), "BankIssueType", "");
            servicePoint.getFinance().getBankIssue().setLabel(bankIssueName);
        }

        return servicePoint;
    }

    //region 缓存操作
    public ServicePoint getSimpleFromCacheById(Long id) {
        //
        // add on 2020-4-21 返回属性： id,servicePointNo,name,primaryId,customizePriceFlag
        //
        ServicePoint servicePoint = msServicePointService.getSimpleCacheById(id);
        if (servicePoint == null) {
            servicePoint = new ServicePoint(id);
        }
        return servicePoint;
    }

    /**
     * 按id获得网点信息
     * 优先从缓存中取
     *
     * @param id
     * @return
     */
    public ServicePoint getFromCache(Long id) {
        ServicePoint servicePoint = msServicePointService.getCacheById(id);
        if (servicePoint != null && servicePoint.getProductCategoryIds() != null && !servicePoint.getProductCategoryIds().isEmpty()) {
            Map<Long, ProductCategory> productCategoryMap = ProductUtils.getAllProductCategoryMap();
            List<ProductCategory> productCategories = Lists.newArrayList();
            ProductCategory productCategory;
            for (Long pId : servicePoint.getProductCategoryIds()) {
                productCategory = productCategoryMap.get(pId);
                if (productCategory != null) {
                    productCategories.add(productCategory);
                }
            }
            servicePoint.setProductCategories(productCategories);
        }
        return getServicePointExtraProperties(servicePoint, false);
    }

    /**
     * 按id获得网点信息
     * 优先从缓存中取
     *
     * @param id
     * @return
     */
    public ServicePoint getFromCacheAsRequired(Long id,Integer requiredTags) {
        if(id == null || id <= 0){
            return null;
        }
        ServicePoint servicePoint = msServicePointService.getCacheById(id);
        if(requiredTags == null || requiredTags <= 0){
            return servicePoint;
        }
        if (servicePoint.getLevel() != null && Integer.parseInt(servicePoint.getLevel().getValue()) > 0) {
            Dict servicePointLevel = MSDictUtils.getDictByValue(servicePoint.getLevel().getValue(), "ServicePointLevel");
            //String levelName = MSDictUtils.getDictLabel(servicePoint.getLevel().getValue(), "ServicePointLevel", "");
            //servicePoint.getLevel().setLabel(levelName);
            if(servicePointLevel != null){
                servicePoint.setLevel(servicePointLevel);
            }
        }
        if(ServicePointRequiredTagEnum.PRIMAY.hasTag(requiredTags)){
            Long primaryId = Optional.ofNullable(servicePoint.getPrimary()).map(t->t.getId()).orElse(0L);
            if(primaryId>0) {
                Engineer engineer = msEngineerService.getByIdFromCache(primaryId);
                if (engineer != null) {
                    servicePoint.setPrimary(engineer);
                }
            }
        }
        if(ServicePointRequiredTagEnum.CATEGORY_NAMES.hasTag(requiredTags)){
            if (!CollectionUtils.isEmpty(servicePoint.getProductCategoryIds())) {
                Map<Long, ProductCategory> productCategoryMap = ProductUtils.getAllProductCategoryMap();
                List<ProductCategory> productCategories = Lists.newArrayList();
                ProductCategory productCategory;
                for (Long pId : servicePoint.getProductCategoryIds()) {
                    productCategory = productCategoryMap.get(pId);
                    if (productCategory != null) {
                        productCategories.add(productCategory);
                    }
                }
                servicePoint.setProductCategories(productCategories);
            }
        }
        if(ServicePointRequiredTagEnum.AREA.hasTag(requiredTags)){
            if(servicePoint.getArea() != null && servicePoint.getArea().getId() != null && servicePoint.getArea().getId() > 0 && StringUtils.isBlank(servicePoint.getArea().getName())) {
                Area area = areaService.getFromCache(servicePoint.getArea().getId());
                if (area != null) {
                    servicePoint.setArea(area);
                }
            }
        }
        if(ServicePointRequiredTagEnum.FINANCE.hasTag(requiredTags)){
            ServicePointFinance servicePointFinance = servicePointFinanceService.getFromCache(id);
            if (servicePointFinance != null) {
                servicePoint.setFinance(servicePointFinance);
            }
        }
        if(ServicePointRequiredTagEnum.FINANCE_BANK_INFO.hasTag(requiredTags) && ServicePointRequiredTagEnum.FINANCE.hasTag(requiredTags)){
            ServicePointFinance finance = servicePoint.getFinance();
            if (finance.getPaymentType() != null && Integer.parseInt(finance.getPaymentType().getValue()) > 0) {
                String paymentTypeLabel = MSDictUtils.getDictLabel(finance.getPaymentType().getValue(), "PaymentType", "");
                finance.getPaymentType().setLabel(paymentTypeLabel);
            }
            if (finance.getBank() != null && Integer.parseInt(finance.getBank().getValue()) > 0) {
                String bankName = MSDictUtils.getDictLabel(finance.getBank().getValue(), "banktype", "");
                finance.getBank().setLabel(bankName);
            }
            /*货币
            if (finance.getUnit() != null && StringUtils.isNotBlank(finance.getUnit().getValue())) {
                String unitName = MSDictUtils.getDictLabel(finance.getUnit().getValue(), "unit", "");
                finance.getUnit().setLabel(unitName);
            }*/
            if (finance.getBankIssue() != null && StringUtils.toInteger(finance.getBankIssue().getValue()) > 0) {
                String bankIssueName = MSDictUtils.getDictLabel(finance.getBankIssue().getValue(), "BankIssueType", "");
                finance.getBankIssue().setLabel(bankIssueName);
            }
        }
        return servicePoint;
    }

    /**
     * 获得网点下安维列表
     * @param servicePointId 网点Id
     */
    public List<Engineer> getEngineerListOfServicePoint(Long servicePointId){
        List<Engineer> engineerList = getEngineersFromCache(servicePointId);
        return engineerList;
    }


    /**
     * 获得网点下所有安维人员清单
     *
     * @param id 网点id
     * @return
     */
    public List<Engineer> getEngineersFromCache(Long id) {
        // add on 2019-11-9 begin
        List<Engineer> list = Lists.newArrayList();
        List<Engineer> engineerList = msEngineerService.findEngineerByServicePointIdFromCache(id);
        if (engineerList == null || engineerList.isEmpty()) {
            return engineerList;
        }
        engineerList = engineerList.stream().filter(engineer -> engineer.getDelFlag().equals(Engineer.DEL_FLAG_NORMAL)).collect(Collectors.toList());
        List<Long> engineerIds = engineerList.stream().map(Engineer::getId).collect(Collectors.toList());
        List<User> userList = systemService.findEngineerAccountList(engineerIds, null);
        Map<Long, User> userMap = userList != null && !userList.isEmpty() ? userList.stream().filter(r->r.getEngineerId() != null).collect(Collectors.toMap(User::getEngineerId, Function.identity())):Maps.newHashMap();

        engineerList.stream().forEach(engineer -> {
            User user = userMap.get(engineer.getId());
            if (user != null) {
                engineer.setAppLoged(user.getAppLoged());
                engineer.setAccountId(user.getId());
            }

            if (engineer.getLevel() != null && Integer.parseInt(engineer.getLevel().getValue()) > 0) {
                String levelName = MSDictUtils.getDictLabel(engineer.getLevel().getValue(), "ServicePointLevel", "");
                engineer.getLevel().setLabel(levelName);
            }
        });
        list.addAll(engineerList);
        //return engineerList;
        // add on 2019-11-9 end

        if (list == null || list.size() == 0) {
            Engineer engineer = new Engineer();
            engineer.setServicePoint(new ServicePoint(id));
            list = findEngineerList(engineer);//切换为微服务
        }
        return list;
    }

    /**
     * 获得网点下某个安维人员信息
     *
     * @param servicePointId 网点id
     * @param engineerId     安维id
     * @return
     */
    public Engineer getEngineerFromCache(Long servicePointId, Long engineerId) {
        Engineer engineer = null;
        if (servicePointId == null || engineerId == null) {
            return engineer;
        }
        // add on 2019-10-31 begin
        engineer = msEngineerService.getEngineerFromCache(servicePointId, engineerId);
        if (engineer != null) {
            User user = systemService.getUserByEngineerId(engineer.getId());
            if (user != null) {
                engineer.setAppLoged(user.getAppLoged());
                engineer.setAccountId(user.getId());
            }

            if (engineer.getLevel() != null && Integer.parseInt(engineer.getLevel().getValue()) > 0) {
                String levelName = MSDictUtils.getDictLabel(engineer.getLevel().getValue(), "ServicePointLevel", "");
                engineer.getLevel().setLabel(levelName);
            }
        }

        return engineer;
        // add on 2019-10-31 end
    }

    /**
     *  按需读取网点价格
     * @param servicePointId    网点id
     * @param products  NameValuePair<产品id,服务项目id>
     * @return
     */
    public List<ServicePrice> getPricesByProductsFromCache(Long servicePointId,List<NameValuePair<Long,Long>> products){
        return msServicePointPriceService.findPricesListByCustomizePriceFlagFromCache(servicePointId,products);
    }

    /**
     *  按需读取网点价格
     * @param servicePointId    网点id
     * @param products  NameValuePair<产品id,服务项目id>
     * @return
     */
    public Map<String,ServicePrice> getPriceMapByProductsFromCache(Long servicePointId,List<NameValuePair<Long,Long>> products){
        List<ServicePrice> prices = msServicePointPriceService.findPricesListByCustomizePriceFlagFromCache(servicePointId,products);
        if(prices==null){
            return null;
        }
        if(CollectionUtils.isEmpty(prices)){
            return Maps.newHashMapWithExpectedSize(0);
        }
        return prices.stream().collect(Collectors.toMap(
                e-> String.format("%d:%d",e.getProduct().getId(),e.getServiceType().getId()),
                e-> e
        ));
    }

    /**
     * 按需读取网点偏远区域价格
     *
     * @param servicePointId 网点id
     * @param products       NameValuePair<产品id,服务项目id>
     * @return
     */
    public Map<String, ServicePrice> getRemotePriceMapByProductsFromCache(Long servicePointId, List<NameValuePair<Long, Long>> products) {
        List<ServicePrice> prices = msServicePointPriceService.findPricesListByRemotePriceFlagFromCacheForSD(servicePointId, products);
        if (prices == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(prices)) {
            return Maps.newHashMapWithExpectedSize(0);
        }
        return prices.stream().collect(Collectors.toMap(
                e -> String.format("%d:%d", e.getProduct().getId(), e.getServiceType().getId()),
                e -> e
        ));
    }

    /**
     *  按需读取网点价格
     * @param servicePointId    网点id
     * @param productId         产品id
     * @param serviceTypeId     服务项目id
     * @return
     */
    public ServicePrice getPriceByProductAndServiceTypeFromCache(long servicePointId,long productId,long serviceTypeId){
        if(servicePointId <= 0 || productId <= 0 || serviceTypeId <= 0){
            return null;
        }
        List<ServicePrice> prices =  this.getPricesByProductsFromCache(servicePointId,Lists.newArrayList(new NameValuePair<Long,Long>(productId,serviceTypeId)));
        if(CollectionUtils.isEmpty(prices)){
            return null;
        }
        return prices.get(0);
    }

    //endregion 缓存操作

    @Transactional(readOnly = false)
    public void insertServicePoint(ServicePoint servicePoint) {
        //
        // 此方法只添加新网点  add on 2020-5-19
        //
        boolean isNew = servicePoint.getIsNewRecord();
        if (!isNew) {
            return;
        }
        //  检查微服务连接是否通畅
        msCommonQueryService.checkConnection();

        Long servicePointId = null;
        String lockkey = null;
        Boolean locked = false;
        ServicePointFinance finance = servicePoint.getFinance();
        String bankNo = finance.getBankNo();
        Dict dictBankIssue = finance.getBankIssue();
        if (StringUtils.isBlank(bankNo) && (dictBankIssue == null || StringUtils.isBlank(dictBankIssue.getValue()) || dictBankIssue.getValue().equalsIgnoreCase("0"))) {
            dictBankIssue = new Dict("1", "暂无付款账号");//1-暂无付款账号
            finance.setBankIssue(dictBankIssue);
        }else{
            Dict nbankIssue = MSDictUtils.getDictByValue(dictBankIssue.getValue(), "BankIssueType");//切换为微服务
            if (nbankIssue != null) {
                finance.setBankIssue(nbankIssue);
            }
        }

        //同步paymentType,bank,bank_no,bank_owner,invoice_flag at 2018/08/30 by ryan
        servicePoint.setPaymentType(finance.getPaymentType());
        servicePoint.setInvoiceFlag(finance.getInvoiceFlag());
        servicePoint.setBank(finance.getBank());
        servicePoint.setBankNo(finance.getBankNo());
        servicePoint.setBankOwner(finance.getBankOwner());
        servicePoint.setBankIssue(finance.getBankIssue());
        servicePoint.setDiscountFlag(finance.getDiscountFlag());//2018/12/07 ryan
        if (finance.getDiscountFlag() == DISCOUNT_FLAG_DISABLED) {
            finance.setDiscount(0.0D);   //add on 2020-4-6
        } else {
            double discount = finance.getDiscount();
            if (discount >0) {
                discount = discount/100;
                finance.setDiscount(discount);
            }
        }
        //end
        if(servicePoint.getDegree() == 30){
            servicePoint.setInsuranceFlag(0);
            servicePoint.setDepositFromOrderFlag(0);
            servicePoint.getMdDepositLevel().setId(0L);
            updateInsuranceFlag(servicePoint.getId(),servicePoint.getInsuranceFlag());

            servicePoint.setTimeLinessFlag(0);  //返现网点，关闭快可立时效
        }
        if(servicePoint.getRemotePriceEnabledFlag() == 1){
            servicePoint.setRemotePriceType(40);
            servicePoint.setRemotePriceFlag(1);
        }else {
            servicePoint.setRemotePriceType(0);
            servicePoint.setRemotePriceFlag(0);
        }
        //add on 2020-5-20 界面数据获取 begin
        //1. 获取网点产品列表,网点品类列表
        List<Long> products = Lists.newArrayList();
        if (StringUtils.isNoneBlank(servicePoint.getProductIds())) {
            products = Arrays.stream(servicePoint.getProductIds().split(","))
                    .map(t -> Long.valueOf(t))
                    .collect(Collectors.toList());

            log.warn("网点产品:{}", products);
            if (!products.isEmpty()) {
                Map<Long, Product> productMap = productService.getProductMap(products);
                List<Long> productCategoryIds = productMap.values().stream().filter(i->i.getCategory() != null && i.getCategory().getId() != null)
                        .map(i->i.getCategory().getId()).distinct().collect(Collectors.toList());
                servicePoint.setProductCategoryIds(productCategoryIds);
                log.warn("网点品类:{}", servicePoint.getProductCategoryIds());
            }
        }
        //2. 获取网点区域
        List<Long> areas = Lists.newArrayList();        //省,市,区/县id列表
        List<Long> townAreaIds = Lists.newArrayList();  //乡镇/街道id列表
        if (StringUtils.isNoneBlank(servicePoint.getAreaIds())) {
            String areaIds = servicePoint.getAreaIds().replace("&quot;", "\"");
            servicePoint.setAreaIds(areaIds);
            JSONArray jsonArray = JSONArray.fromObject(servicePoint.getAreaIds());

            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                    String strId = jsonObj.getString("id");
                    String strType = jsonObj.getString("type");
                    Long lType = Long.valueOf(strType).longValue();
                    if (lType < Area.TYPE_VALUE_TOWN) {
                        areas.add(Long.valueOf(strId));
                    } else {
                        townAreaIds.add(Long.valueOf(strId));
                    }
                }
            }
            log.warn("网点省,市,区/县id列表:{}", areas);
            log.warn("网点乡镇/街道id列表:{}", townAreaIds);
        }
        //3. 主账号师傅区域
        // 主账号师傅负责的区域
        List<Long> engineerAreaIds = Lists.newArrayList();
        if (areas != null && areas.size() > 0) {
            //只保存区县级(type=4),取交集
            Set<Long> areaAll = new HashSet<>();
//            Set<RedisZSetCommands.Tuple> areaSets = redisUtils.zRangeWithScore(RedisConstant.RedisDBType.REDIS_SYS_DB, String.format(RedisConstant.SYS_AREA_TYPE, "4"), 0, -1);
//            areaAll = areaSets.stream().map(t -> t.getScore().longValue()).collect(Collectors.toSet());

            List<Area> areaList = msSysAreaService.findListByTypeFromCache(Area.TYPE_VALUE_COUNTY);
            areaAll = CollectionUtils.isEmpty(areaList)?Sets.newHashSet():areaList.stream().map(t -> t.getId()).collect(Collectors.toSet());

            Set<Long> areaSet = areas.stream().collect(Collectors.toSet());
            List<Long> areaIds = Sets.intersection(areaSet, areaAll).stream().collect(Collectors.toList());
            engineerAreaIds.addAll(areaIds);
            log.warn("主账号师傅负责的区域列表:{}", engineerAreaIds);
        }
        //add on 2020-5-20 界面数据获取 end

        // add on 2020-5-20 begin
        // 微服务调用
        servicePoint.preInsert();  //add on 2020-1-15 添加网点价格时需要用到网点updateBy作为createBy
        Engineer primary = servicePoint.getPrimary();
        primary.setCreateDate(servicePoint.getCreateDate());
        primary.setCreateBy(servicePoint.getCreateBy());
        primary.setServicePoint(servicePoint);
        primary.setMasterFlag(1);
        primary.setArea(servicePoint.getArea());
        primary.setAddress(servicePoint.getAddress());  //主帐号的地址默认和网点相同

        if(servicePoint.getPrimary() != null && servicePoint.getPrimary().getEngineerCerts() !=null){
            primary.setEngineerCerts(servicePoint.getPrimary().getEngineerCerts());
        }


        if(servicePoint.getAppFlag() == 0){
            primary.setAppFlag(servicePoint.getAppFlag());
        }else {
            primary.setAppFlag(servicePoint.getPrimary().getAppFlag());
        }

        MDEngineerAddress engineerAddress = new MDEngineerAddress();
        engineerAddress.setServicePointId(servicePoint.getId());
        engineerAddress.setUserName(servicePoint.getPrimary().getName());
        engineerAddress.setContactInfo(servicePoint.getPrimary().getContactInfo());
        engineerAddress.setAreaId(servicePoint.getArea().getId());
        //
        Area districtArea = areaService.getFromCache(servicePoint.getArea().getId());
        Long cityAreaId = Optional.ofNullable(districtArea).map(Area::getParent).map(Area::getId).orElse(0L);
        Area cityArea = areaService.getFromCache(cityAreaId);
        Long provinceAreaId = Optional.ofNullable(cityArea).map(Area::getParent).map(Area::getId).orElse(0L);
        engineerAddress.setProvinceId(provinceAreaId);
        engineerAddress.setCityId(cityAreaId);
        //
        engineerAddress.setAddress(servicePoint.getArea().getFullName().concat(servicePoint.getSubAddress()));
        engineerAddress.setAddressFlag(MDEngineerEnum.EngineerAddressFlag.SERVICEPOINT.getValue());  // 默认使用网点地址
        primary.setEngineerAddress(engineerAddress);

        try {
            // 检查网点编号是否存在
            Long existsServicePointId = msServicePointService.getIdByServicePointNoForMD(servicePoint.getServicePointNo());
            if(existsServicePointId != null){
                throw new RuntimeException("网点编号已经存在.");
            }

            //1.调用网点微服务
            NameValuePair<Long, Long> nameValuePair = msServicePointService.saveServicePointAndEngineer(servicePoint, primary);
            servicePointId = nameValuePair.getName();   // 网点Id
            Long engineerId = nameValuePair.getValue();  // 主账号师傅Id
            if (servicePointId == null || engineerId == null) {
                throw new RuntimeException("调用保存网点微服务后，返回的网点id为空或安维id为空.ServicePointId="+servicePointId+",engineerId="+engineerId);
            }
            servicePoint.setId(servicePointId);
            primary.setId(engineerId);
            servicePoint.setPrimary(primary);

            //2.调用网点产品微服务
            if (!org.springframework.util.ObjectUtils.isEmpty(products)) {
                List<List<Long>> productParts = Lists.partition(products, 500);
                productParts.stream().forEach(list -> {
                    msServicePointProductService.assignProducts(list, servicePoint.getId());
                });
                msProductCategoryServicePointService.update(servicePoint.getId(), servicePoint.getProductCategoryIds());
            }

            //3.调用网点区域微服务
            if (!org.springframework.util.ObjectUtils.isEmpty(areas)) {
                List<List<Long>> areaParts = Lists.partition(areas, 500);    // 添加网点区域到微服务
                areaParts.stream().forEach(list -> {
                    msServicePointAreaService.assignAreas(servicePoint.getId(), list);
                });
            }

            //4.调用师傅区域微服务
            if (engineerAreaIds != null && !engineerAreaIds.isEmpty()) {
                List<List<Long>> engineerAreaParts = Lists.partition(engineerAreaIds, 500);
                engineerAreaParts.stream().forEach(list->{
                    msEngineerAreaService.assignEngineerAreas(list, servicePoint.getPrimary().getId());
                });
            }

            //5.网点4级区域微服务
            List<ServicePointStation> saveToMSList = Lists.newArrayList(); // add on 2019-12-28
            log.warn("开始处理街道/乡镇数据...");
            if (!org.springframework.util.ObjectUtils.isEmpty(townAreaIds)) {
                log.warn("要新增或保存的街道/乡镇数据有:{}笔.", townAreaIds.size());
                townAreaIds.stream().forEach(r -> {
                    ServicePointStation servicePointStation = ServicePointStation.builder()
                            .servicePoint(servicePoint)
                            .build();

                    servicePointStation.setDelFlag(ServicePointStation.DEL_FLAG_NORMAL);
                    List<Long> inputAutoPlanIds = Lists.newArrayList();
                    ServicePointStation retServicePointStation = servicePointStationService.generateServicePointStationNew(r, servicePointStation, inputAutoPlanIds, new ArrayList<ServicePointStation>());
                    saveToMSList.add(retServicePointStation);
                });
                if (!org.springframework.util.ObjectUtils.isEmpty(saveToMSList)) {
                    List<ServicePointStation> servicePointStationList = msServicePointStationService.batchSave(saveToMSList);
                    /*
                    // mark on 2020-11-24 begin 停止往队列 MS:MQ:ES:SYNC:SERVICEPOINT:STATION 发送消息
                    if (servicePointStationList != null && !servicePointStationList.isEmpty()) {
                        servicePointStationList.stream().forEach(servicePointStationService::saveForWeb);
                    }
                    */
                }
            }
            // .写网点操作日志
            servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.CREATE_SERVICEPOINT, "创建网点",
                    ServicePointLogService.toServicePointJson(servicePoint), servicePoint.getCreateBy());
            //备注
            if (StringUtils.isNotBlank(servicePoint.getRemarks())) {
                servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_REMARK, "网点备注", servicePoint.getRemarks(), UserUtils.getUser());
            }
        } catch (Exception ex) {
            LogUtils.saveLog("新增网点", "ServicePointService.insertServicePoint(1)-调用微服务", servicePoint.getServicePointNo(), ex, UserUtils.getUser());
            throw new RuntimeException(ex.getMessage());
        }
        // add on 2020-5-20 微服务调用 end

        //锁
        lockkey = String.format("lock:servicepoint:%s", servicePointId);
        //获得锁
        locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);//1分钟
        if (!locked) {
            throw new RuntimeException("网点正在修改中，请稍候重试。");
        }
        User actionUser = servicePoint.getCreateBy();
        Integer paymentTypeValue = finance.getPaymentType().getIntValue();
        Integer oldPaymentTypeValue = null;

        try {
            // 保存到Web本地数据  begin
            //网点财务
            finance.setId(servicePoint.getId());
            if (StringUtils.isBlank(finance.getDebtsDescrption())) {
                finance.setDebtsDescrption("");
            }
            dao.insertFI(finance);

            //add sys_user,帐号:手机号 密码:手机号
            User user = new User();
            user.setCompany(new Office(servicePoint.getId()));//网点
            user.setLoginName(primary.getContactInfo());
            user.setName(primary.getName());
            user.setMobile(primary.getContactInfo());
            user.setUserType(User.USER_TYPE_ENGINEER);
            user.setSubFlag(0);
            //user.getRoleList().add(new Role(6L));  // mark on 2020-11-18
            user.setRole(new Role(6L));
            user.setPassword(SystemService.entryptPassword(StringUtils.right(primary.getContactInfo().trim(), 6)));
            user.setEngineerId(primary.getId());
            user.setCreateBy(primary.getCreateBy());
            user.setCreateDate(primary.getCreateDate());
            userDao.insert(user);
            //userDao.insertUserRole(user);//角色     //mark on 2020-11-18 //原因：调用此方法出现 java.sql.SQLException: Lock wait timeout exceeded; try restarting transaction
            userDao.insertSingleUserRole(user); //角色  //add on 2020-11-18
            MSUserUtils.addUserToRedis(user);//user微服务

            //==== 发送消息 begin======
            if (oldPaymentTypeValue == null || !paymentTypeValue.equals(oldPaymentTypeValue)) {
                int year = DateUtils.getYear(servicePoint.getCreateDate());
                MQInserDefaultsMessage.InsertDefaultsMessage insertDefaultsMessage = MQInserDefaultsMessage.InsertDefaultsMessage.newBuilder()
                        .setServicepointId(servicePointId)
                        .setPaymentType(paymentTypeValue)
                        .setYear(year)
                        .build();
                try {
                    insertFIDefaultsSender.send(insertDefaultsMessage);
                } catch (Exception e) {
                    LogUtils.saveLog("订单对帐.发送网点对帐队列", "FI:insertFIDefaultsSender.send",
                            new JsonFormat().printToString(insertDefaultsMessage), new Exception(e.getLocalizedMessage()), actionUser);
                }
            }

            // 网点价格
            if (servicePoint.getRemotePriceFlag() != null && servicePoint.getRemotePriceFlag() == 1) {
                //发送生成网点价格消息
                MQServicePointPriceMessage.ServicePointPriceMessage.Builder  servicePointPriceMessage =  MQServicePointPriceMessage.ServicePointPriceMessage.newBuilder();
                servicePointPriceMessage.setServicePointId(servicePoint.getId());
                servicePointPriceMessage.setSyncType(MDServicePointPriceSyncTypeEnum.ADD.getValue());
                servicePointPriceMessage.setPriceTypeFlag(MDServicePointEnum.PriceTypeFlag.REMOTEPRICE.getValue());
                servicePointPriceMessage.setUserId(actionUser != null && actionUser.getId()!= null?actionUser.getId():0L);
                try {
                    servicePointPriceSender.send(servicePointPriceMessage.build());
                } catch (Exception ex) {
                }
            }

            // 更新网点财务缓存
            try {
                servicePointFinanceService.updateCache(finance);
            } catch (Exception ex) {
                log.error("更新网点财务缓存失败,id:{} 失败原因:{}", finance.getId(), ex);
            }
        } catch (Exception e) {
            LogUtils.saveLog(isNew ? "新增网点" : "修改网点", "ServicePointService.insertServicePoint", servicePoint.getServicePointNo(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    @Transactional(readOnly = false)
    public void permissionSettingSave(ServicePoint servicePoint) {
        // 网点权限设定保存
        boolean isNew = servicePoint.getIsNewRecord();
        Long servicePointId = null;
        String lockkey = null;
        Boolean locked = false;
        ServicePoint cachedServicePoint = null;
        if(servicePoint.getRemotePriceEnabledFlag() == 1){
            servicePoint.setRemotePriceType(40);
            servicePoint.setRemotePriceFlag(1);
        }else {
            servicePoint.setRemotePriceType(0);
            servicePoint.setRemotePriceFlag(0);
        }
        // add on 2020-6-1 begin
        int customizePriceFlag = 0;
        boolean serviceResetPrice = false;
        boolean remoterResetPrice = false;
        int resetPrice = 0;
        ServicePoint oldServicePoint = getSimple(servicePoint.getId());
        if (oldServicePoint != null) {
            customizePriceFlag = Optional.ofNullable(oldServicePoint.getCustomizePriceFlag()).orElse(0);
            if (servicePoint.getCustomizePriceFlag().equals(1) && customizePriceFlag == 0) {
                serviceResetPrice = true;
            }
            if(oldServicePoint.getRemotePriceFlag() == null){
                oldServicePoint.setRemotePriceFlag(0);
            }
            if(servicePoint.getRemotePriceFlag() != null) {
                if (oldServicePoint.getRemotePriceFlag() == 0 && servicePoint.getRemotePriceFlag() == 1) {
                    remoterResetPrice = true;
                }
            }
        }

        if(serviceResetPrice){
            resetPrice = 1;
            if(remoterResetPrice){
                resetPrice = 3;
            }else if(servicePoint.getResetPrice() == 2){
                resetPrice = 3;
            }
        }else if(remoterResetPrice){
            resetPrice = 2;
            if(servicePoint.getResetPrice() == 1){
                resetPrice = 3;
            }
        }

        if(servicePoint.getResetPrice() == 3){
            resetPrice = 3;
        }else if(resetPrice == 0){
            resetPrice = servicePoint.getResetPrice();
        }
        // add on 2020-6-1 end
        servicePoint.setResetPrice(resetPrice);

        ServicePointFinance frontFinance = servicePoint.getFinance();       // add on 2019-6-12   //从前端传递过来的财务配置数据
        ServicePointFinance finance = dao.getFinance(servicePoint.getId()); // add on 2019-6-12   //从数据库中获取财务数据
        if (frontFinance != null) {
            finance.setDiscountFlag(frontFinance.getDiscountFlag());  // 是否扣点标志
            if (frontFinance.getDiscountFlag() == DISCOUNT_FLAG_DISABLED) {
                finance.setDiscount(0.0D);                // add on 2020-4-6
            } else {
                double discount = frontFinance.getDiscount();
                if (discount >0) {
                    discount = discount/100;
                    finance.setDiscount(discount);
                }
            }
        }

        //同步paymentType,bank,bank_no,bank_owner,invoice_flag at 2018/08/30 by ryan
        servicePoint.setPaymentType(finance.getPaymentType());
        servicePoint.setInvoiceFlag(finance.getInvoiceFlag());
        servicePoint.setBank(finance.getBank());
        servicePoint.setBankNo(finance.getBankNo());
        servicePoint.setBankOwner(finance.getBankOwner());
        servicePoint.setBankIssue(finance.getBankIssue());
        servicePoint.setDiscountFlag(finance.getDiscountFlag());  //2018/12/07 ryan  // 扣点标志
        servicePoint.setFinance(finance);
        //end
        if(servicePoint.getDegree() == 30){
            servicePoint.setInsuranceFlag(0);
            servicePoint.setDepositFromOrderFlag(0);
            if(servicePoint.getMdDepositLevel() == null){
                servicePoint.setMdDepositLevel(new MDDepositLevel());
            }
            servicePoint.getMdDepositLevel().setId(0L);
            updateInsuranceFlag(servicePoint.getId(),servicePoint.getInsuranceFlag());
            servicePoint.setTimeLinessFlag(0);
        }

        if (!isNew) {
            servicePointId = servicePoint.getId();
            cachedServicePoint = getFromCache(servicePointId);
            //TODO: md_servicepoint-status -> 黑名单状态不允许变更为其他状态
            if (cachedServicePoint != null && cachedServicePoint.getStatusValue() == ServicePointStatus.BLACKLIST.getValue()) {
                servicePoint.setStatus(ServicePointStatus.createDict(ServicePointStatus.BLACKLIST));
            }
        }

        Long id = servicePointId;
        //锁
        lockkey = String.format("lock:servicepoint:%s", id);
        //获得锁
        locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);//1分钟
        if (!locked) {
            throw new RuntimeException("网点正在修改中，请稍候重试。");
        }
        User actionUser = servicePoint.getCreateBy();

        try {
            List<Long> productIdList = Lists.newArrayList();  //修改前产品id列表
            if (!isNew) {
                Boolean resetPermit = false;//无重置权限
                if (SecurityUtils.getSubject().isPermitted("md:servicepoint:defaultpriceedit")) {
                    resetPermit = true;
                }
                productIdList = getProductIdsById(servicePoint.getId());  //原产品ID列表 // add on 2019-12-17

                servicePoint.preUpdate();

                //add on 2019-9-14 begin
                MSErrorCode msErrorCode = msServicePointService.updateServicePointForKeySetting(servicePoint);
                if (msErrorCode.getCode() >0) {
                    throw new RuntimeException("调用微服务保存网点权限设定失败,失败原因:"+msErrorCode.getMsg());
                }
                //add on 2019-9-14 end
                if(servicePoint.getRemotePriceEnabledFlag() == 0){
                    assert oldServicePoint != null;
                    servicePoint.setRemotePriceFlag(oldServicePoint.getRemotePriceFlag());
                }
                //TODO：写网点操作日志
                servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT, "编辑权限设定的网点资料",
                        ServicePointLogService.toServicePointJsonForPermissionSettting(servicePoint), servicePoint.getCreateBy());
                //备注日志

                servicePoint.setUpdateBy(servicePoint.getCreateBy());
                servicePoint.setUpdateDate(servicePoint.getCreateDate());
                dao.updateFI(finance);

                //修改时，不更改主帐号的区域，地址及安维-区域列表
                //主帐号修改姓名
                Engineer primary = getEngineer(servicePoint.getPrimary().getId());
                primary.setName(servicePoint.getPrimary().getName());
                primary.setContactInfo(servicePoint.getPrimary().getContactInfo());  // 主账号电话   add on 2019-6-12
//                primary.setAppFlag(servicePoint.getPrimary().getAppFlag());          // 允许手机接单 add on 2019-6-12
                primary.setLevel(servicePoint.getPrimary().getLevel());   //更新师傅等级  //add on 2020-5-13
                if(servicePoint.getAppFlag() == 0){
                    primary.setAppFlag(servicePoint.getAppFlag());
                }else {
                    primary.setAppFlag(servicePoint.getPrimary().getAppFlag());
                }
                HashMap<String, Object> maps = Maps.newHashMap();
                maps.put("id", primary.getId());
                maps.put("name", primary.getName());
                maps.put("contactInfo", primary.getContactInfo());  // add on 2019-6-12
                maps.put("appFlag", primary.getAppFlag());          // add on 2019-6-12
                maps.put("updateBy", servicePoint.getUpdateBy());
                maps.put("updateDate", servicePoint.getUpdateDate());
                if (primary.getLevel() != null && StringUtils.isNotBlank(primary.getLevel().getValue()) ) {
                    maps.put("level", primary.getLevel().getValue());   // add on 2020-5-13
                }

                //  add on 2019-10-18 begin
                //  Engineer微服务
                MSErrorCode msErrorCodeEngineer = msEngineerService.updateEngineerName(primary);
                if (msErrorCodeEngineer.getCode() >0) {
                    throw new RuntimeException("调用微服务保存安维人员设定失败,失败原因:" + msErrorCodeEngineer.getMsg());
                }
                //  add on 2019-10-18 end

                //sys_user
                maps.remove("id");
                maps.remove("contactInfo");   // add on 2019-6-12
                maps.remove("appFlag");       // add on 2019-6-12
                maps.put("engineerId", primary.getId());
                userDao.updateUserByEngineerId(maps);

                servicePoint.setPrimary(primary);
                msUserService.refreshUserCacheByEngineerId(primary.getId());//user微服务

                //TODO:同步网点到ElasticSearch（es）
                log.warn(String.format("是否同步ElasticSearch:%s", syncServicePoint2ES));
                if (syncServicePoint2ES) {
                    // updateServicePointToEs(servicePoint, cachedServicePoint); //mark on 2020-11-25  //不需要同步旧的自动派单消息了
                }


                //有价格维护权限，且选择了重置价格
                if (resetPermit && servicePoint.getResetPrice() > 0) {  // add on 2020-6-1
                    //add on 2020-3-8 begin
                    //发送生成网点价格消息
                    MQServicePointPriceMessage.ServicePointPriceMessage.Builder  servicePointPriceMessage =  MQServicePointPriceMessage.ServicePointPriceMessage.newBuilder();
                    servicePointPriceMessage.setServicePointId(servicePoint.getId());
                    servicePointPriceMessage.setSyncType(MDServicePointPriceSyncTypeEnum.RESET.getValue());
                    if(servicePoint.getResetPrice() == 1){
                        servicePointPriceMessage.setPriceTypeFlag(MDServicePointEnum.PriceTypeFlag.SERVICEPRICE.getValue());
                    }else if(servicePoint.getResetPrice() == 2){
                        servicePointPriceMessage.setPriceTypeFlag(MDServicePointEnum.PriceTypeFlag.REMOTEPRICE.getValue());
                    }else if(servicePoint.getResetPrice() == 3){
                        servicePointPriceMessage.setPriceTypeFlag(MDServicePointEnum.PriceTypeFlag.BOTH.getValue());
                    }
                    UserUtils.getUser();
                    servicePointPriceMessage.setUserId(UserUtils.getUser().getId() != null ? UserUtils.getUser().getId(): 0L);
                    servicePointPriceSender.send(servicePointPriceMessage.build());
                    //add on 2020-3-8 end
                }


            }

            //cache

            // add on 2020-5-5 begin
            // 更新网点财务缓存
            ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(servicePoint.getId());
            if (cachedServicePointFinance != null) {
                cachedServicePointFinance.setPaymentType(finance.getPaymentType());
                cachedServicePointFinance.setInvoiceFlag(finance.getInvoiceFlag());
                cachedServicePointFinance.setBank(finance.getBank());
                cachedServicePointFinance.setBankNo(finance.getBankNo());
                cachedServicePointFinance.setBankOwner(finance.getBankOwner());
                cachedServicePointFinance.setBankIssue(finance.getBankIssue());
                cachedServicePointFinance.setDiscountFlag(finance.getDiscountFlag());//2018/12/07 ryan
                cachedServicePointFinance.setDiscount(finance.getDiscount());
                servicePointFinanceService.updateCache(cachedServicePointFinance);
            }
            // add on 2020-5-5 end
        } catch (Exception e) {
            LogUtils.saveLog(isNew ? "新增网点" : "修改网点", "ServicePointService.permissionSettingSave", servicePoint.getServicePointNo(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    @Transactional(readOnly = false)
    public void saveBaseInfo(ServicePoint servicePoint) {
        //  保存网点的基本信息  //add on 2019-6-15
        //  此方法仅用来修改网点信息时用到
        //System.out.println(servicePoint);
        boolean isNew = servicePoint.getIsNewRecord();
        if (isNew) {
            throw new RuntimeException("修改网点信息，调用方法错误。");
        }
        // msServicePointService.checkTestQuery();
        // add on 2020-6-2 begin
        // 当网点分级撤换时(如返现网点转换为使用网点)，网点价格属性为标准价
        ServicePoint pastServicePoint = msServicePointService.getById(servicePoint.getId());
        if (pastServicePoint != null) {
            int degree = Optional.ofNullable(pastServicePoint.getDegree()).orElse(0);
            if (servicePoint.getDegree() != null && servicePoint.getDegree().intValue() != degree) {
                servicePoint.setCustomizePriceFlag(0); // 0 --标准价
            }
        }
        // add on 2020-6-2 end

        Long servicePointId = null;
        String lockkey = null;
        Boolean locked = false;
        ServicePoint cachedServicePoint = null;
        ServicePointFinance finance = servicePoint.getFinance();
        String bankNo = finance.getBankNo();
        Dict dictBankIssue = finance.getBankIssue();
        if (StringUtils.isBlank(bankNo) && (dictBankIssue == null || StringUtils.isBlank(dictBankIssue.getValue()) || dictBankIssue.getValue().equalsIgnoreCase("0"))) {
            dictBankIssue = new Dict("1", "暂无付款账号");  //1-暂无付款账号
            finance.setBankIssue(dictBankIssue);
        }else{
            Dict nbankIssue = MSDictUtils.getDictByValue(dictBankIssue.getValue(), "BankIssueType");//切换为微服务
            if (nbankIssue != null) {
                finance.setBankIssue(nbankIssue);
            }
        }

        //同步paymentType,bank,bank_no,bank_owner,invoice_flag at 2018/08/30 by ryan
        servicePoint.setPaymentType(finance.getPaymentType());
        servicePoint.setInvoiceFlag(finance.getInvoiceFlag());
        servicePoint.setBank(finance.getBank());
        servicePoint.setBankNo(finance.getBankNo());
        servicePoint.setBankOwner(finance.getBankOwner());
        servicePoint.setBankIssue(finance.getBankIssue());
        servicePoint.setDiscountFlag(finance.getDiscountFlag());//2018/12/07 ryan
        if (finance.getDiscountFlag() == DISCOUNT_FLAG_DISABLED) {
            finance.setDiscount(0.0D);                // add on 2020-4-6
        } else {
            double discount = finance.getDiscount();
            if (discount >0) {
                discount = discount/100;
                finance.setDiscount(discount);
            }
        }

        if(servicePoint.getDegree() == 30){
            servicePoint.setInsuranceFlag(0);
            servicePoint.setDepositFromOrderFlag(0);
            servicePoint.getMdDepositLevel().setId(0L);
            updateInsuranceFlag(servicePoint.getId(),servicePoint.getInsuranceFlag());

            servicePoint.setTimeLinessFlag(0); //返现网点关闭快可立时效
        }
        if(servicePoint.getRemotePriceEnabledFlag() == 1){
            servicePoint.setRemotePriceType(40);
            servicePoint.setRemotePriceFlag(1);
        }else {
            servicePoint.setRemotePriceType(0);
            servicePoint.setRemotePriceFlag(0);
        }
        //end
        if (!isNew) {
            servicePointId = servicePoint.getId();
            cachedServicePoint = getFromCache(servicePointId);  // add on 2019-3-19 by TimXu
            //TODO: md_servicepoint-status -> 黑名单状态不允许变更为其他状态
            if (cachedServicePoint != null && cachedServicePoint.getStatusValue() == ServicePointStatus.BLACKLIST.getValue()) {
                servicePoint.setStatus(ServicePointStatus.createDict(ServicePointStatus.BLACKLIST));
            }
        }

        lockkey = String.format("lock:servicepoint:%s", servicePointId);  //获得锁
        locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);//1分钟
        if (!locked) {
            throw new RuntimeException("网点正在修改中，请稍候重试。");
        }

        User actionUser = UserUtils.getUser();
        Integer paymentTypeValue = finance.getPaymentType().getIntValue();
        Integer oldPaymentTypeValue = null;

        try {
            List<Long> productIdList = Lists.newArrayList();//修改前产品id列表
            if (!isNew) {
                Engineer primary = getEngineer(servicePoint.getPrimary().getId());  // 获取主账号
                oldPaymentTypeValue = dao.getServicePointPaymentType(servicePointId);  //从DB中获取付款类型(如月结,日结)
                //productIdList = dao.getProductIdsById(servicePoint.getId());           //获取网点的原产品ID列表 //mark on 2019-12-17
                productIdList = getProductIdsById(servicePoint.getId());           //获取网点的原产品ID列表 // add on 2019-12-17

                //super.save(servicePoint);  //mark on 2020-1-14  web端去servicePoint
                //备注日志
                if(StringUtils.isNotBlank(servicePoint.getRemarks())){
                    assert cachedServicePoint != null;
                    if(!servicePoint.getRemarks().equals(cachedServicePoint.getRemarks())) {
                        //updateRemark(servicePointId, servicePoint.getRemarks());   //mark on 2019-10-11
                        updateRemarkWithoutSaveToMS(servicePointId, servicePoint.getRemarks()); //add on 2019-10-11
                    }
                }
                // update fi
                finance.setId(servicePoint.getId());
                dao.updateFI(finance);

                servicePoint.setUpdateBy(UserUtils.getUser());
                servicePoint.setUpdateDate(new Date());

                //修改时，不更改主帐号的区域，地址及安维-区域列表
                //主帐号修改姓名
                primary.setName(servicePoint.getPrimary().getName());
                if(servicePoint.getAppFlag() == 0){
                    primary.setAppFlag(servicePoint.getAppFlag());
                }else {
                    primary.setAppFlag(servicePoint.getPrimary().getAppFlag());
                }

                if(servicePoint.getPrimary() !=null && servicePoint.getPrimary().getEngineerCerts() !=null){
                    primary.setEngineerCerts(servicePoint.getPrimary().getEngineerCerts());
                }

                if(servicePoint.getPrimary() !=null && servicePoint.getPrimary().getIdNo() !=null){
                    primary.setIdNo(servicePoint.getPrimary().getIdNo());
                }

                HashMap<String, Object> maps = Maps.newHashMap();
                maps.put("id", primary.getId());
                maps.put("name", primary.getName());
                maps.put("updateBy", servicePoint.getUpdateBy());
                maps.put("updateDate", servicePoint.getUpdateDate());
                //dao.updateEngineerByParams(maps);   //mark on 2020-1-13  去除md_engineer

                //sys_user
                maps.remove("id");
                maps.put("engineerId", primary.getId());
                userDao.updateUserByEngineerId(maps);

                servicePoint.setPrimary(primary);

                // 写网点操作日志
                servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT, "编辑网点基本资料",
                        ServicePointLogService.toServicePointJson(servicePoint), servicePoint.getUpdateBy());

                //updateEngineerCache(primary);//更新主账号缓存  //mark on 2020-1-14  web端去servicePoint
                msUserService.refreshUserCacheByEngineerId(primary.getId());//更新user微服务

                // 同步网点到ElasticSearch（es）
                //System.out.println(String.format("是否同步ElasticSearch:%s",syncServicePoint2ES));
                if (syncServicePoint2ES) {
                    //updateServicePointToEs(servicePoint, cachedServicePoint);  // mark on 2020-11-25 //不需要同步旧的自动派单消息了
                }
            }

            if (!paymentTypeValue.equals(oldPaymentTypeValue)) {
                int year = DateUtils.getYear(servicePoint.getCreateDate());
                MQInserDefaultsMessage.InsertDefaultsMessage insertDefaultsMessage = MQInserDefaultsMessage.InsertDefaultsMessage.newBuilder()
                        .setServicepointId(servicePointId)
                        .setPaymentType(paymentTypeValue)
                        .setYear(year)
                        .build();
                try {
                    insertFIDefaultsSender.send(insertDefaultsMessage);
                } catch (Exception e) {
                    LogUtils.saveLog("订单对帐.发送网点对帐队列", "FI:insertFIDefaultsSender.send",
                            new JsonFormat().printToString(insertDefaultsMessage), new Exception(e.getLocalizedMessage()), actionUser);
                }
            }


            // add on 2019-9-10 begin
            if (!isNew) {  // 更新
                // ServicePoint微服务
                MSErrorCode msErrorCode = msServicePointService.save(servicePoint,false);
                if (msErrorCode.getCode() >0) {
                    throw new RuntimeException("调用微服务保存网点信息出错.错误信息:"+msErrorCode.getMsg());
                }

                // add on 2019-10-18 begin
                // Engineer微服务
                MSErrorCode msErrorCodeEngineer = msEngineerService.updateEngineerName(servicePoint.getPrimary());
                if (msErrorCodeEngineer.getCode() >0) {
                    throw new RuntimeException("调用微服务保存安维人员信息出错.错误信息:"+msErrorCodeEngineer.getMsg());
                }
                // add on 2019-10-18 end

                //有价格维护权限，且选择了重置价格
                if(pastServicePoint != null) {
                    if (pastServicePoint.getRemotePriceFlag() == null){
                        pastServicePoint.setRemotePriceFlag(0);
                    }
                    if (pastServicePoint.getRemotePriceFlag() == 0 && servicePoint.getRemotePriceFlag() == 1) {
                        // 发送生成网点价格消息
                        MQServicePointPriceMessage.ServicePointPriceMessage.Builder servicePointPriceMessage = MQServicePointPriceMessage.ServicePointPriceMessage.newBuilder();
                        servicePointPriceMessage.setServicePointId(servicePoint.getId());
                        servicePointPriceMessage.setSyncType(MDServicePointPriceSyncTypeEnum.RESET.getValue());
                        servicePointPriceMessage.setPriceTypeFlag(MDServicePointEnum.PriceTypeFlag.REMOTEPRICE.getValue());
                        UserUtils.getUser();
                        servicePointPriceMessage.setUserId(UserUtils.getUser().getId() != null ? UserUtils.getUser().getId() : 0L);
                        servicePointPriceSender.send(servicePointPriceMessage.build());
                        //add on 2020-3-8 end
                    }
                }
            }
            // add on 2019-9-10 end

            // cache
            // add on 2020-5-5 begin
            // 更新网点财务缓存
            ServicePointFinance cachedServicePointFinance = servicePointFinanceService.getFromCache(servicePoint.getId());
            if (cachedServicePointFinance != null) {
                cachedServicePointFinance.setPaymentType(finance.getPaymentType());
                cachedServicePointFinance.setInvoiceFlag(finance.getInvoiceFlag());
                cachedServicePointFinance.setBank(finance.getBank());
                cachedServicePointFinance.setBankNo(finance.getBankNo());
                cachedServicePointFinance.setBankOwner(finance.getBankOwner());
                cachedServicePointFinance.setBankIssue(finance.getBankIssue());
                cachedServicePointFinance.setDiscountFlag(finance.getDiscountFlag());//2018/12/07 ryan
                cachedServicePointFinance.setDiscount(finance.getDiscount());
                servicePointFinanceService.updateCache(cachedServicePointFinance);
            }
            // add on 2020-5-5 end
        } catch (Exception e) {
            LogUtils.saveLog(isNew ? "新增网点" : "修改网点基础资料", "ServicePointService.save", servicePoint.getServicePointNo(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    @Transactional(readOnly = false)
    public void saveProducts(ServicePoint servicePoint) {
        log.warn("保存网点的产品信息,传入的servicePoint:{},products:{}",servicePoint.getId(),servicePoint.getProductIds());

        boolean isNew = servicePoint.getIsNewRecord();
        Long id = null;
        ServicePoint cachedServicePoint = null;
        if (!isNew) {
            id = servicePoint.getId();
            cachedServicePoint = getFromCache(id);
            servicePoint.setName(cachedServicePoint.getName());
            servicePoint.setUseDefaultPrice(cachedServicePoint.getUseDefaultPrice());
        }

        String lockkey = String.format("lock:servicepoint:%s", id);  //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);   //1分钟
        if (!locked) {
            throw new RuntimeException("网点正在修改中，请稍候重试。");
        }

        User actionUser = UserUtils.getUser();  //mark on 2020-3-5
        try {
            List<Long> productIdList = Lists.newArrayList();//修改前产品id列表

            if (!isNew) {
                productIdList = getProductIdsById(servicePoint.getId());//原产品ID列表  //add on 2019-12-17

                //product,缓存 list
                List<Long> products = Lists.newArrayList();
                if (StringUtils.isNoneBlank(servicePoint.getProductIds())) {
                    products = Arrays.stream(servicePoint.getProductIds().split(","))
                            .map(t -> Long.valueOf(t))
                            .collect(Collectors.toList());

                    if (!products.isEmpty()) {
                        Map<Long, Product> productMap = productService.getProductMap(products);
                        List<Long> productCategoryIds = productMap.values().stream().filter(i->i.getCategory() != null && i.getCategory().getId() != null)
                                .map(i->i.getCategory().getId()).distinct().collect(Collectors.toList());

                        msProductCategoryServicePointService.update(servicePoint.getId(), productCategoryIds);
                    }
                }

                Boolean resetPermit = false;//无重置权限
                if (SecurityUtils.getSubject().isPermitted("md:servicepoint:defaultpriceedit")) {
                    resetPermit = true;
                }
                //原产品列表
                Set<Long> productIdSet = productIdList.stream().collect(Collectors.toSet());
                // add on 2019-12-17 begin
                msServicePointProductService.removeProducts(servicePoint.getId());
                if (!products.isEmpty()) {
                    List<List<Long>> productParts = Lists.partition(products, 500);
                    productParts.stream().forEach(list -> {
                        msServicePointProductService.assignProducts(list, servicePoint.getId());
                    });
                }
                // add on 2019-12-17 end
                // 写网点操作日志
                servicePointLogService.saveServicePointLog(id, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT, "编辑网点基本资料",
                        "修改网点产品信息.", actionUser);

                // 无重置权限，或选择不重置价格,只添加新增的产品的价格
                if (!resetPermit || servicePoint.getResetPrice() == 0) {
                    Set<Long> productListIdSet = products.stream().collect(Collectors.toSet());
                    //找到新增的产品
                    productListIdSet.removeAll(productIdSet);
                    if (productListIdSet.size() > 0) {
                        //add on 2020-3-8 begin
                        //发送生成网点价格消息
                        MQServicePointPriceMessage.ServicePointPriceMessage.Builder  servicePointPriceMessage =  MQServicePointPriceMessage.ServicePointPriceMessage.newBuilder();
                        servicePointPriceMessage.setServicePointId(servicePoint.getId());
                        servicePointPriceMessage.addAllProductId(Lists.newArrayList(productListIdSet));
                        servicePointPriceMessage.setSyncType(MDServicePointPriceSyncTypeEnum.PARTADD.getValue());
                        servicePointPriceMessage.setPriceTypeFlag(MDServicePointEnum.PriceTypeFlag.BOTH.getValue());
                        servicePointPriceMessage.setUserId(UserUtils.getUser() != null && UserUtils.getUser().getId()!= null? UserUtils.getUser().getId(): 0L);
                        servicePointPriceSender.send(servicePointPriceMessage.build());
                        //add on 2020-3-8 end
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.saveLog(isNew ? "新增网点产品" : "修改网点产品", "ServicePointService.save", "servicePointId:"+servicePoint.getId(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    @Transactional(readOnly = false)
    public void saveAreas(ServicePoint servicePoint) {
        log.warn("保存网点的区域信息,传入的servicePoint:{}", servicePoint.getId());
        boolean isNew = servicePoint.getIsNewRecord();
        Long servicePointId = servicePoint.getId();

        String lockkey = String.format("lock:servicepoint:%s", servicePointId);  //获得锁
        Boolean locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60); //1分钟
        if (!locked) {
            throw new RuntimeException("网点正在修改中，请稍候重试。");
        }

        User actionUser = servicePoint.getCreateBy();
        try {
            saveAreasToDb(servicePoint);
        } catch (Exception e) {
            LogUtils.saveLog(isNew ? "新增网点区域" : "修改网点区域", "ServicePointService.save", servicePoint.getServicePointNo(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    @Transactional
    protected void saveAreasToDb(ServicePoint servicePoint) {
        //
        // 保存网点区域
        //
        boolean isNew = servicePoint.getIsNewRecord();
        ServicePoint cachedServicePoint = null;
        List<Long> beforeAreas = Lists.newArrayList();
        if (!isNew) {
            cachedServicePoint = getFromCache(servicePoint.getId());
            servicePoint.setName(Optional.ofNullable(cachedServicePoint).map(ServicePoint::getName).orElse(""));
            log.warn("删除网点的区域数据：{}",servicePoint);
            beforeAreas = msServicePointAreaService.findAreaIds(servicePoint.getId());
            msServicePointAreaService.removeAreas(servicePoint.getId()); // add on 2019-12-3
        }

        Long servicePointId = servicePoint.getId();
        List<ServicePointStation> saveToMSList = Lists.newArrayList(); // add on 2019-12-28
        List<ServicePointStation> delToMSList = Lists.newArrayList(); // add on 2019-12-28

        //area,区域不缓存
        List<Long> areas = Lists.newArrayList();
        List<Long> countyAreaIds = Lists.newArrayList();
        List<Long> townAreaIds = Lists.newArrayList();
        List<Long> beforeAreaIds = Lists.newArrayList();

        if (StringUtils.isNoneBlank(servicePoint.getAreaIds())) {
            String areaIds = servicePoint.getAreaIds().replace("&quot;", "\"");
            servicePoint.setAreaIds(areaIds);
            JSONArray jsonArray = JSONArray.fromObject(servicePoint.getAreaIds());
            log.warn("ServicePoint:{},传入的区域数据:{}", servicePointId, jsonArray);

            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                    String strId = jsonObj.getString("id");
                    String strType = jsonObj.getString("type");
                    Long lType = Long.valueOf(strType).longValue();
                    if (lType < Area.TYPE_VALUE_TOWN) {
                        areas.add(Long.valueOf(strId));
                        // add on 2020-6-12 begin
                        // 记录区/县,用来去除师傅所负责的区域
                        if (lType.intValue() == Area.TYPE_VALUE_COUNTY.intValue()) {
                            countyAreaIds.add(Long.valueOf(strId));
                        }
                        // add on 2020-6-12 end
                    } else {
                        townAreaIds.add(Long.valueOf(strId));
                    }
                }
            }
            log.warn("ServicePoint:{},街道/乡:{},其他:{}", servicePointId, townAreaIds, areas);

            //按每500个一组分割
            List<List<Long>> areaParts = Lists.partition(areas, 500);
            areaParts.stream().forEach(list -> {
                log.warn("为网点添加区域数据：{},区域：{}",servicePoint,list);
                msServicePointAreaService.assignAreas(servicePointId, list); // add on 2019-12-3
            });

            // 处理服务点数据   on 2019-5-29  begin
            // 保存乡镇数据到(新增,修改)
            List<Long> idsCopy = Lists.newArrayList();
            if (!org.springframework.util.ObjectUtils.isEmpty(townAreaIds)) {
                //System.out.println("要新增或保存的街道/乡镇数据有:" + townAreaIds.size()+"笔.");
                // add on 2019-12-27 begin
                ServicePointStation servicePointStation = ServicePointStation.builder()
                        .servicePoint(servicePoint)
                        .build();
                servicePointStation.setDelFlag(ServicePointStation.DEL_FLAG_NORMAL);
                List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);
                List<Long> inputAutoPlanIds = Lists.newArrayList();

                if (!org.springframework.util.ObjectUtils.isEmpty(servicePointStationList)) {
                    //inputAutoPlanIds = servicePointStationList.stream().filter(r-> r.getAutoPlanFlag()!= null && r.getAutoPlanFlag().longValue() >0).map(r->r.getAutoPlanFlag().longValue()).collect(Collectors.toList());
                    inputAutoPlanIds = servicePointStationList.stream().filter(r-> r.getAutoPlanFlag()!= null && r.getAutoPlanFlag().longValue() >0).map(r->r.getArea().getId().longValue()).collect(Collectors.toList());
                }
                List<Long> finalInputAutoPlanIds = inputAutoPlanIds;
                // add on 2019-12-27 end
                townAreaIds.stream().forEach(r -> {
                    ServicePointStation retServicePointStation = servicePointStationService.generateServicePointStationNew(r, servicePointStation, finalInputAutoPlanIds, servicePointStationList);  // add on 2019-12-27
                    // add on 2019-10-10 begin
                    if (retServicePointStation != null && retServicePointStation.getServicePoint()==null) {
                        retServicePointStation.setServicePoint(servicePoint);
                    }
                    // add on 2019-10-10 end
                    saveToMSList.add(retServicePointStation);                      // add on 2019-12-28
                });
                idsCopy.addAll(townAreaIds);
            }

            // 获取DB中已建好的当前网点的服务点集合
            ServicePointStation servicePointStation = ServicePointStation.builder()
                    .servicePoint(servicePoint)
                    .build();
            List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);
            List<Long> areaIdArray = Lists.newArrayList();
            if (!org.springframework.util.ObjectUtils.isEmpty(servicePointStationList)) {
                areaIdArray = servicePointStationList.stream().filter(r -> r.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL))
                        .map(ServicePointStation::getArea)
                        .map(Area::getId)
                        .collect(Collectors.toList());
            }


            Optional.ofNullable(areaIdArray).ifPresent(beforeAreaIds::addAll);

            if (!org.springframework.util.ObjectUtils.isEmpty(areaIdArray)) {
                areaIdArray.removeAll(idsCopy);
                if (!org.springframework.util.ObjectUtils.isEmpty(areaIdArray)) {
                    //System.out.println("要删除的街道/乡镇数据有:" + townAreaIds.size()+"笔.");
                    areaIdArray.stream().forEach(r -> {
                        Area area = areaService.getFromCache(r);
                        ServicePointStation entity = ServicePointStation.builder()
                                .servicePoint(servicePoint)
                                .area(area)
                                .build();

                        // add on 2019-12-26 begin
                        ServicePointStation retServicePointStation = org.springframework.util.ObjectUtils.isEmpty(servicePointStationList)?
                                null: servicePointStationList.stream().filter(x->x.getServicePoint().getId().equals(entity.getServicePoint().getId()) && x.getArea().getId().equals(entity.getArea().getId())).findFirst().orElse(null);
                        // add on 2019-12-26 end

                        if (!org.springframework.util.ObjectUtils.isEmpty(retServicePointStation)) {
                            log.warn("街道/乡镇数据删除,ServicePoint:{},Area:{}",retServicePointStation,area.getFullName());
                            //servicePointStationService.delete(retServicePointStation);    // mark on 2019-12-28
                            servicePointStationService.deleteForWeb(retServicePointStation); // add  on 2019-12-28
                            delToMSList.add(retServicePointStation);  // add  on 2019-12-28
                        }
                    });
                }
            }
            // 处理服务点数据   on 2019-5-29  end
            // add on 2019-12-28 begin
            if (!org.springframework.util.ObjectUtils.isEmpty(saveToMSList)) {
                List<ServicePointStation> servicePointStations = msServicePointStationService.batchSave(saveToMSList);
                if (servicePointStations != null && !servicePointStations.isEmpty()) {
                    //servicePointStations.stream().forEach(servicePointStationService::saveForWeb);  //mark on 2020-11-24 停止往队列：MS:MQ:ES:SYNC:SERVICEPOINT:STATION 写消息
                }
            }
            if (!org.springframework.util.ObjectUtils.isEmpty(delToMSList)) {
                MSErrorCode msErrorCode = msServicePointStationService.batchDelete(delToMSList);
                if (msErrorCode.getCode() > 0) {
                    throw new RuntimeException("删除网点服务区域失败.失败原因:" + msErrorCode.getCode());
                }
            }
            //  add on 2019-12-28 end
        }
        // add on 2020-2-11 begin
        Long autoPlan = servicePointStationService.autoPlanByServicePointId(servicePoint.getId());
        if((autoPlan== null || autoPlan<=0)){
            updateAutoPlanFlag(servicePoint.getId(),ServicePoint.AUTO_PLAN_FLAG_DISABLED);
        }
        // add on 2020-2-11 end
        // add on 2020-6-12 begin
        // 删除失效的师傅区域
        if (!CollectionUtils.isEmpty(countyAreaIds)) {
            msEngineerAreaService.deleteEnigineerAreas(servicePointId, countyAreaIds);
        }
        // add on 2020-6-12 end
        // add on 2020-4-14 begin 写日志
        StringBuilder strLog = new StringBuilder();
        strLog.append("servicePointId:")
                .append(servicePoint.getId())
                .append(",保存前区域id：")
                .append((CollectionUtils.isEmpty(beforeAreas)?"":beforeAreas.stream().sorted().collect(Collectors.toList()).toString()))
                .append(",保存前4级区域id：")
                .append((CollectionUtils.isEmpty(beforeAreaIds)?"":beforeAreaIds.stream().sorted().collect(Collectors.toList()).toString()))
                .append(",保存后区域id：")
                .append((CollectionUtils.isEmpty(areas)?"":areas.stream().sorted().collect(Collectors.toList()).toString()))
                .append(",保存后4级区域id：")
                .append((CollectionUtils.isEmpty(townAreaIds)?"":townAreaIds.stream().sorted().collect(Collectors.toList()).toString()));

        LogUtils.saveLog("基础资料-修改网点区域", "ServicePointService.saveAreasToDb", strLog.toString(), null, UserUtils.getUser());
        // add on 2020-4-14 end
    }

    /**
     * 返回所有有效的网点
     *
     * @return
     */
    @Override
    public List<ServicePoint> findAllList() {
        List<ServicePoint> servicePointList = dao.findAllList();
        List<LongTwoTuple> servicePointProductCategoryMappings = dao.findAllServicePointProductCategoryMapping();
        Map<Long, List<ProductCategory>> mappingsMap = Maps.newHashMap();
        if (!servicePointProductCategoryMappings.isEmpty()) {
            Map<Long, ProductCategory> productCategoryMap = ProductUtils.getAllProductCategoryMap();
            ProductCategory productCategory;
            for (LongTwoTuple twoTuple : servicePointProductCategoryMappings) {
                productCategory = productCategoryMap.get(twoTuple.getBElement());
                if (productCategory != null) {
                    if (mappingsMap.containsKey(twoTuple.getAElement())) {
                        mappingsMap.get(twoTuple.getAElement()).add(productCategory);
                    } else {
                        mappingsMap.put(twoTuple.getAElement(), Lists.newArrayList(productCategory));
                    }
                }
            }
        }
        //切换为微服务
        if (servicePointList != null && servicePointList.size() > 0) {
            Map<String, Dict> bankTypeMap = MSDictUtils.getDictMap("banktype");
            Map<String, Dict> paymentTypeMap = MSDictUtils.getDictMap("PaymentType");
            Map<String, Dict> levelMap = MSDictUtils.getDictMap("ServicePointLevel");
            Map<String, Dict> bankIssueTypeMap = MSDictUtils.getDictMap("BankIssueType");
            List<ProductCategory> productCategories;
            for (ServicePoint servicePoint : servicePointList) {
                if (servicePoint.getFinance() != null && servicePoint.getFinance().getBank() != null &&
                        StringUtils.toInteger(servicePoint.getFinance().getBank().getValue()) > 0) {
                    Dict bankTypeDict = bankTypeMap.get(servicePoint.getFinance().getBank().getValue());
                    servicePoint.getFinance().getBank().setLabel(bankTypeDict != null ? bankTypeDict.getLabel() : "");
                }
                if (servicePoint.getFinance().getPaymentType() != null && Integer.parseInt(servicePoint.getFinance().getPaymentType().getValue()) > 0) {
                    servicePoint.getFinance().setPaymentType(paymentTypeMap.get(servicePoint.getFinance().getPaymentType().getValue()));
                }
                if (servicePoint.getLevel() != null && Integer.parseInt(servicePoint.getLevel().getValue()) > 0) {
                    servicePoint.setLevel(levelMap.get(servicePoint.getLevel().getValue()));
                }
                if (servicePoint.getFinance() != null && servicePoint.getFinance().getBankIssue() != null &&
                        StringUtils.toInteger(servicePoint.getFinance().getBankIssue().getValue()) > 0) {
                    Dict bankIssueTypeDict = bankIssueTypeMap.get(servicePoint.getFinance().getBankIssue().getValue());
                    servicePoint.getFinance().getBankIssue().setLabel(bankIssueTypeDict != null ? bankIssueTypeDict.getLabel() : "");
                }
                productCategories = mappingsMap.get(servicePoint.getId());
                if (productCategories != null) {
                    servicePoint.setProductCategories(productCategories);
                }
            }
        }
        return servicePointList;
    }


    /**
     * 分页查询
     * 先从数据库返回id,再根据id从缓存中读取，缓存不存在则再从数据库读取并更新至缓存
     */
    @Override
    public Page<ServicePoint> findPage(Page<ServicePoint> page, ServicePoint entity) {
        entity.setPage(page);

        // add on 2019-12-29 begin
        List<Long> ids = findIdListFromMS(entity);  // add on 2019-12-29
        // add on 2020-8-14 begin
        boolean isSaveLog= false;
        StringBuffer stringBuffer = new StringBuffer();
        // add on 2020-8-14 end
        page.initialize();
        ServicePoint s;
        for (Long id : ids) {
            s = get(id);
            if (s == null || s.getId() == null) {
                //from db
                //切换为微服务 s = dao.get(id);
                s = get(id);
                if (s != null) {
                    page.getList().add(s);
                }
            } else {
                page.getList().add(s);
            }
        }

        //设置网点的覆盖区域
        if (ids != null && ids.size() > 0) {
            Map<Long,String> serviceAreaMap = getServicePointServiceAreas(ids.size() < 100 ? ids : null); // add on 2019-12-3
            for (ServicePoint item : page.getList()) {
                item.setServiceAreas(serviceAreaMap.get(item.getId()));
            }
        }

        return page;
    }

    public Page<ServicePoint> findPricePage(Page<ServicePoint> page, ServicePoint entity) {
        entity.setPage(page);

        List<Long> ids = findIdListFromMSWithPrice(entity);

        page.initialize();
        ServicePoint s;
        for (Long id : ids) {
            s = get(id);
            if (s != null) {
                page.getList().add(s);
            }
        }

        //设置网点的覆盖区域
        if (ids != null && ids.size() > 0) {
            Map<Long,String> serviceAreaMap = getServicePointServiceAreas(ids.size() < 100 ? ids : null); // add on 2019-12-3
            for (ServicePoint item : page.getList()) {
                item.setServiceAreas(serviceAreaMap.get(item.getId()));
                if(item.getRemotePriceFlag() == null){
                    item.setRemotePriceFlag(0);
                }
                if(item.getRemotePriceType() == null){
                    item.setRemotePriceType(0);
                }
                if(item.getRemotePriceEnabledFlag() == null){
                    item.setRemotePriceEnabledFlag(0);
                }
            }
        }

        return page;
    }

    public Map<Long,String> getServicePointServiceAreas(List<Long> servicePointIds) {
        // add on 2019-12-3 ,用来取代dao.getServicePointServiceAreas()
        if (servicePointIds == null || servicePointIds.isEmpty()) {
            return Maps.newHashMap();
        }
        long start = System.currentTimeMillis();
        List<MDServicePointArea> mdServicePointAreaList = Lists.newArrayList();
        if (servicePointIds.size() >100) {
            List<MDServicePointArea> finalServicePointAreaList = Lists.newArrayList();
            Lists.partition(servicePointIds, 100).forEach(ids->{
                List<MDServicePointArea> partAreaList = msServicePointAreaService.findServicePointAreasByServicePointIds(ids);
                if (!org.springframework.util.ObjectUtils.isEmpty(partAreaList)) {
                    finalServicePointAreaList.addAll(partAreaList);
                }
            });
            if (!org.springframework.util.ObjectUtils.isEmpty(finalServicePointAreaList)) {
                mdServicePointAreaList.addAll(finalServicePointAreaList);
            }
        } else {
            mdServicePointAreaList = msServicePointAreaService.findServicePointAreasByServicePointIds(servicePointIds);
        }
        long end = System.currentTimeMillis();
        log.warn("从MS中取网点区域耗时(毫秒):{}", end-start);

        start = System.currentTimeMillis();
        Map<Long,String> servicePointMap = Maps.newHashMap();
        Map<Long,List<Long>> servicePointAreaMap = !org.springframework.util.ObjectUtils.isEmpty(mdServicePointAreaList)?mdServicePointAreaList.stream().collect(Collectors.groupingBy(MDServicePointArea::getServicePointId, Collectors.mapping(MDServicePointArea::getAreaId, Collectors.toList()))):Maps.newHashMap();
        List<Long> areaIdList = !org.springframework.util.ObjectUtils.isEmpty(mdServicePointAreaList)?mdServicePointAreaList.stream().map(MDServicePointArea::getAreaId).distinct().collect(Collectors.toList()):Lists.newArrayList();
        // add on 2020-8-22 begin
        List<Area> areaList = Lists.newArrayList();
        if (areaIdList != null && !areaIdList.isEmpty()) {
            for (List<Long> longList : Lists.partition(areaIdList, 100)) {
                List<Area> partAreaList = msSysAreaService.findDistrictNameListByAreaIds(longList);
                if (partAreaList != null && !partAreaList.isEmpty()) {
                    areaList.addAll(partAreaList);
                }
            }
        }
        // add on 2020-8-22 end
        if (org.springframework.util.ObjectUtils.isEmpty(areaList)){
            areaList = Lists.newArrayList();
        } else {
            areaList = areaList.stream().filter(x->areaIdList.contains(x.getId())).collect(Collectors.toList());
            if (org.springframework.util.ObjectUtils.isEmpty(areaList)) {
                areaList = Lists.newArrayList();
            }
        }
        List<Area> finalAreaList = areaList;
        servicePointAreaMap.forEach((k,v)->{
            if (v != null && !v.isEmpty()) {
                String strAreaName = finalAreaList.stream().filter(x->v.contains(x.getId())).map(Area::getName).collect(Collectors.joining(","));
                servicePointMap.put(k, strAreaName);
            }
        });
        end = System.currentTimeMillis();
        log.warn("从MS中取网点区域，再区域缓存中取名字耗时(毫秒):{}", end-start);
        return servicePointMap;
    }

    public List<ServicePointServiceArea> getAllServicePointServiceAreas(List<Long> servicePointIds) {
        // add on 2019-12-13 ,用来取代dao.getAllServicePointServiceAreas()
        if (servicePointIds == null || servicePointIds.isEmpty()) {
            return Lists.newArrayList();
        }
        long start = System.currentTimeMillis();
        List<MDServicePointArea> mdServicePointAreaList = msServicePointAreaService.findServicePointAreasByServicePointIds(servicePointIds);
        long end = System.currentTimeMillis();
        log.warn("从MS中取网点区域耗时(毫秒):{}", end-start);

        start = System.currentTimeMillis();
        List<Long> areaIdList = !org.springframework.util.ObjectUtils.isEmpty(mdServicePointAreaList)?mdServicePointAreaList.stream().map(MDServicePointArea::getAreaId).distinct().collect(Collectors.toList()):Lists.newArrayList();

        // add on 2020-8-22 begin
        List<Area> areaList = Lists.newArrayList();
        if (areaIdList != null && !areaIdList.isEmpty()) {
            for (List<Long> longList : Lists.partition(areaIdList, 100)) {
                List<Area> partAreaList = msSysAreaService.findDistrictNameListByAreaIds(longList);
                if (partAreaList != null && !partAreaList.isEmpty()) {
                    areaList.addAll(partAreaList);
                }
            }
        }
        // add on 2020-8-22 end

        if (org.springframework.util.ObjectUtils.isEmpty(areaList)){
            areaList = Lists.newArrayList();
        } else {
            areaList = areaList.stream().filter(x->areaIdList.contains(x.getId())).collect(Collectors.toList());
            if (org.springframework.util.ObjectUtils.isEmpty(areaList)) {
                areaList = Lists.newArrayList();
            }
        }
        Map<Long,String> areaMap = !org.springframework.util.ObjectUtils.isEmpty(areaList) ? areaList.stream().collect(Collectors.toMap(Area::getId, Area::getName)):Maps.newHashMap();

        List<ServicePointServiceArea> servicePointServiceAreaList = Lists.newArrayList();
        if (!org.springframework.util.ObjectUtils.isEmpty(mdServicePointAreaList)) {
            mdServicePointAreaList.stream().forEach(mdServicePointArea -> {
                String  strArea = areaMap.get(mdServicePointArea.getAreaId());  // 只取4级区域
                if (strArea != null) {
                    ServicePointServiceArea servicePointServiceArea = new ServicePointServiceArea();
                    servicePointServiceArea.setServicePointId(mdServicePointArea.getServicePointId());
                    servicePointServiceArea.setAreaId(mdServicePointArea.getAreaId());
                    servicePointServiceArea.setAreaName(areaMap.get(mdServicePointArea.getAreaId()));
                    servicePointServiceAreaList.add(servicePointServiceArea);
                }
            });
        }
        end = System.currentTimeMillis();
        log.warn("从MS中取网点区域，再区域缓存中取名字耗时(毫秒):{}", end-start);
        return servicePointServiceAreaList;
    }


    /**
     * 按区县/街道/品类 分页查询可派单列表(以完成单数量倒序排序)
     * 只查询level 1 ~ 5的,且status=10
     */
    public Page<ServicePoint> findServicePointListForPlanNew(Page<ServicePoint> page, ServicePoint entity) {
        entity.setPage(page);

        // add on 2019-12-30 begin
        List<Long> ids = findServicePointIdsForPlanFromMS(entity);

        page.initialize();
        if (ids != null && !ids.isEmpty()) {
            ServicePoint s;
            for (Long id : ids) {
                s = getFromCache(id);
                if (s != null && s.getId() != null) {
                    page.getList().add(s);
                }
                else {
                    s = get(id);
                    if (s != null) {
                        page.getList().add(s);
                    }
                }
            }
        }

        return page;
    }

    /**
     * 查询并分页显示网点
     * 只查询有效网点，且有质保等级设定，不包含返现网点
     */
    public Page<ServicePoint> findServicePointListForDeposit(Page<ServicePoint> page, ServicePoint entity) {
        //查询微服务，返回网点ID列表
        Page<Long> msPage = new Page<>(page.getPageNo(),page.getPageSize());
        msPage = msServicePointService.findIdsByServicePointWithDepositLevelForSD(msPage, entity);
        List<Long> ids = msPage.getList();
        page.setCount(msPage.getCount());
        page.initialize();
        if (!CollectionUtils.isEmpty(ids)) {
            Map<Long,MDDepositLevel> depositLevelMap = msDepositLevelService.getAllLevelMap();
            Map<Long,ServicePointFinance> financeMap = getDepositByIds(ids);
            ServicePoint s;
            MDDepositLevel depositLevel;
            Long depositeLevelId;
            ServicePointFinance finance;
            for (Long id : ids) {
                s = getFromCache(id);
                if (s != null && s.getId() != null) {
                    //质保金等级
                    depositeLevelId = Optional.ofNullable(s.getMdDepositLevel()).map(t->t.getId()).orElse(null);
                    if(depositeLevelId != null && depositeLevelId > 0) {
                        depositLevel = depositLevelMap.get(depositeLevelId);
                        if (depositLevel != null) {
                            s.setMdDepositLevel(depositLevel);
                        }
                    }
                    //已缴质保金额
                    finance = financeMap.get(id);
                    s.setFinance(finance);
                    page.getList().add(s);
                }
                else {
                    s = get(id);
                    if (s != null) {
                        page.getList().add(s);
                    }
                }
            }
        }

        return page;
    }

    /**
     * 根据ID列表获取网点质保金余额
     * @param ids
     * @return
     */
    public Map<Long,ServicePointFinance> getDepositByIds(List<Long> ids){
        if(CollectionUtils.isEmpty(ids)){
            return Maps.newHashMap();
        }
        List<ServicePointFinance> list = dao.getDepositByIds(ids);
        if(CollectionUtils.isEmpty(list)){
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(ServicePointFinance::getId,t->t));
    }

    /**
     * 根据ID从主库获取网点质保金余额
     */
    public ServicePointFinance getDepositFromMasterById(Long id){
        return dao.getDepositFromMasterById(id);
    }

    /**
     * 质保金充值
     * @param currency
     * @return
     */
    @Transactional
    public long depositRecharge(EngineerCurrencyDeposit currency){
        long servicePointId = Optional.ofNullable(currency).map(EngineerCurrencyDeposit::getServicePoint).map(ServicePoint::getId).orElse(0l);
        if(servicePointId <= 0){
            return 0l;
        }
        ServicePointFinance finance = getDepositFromMasterById(servicePointId);
        if(finance == null){
            throw new OrderException("读取网点质保金失败");
        }
        currency.setBeforeBalance(finance.getDeposit());
        currency.setBalance(currency.getAmount() + finance.getDeposit());
        return dao.updateDepositWhenRecharge(servicePointId,currency.getAmount());
    }

    /**
     * 分页查询待审核网点
     */
    public Page<ServicePoint> findApprovePage(Page<ServicePoint> page, ServicePoint entity) {
        entity.setPage(page);
        page.setList(dao.findApproveList(entity));
        //切换为微服务
        Map<String, Dict> levelMap = MSDictUtils.getDictMap("ServicePointLevel");
        for (ServicePoint servicePoint : page.getList()) {
            if (servicePoint.getLevel() != null && Integer.parseInt(servicePoint.getLevel().getValue()) > 0) {
                servicePoint.getLevel().setLabel(levelMap.get(servicePoint.getLevel().getValue()).getLabel());
            }
        }
        return page;
    }

    public Page<ServicePoint> findServicePointListForStation(Page<ServicePoint> page, ServicePoint entity)
    {
        entity.setPage(page);
        List<Long> ids = findIdListFromMS(entity);

        page.initialize();
        ServicePoint s;
        for (Long id : ids) {
            s = getFromCache(id);
            if (s == null || s.getId() == null) {
                s = get(id);
                if (s != null) {
                    page.getList().add(s);
                }
            } else {
                page.getList().add(s);
            }
        }

        if (ids.size() > 0) {
            List<ServicePointServiceArea> servicePointServiceAreaList = getAllServicePointServiceAreas(ids.size() < 100 ? ids : null);
            if (org.springframework.util.ObjectUtils.isEmpty(servicePointServiceAreaList)) {
                return page;
            }

            // add on 2020-8-25 begin
            List<Long> areaIds = servicePointServiceAreaList.stream().map(r->r.getAreaId()).distinct().collect(Collectors.toList());
            List<Area> totalAreaList = Lists.newArrayList();
            if (areaIds != null && !areaIds.isEmpty()) {
                for (List<Long> longList : Lists.partition(areaIds, 100)) {
                    List<Area> partAreaList = msSysAreaService.findListByAreaIdList(longList);
                    if (partAreaList!= null && !partAreaList.isEmpty()) {
                        totalAreaList.addAll(partAreaList);
                    }
                }
            }
            Map<Long,List<Long>> servicePointAreaIdsMap = servicePointServiceAreaList.stream()
                    .collect(Collectors.groupingBy(ServicePointServiceArea::getServicePointId, Collectors.mapping(ServicePointServiceArea::getAreaId, Collectors.toList())));
            // add on 2020-8-25 end
            for (ServicePoint item : page.getList()) {
                List<Long> servicepointAreaIds = servicePointAreaIdsMap.get(item.getId());
                List<Area> areaList = !ObjectUtils.isEmpty(totalAreaList)? totalAreaList.stream().filter(x->servicepointAreaIds.contains(x.getId())).collect(Collectors.toList()): Lists.newArrayList();
                // add on 2020-8-25 end
                if (!org.springframework.util.ObjectUtils.isEmpty(areaList)) {
                    ServicePointStation servicePointStation = ServicePointStation.builder()
                            .servicePoint(item)
                            .orderBy("a.servicepoint_id,c.parent_id")
                            .build();
                    List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);

                    for(Area area:areaList) {
                        // add on 2020-2-10 begin
                        List<String> townNameList = Lists.newArrayList();
                        try {
                            townNameList = servicePointStationList.stream()
                                    .filter(r -> r.getDelFlag().equals(Area.DEL_FLAG_NORMAL) && r.getArea().getParent().getId().equals(area.getId()))
                                    .map(ServicePointStation::getName).collect(Collectors.toList());
                        } catch(Exception ex){}
                        // add on 2020-2-10 end

                        String strTownName = "";
                        if (!org.springframework.util.ObjectUtils.isEmpty(townNameList)) {
                            strTownName = org.apache.commons.lang.StringUtils.join(townNameList.toArray(), ",");
                        }
                        area.setRemarks(strTownName);
                    }
                    areaList = areaList.stream().sorted(Comparator.comparing(r->r.getParent().getId())).collect(Collectors.toList());
                    item.setAreas(areaList);
                }
            }
        }
        return page;
    }

    /**
     * 审核网点
     *
     * @param ids
     * @param updateBy
     */
    @Transactional(readOnly = false)
    public void approve(List<Long> ids, Long updateBy) {
        MSErrorCode msErrorCode = msServicePointService.approve(ids, updateBy);
        if (msErrorCode.getCode()>0) {
            throw new RuntimeException("调用网点微服务审核网点失败.原因:"+msErrorCode.getMsg());
        }
        try {
            List<ServicePoint> list = findListByIds(ids);
            if (list != null && !list.isEmpty()) {
                list.stream().forEach(servicePoint -> {
                    // updateServicePointCache(servicePoint);//mark on 2020-1-14  web端去servicePoint
                });
            }
        } catch (Exception e1) {
            log.error("update cache: approve servicepoint,", e1);
        }
    }

    /**
     * 根据网点id列表获取网点列表
     * @param ids
     * @return
     */
    public List<ServicePoint> findListByIds(List<Long> ids) {
        List<ServicePoint> servicePointList = msServicePointService.findListByIds(ids);
        if (servicePointList != null && !servicePointList.isEmpty()) {
            servicePointList.stream().forEach(servicePoint -> {
                getServicePointExtraProperties(servicePoint, false);
            });
        }
        return servicePointList;
    }

    /**
     * 读取网点负责的区域列表
     *
     * @param id 网点id
     * @return
     */
    public List<Area> getAreas(Long id) {
        List<Long> areaIdList = msServicePointAreaService.findAreaIds(id);
        List<Area> areaList = ObjectUtils.isEmpty(areaIdList)?Lists.newArrayList():areaService.findServicePointAreas(areaIdList);
        return areaList;
    }

    /**
     * 读取网点负责的区域列表
     *
     * @param id 网点id
     * @return
     */
    public List<Integer> getAreaIds(Long id) {

        List<Long> areaIdsFromMS = msServicePointAreaService.findAreaIds(id);
        List<Integer> areaIds = areaIdsFromMS != null && !areaIdsFromMS.isEmpty()? areaIdsFromMS.stream().map(r->r.intValue()).collect(Collectors.toList()) : Lists.newArrayList();
        return areaIds;
    }

    /**
     * 读取网点负责的产品id列表
     *
     * @param id
     * @return
     */
    public List<Integer> getProductIds(Long id) {
        MDServicePointProduct mdServicePointProduct = new MDServicePointProduct();
        mdServicePointProduct.setServicePointId(id);
        List<Long> productIdsFromMS = msServicePointProductService.findProductIds(mdServicePointProduct);
        List<Integer> productIds = !org.springframework.util.ObjectUtils.isEmpty(productIdsFromMS)?productIdsFromMS.stream().map(x->x.intValue()).collect(Collectors.toList()) : Lists.newArrayList();
        return productIds;
    }

    /**
     * 读取网点负责的产品id列表(后台)
     *
     * @param id
     * @return
     */
    public List<Product> getProducts(Long id) {

        List<Product> productList = Lists.newArrayList();
        List<Integer> productIds = getProductIds(id);
        String productIdStr = productIds != null && !productIds.isEmpty()?productIds.stream().map(r->r.toString()).distinct().collect(Collectors.joining(",")):"";

        Product product = new Product();
        product.setProductIds(productIdStr);
        //原sql要求输出字段：p.id,p.name,p.set_flag,p.sort,p.product_category_id as "productCategory.id"
        List<Product> products = msProductService.findListByConditions(product);
        if (products != null && !products.isEmpty()) {
            // 按sort排序
            productList = products.stream().sorted(Comparator.comparing(Product::getSort)).collect(Collectors.toList());
        }
        return productList;
        // add on 2019-8-21 end
    }

    public List<ServicePointProduct>  getServicePointProductsByIdsNew(ServicePrice servicePrice) {
        // add on 2019-12-18
        MDServicePointProductDto mdServicePointProductDto = new MDServicePointProductDto();
        List<ServicePointProduct> servicePointProductList = Lists.newArrayList();
        if (servicePrice.getServicePoint()!= null) {
            MDServicePointDto servicePointDto = new MDServicePointDto();
            servicePointDto.setId(servicePrice.getServicePoint().getId());
            mdServicePointProductDto.setServicePoint(servicePointDto);
        }
        if (servicePrice.getProduct()!= null) {
            MDProductDto productDto = new MDProductDto();
            productDto.setId(servicePrice.getProduct().getId());
            if (servicePrice.getProductCategory()!= null) {
                productDto.setProductCategoryId(servicePrice.getProductCategory().getId());
            }
            mdServicePointProductDto.setProduct(productDto);
        }

        if (servicePrice.getPage() != null) {
            mdServicePointProductDto.setPage(new MSPage<>(servicePrice.getPage().getPageNo(), servicePrice.getPage().getPageSize()));
            servicePointProductList = msServicePointProductService.findList(mdServicePointProductDto);
            servicePrice.getPage().setPageNo(mdServicePointProductDto.getPage().getPageNo());
            servicePrice.getPage().setPageSize(mdServicePointProductDto.getPage().getPageSize());
            servicePrice.getPage().setCount(mdServicePointProductDto.getPage().getRowCount());
        }
        return servicePointProductList;
    }

    /**
     * 逻辑删除网点
     * 同时删除关联区域,逻辑删除安维账号,
     * 同时清除缓存
     *
     * @param servicePoint
     */
    @Transactional(readOnly = false)
    public void delete(ServicePoint servicePoint) {
        if (servicePoint == null || servicePoint.getId() == null) return;


        servicePoint.setDelFlag(ServicePoint.DEL_FLAG_DELETE);

        MSErrorCode msErrorCode = msServicePointService.delete(servicePoint);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务删除网点时失败.失败原因:" + msErrorCode.getMsg());
        }

        // add on 2019-9-12 end
        //TODO：写网点操作日志
        servicePointLogService.saveServicePointLog(servicePoint.getId(), ServicePointLog.ServicePointLogType.DEL_SERVICEPOINT, "删除网点", "", servicePoint.getCreateBy());
    }

    /**
     * 检查编号是否重复
     *
     * @param exceptId 排除的id
     * @param no       编号
     * @return "true"  :   不存在
     * "false" :   存在
     */
    public String checkServicePointNo(Long exceptId, String no) {
        if (StringUtils.isBlank(no)) {
            return "true";
        }
        return msServicePointService.getServicePointNo(no, exceptId)==null?"true":"false"; // add on 2019-9-11
    }

    /**
     * 检查编号是否重复
     *
     * @param exceptId 排除的id
     * @param contact  手机号
     * @return "true"  :   不存在
     * "false" :   存在
     */
    public String checkServicePointContact(Long exceptId, String contact) {
        if (StringUtils.isBlank(contact)) {
            return "true";
        }
        return msServicePointService.getServicePointIdByContact(contact, exceptId)== null ? "true" : "false"; //add on 2019-9-11
    }

    /**
     * 检查编号是否重复
     *
     * @param exceptId 排除的id
     * @param bankNo   银行卡号
     * @return "true"  :   不存在
     * "false" :   存在
     */
    public String checkBankNo(Long exceptId, String bankNo) {
        if (StringUtils.isBlank(bankNo)) {
            return "true";
        }
        return msServicePointService.getServicePointIdByBankNo(bankNo, exceptId) == null?"true":"false";  //add on 2019-9-11
    }

    /**
     * 保存派单的备注
     * @param servicePointId
     * @param planRemark
     */
    public void savePlanRemark (Long servicePointId,String planRemark){
        User user=UserUtils.getUser();
        if (servicePointId==null ){return;}

        List<ServicePointPlanRemarkModel> lists =getPlanRemarks(servicePointId);

        if (lists != null && lists.size() > 0 && lists.get(0).getPlanRemark().equalsIgnoreCase(planRemark)){
            throw  new RuntimeException("该备注已经保存。");
        }

        ServicePointPlanRemarkModel entity=new ServicePointPlanRemarkModel();
        entity.setDate(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"));
        entity.setName(user.getName());
        entity.setPlanRemark(planRemark);
        lists.add(entity);

        MSErrorCode msErrorCode = msServicePointService.updatePlanRemark(servicePointId, planRemark);
        if (msErrorCode.getCode()>0) {
            throw new RuntimeException("调用微服务保存派单备注信息失败.失败原因:"+msErrorCode.getMsg());
        }

        //TODO: 写网点操作日志
        servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_PLAN_REMARK, "派单备注", planRemark, user);
    }

    /**
     * 单独保存备注
     * @param servicePointId
     * @param remark
     */
    @Transactional
    public void updateRemark (Long servicePointId,String remark){
        doUpdateRemark(servicePointId,remark,((id, remarks)->{
            MSErrorCode msErrorCode = msServicePointService.updateRemark(id, remarks);
            if (msErrorCode.getCode() >0) {
                throw new RuntimeException("更新网点备注信息失败,失败原因:"+msErrorCode.getMsg());
            }
        }));
    }

    public void updateRemarkWithoutSaveToMS (Long servicePointId,String remark){
        doUpdateRemark(servicePointId,remark,null);
    }

    public void doUpdateRemark (Long servicePointId, String remark, BiConsumer<Long,String> callback){
        User user=UserUtils.getUser();
        if (servicePointId==null ){return;}

        List<ServicePointPlanRemarkModel> lists =getRemarksList(servicePointId);

        if (lists != null && lists.size() > 0 && lists.get(0).getPlanRemark().equalsIgnoreCase(remark)){
            //throw  new RuntimeException("该备注已经保存。");  //mark on 2020-2-24
            return;  //add on 2020-2-24
        }

        // mark on 2019-9-11 //取消保存json数据到servicePoint中
        ServicePointPlanRemarkModel entity=new ServicePointPlanRemarkModel();
        entity.setDate(DateUtils.getDate("yyyy-MM-dd HH:mm:ss"));
        entity.setName(user.getName());
        entity.setPlanRemark(remark);
        lists.add(entity);

        Optional.ofNullable(callback).ifPresent(c->c.accept(servicePointId, remark));
        //TODO: 写网点操作日志
        servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_REMARK, "网点备注", remark, user);
        //读取网点再更新缓存
        ServicePoint servicePoint = getFromCache(servicePointId);
        if(servicePoint != null) {
            servicePoint.setRemarks(remark);
        }
        // 更新缓存
    }

    /**
     * 获取安维网店的历史派单备注列表
     * @param servicePointId
     * @return
     */
    public  List<ServicePointPlanRemarkModel> getPlanRemarks(Long servicePointId){
        List<ServicePointPlanRemarkModel> lists= servicePointLogService.getHisPlanRemarks(servicePointId);  // add on 2019-9-11
        if (lists!=null && lists.size()>0){
            lists.sort(new Comparator<ServicePointPlanRemarkModel>() {
                @Override
                public int compare(ServicePointPlanRemarkModel o1, ServicePointPlanRemarkModel o2) {
                    if (StringUtils.isNotBlank(o1.getDate()) && StringUtils.isNotBlank(o2.getDate())){
                        return DateUtils.parseDate(o2.getDate()).compareTo(DateUtils.parseDate(o1.getDate()));
                    }else {
                        return 0;
                    }

                }
            });
        }

        if (lists==null){
            lists=new ArrayList<>();
        }
        return lists;
    }


    /**
     * 获取安维网店的历史派单备注列表
     * @param servicePointId
     * @return
     */
    public  List<ServicePointPlanRemarkModel> getRemarksList(Long servicePointId){
        List<ServicePointPlanRemarkModel> lists = servicePointLogService.getHisRemarks(servicePointId);  //add on 2019-9-11
        if (lists!=null && lists.size()>0){
            lists.sort(new Comparator<ServicePointPlanRemarkModel>() {
                @Override
                public int compare(ServicePointPlanRemarkModel o1, ServicePointPlanRemarkModel o2) {
                    if (StringUtils.isNotBlank(o1.getDate()) && StringUtils.isNotBlank(o2.getDate())){
                        return DateUtils.parseDate(o2.getDate()).compareTo(DateUtils.parseDate(o1.getDate()));
                    }else {
                        return 0;
                    }

                }
            });
        }
        if (lists==null){
            lists=new ArrayList<>();
        }
        return lists;
    }

    /**
     * 读取网点余额,最后付款日期及最后付款金额
     * @param servicePointIds
     * @return
     */
    public List<Map<String,Object>> getServicePointBalances(List<Long> servicePointIds){
        return dao.getServicePointBalances(servicePointIds);
    }

    //endregion 网点

    //region 安维人员管理

    /**
     * 检查安维手机是否重复
     *
     * @param exceptId 排除的id
     * @param mobile   编号
     * @return "true"  :   不存在
     * "false" :   存在
     */
    public String checkEngineerMobile(Long exceptId, String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return "true";
        }
        return msEngineerService.getEngineerIdByMobile(mobile, exceptId) == null ? "true" : "false"; // add on 2019-10-21
    }

    public Engineer getEngineer(Long id) {
        Engineer engineer = engineerService.getEngineer(id);  // add on 2019-10-21
        return engineer;
    }

    /**
     * 为Engineer数据赋值 // add on 2019-9-16
     * @param engineer
     * @return
     */
    public Engineer populateEngineer(Engineer engineer) {
        if (engineer != null) {
            // 调用微服务ServicePoint
            ServicePoint servicePoint = msServicePointService.getById(engineer.getServicePoint().getId());
            if (servicePoint != null) {
                engineer.getServicePoint().setServicePointNo(servicePoint.getServicePointNo());
                engineer.getServicePoint().setName(servicePoint.getName());
            }
        }
        // add on 2019-9-16 end
        //切换为微服务
        if (engineer != null && engineer.getLevel() != null && Integer.parseInt(engineer.getLevel().getValue()) > 0) {
            String levelName = MSDictUtils.getDictLabel(engineer.getLevel().getValue(), "ServicePointLevel", "");
            engineer.getLevel().setLabel(levelName);
        }
        return engineer;
    }

    /**
     * 读取安维负责的区域ID列表
     *
     * @param id 安维id
     * @return
     */
    public List<Long> getEngineerAreaIds(Long id) {
        return msEngineerAreaService.findEngineerAreaIds(id); //add on 2019-11-7 //EngineerArea微服务
    }

    /**
     * 安维负责的区域列表
     *
     * @param id
     * @return
     */
    public List<Area> getEngineerAreaList(Long id) {
        List<Long> areaIds = getEngineerAreaIds(id);
        List<Area> areaListFromMS = areaService.findEngineerAreas(areaIds);
        return areaListFromMS;
    }

    //切换为微服务
    public List<Engineer> findEngineerList(Engineer engineer) {
        // add on 2019-11-9 begin
        engineer.setAppFlag(-1);  // add on 2020-12-11
        List<Engineer> engineerList = Lists.newArrayList();
        Page<Engineer> engineerPage = new Page<>();
        engineerPage.setPageSize(1000);
        engineer.setPage(engineerPage);
        Page<Engineer> engineerIdPage = msEngineerService.findEngineerList(engineerPage, engineer);
        if (engineerIdPage.getList()!= null && !engineerIdPage.getList().isEmpty()) {
            engineerList.addAll(engineerIdPage.getList());
        }

        for(int i=2; i< engineerIdPage.getTotalPage()+1;i++) {
            Page<Engineer> engineerPage1 = new Page<>();
            engineerPage1.setPageSize(1000);
            engineerPage1.setPageNo(i);
            engineer.setPage(engineerPage1);

            Page<Engineer> engineerIdPage1 = msEngineerService.findEngineerList(engineerPage1, engineer);
            //log.warn("记录数{}",engineerIdPage1.getList());
            if (engineerIdPage1.getList()!= null && !engineerIdPage1.getList().isEmpty()) {
                engineerList.addAll(engineerIdPage1.getList());
            }
        }

        if (engineerList == null || engineerList.isEmpty()) {
            return engineerList;
        }
        List<Long> engineerIds = engineerList.stream().map(Engineer::getId).distinct().collect(Collectors.toList());
        List<User> userList = Lists.newArrayList();
        if (engineerIds != null && !engineerIds.isEmpty()) {
            if (engineerIds.size()>200) {
                List<User> partUsers  = Lists.newArrayList();
                Lists.partition(engineerIds, 200).forEach(partIds->{
                    List<User> tempUserList = systemService.findEngineerAccountList(partIds, null);
                    if (tempUserList != null && !tempUserList.isEmpty()) {
                        partUsers.addAll(tempUserList);
                    }
                });
                if (partUsers != null && !partUsers.isEmpty()) {
                    userList.addAll(partUsers);
                }
            } else {
                userList = systemService.findEngineerAccountList(engineerIds, null);
            }
        }

        Map<Long, User> userMap = userList != null && !userList.isEmpty() ? userList.stream().filter(r->r.getEngineerId() != null).collect(Collectors.toMap(User::getEngineerId, Function.identity())):Maps.newHashMap();

        engineerList.stream().forEach(engineerEntity -> {
            User user = userMap.get(engineerEntity.getId());
            if (user != null) {
                engineerEntity.setAppLoged(user.getAppLoged());
                engineerEntity.setAccountId(user.getId());
            }

            if (engineerEntity.getLevel() != null && Integer.parseInt(engineerEntity.getLevel().getValue()) > 0) {
                String levelName = MSDictUtils.getDictLabel(engineerEntity.getLevel().getValue(), "ServicePointLevel", "");
                engineerEntity.getLevel().setLabel(levelName);
            }
        });
        return engineerList;
        // add on 2019-11-9 end

        //return list;
    }

    public Page<Engineer> findEngineerListForPage(Page<Engineer> page, Engineer engineer) {
        // add 2019-11-9
        Page<Engineer> engineerPage = msEngineerService.findEngineerList(page, engineer);
        List<Engineer> engineerList = engineerPage.getList();

        if (engineerList != null && engineerList.size() > 0) {
            List<Long> engineerIds = engineerList.stream().map(engineer1 -> engineer1.getId()).distinct().collect(Collectors.toList());
            List<User> userList = systemService.findEngineerAccountList(engineerIds, null); //subFlag 1：是子帐号　0：不是子帐号
            Map<Long,User> userMap = userList!= null&& !userList.isEmpty()?userList.stream().collect(Collectors.toMap(User::getEngineerId,Function.identity())):Maps.newHashMap();
            Map<Long, String> serviceAreaMap = getEngineerServiceAreas(engineerIds); // add on 2019-11-8

            Map<String, Dict> levelMap = MSDictUtils.getDictMap("EngineerLevel");
            for (Engineer eng : engineerList) {
                if (eng.getLevel() != null && Integer.parseInt(eng.getLevel().getValue()) > 0) {
                    eng.setLevel(levelMap.get(eng.getLevel().getValue()));
                }
                if (eng.getArea()!= null && eng.getArea().getId() != null) {
                    Area area = areaService.getFromCache(eng.getArea().getId());
                    eng.setArea(area);
                }

                User user = userMap.get(eng.getId());
                if (user != null) {
                    eng.setAccountId(user.getId());
                    eng.setAppLoged(user.getAppLoged());
                }
                eng.setAreas(serviceAreaMap.get(eng.getId()));
            }
        }

        engineerPage.setList(engineerList);
        return engineerPage;
    }

    /**
     * 按手机号返回安维帐号
     *
     * @param phone    手机号
     * @param expectId 排除安维id
     */
    public User getEngineerByPhoneExpect(String phone, Long expectId) {
        return dao.getEngineerByPhoneExpect(phone, expectId);
    }

    /**
     * 按帐号ID获得有APP权限的安维师傅的基本信息
     *
     * @param userId 帐号ID
     * @return
     */
    public Engineer getAppEngineer(Long userId, Long tokenTimeOut) {
        String key = String.format(RedisConstant.APP_SESSION, userId);
        Engineer engineer = null;
        Long engineerId = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_NEW_APP_DB, key, "engineerId", Long.class);
        Long servicePointId = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_NEW_APP_DB, key, "servicePointId", Long.class);
        if (engineerId != null && servicePointId != null) {
            engineer = getEngineerFromCache(servicePointId, engineerId);
        } else {
            engineer = getEngineerByUserId(userId); // add on 2019-10-22 //Engineer微服务
        }
        ServicePoint servicePoint = getFromCache(engineer.getServicePoint().getId()); // add on 2020-3-4
        if (servicePoint != null) {
            engineer.setServicePoint(servicePoint);
        }
        if (engineer != null) {
            //更新网点主帐号：app已登录
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_NEW_APP_DB, key, "engineerId", engineer.getId(), tokenTimeOut);
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_NEW_APP_DB, key, "servicePointId", engineer.getServicePoint().getId(), tokenTimeOut);
        }
        return engineer;
    }

    public Engineer getEngineerByUserId(Long userId) {
        // add on 2019-10-22
        // Engineer微服务
        User user = systemService.getUser(userId);
        if (user != null && user.getDelFlag().equals(User.DEL_FLAG_NORMAL)) {
            Long engineerId = user.getEngineerId();
            Engineer engineer = msEngineerService.getByIdFromCache(engineerId);
            if (engineer != null) {
                engineer.setAppLoged(user.getAppLoged());
                engineer.setAccountId(user.getId());
                if (engineer.getArea() != null && engineer.getArea().getId() != null) {
                    Area area = areaService.getFromCache(engineer.getArea().getId());
                    if (area != null){
                        engineer.getArea().setName(area.getName());
                        engineer.getArea().setFullName(area.getFullName());
                    }
                }
                engineer = populateEngineer(engineer);
                return engineer;
            }
        }
        return null;
    }

    /**
     * 分页查询
     * 先从数据库返回id,再根据id从缓存中读取，缓存不存在则再从数据库读取并更新至缓存
     */
    public Page<Engineer> findPage(Page<Engineer> page, Engineer entity) {
        entity.setPage(page);
        return findEngineerListForPage(page, entity);
    }

    public Map<Long, String> getEngineerServiceAreas(List<Long> engineerIds) {
        List<MDEngineerArea> mdEngineerAreaList = msEngineerAreaService.findEngineerAreasWithIds(engineerIds);

        Map<Long, String> serviceAreaMap = Maps.newHashMap();
        if (mdEngineerAreaList != null && !mdEngineerAreaList.isEmpty()) {
            // 获取区域id列表，再通过区域id列表获取区域哈希对象
            List<Long> areaIds = mdEngineerAreaList.stream().map(MDEngineerArea::getAreaId).distinct().collect(Collectors.toList());
            Map<Long,String> areaMap = Maps.newHashMap();
            if (areaIds != null && !areaIds.isEmpty()) {
                areaIds.stream().forEach(r->{
                    Area area = areaService.getFromCache(r,Area.TYPE_VALUE_COUNTY);
                    if (area != null) {
                        areaMap.put(r, area.getName());
                    }
                });
            }
            // 生成key为安维id,value为区域名称哈希对象
            Map<Long, List<MDEngineerArea>> engineerAreaMap = mdEngineerAreaList.stream().collect(Collectors.groupingBy(MDEngineerArea::getEngineerId));
            engineerAreaMap.forEach((k,v)->{
                if (v != null && !v.isEmpty()) {
                    String areaNames = v.stream().filter(mdEngineerArea -> areaMap.get(mdEngineerArea.getAreaId()) != null).map(mdEngineerArea -> areaMap.get(mdEngineerArea.getAreaId())).collect(Collectors.joining(","));
                    serviceAreaMap.put(k, areaNames);
                }
            });
        }
        return serviceAreaMap;
    }



    /**
     * 所有安维人员姓名//为报表网点付款清单所需 (add on 2019-9-25)
     * 用于在redis中按网点缓存其下属安维人员列表
     * key: MD:SERVICEPOINT:ENGINEER:#id
     */
    public List<Engineer> findAllEngineersName(List<Long> engineerIds) {
        return findAllEngineersName(engineerIds, Arrays.asList("id","name","appFlag","contactInfo"));
    }

    public List<Engineer> findAllEngineersName(List<Long> engineerIds, List<String> fields) {
        if (engineerIds != null && engineerIds.size()>0) {
            return msEngineerService.findEngineersByIds(engineerIds, fields);
        }
        return Lists.newArrayList();
    }


    /**
     * 重置密码
     * 手机号后6位
     *
     * @param engineer
     */
    @Transactional(readOnly = false)
    public void resetPassword(User engineer) {
        dao.resetPassword(engineer);
    }

    /**
     * 保存
     */
    @Transactional(readOnly = false)
    public void save(Engineer engineer) {
        boolean isNew = engineer.getIsNewRecord();

        ServicePoint servicePoint = getFromCache(engineer.getServicePoint().getId());
        if (servicePoint == null) {
            servicePoint = get(engineer.getServicePoint().getId());
        }
        if (engineer.getEngineerAddress() != null && engineer.getEngineerAddress().getAreaName() != null) {
            String address = engineer.getEngineerAddress().getAreaName() + engineer.getEngineerAddress().getAddress();
            engineer.getEngineerAddress().setAddress(address);
        }
        if(engineer.getEngineerAddress() != null && engineer.getEngineerAddress().getAreaId() != null){
            Area city = areaService.getFromCache(engineer.getEngineerAddress().getAreaId());
            if(city != null){
                engineer.getEngineerAddress().setCityId(city.getParentId());
                Area province = areaService.getFromCache(city.getParentId());
                if(province != null){
                    engineer.getEngineerAddress().setProvinceId(province.getParentId());
                }
            }
        }
        if(engineer.getArea() != null && engineer.getArea().getId() != null){
            engineer.setAddress(engineer.getAddress().replace(engineer.getArea().getFullName(),""));
            engineer.setAddress(engineer.getArea().getFullName() + " " + engineer.getAddress());
        }
        if (isNew) {
            servicePoint.setSubEngineerCount(servicePoint.getSubEngineerCount() + 1);
            // add on 2020-12-26 begin
            // 先查询该网点是否有主账号
            Integer masterEngineerCount = msEngineerService.checkMasterEngineer(engineer.getServicePoint().getId(), null);
            if (masterEngineerCount != null && masterEngineerCount == 0 && engineer.getMasterFlag() == 0) {
                throw new RuntimeException("网点:" + servicePoint.getServicePointNo()+"没有主账号,请设置主账号!" );
            }
            // add on 2020-12-26 end

            engineer.preInsert();
            MSErrorCode msErrorCode = msEngineerService.save(engineer,true);
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("保存师傅信息到微服务中出错。出错原因:" + msErrorCode.getMsg());
            }
            List<Long> engineerIds = Lists.newArrayList();
            if (engineer.getMasterFlag() == 1) {
                servicePoint.setUpdateBy(engineer.getCreateBy());
                servicePoint.setUpdateDate(engineer.getCreateDate());
                servicePoint.setPrimary(engineer);
                engineerIds = msEngineerService.findSubEngineerIds(engineer.getId(), servicePoint.getId());
                if (engineerIds != null && engineerIds.size() > 0) {
                    dao.resetUserEngineerSubFlag(engineerIds);
                }

            }
            //add sys_user,帐号:手机号 密码:手机号
            User user = new User();
            user.setCompany(new Office(engineer.getServicePoint().getId()));//网点
            user.setLoginName(engineer.getContactInfo());
            user.setName(engineer.getName());
            user.setMobile(engineer.getContactInfo());
            user.setUserType(User.USER_TYPE_ENGINEER);
            user.setSubFlag(engineer.getMasterFlag() == 1 ? 0 : 1);
            user.getRoleList().add(new Role(6l));
            user.setPassword(SystemService.entryptPassword(StringUtils.right(engineer.getContactInfo().trim(), 6)));//手机号后6位
            user.setCreateBy(engineer.getCreateBy());
            user.setCreateDate(engineer.getCreateDate());
            user.setEngineerId(engineer.getId());
            userDao.insert(user);
            // add on 2020-10-14 begin
            if (engineer.getMasterFlag() == 1) {
                userDao.insertUserRole(user);//角色
            }
            // add on 2020-10-14 end
            MSUserUtils.addUserToRedis(user);//user微服务
            engineer.setAccountId(user.getId());// 18/01/16
            //区域
            List<Long> areas = engineer.getAreaIds();

            HashMap<String, Object> maps = Maps.newHashMap();
            if (engineer.getMasterFlag() == 0) {
                //更新网点数量
                maps.put("subEngineerCount", 1);
                maps.put("id", servicePoint.getId());
            }
            if (engineer.getMasterFlag() == 1) {
                updatePrimaryAccount(servicePoint);                  //add on 2019-9-17
                if (engineerIds != null && engineerIds.size() > 0) {
                    MSErrorCode msErrorCode1 = msEngineerService.resetEngineerMasterFlag(engineerIds);
                    if (msErrorCode1.getCode() > 0) {
                        throw new RuntimeException("重置网点下师傅主账号信息出错。出错原因:" + msErrorCode1.getMsg());
                    }
                }
            } else if (engineer.getMasterFlag() == 0) {
                updateServicePointByMap(maps);    // add on 2019-10-4
            }
            // add on 2019-10-17 end
            // add on 2019-11-7 begin  //EngineerArea微服务
            if (areas != null && !areas.isEmpty()) {
                msEngineerAreaService.assignEngineerAreas(areas, engineer.getId());
            }
            // add on 2019-11-7 end
        } else {
            // add on 2020-12-26 begin
            // 先查询该网点是否有主账号
            Integer masterEngineerCount = msEngineerService.checkMasterEngineer(engineer.getServicePoint().getId(), engineer.getId());
            if (masterEngineerCount != null && masterEngineerCount == 0 && engineer.getMasterFlag() == 0) {
                throw new RuntimeException("网点:" + servicePoint.getServicePointNo()+"没有主账号,请设置主账号!" );
            }
            // add on 2020-12-26 end

            engineer.preUpdate();
            // add on 2019-10-17 begin
            // 集中调用微服务
            MSErrorCode msErrorCode = msEngineerService.save(engineer,false);
            if (msErrorCode.getCode() > 0) {
                throw new RuntimeException("更新师傅信息到微服务中出错。出错原因:" + msErrorCode.getMsg());
            }
            // add on 2019-10-17 end
            if (engineer.getMasterFlag() != engineer.getOrgMasterFlag()) {
                //主帐号->子帐号
                if (engineer.getMasterFlag() == 0) {
                    servicePoint.setPrimary(new Engineer(0l));
                } else {
                    //子帐号 -> 主帐号
                    servicePoint.setPrimary(engineer);
                }
                servicePoint.setUpdateDate(engineer.getUpdateDate());
                servicePoint.setUpdateBy(engineer.getUpdateBy());
                updatePrimaryAccount(servicePoint);  //add on 2019-10-4
                if (servicePoint.getPrimary().getId() > 0l) {
                    List<Long> engineerIds = msEngineerService.findSubEngineerIds(engineer.getId(), servicePoint.getId()); //add on 2019-10-17
                    if (engineerIds != null && engineerIds.size() > 0) {
                        dao.resetUserEngineerSubFlag(engineerIds);
                        // add on 2019-10-17 begin
                        // Engineer微服务
                        MSErrorCode msErrorCode1 = msEngineerService.resetEngineerMasterFlag(engineerIds);
                        if (msErrorCode1.getCode() > 0) {
                            throw new RuntimeException("重置网点下师傅主账号信息出错。出错原因:" + msErrorCode1.getMsg());
                        }
                        // add on 2019-10-17 end
                    }
                }
                // add on 2020-10-14 begin
                // 当子师傅转为主账号后，给其添加角色,不然登录Web系统会因没有权限出现一片空白
                if (engineer.getMasterFlag() == 1) {
                    User user = userDao.getByEngineerId(engineer.getId());
                    user.getRoleList().add(new Role(6L));
                    Long userId  = userDao.getUserRoleByUserId(user.getId()); // 查询当前用户是否有角色  add on 2020-11-21
                    if (userId != null) {
                        userDao.deleteUserRole(user);
                    }
                    userDao.insertUserRole(user);
                }
                // add on 2020-10-14 begin
            }
            //修改登录信息
            dao.updateUser(engineer);

            msUserService.refreshUserCacheByEngineerId(engineer.getId());//user微服务

        }
    }


    /**
     * 升级，加入新网点
     */
    @Transactional(readOnly = false)
    public void upgrade(Engineer engineer) {
        // TODO： 当前代码段没有地方调用，如果后续要用此处的话，不要忘记发送MQServcicePointEngineerMessage  // 2019-10-24

        Engineer orgEngineer = getEngineer(engineer.getId());//原始信息 //切换为微服务

        ServicePoint servicePoint = get(engineer.getServicePoint().getId());//新网点
        engineer.setMasterFlag(1);//主帐号
        engineer.preUpdate();

        msEngineerService.upgradeEngineer(engineer);  // add on 2019-10-18 // Engineer微服务

        //原网点的接单数等不变更

        servicePoint.setPrimary(engineer);
        servicePoint.setOrderCount(servicePoint.getOrderCount() + engineer.getOrderCount());
        servicePoint.setPlanCount(servicePoint.getPlanCount() + engineer.getPlanCount());
        servicePoint.setBreakCount(servicePoint.getBreakCount() + engineer.getBreakCount());
        servicePoint.setUpdateBy(engineer.getUpdateBy());
        servicePoint.setUpdateDate(engineer.getUpdateDate());

        MSErrorCode msErrorCode = msServicePointService.upgradeServicePoint(servicePoint);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务升级网点失败，失败原因:"+msErrorCode.getMsg());
        }
    }

    /**
     * 检查网点下是否存在其他的主帐号
     *
     * @param id       网点id
     * @param expectId 排除安维人员id
     * @return
     */
    public int checkMasterEngineer(Long id, Long expectId) {
        return msEngineerService.checkMasterEngineer(id, expectId);  // add on 2019-10-22 //Engineer微服务
    }

    /**
     * 停用安维人员
     * 逻辑删除
     */
    @Transactional(readOnly = false)
    public void delete(Engineer engineer) {
        if (engineer.getServicePoint() == null || engineer.getServicePoint().getId() == 0) {
            engineer = getEngineer(engineer.getId());//切换为微服务
        }
        engineer.preUpdate();
        engineer.setDelFlag(Engineer.DEL_FLAG_DELETE);
        msEngineerService.delete(engineer);   // add on 2019-10-18
        //禁止安维登录
        dao.deleteUser(engineer);

        //被删除的安维是主帐号,变更网点主帐号为0
        if (engineer.getMasterFlag() == 1) {
            ServicePoint point = getFromCache(engineer.getServicePoint().getId());
            if (point == null) {
                //切换为微服务 point = dao.get(engineer.getServicePoint().getId());
                point = get(engineer.getServicePoint().getId());
            }
            point.setPrimary(new Engineer(0l));
            point.setUpdateBy(engineer.getUpdateBy());
            point.setUpdateDate(engineer.getUpdateDate());

            updatePrimaryAccount(point);  //add on 2019-10-4
        }
    }

    /**
     * 启用安维人员
     */
    @Transactional(readOnly = false)
    public void enable(Engineer engineer) {
        if (engineer.getServicePoint() == null || engineer.getServicePoint().getId() == 0) {
            engineer = getEngineer(engineer.getId());//切换为微服务
        }
        engineer.preUpdate();
        engineer.setDelFlag(Engineer.DEL_FLAG_NORMAL);

        msEngineerService.delete(engineer);  //add on 2019-10-18
        //禁止安维登录
        dao.deleteUser(engineer);
        ServicePoint point = getFromCache(engineer.getServicePoint().getId());
        //启用的是主帐号,变更网点主帐号
        if (engineer.getMasterFlag() == 1 && point != null && (point.getPrimary() == null || point.getPrimary().getId() == null || point.getPrimary().getId() == 0l)) {
            if (point == null) {
                //切换为微服务 point = dao.get(engineer.getServicePoint().getId());
                point = get(engineer.getServicePoint().getId());
            }
            point.setPrimary(engineer);
            point.setUpdateBy(engineer.getUpdateBy());
            point.setUpdateDate(engineer.getUpdateDate());

            updatePrimaryAccount(point);
        }
    }

    /**
     * 按区域+产品类目读取可APP接单的安维主帐号列表(User)
     * 供短信及APP通知
     *
     * @param areaId        区县Id
     * @param categoryId    类目Id
     * @return List<User>
     */
    public List<User> getEngineerAccountsListByAreaAndProductCategory(Long areaId,Long categoryId) {
        // step: 1. 先按区域获取网点帐号列表
        //       2. 再根据帐号列表+品类获取ServicePointId列表
        //       3. 根据servicePointId列表获取排序后的servicePoint列表
        //       4. 轮询servicePoint列表，根据servicePoint从userList获取数据，组成新的userList，并返回
        List<User> userList = getEngineerAccountsListByAreaAndProductCategoryFromDB(areaId,categoryId); //add on 2019-11-8

        if (CollectionUtils.isEmpty(userList)) {
            return userList;
        }
        List<Long> servicePointIds = userList.stream().map(User::getServicePointId).distinct().collect(Collectors.toList());
        List<ServicePoint> servicePointList;
        if (servicePointIds != null && !servicePointIds.isEmpty()) {
            servicePointList = msServicePointService.findListByIds(servicePointIds);
            if (servicePointList != null && !servicePointList.isEmpty()) {
                Function<ServicePoint,Integer> levelSort = servicePoint -> StringUtils.toInteger(servicePoint.getLevel().getValue());
                Function<ServicePoint,Integer> planCountSort = servicePoint -> servicePoint.getPlanCount();
                // 排序规则:order by p.level desc,p.plan_count desc
                servicePointList = servicePointList.stream().sorted(Comparator.comparing(levelSort).reversed().thenComparing(planCountSort).reversed()).collect(Collectors.toList());
                // 生成以ServicePointId为key,value为用户列表的Map
                Map<Long, List<User>> userMap = userList.stream().collect(Collectors.groupingBy(User::getServicePointId));
                // 输出排序的数据
                List<User> sortedUser = Lists.newArrayList();
                servicePointList.stream().forEach(servicePoint -> {
                    List<User> fliterUserList = userMap.get(servicePoint.getId()).stream().map(user->{
                        user.setShortMessageFlag(servicePoint.getShortMessageFlag());
                        return user;
                    }).collect(Collectors.toList());
                    sortedUser.addAll(fliterUserList);
                });
                return sortedUser;
            }
        }
        return userList;
    }

    public List<User> getEngineerAccountsListByAreaAndProductCategoryFromDB(Long areaId, Long categoryId) {
        // 根据服务区域id查询出对象的安维人员列表  // add on 2019-11-8
        List<Engineer> engineerList = msEngineerService.findEngineerListByServiceAreaId(areaId);
        List<Long> engineerIds;
        if(CollectionUtils.isEmpty(engineerList)){
            return Lists.newArrayListWithCapacity(0);
        }

        Supplier<Stream<Engineer>> streamSupplier = () -> engineerList.stream();
        engineerIds = streamSupplier.get().map(r->r.getId()).distinct().collect(Collectors.toList());
        Map<Long,Engineer> engineerMap = streamSupplier.get().collect(Collectors.toMap(Engineer::getId, Function.identity()));
        int size = engineerIds.size();
        List<User> userList = Lists.newArrayListWithCapacity(size);
        if (size > 100) {
            List<User> finalUserList = Lists.newArrayList();
            Lists.partition(engineerIds,100).stream().forEach(longIds ->{
                List<User> partUsers = systemService.findEngineerAccountList(longIds, 0); //subFlag 是否为子帐号标记，1：是子帐号　0：不是子帐号
                if (partUsers != null && !partUsers.isEmpty()) {
                    finalUserList.addAll(partUsers);
                }
            });
            userList.addAll(finalUserList);
        } else {
            userList = systemService.findEngineerAccountList(engineerIds, 0); //subFlag 是否为子帐号标记，1：是子帐号　0：不是子帐号
        }


        if (!CollectionUtils.isEmpty(userList)) {
            userList.stream().forEach(user->{
                Engineer engineer = engineerMap.get(user.getEngineerId());
                if (engineer != null) {
                    user.setAppFlag(engineer.getAppFlag());
                    user.setServicePointId(engineer.getServicePoint().getId());
                }
            });
            //按品类筛选
            if (categoryId != null && categoryId >0) {
                Set<Long> spIdSet = Sets.newLinkedHashSetWithExpectedSize(size);
                List<Long> sids = streamSupplier.get().map(t -> t.getServicePoint().getId()).distinct().collect(Collectors.toList());
                if (sids.size() > 100) {
                    Lists.partition(sids, 100).stream().forEach(longIds -> {
                        //List<Long> partIds = productCategoryServicePointMappingService.findListByProductCategoryIdAndServicePointIds(longIds, categoryId); // mark on 2020-6-12
                        List<Long> partIds = msProductCategoryServicePointService.findListByProductCategoryIdAndServicePointIds(longIds, categoryId); // add on 2020-6-12
                        if (!CollectionUtils.isEmpty(partIds)) {
                            spIdSet.addAll(partIds);
                        }
                    });
                } else {
                    //sids = productCategoryServicePointMappingService.findListByProductCategoryIdAndServicePointIds(sids, categoryId);  //mark on 2020-6-12
                    sids = msProductCategoryServicePointService.findListByProductCategoryIdAndServicePointIds(sids, categoryId); // add on 2020-6-12
                    spIdSet.addAll(sids);
                }
                if(!spIdSet.isEmpty()){
                    userList = userList.stream().filter(user -> spIdSet.contains(user.getServicePointId())).collect(Collectors.toList());
                }
            }
        }
        return userList;
    }

    //endregion 安维人员管理

    //region 网点价格

    /**
     * 获得价格（后台）
     *
     * @param id
     * @return
     */
    public ServicePrice getPrice(Long id) {
        ServicePrice servicePriceFromMS = msServicePointPriceService.getPrice(id);
        return servicePriceFromMS;
    }

    @Deprecated
    public void getPriceCompare(ServicePrice dbServicePrice, ServicePrice msServicePrice) {
        // 用来比较单笔网点价格信息 辅助方法 后面要删除 // add on 2019-12-20
        try {
            String msg ="";
            if (msServicePrice ==null) {
                if (dbServicePrice != null) {
                    msg = "db:"+dbServicePrice.toString()+",MS返回为null";
                }
            } else  {
                if (dbServicePrice == null) {
                    msg = "MS:"+msServicePrice.toString()+",DB返回为null";
                } else {
                    if (dbServicePrice.getId().intValue() != msServicePrice.getId().intValue()
                            || dbServicePrice.getServicePoint().getId().intValue() != msServicePrice.getServicePoint().getId().intValue()
                            || !dbServicePrice.getServicePoint().getServicePointNo().equals(msServicePrice.getServicePoint().getServicePointNo())
                            || !dbServicePrice.getServicePoint().getName().equals(msServicePrice.getServicePoint().getName())
                            || dbServicePrice.getProduct().getId().intValue() != msServicePrice.getProduct().getId().intValue()
                            || !dbServicePrice.getProduct().getName().equals(msServicePrice.getProduct().getName())
                            || dbServicePrice.getServiceType().getId().intValue() != msServicePrice.getServiceType().getId().intValue()
                            || !dbServicePrice.getServiceType().getName().equals(msServicePrice.getServiceType().getName())
                            || !dbServicePrice.getServiceType().getCode().equals(msServicePrice.getServiceType().getCode())
                            || dbServicePrice.getPrice() != msServicePrice.getPrice()
                            || dbServicePrice.getDiscountPrice() != msServicePrice.getDiscountPrice()
                            || dbServicePrice.getPriceType().hashCode() != msServicePrice.getPriceType().hashCode()
                            || dbServicePrice.getDelFlag().intValue() != msServicePrice.getDelFlag().intValue()
                    ) {
                        msg = "DB:"+dbServicePrice.toString()+",MS:"+msServicePrice.toString()+ " 数据明细不符。";
                    }
                }
            }
            if (msg != "") {
                msg = "网点价格DB与MS不一致，"+ msg;
                LogUtils.saveLog("基础资料", "ServicePointService.getPrice", msg, null, UserUtils.getUser());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 停用单个价格（后台）
     *
     * @param id
     */
    @Transactional(readOnly = false)
    public void stopPrice(Long id) {
        User user = UserUtils.getUser();

        ServicePrice price = getPrice(id);
        if (price != null && price.getDelFlag() != ServicePrice.DEL_FLAG_DELETE) {

            msServicePointPriceService.stopPrice(price);  //add on 2019-12-20

            // add on 2020-2-24 begin
            updateCustomizePriceFlag(ServicePoint.CUSTOMIZE_PRICE_FLAG_DISABLED, price.getServicePoint().getId(), price.getPriceType().getIntValue());
            // add on 2020-2-24 end
        }
    }

    /**
     * 启用单个价格（后台）
     *
     * @param id
     */
    @Transactional(readOnly = false)
    public void activePrice(Long id) {
        User user = UserUtils.getUser();

        ServicePrice price = getPrice(id);        //add on 2019-8-27
        if (price != null && price.getDelFlag() == ServicePrice.DEL_FLAG_DELETE) {
            msServicePointPriceService.activePrice(price);  //add on 2019-12-20
            updateCustomizePriceFlag(ServicePoint.CUSTOMIZE_PRICE_FLAG_DISABLED, price.getServicePoint().getId(), price.getPriceType().getIntValue());
        }
    }

    /**
     * 保存单个价格修改（后台）
     *
     * @param price
     */
    @Transactional(readOnly = false)
    public void savePrice(ServicePrice price) {
        // add on 2020-2-24 begin
        int customizePriceFlag = ServicePoint.CUSTOMIZE_PRICE_FLAG_DISABLED;
        ProductPrice productPrice = msProductPriceService.getEngineerPriceByProductIdAndServiceTypeIdAndPriceType(price.getPriceType().getIntValue(), price.getProduct().getId(), price.getServiceType().getId());
        if (productPrice != null) {
            if ((price.getPrice() != productPrice.getEngineerStandardPrice().doubleValue() ||
            price.getDiscountPrice() != productPrice.getEngineerDiscountPrice().doubleValue())
                && price.getDelFlag() == ServicePrice.DEL_FLAG_NORMAL ) {
                price.setCustomizeFlag(ServicePrice.CUSTOMIZE_FLAG_ENABLED);  //自定义价格
                customizePriceFlag = ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED;
            } else {
                price.setCustomizeFlag(ServicePrice.CUSTOMIZE_FLAG_DISABLED);  //参考价格
            }
        }
        Long servicePointId = price.getServicePoint().getId();
        // add on 2020-2-24 end

        // add on 2020-3-12 begin
        ServicePoint servicePointFromMS = msServicePointService.getSimpleById(servicePointId);
        int useDefaultPrice = servicePointFromMS.getUseDefaultPrice();
        int customizePriceFlagFromDB  = servicePointFromMS.getCustomizePriceFlag();
        String strServicepointNo = servicePointFromMS.getServicePointNo();
        boolean startWithYH = strServicepointNo.toUpperCase().startsWith("YH");
        Dict priceType = MSDictUtils.getDictByValue(useDefaultPrice+"","PriceType");

        if (customizePriceFlagFromDB == ServicePoint.AUTO_PLAN_FLAG_DISABLED
                && customizePriceFlag == ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED
                && !startWithYH) {
            throw new RuntimeException( "网点现在使用[ "+priceType.getLabel()+" ]价格，不能修改。");
        }
        // add on 2020-3-12 end

        msServicePointPriceService.updatePrice(price);  // add on 2019-12-20

        // add on 2020-2-24 begin
        updateCustomizePriceFlag(customizePriceFlag, servicePointId, price.getPriceType().getIntValue());
    }

    /**
     * 更新网点是否启用自定义价格 2020-2-24
     * @param customizePriceFlag
     * @param servicePointId
     * @param priceType
     */
    public void updateCustomizePriceFlag(int customizePriceFlag, Long servicePointId, int priceType) {
        long iBegin = System.currentTimeMillis();
        String strMsg ="";
        if (customizePriceFlag == ServicePrice.CUSTOMIZE_FLAG_ENABLED) {
            customizePriceFlag = ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED;
        } else {
            // 找到该网点下的所有的网点价格记录
            List<Long> servicePointIds = Arrays.asList(servicePointId);
            List<ServicePrice> pricesFromMS = msServicePointPriceService.findPricesByPoints(servicePointIds, null, null);

            // 找该网点下所有的产品的参考价格
            List<Long> productIds = Lists.newArrayList();
            if (pricesFromMS != null && !pricesFromMS.isEmpty()) {
                productIds = pricesFromMS.stream().map(r -> r.getProduct().getId()).distinct().collect(Collectors.toList());
            }
            List<ProductPrice> productPriceList = msProductPriceService.findGroupList(priceType, productIds, null,null,null);

            // 以参考价格为准与找到的网点价格比较标准价跟优惠加是否相等
            for(ProductPrice productPriceEntity:  productPriceList) {
                //1.先查找
                ServicePrice servicePriceEntity = pricesFromMS.stream().filter(x->x.getProduct().getId().longValue() == productPriceEntity.getProduct().getId().longValue()
                        && x.getServiceType().getId().longValue() == productPriceEntity.getServiceType().getId().longValue()).findFirst().orElse(null);
                //2。找到了比较价格
                if (servicePriceEntity != null && servicePriceEntity.getDelFlag().equals(ServicePrice.DEL_FLAG_NORMAL)) {
                    if (servicePriceEntity.getPrice() != productPriceEntity.getEngineerStandardPrice() ||
                            servicePriceEntity.getDiscountPrice() != productPriceEntity.getEngineerDiscountPrice()) {
                        Product product = productService.get(productPriceEntity.getProduct().getId());
                        strMsg = product.getName();
                        customizePriceFlag = ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED;
                        break;
                    }
                }
            }
        }

        // add on 2020-3-12 begin
        ServicePoint servicePointFromMS = msServicePointService.getSimpleById(servicePointId);
        int useDefaultPrice = servicePointFromMS.getUseDefaultPrice();
        int customizePriceFlagFromDB  = servicePointFromMS.getCustomizePriceFlag();
        boolean startWithYH = servicePointFromMS.getServicePointNo().toUpperCase().startsWith("YH");
        boolean degree = servicePointFromMS.getDegree().equals(30);
        Dict priceTypeFromDB = MSDictUtils.getDictByValue(useDefaultPrice+"","PriceType");
        if(priceType != 40) {
            if (customizePriceFlagFromDB == ServicePoint.AUTO_PLAN_FLAG_DISABLED
                    && customizePriceFlag == ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED
                    && !degree) {
                throw new RuntimeException(" 产品：[" + strMsg + "] 使用的不是标准价,请调整! 网点现在使用[ " + priceTypeFromDB.getLabel() + " ]价格，不能修改。");
            }
        }
        // add on 2020-3-12 end

        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setId(servicePointId);
        servicePoint.setCustomizePriceFlag(customizePriceFlag);
        servicePoint.setRemotePriceFlag(ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED);
        if(priceType == 40){
            msServicePointPriceService.updateRemotePriceFlag(servicePoint);
        }else if(priceType < 40){
            msServicePointService.updateCustomizePriceFlag(servicePoint);
        }

        long iEnd = System.currentTimeMillis();
        log.warn("updateCustomizePriceFlag耗时:{}毫秒.", iEnd-iBegin);
    }


    /**
     * 分页查询（后台管理）所有产品，即使没有维护价格
     * 先从数据库返回网点id,再根据id从数据库读取
     * 保存在map属性中
     */
    public Page<ServicePrice> findPage(Page<ServicePrice> page, ServicePrice entity) {

        HashMap<String, List<HashMap<String, Object>>> servicePointPriceListMap = new HashMap<>();
        List<HashMap<String, Object>> servicePointProductPriceList = Lists.newArrayList();
        HashMap<String, Object> servicePointProductPriceMap;
        List<HashMap<String, Object>> servicePointPriceList;
        HashMap<String, Object> servicePointPriceMap;
        List<ServicePrice> servicePriceList;

        entity.setPage(page);

        // add on 2019-12-18 begin
        List<ServicePointProduct> servicePointProductsAll = getServicePointProductsByIdsNew(entity);      //add on 2019-12-18
        // add on 2019-12-18 end

        List<Long> servicePointIds = servicePointProductsAll.stream()
                .map(t -> t.getServicePoint().getId())
                .distinct()
                .collect(Collectors.toList());

        List<ServicePrice> prices = msServicePointPriceService.findPricesByPoints(servicePointIds, entity.getProduct().getId(), null); // add on 2019-12-23

        //产品参考价格
        List<Long> productIds = servicePointProductsAll.stream()
                .map(t -> t.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());
        List<ProductPrice> productPrices = productPriceService.findGroupList(productIds, null, null, entity.getServicePoint().getId(), null);

        //mark on 2019-10-11
        //List<ServiceType> serviceTypes = typeService.findAllList();
        //调用微服务获取服务类型,只返回id 和服务名称 start 2019-10-11
        List<ServiceType> serviceTypes = typeService.findAllListIdsAndNames();
        //end
        serviceTypes = ServiceType.ServcieTypeOrdering.sortedCopy(serviceTypes);//服务类型

        ServicePoint servicePoint = null;
        Product product;
        ProductPrice productPrice;
        ServicePrice price;

        for (Long servicePointId : servicePointIds) {
            final Long spi = servicePointId;
            servicePoint = servicePointProductsAll.stream()
                    .filter(spp -> Objects.equals(spp.getServicePoint().getId(), spi))
                    .findFirst().orElse(null).getServicePoint();

            servicePointProductPriceMap = new HashMap<>();
            servicePointPriceList = Lists.newArrayList();

            servicePointProductPriceMap.put("servicePointId", servicePoint.getId());
            servicePointProductPriceMap.put("servicePointNo", servicePoint.getServicePointNo());
            servicePointProductPriceMap.put("servicePointName", servicePoint.getName());

            List<ServicePointProduct> servicePointProducts = servicePointProductsAll.stream()
                    .filter(t -> Objects.equals(t.getServicePoint().getId(), spi))
                    .collect(Collectors.toList());

            for (ServicePointProduct servicePointProduct : servicePointProducts) {
                product = servicePointProduct.getProduct();
                servicePointPriceMap = new HashMap<>();
                servicePriceList = Lists.newArrayList();
                final Long productId = product.getId();

                servicePointPriceMap.put("productId", product.getId());
                servicePointPriceMap.put("productName", product.getName());

                for (ServiceType serviceType : serviceTypes) {
                    final Long serviceTypeId = serviceType.getId();
                    //已有价格
                    price = prices.stream()
                            .filter(t -> Objects.equals(t.getProduct().getId(), productId)
                                    && Objects.equals(t.getServiceType().getId(), serviceTypeId)
                                    && Objects.equals(t.getServicePoint().getId(), spi))
                            .findFirst().orElse(null);
                    if (price != null) { //维护
                        price.setFlag(0);
                        servicePriceList.add(price);
                        continue;
                    }
                    //参考价格
                    productPrice = productPrices.stream().filter(t -> Objects.equals(t.getProduct().getId(), productId)
                            && Objects.equals(t.getServiceType().getId(), serviceTypeId))
                            .findFirst().orElse(null);
                    if (productPrice != null) {
                        price = new ServicePrice();
                        price.setServiceType(serviceType);
                        price.setReferPrice(productPrice.getEngineerStandardPrice());
                        price.setReferDiscountPrice(productPrice.getEngineerDiscountPrice());
                        price.setFlag(1);//有参考价格
                    } else {
                        price = new ServicePrice();
                        price.setServiceType(serviceType);
                        price.setFlag(2);//无参考价格
                    }
                    servicePriceList.add(price);
                }
                servicePointPriceMap.put("servicePriceList", servicePriceList);
                servicePointPriceList.add(servicePointPriceMap);
            }

            servicePointProductPriceMap.put("servicePointPriceList", servicePointPriceList);
            servicePointProductPriceList.add(servicePointProductPriceMap);
        }
        servicePointPriceListMap.put("list", servicePointProductPriceList);
        page.setMap(servicePointPriceListMap);
        return page;
    }

    //endregion 网点价格

    //region 网点基础资料报表

    /**
     * 通过网点列表批量获取网点账务列表数据  // add on 2019-11-9
     * @param ids
     * @return
     */
    public List<ServicePointFinance> findFinanceListByIds(List<Long> ids) {
        List<ServicePointFinance> servicePointFinanceList = Lists.newArrayList();
        if (ids != null && !ids.isEmpty()) {
            ids = ids.stream().distinct().collect(Collectors.toList());  // 去掉重复id
            if (ids.size() > 100) {
                List<ServicePointFinance> finalServicePointFinanceList = Lists.newArrayList();
                Lists.partition(ids, 100).stream().forEach(longs -> {
                    List<ServicePointFinance> partServicePointFinanceList = dao.findFinanceListByIds(longs);
                    if (partServicePointFinanceList != null && !partServicePointFinanceList.isEmpty()) {
                        finalServicePointFinanceList.addAll(partServicePointFinanceList);
                    }
                });
                servicePointFinanceList.addAll(finalServicePointFinanceList);
            } else {
                servicePointFinanceList = dao.findFinanceListByIds(ids);
            }
        }
        return servicePointFinanceList;
    }

    /**
     * 通过网点列表批量获取网点账务哈希数据 // add on 2019-11-9
     * @param ids
     * @return
     */
    public Map<Long,ServicePointFinance> findFinanceListByIdsToMap(List<Long> ids) {
        List<ServicePointFinance> servicePointFinanceList = findFinanceListByIds(ids);
        return servicePointFinanceList != null && !servicePointFinanceList.isEmpty()?servicePointFinanceList.stream().collect(Collectors.toMap(ServicePointFinance::getId,Function.identity())):Maps.newHashMap();
    }

    /**
     * 根据id(财务id与网点id相等)获取网点财务信息// add on 2019-9-30
     * @param servicePointId
     * @return
     */
    public ServicePointFinance getFinance(Long servicePointId) {
        return dao.getFinanceNew(servicePointId);
    }

    //endregion 网点基础资料报表

    /**
     * 更新网点付款失败原因
     *
     * @param servicePointFinance
     */
    public void updateBankIssue(ServicePointFinance servicePointFinance) {
        //dao.updateBankIssue(servicePointFinance);  //mark on 2020-2-12
        MSErrorCode msErrorCode = msServicePointService.updateBankIssue(servicePointFinance.getId(), servicePointFinance.getBankIssue().getValue());
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新网点付款失败.原因:"+msErrorCode.getMsg());
        }
        // add on 2020-2-29 begin
        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setId(servicePointFinance.getId());
        servicePoint.setBankIssue(servicePointFinance.getBankIssue());
        String strContent = "bankIssue:"+ servicePointFinance.getBankIssue().getValue();
        servicePointLogService.saveServicePointLog(servicePointFinance.getId(), ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_BANKISSUE, "更新网点付款失败原因",
                strContent, UserUtils.getUser());
        // add on 2020-2-29 end
    }

    /**
     * 更新网点付款失败原因
     *
     * @param servicePointFinance
     */
    public void updateBankIssueFI(ServicePointFinance servicePointFinance) {
        dao.updateBankIssueFI(servicePointFinance);
    }

    /**
     * 获取网点所有金额
     *
     * @param id
     * @return
     */
    public ServicePointFinance getAmounts(Long id) {
        return dao.getAmounts(id);
    }

    /**
     * 获取即结未付款清单
     *
     * @return
     */
    public List<ServicePointPayCondition> getPayableDailyList(List<Integer> exceptBankIds) {
        return Lists.newArrayList();
    }

    /**
     * 获取即结未付款清单(去ServicePoint) // add on 2019-9-29
     *
     * @return
     */
    public List<ServicePointPayCondition> getPayableDailyListWithoutServicePoint(List<Integer> exceptBankIds) {
        //  去ServicePoint
        List<ServicePointPayCondition> servicePointPayConditionList = dao.getPayableDailyListWithoutServicePoint(exceptBankIds);  //add on 2020-1-15

        List<ServicePointPayCondition> filterServicePointPayConditionList = ServicePointPayConditionValidateServicePoint(servicePointPayConditionList);
        return filterServicePointPayConditionList;
    }


    /**
     * 获取月结未付款清单
     *
     * @return
     */
    public List<ServicePointPayCondition> getPayableMonthlyList(List<Integer> exceptBankIds) {
        return Lists.newArrayList();
    }

    /**
     * 获取月结未付款清单(去ServicePoint add on 2019-9-29)
     *
     * @return
     */
    public List<ServicePointPayCondition> getPayableMonthlyListWithoutServicePoint(List<Integer> exceptBankIds) {

        List<ServicePointPayCondition> servicePointPayConditionList = dao.getPayableMonthlyListWithoutServicePoint(exceptBankIds);

        List<ServicePointPayCondition> filterServicePointPayConditionList = ServicePointPayConditionValidateServicePoint(servicePointPayConditionList);
        return filterServicePointPayConditionList;
    }

    /**
     * 为网点付款判断servicePoint是否有效 //add on 2019-9-29
     * @param servicePointPayConditionList
     * @return
     */
    public List<ServicePointPayCondition> ServicePointPayConditionValidateServicePoint(List<ServicePointPayCondition> servicePointPayConditionList ) {
        List<ServicePointPayCondition> filterServicePointPayConditionList = Lists.newArrayList();
        if (servicePointPayConditionList != null && !servicePointPayConditionList.isEmpty()) {
            List<Long> servicePointIds = servicePointPayConditionList.stream().map(ServicePointPayCondition::getServicePointId).distinct().collect(Collectors.toList());

            List<MDServicePointViewModel> servicePointViewModelList = msServicePointService.findBatchByIdsByCondition(servicePointIds, Arrays.asList("id"),ServicePoint.DEL_FLAG_NORMAL);
            if (servicePointViewModelList != null && !servicePointViewModelList.isEmpty()) {
                List<Long> normalServicePointIds = servicePointViewModelList.stream().map(MDServicePointViewModel::getId).collect(Collectors.toList());
                return servicePointPayConditionList.stream().filter(r->normalServicePointIds.contains(r.getServicePointId())).collect(Collectors.toList());
            }
        }

        return filterServicePointPayConditionList;
    }

    /**
     * 获取即结未付款清单明细
     *
     * @param servicePointFinance
     * @return
     */
    public List<ServicePoint> getPayableDailyDetailList(ServicePointFinance servicePointFinance, List<Integer> exceptBankIds, Long areaId) {
        return Lists.newArrayList();
    }

    /**
     * 获取即结未付款清单明细(去ServicePoint) // add on 2019-9-29
     *
     * @param servicePointFinance
     * @return
     */
    public List<ServicePoint> getPayableDailyDetailListWithoutServicePoint(ServicePointFinance servicePointFinance, List<Integer> exceptBankIds, Long areaId) {
        List<ServicePoint> servicePointList = dao.getPayableDailyDetailListWithoutServicePoint(servicePointFinance, exceptBankIds, areaId);
        List<ServicePoint> filterServicePointList = refineServicePoint(servicePointList, areaId);
        // 根据网点名称排序
        if (filterServicePointList != null && !filterServicePointList.isEmpty()) {
            return filterServicePointList.stream().sorted(Comparator.comparing(ServicePoint::getName)).collect(Collectors.toList());
        }

        return filterServicePointList;
    }

    /**
     * 获取月结未付款清单明细
     *
     * @param servicePointFinance
     * @return
     */
    public List<ServicePoint> getPayableMonthlyDetailList(ServicePointFinance servicePointFinance, List<Integer> exceptBankIds, Long areaId) {
        return Lists.newArrayList();
    }

    /**
     * 获取月结未付款清单明细(去ServicePoint) //add on 2019-9-29
     *
     * @param servicePointFinance
     * @return
     */
    public List<ServicePoint> getPayableMonthlyDetailListWithoutServicePoint(ServicePointFinance servicePointFinance, List<Integer> exceptBankIds, Long areaId) {
        List<ServicePoint> servicePointList = dao.getPayableMonthlyDetailListWithoutServicePoint(servicePointFinance, exceptBankIds, areaId);
        return refineServicePoint(servicePointList, areaId);
    }

    /**
     * 补充网点的相关信息
     * @param servicePointList
     * @return
     */
    public List<ServicePoint> refineServicePoint(List<ServicePoint> servicePointList, Long areaId) {
        if (servicePointList == null || servicePointList.isEmpty()) {
            return servicePointList;
        }

        List<Long> servicePointIds = servicePointList.stream().map(ServicePoint::getId).distinct().collect(Collectors.toList());

        // add on 2019-10-14 begin
        final List<Long> engineerIds = Lists.newArrayList();
        List<String> fields = Arrays.asList("id","servicePointNo","name","contactInfo1","contactInfo2","primaryId","areaId","bankOwnerIdNo","bankOwnerPhone");
        List<MDServicePointViewModel> servicePointViewModelList = msServicePointService.findBatchByIdsByCondition(servicePointIds, fields, ServicePoint.DEL_FLAG_NORMAL,((engineerIdsFromMS)->
                engineerIds.addAll(engineerIdsFromMS)
        ));

        Map<Long, MDServicePointViewModel> servicePointViewModelMap = Maps.newHashMap();
        if (servicePointViewModelList != null && !servicePointViewModelList.isEmpty()) {
            // 去掉无效数据
            Predicate<MDServicePointViewModel> servicePointPredicate = null ;
            if (areaId != null) {
                String strParentIds = "%,"+areaId+",%";
                List<Area> areaList = areaService.findByParentIdsLike(strParentIds);
                if (areaList != null && !areaList.isEmpty()) {
                    List<Long> areaIds = areaList.stream().map(Area::getId).distinct().collect(Collectors.toList());
                    servicePointPredicate = r ->areaIds.contains(r.getAreaId());
                }
                if (servicePointPredicate != null) {
                    servicePointViewModelList = servicePointViewModelList.stream().filter(servicePointPredicate).collect(Collectors.toList());
                    // 获取安维的人员id
                    List<Long> filterEngineerIds = servicePointViewModelList.stream().map(t->t.getPrimaryId()).distinct().collect(Collectors.toList());
                    engineerIds.clear();
                    engineerIds.addAll(filterEngineerIds);
                }
            }
            servicePointViewModelMap = servicePointViewModelList.stream().collect(Collectors.toMap(MDServicePointViewModel::getId, Function.identity()));
        }

        //获取具体的安维人员信息
        Map<Long, String> engineerMap = Maps.newHashMap();
        if (engineerIds != null && !engineerIds.isEmpty()) {
            List<Engineer> engineerList = findAllEngineersName(engineerIds,Arrays.asList("id","name"));
            if (engineerList != null && !engineerList.isEmpty()) {
                engineerMap = engineerList.stream().collect(Collectors.toMap(Engineer::getId, Engineer::getName));
            }
        }
        final Map<Long, String> finalEngineerMap = engineerMap;
        final Map<Long, MDServicePointViewModel> finalServicePointViewModelMap = servicePointViewModelMap;

        // 进行inner join判断
        List<ServicePoint> filterServicePointList = Lists.newArrayList();
        servicePointList.stream().forEach(servicePoint -> {
            MDServicePointViewModel servicePointVM = finalServicePointViewModelMap.get(servicePoint.getFinance().getId());
            if (servicePointVM != null) {
                servicePoint.setId(servicePointVM.getId());
                servicePoint.setServicePointNo(servicePointVM.getServicePointNo());
                servicePoint.setName(servicePointVM.getName());
                servicePoint.setContactInfo1(servicePointVM.getContactInfo1());
                servicePoint.setContactInfo2(servicePointVM.getContactInfo2());
                servicePoint.setBankOwnerPhone(servicePointVM.getBankOwnerPhone());
                servicePoint.setBankOwnerIdNo(servicePointVM.getBankOwnerIdNo());
                if (servicePointVM.getPrimaryId() != null ) {
                    Engineer engineer = new Engineer();
                    engineer.setName(finalEngineerMap.get(servicePointVM.getPrimaryId()));
                    servicePoint.setPrimary(engineer);
                }

                filterServicePointList.add(servicePoint);
            }
        });
        // add on 2019-10-14 end


        return filterServicePointList;
    }

    /**
     * 获取应付为负月结列表
     * @return
     */
    public List<ServicePointPayableMonthly> getPayableMinusMonthlyList(){
        return dao.getPayableMinusMonthlyList();
    }

    /**
     * 获取应付为负月结列表--根据网点ID
     * @return
     */
    public List<ServicePointPayableMonthly> getPayableMinusMonthlyListByServicePointId(Long servicePointId){
        return dao.getPayableMinusMonthlyListByServicePointId(servicePointId);
    }

    /**
     * 单独更新自动派单
     * @param servicePointId
     * @param autoPlanFlag
     */
    @Transactional
    public void updateAutoPlanFlag (Long servicePointId,Integer autoPlanFlag){
        User user = UserUtils.getUser();
        if (org.springframework.util.ObjectUtils.isEmpty(servicePointId) ){return;}
        //TODO: 写网点操作日志
        servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_AUTOPLANFLAG, "网点自动派单", String.valueOf(autoPlanFlag), user);

        //读取网点再更新缓存
        ServicePoint servicePoint = getFromCache(servicePointId);
        ServicePoint cachedServicePoint = new ServicePoint();
        BeanUtils.copyProperties(servicePoint,cachedServicePoint);
        if(servicePoint != null) {
            servicePoint.setAutoPlanFlag(autoPlanFlag);
            Optional.of(user).map(User::getId).ifPresent(r->{
                servicePoint.setUpdateBy(user);
            });
            servicePoint.setUpdateDate(new Date());
            // add on 2019-9-17 begin
            MSErrorCode msErrorCode = msServicePointService.updateAutoPlanFlag(servicePoint);
            if (msErrorCode.getCode() >0) {
                throw new RuntimeException("调用微服务更新网点自动派单标志失败.失败原因:"+msErrorCode.getMsg());
            }
        }
    }

    /**
     * 单独更新网点等级
     * @param servicePointId
     * @param level
     */
    @Transactional
    public void updateLevel (Long servicePointId,Integer level){
        User user=UserUtils.getUser();
        if (org.springframework.util.ObjectUtils.isEmpty(servicePointId) ){return;}
        //TODO: 写网点操作日志
        servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_AUTOPLANFLAG, "网点自动派单", String.valueOf(level), user);

        //读取网点再更新缓存
        ServicePoint servicePoint = getFromCache(servicePointId);
        if(servicePoint != null) {
            servicePoint.setLevel(new Dict(String.valueOf(level)));
            Optional.of(user).map(User::getId).ifPresent(r->{
                servicePoint.setUpdateBy(user);
            });
            servicePoint.setUpdateDate(new Date());

            // add on 2019-9-17 begin
            MSErrorCode msErrorCode = msServicePointService.updateLevel(servicePoint);
            if (msErrorCode.getCode() >0) {
                throw new RuntimeException("调用微服务更新网点等级失败。失败原因:"+msErrorCode.getCode());
            }
            // add on 2019-9-17 end
        }
    }

    //region api functions

    /**
     * 获取用户信息
     *
     * @param loginUserInfo
     * @return
     */
    public RestResult<Object> getUserInfo(RestLoginUserInfo loginUserInfo, RestGetUserInfo getUserInfo) {
        RestEnum.UserInfoType userInfoType = RestEnum.UserInfoType.valueOf(RestEnum.UserInfoTypeString[getUserInfo.getType()]);
        RestEngineer userInfo = new RestEngineer();
        Engineer engineer = getEngineerFromCache(loginUserInfo.getServicePointId(), loginUserInfo.getEngineerId());
        userInfo.setName(engineer.getName());
        userInfo.setPhoto("");
        if (userInfoType == RestEnum.UserInfoType.All || userInfoType == userInfoType.Base) {
            userInfo.setOrderCount(engineer.getOrderCount());
            userInfo.setPlanCount(engineer.getPlanCount());
            userInfo.setBreakCount(engineer.getBreakCount());
            if (loginUserInfo.getPrimary()) {
                ServicePointFinance servicePointFinance = getAmounts(loginUserInfo.getServicePointId());
                userInfo.setBalance(servicePointFinance.getBalance());
            }
        }
        if (userInfoType == RestEnum.UserInfoType.All || userInfoType == userInfoType.Detail) {
            userInfo.setPhone(engineer.getContactInfo());
            userInfo.setAddress(engineer.getAddress());
            List<Long> areaIds = getEngineerAreaIds(loginUserInfo.getEngineerId());
            Area source;
            RestArea target;
            List<RestArea> restAreaList = Lists.newArrayList();
            for (Long areaId : areaIds) {
                source = areaService.getFromCache(areaId);
                target = mapper.map(source, RestArea.class);
                restAreaList.add(target);
            }
            userInfo.setAreaList(restAreaList);
            ServicePoint servicePoint = getFromCache(engineer.getServicePoint().getId());
            userInfo.setServicePointName(servicePoint.getName());
        }
        return RestResultGenerator.success(userInfo);
    }

    public RestResult<Object> getServicePointInfo(Long servicePointId) {
        ServicePoint servicePoint = getFromCache(servicePointId);
        RestServicePoint target = mapper.map(servicePoint, RestServicePoint.class);
        //切换为微服务
        RestDict type = mapper.map(MSDictUtils.getDictByValue(String.valueOf(servicePoint.getProperty() == 0 ? 1 : servicePoint.getProperty()), "ServicePointProperty"), RestDict.class);
        target.setType(type);
        target.setPhone1(servicePoint.getContactInfo1());
        target.setPhone2(servicePoint.getContactInfo2());
        //latitude
        //longitude
        //contractImage;
        //idCardImage;
        //otherImage1;
        //otherImage2;
        target.setPrimaryName(servicePoint.getPrimary().getName());
        RestServicePointFinance finance = mapper.map(servicePoint.getFinance(), RestServicePointFinance.class);
        target.setFinance(finance);
        //List<Area> areaList = dao.getAreas(servicePointId);  //旧方法  // mark on 2019-12-3
        List<Area> areaList = getAreas(servicePointId);        //新方法  // add on 2019-12-3
        List<RestArea> restAreaList = mapper.mapAsList(areaList, RestArea.class);
        target.setAreaList(restAreaList);
//        List<Product> productList = dao.getProducts(servicePointId);  //mark on 2019-8-21
        List<Product> productList = getProducts(servicePointId);        //add on 2019-8-21
        List<RestProduct> restProductList = mapper.mapAsList(productList, RestProduct.class);
        target.setProductList(restProductList);
        return RestResultGenerator.success(target);
    }

    /**
     * 获取服务网点下的师傅列表
     *
     * @param servicePointId
     * @return
     */
    public RestResult<Object> getEngineerList(Long servicePointId) {
        List<Engineer> engineerList = getEngineersFromCache(servicePointId);
        List<RestEngineer> restEngineerList = mapper.mapAsList(engineerList, RestEngineer.class);
        for (RestEngineer restEngineer : restEngineerList) {
            List<Long> areaIds = getEngineerAreaIds(Long.valueOf(restEngineer.getId()));
            Area source;
            RestArea target;
            List<RestArea> restAreaList = Lists.newArrayList();
            for (Long areaId : areaIds) {
                source = areaService.getFromCache(areaId);
                target = mapper.map(source, RestArea.class);
                restAreaList.add(target);
            }
            restEngineer.setAreaList(restAreaList);
        }
        return RestResultGenerator.success(restEngineerList);
    }

    /**
     * 获取可派单师傅列表
     *
     * @param getPlanEngineerList
     * @param servicePointId
     * @return
     */
    public RestResult<Object> getPlanEngineerList(RestGetPlanEngineerList getPlanEngineerList, Long servicePointId) {
        Long areaId = Long.valueOf(getPlanEngineerList.getAreaId());
        Integer currentEngineerId = null;
        if (getPlanEngineerList.getCurrentEngineerId() != null && getPlanEngineerList.getCurrentEngineerId().length() > 0) {
            currentEngineerId = Integer.valueOf(getPlanEngineerList.getCurrentEngineerId());
        }
        Engineer queryEntity = new Engineer();
        queryEntity.setArea(new Area(areaId));
        queryEntity.setServicePoint(new ServicePoint(servicePointId));
        queryEntity.setExceptId(currentEngineerId);
        queryEntity.setMasterFlag(null);
        queryEntity.setAppFlag(null);
        List<Engineer> engineerList = findEngineerList(queryEntity);//切换为微服务
        List<RestEngineer> restEngineerList = mapper.mapAsList(engineerList, RestEngineer.class);
        for (RestEngineer rEngineer : restEngineerList) {
            List<Long> areaIds = getEngineerAreaIds(Long.valueOf(rEngineer.getId()));
            RestArea target;
            List<RestArea> restAreaList = Lists.newArrayList();
            for (Long aId : areaIds) {
                target = mapper.map(areaService.getFromCache(aId), RestArea.class);
                restAreaList.add(target);
            }
            rEngineer.setAreaList(restAreaList);
        }
        return RestResultGenerator.success(restEngineerList);
    }

    /**
     * 获取网点余额信息
     *
     * @param servicePointId
     * @return
     */
    public RestResult<Object> getBalance(Long servicePointId) {
        RestServicePointBalance restServicePointBalance = null;
        ServicePointFinance servicePointFinance = dao.getFinanceForRestBalance(servicePointId);
        if (servicePointFinance != null) {
            restServicePointBalance = mapper.map(servicePointFinance, RestServicePointBalance.class);
        }
        EngineerCurrency firstEngineerCurrency = engineerCurrencyDao.getFirstCurrency(servicePointId);
        if (firstEngineerCurrency != null && firstEngineerCurrency.getCreateDate() != null) {
            restServicePointBalance.setMonthCount(DateUtils.getDateDiffMonth(firstEngineerCurrency.getCreateDate(), new Date()) + 1);
        } else {
            restServicePointBalance.setMonthCount(0);
        }
        return RestResultGenerator.success(restServicePointBalance);
    }

    //endregion api functions

    //region push data to ElasticSearch

    public void updateServicePointToEs(ServicePoint servicePoint,ServicePoint cachedServicePoint) {
        if (org.springframework.util.ObjectUtils.isEmpty(servicePoint)) {
            log.warn("servicePoint data is null.");
            return;
        }

        int servicePointLevel = Optional.ofNullable(servicePoint.getLevel()).map(Dict::getValue).map(r->{
            if (!r.isEmpty()) {
                return StringUtils.toInteger(r.trim());
            }
            return 0;
        }).orElse(0);

        int servicePointStatus = Optional.ofNullable(servicePoint.getStatus()).map(Dict::getValue).map(r->{
            if (!r.isEmpty()) {
                return StringUtils.toInteger(r.trim());
            }
            return 0;
        }).orElse(0);

        boolean levelUpdateFlag = true; //true - 更新 false - 删除
        boolean statusUpdateFlag = false; //true - 更新 false - 删除
        if (servicePointStatus == ServicePointStatus.NORMAL.getValue()) {
            statusUpdateFlag = true;
        } else {
            statusUpdateFlag = false;
        }

        int servicePointPaymentType = Optional.ofNullable(servicePoint.getFinance()).map(ServicePointFinance::getPaymentType).map(Dict::getValue).map(r->{
            if (!r.isEmpty()) {
                return StringUtils.toInteger(r.trim());
            }
            return 0;
        }).orElse(0);

        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = null;
        boolean bSendFlag = true;
        if (levelUpdateFlag && statusUpdateFlag) {
            // 决定是否更新数据
            // 查看数据是否有变化
            if (!org.springframework.util.ObjectUtils.isEmpty(cachedServicePoint)) {
                String cachedServicePointNo = cachedServicePoint.getServicePointNo();
                String cachedName = cachedServicePoint.getName();
                String cachedContactInfo1 = cachedServicePoint.getContactInfo1();
                Integer cachedAutoPlanFlag = cachedServicePoint.getAutoPlanFlag();

                int cachedServicePointLevel = Optional.ofNullable(cachedServicePoint.getLevel()).map(Dict::getValue).map(r->{
                    if (!r.isEmpty()) {
                        return StringUtils.toInteger(r.trim());
                    }
                    return 0;
                }).orElse(0);

                int cachedServicePointPaymentType = Optional.ofNullable(cachedServicePoint.getFinance()).map(ServicePointFinance::getPaymentType).map(Dict::getValue).map(r->{
                    if (!r.isEmpty()) {
                        return StringUtils.toInteger(r.trim());
                    }
                    return 0;
                }).orElse(0);

                int cachedServicePointStatus = Optional.ofNullable(cachedServicePoint.getStatus()).map(Dict::getValue).map(r->{
                    if (!r.isEmpty()) {
                        return StringUtils.toInteger(r.trim());
                    }
                    return 0;
                }).orElse(0);

                if (servicePoint.getServicePointNo().equals(cachedServicePointNo) &&
                        servicePoint.getName().equals(cachedName) &&
                        servicePoint.getContactInfo1().equals(cachedContactInfo1) &&
                        servicePoint.getAutoPlanFlag().equals(cachedAutoPlanFlag) &&
                        servicePointLevel == cachedServicePointLevel &&
                        servicePointPaymentType == cachedServicePointPaymentType ) {
                    bSendFlag = false;
                    //if (log.isDebugEnabled())
                    {
                        String strNow = new ToStringBuilder(servicePoint)
                                .append("servicePointNo",servicePoint.getServicePointNo())
                                .append("name",servicePoint.getName())
                                .append("contactInfo1",servicePoint.getContactInfo1())
                                .append("autoPlanFlag",servicePoint.getAutoPlanFlag())
                                .append("level",servicePointLevel)
                                .append("paymentType",servicePointPaymentType)
                                .build();
                        String strCache = new ToStringBuilder(cachedServicePoint)
                                .append("servicePointNo",cachedServicePoint.getServicePointNo())
                                .append("name",cachedServicePoint.getName())
                                .append("contactInfo1",cachedServicePoint.getContactInfo1())
                                .append("autoPlanFlag",cachedServicePoint.getAutoPlanFlag())
                                .append("level",cachedServicePointLevel)
                                .append("paymentType",cachedServicePointPaymentType)
                                .build();

                        // log.debug("数据值相同不需要发送更新消息给es.servicePoint:{},cacheServicePoint:{}", strNow, strCache);
                        log.warn("数据值相同不需要发送更新消息给es.servicePoint:{},cacheServicePoint:{}", strNow, strCache);
                    }
                }

                if ((cachedServicePointStatus != servicePointStatus && servicePointStatus == ServicePointStatus.NORMAL.getValue() )) {
                    // 原来的等级不在1-5的范围之内,现在的等级已在1-5之内,此时要添加网点及服务点消息
                    // 或者网点状态从非正常改为正常(10)
                    //pushServicePointAndStationsToEs(servicePoint);  //mark on 2020-11-25 //不需要再发送旧的数据同步消息
                    return;
                }
            }
            if (bSendFlag) {
//                syncServicePointMessage = servicePointStationService.transServicePointMessage.apply(servicePoint,MQSyncType.SyncType.UPDATE);  // mark on 2019-9-14
                syncServicePointMessage = servicePointStationService.transServicePointMessage(servicePoint, MQSyncType.SyncType.UPDATE);   //add on 2019-9-14
                syncServicePointMessage = syncServicePointMessage.toBuilder().setSyncType(MQSyncType.SyncType.UPDATE).build();
            }
        }
        else {
            syncServicePointMessage = MQSyncServicePointMessage.SyncServicePointMessage.newBuilder()
                    .setMessageId(sequenceIdService.nextId())
                    .setSyncType(MQSyncType.SyncType.DELETE)
                    .setServicePointId(servicePoint.getId())
                    .build();
        }

        if (!org.springframework.util.ObjectUtils.isEmpty(syncServicePointMessage)) {
            String json = new JsonFormat().printToString(syncServicePointMessage);
            log.warn("发送es的网点消息,ServicePointMessage:" + json);
        }

        if (bSendFlag) {
            servicePointSender.sendRetry(syncServicePointMessage);
        }
    }

    public void pushServicePointAndStationsToEs(ServicePoint servicePoint) {
        MQSyncServicePointStationMessage.SyncServicePointStationMessage syncServicePointStationMessage = null;

//        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = servicePointStationService.transServicePointMessage.apply(servicePoint,MQSyncType.SyncType.ADD);  //mark on 2019-9-14
        MQSyncServicePointMessage.SyncServicePointMessage syncServicePointMessage = servicePointStationService.transServicePointMessage(servicePoint,MQSyncType.SyncType.ADD);  //add on 2019-9-14

        MQSyncServicePointStationMessage.SyncServicePointStationMessage.Builder syncServicePointStationMessageBuilder = MQSyncServicePointStationMessage.SyncServicePointStationMessage.newBuilder();
        ServicePointStation servicePointStationEntity = ServicePointStation.builder()
                .servicePoint(new ServicePoint(servicePoint.getId()))
                .build();
        List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStationEntity);
        if (!org.springframework.util.ObjectUtils.isEmpty(servicePointStationList)) {
            servicePointStationList.stream().forEach(entity->{
                if (entity.getDelFlag().equals(ServicePointStation.DEL_FLAG_NORMAL)) {
//                    MQSyncServicePointStationMessage.SyncStationMessage syncStationMessage = servicePointStationService.transServicePointStation.apply(entity, MQSyncType.SyncType.ADD);
                    MQSyncServicePointStationMessage.SyncStationMessage syncStationMessage = servicePointStationService.transServicePointStation(entity, MQSyncType.SyncType.ADD);
                    syncServicePointStationMessageBuilder.addStationMessage(syncStationMessage);
                }
            });
        }

        syncServicePointStationMessage = syncServicePointStationMessageBuilder
                //.setMessageId(servicePointStationService.generateMessageId())
                .setMessageId(sequenceIdService.nextId())
                .setServicePointMessage(syncServicePointMessage)
                .setSyncType(MQSyncType.SyncType.ADD)
                .build();

        String json = new JsonFormat().printToString(syncServicePointStationMessage);
        //System.out.printf("发送es的网点服务点新增消息,ServicePointStationMessage:%s", json);
        //System.out.println();
        servicePointStationSender.sendRetry(syncServicePointStationMessage);
    }

    // endregion push data to ElasticSearch


    private List<Long> findIdList(ServicePoint entity, Function<ServicePoint,List<Long>> fun) {
        // add the method on 2019-8-21
        // 目的： 代替dao.findIdList, dao.findServicePointIdsForPlan
        List<Product> productList = null;
        return fun.apply(entity);
    }

    private List<Long> findIdListFromMS(ServicePoint entity) {
        // add on 2019-12-29
        // 从微服务中获取网点id
        List<Long> ids = Lists.newArrayList();
        if (entity.getFinance() != null) {
            if ((entity.getFinance().getInvoiceFlag() != null && entity.getFinance().getInvoiceFlag() >=0) ||
                    (entity.getFinance().getDiscountFlag() != null && entity.getFinance().getDiscountFlag()>=0)) {
                ids = dao.findServicePointIdsFromFinance(entity.getFinance());
            }
        }
        return msServicePointService.findIdList(entity, ids);
    }

    private List<Long> findIdListFromMSWithPrice(ServicePoint entity) {
        // 从微服务中获取网点id
        List<Long> ids = Lists.newArrayList();
        if (entity.getFinance() != null) {
            if ((entity.getFinance().getInvoiceFlag() != null && entity.getFinance().getInvoiceFlag() >=0) ||
                    (entity.getFinance().getDiscountFlag() != null && entity.getFinance().getDiscountFlag()>=0)) {
                ids = dao.findServicePointIdsFromFinance(entity.getFinance());
            }
        }
        return msServicePointService.findIdListWithPrice(entity, ids);
    }

    private List<Long> findServicePointIdsForPlanFromMS(ServicePoint entity) {
        // add on 2019-12-30
        // 从微服务中获取网点id
        List<Long> ids = Lists.newArrayList();
        // TODO: msServicePointService.findServicePointIdsForPlan()现在还不需要ids 2019-12-30
        return msServicePointService.findServicePointIdsForPlan(entity, ids);
    }

    /**
     * 按需更改信息：订单相关统计数量只做增减,客评：平均运算，四舍五入
     * @param maps
     */
    public void updateServicePointByMap(HashMap<String, Object> maps) {
        MSErrorCode msErrorCode = msServicePointService.updateServicePointByMap(maps);
        if (msErrorCode.getCode()>0) {
            throw new RuntimeException("调用微服务按需更新网点信息失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 是否启用网点保险扣除(微服务调用) // add on 2019-9-17
     * @param id  网点id
     * @param appInsuranceFlag
     * @param updateBy
     * @param updateDate
     */
    public void appReadInsuranceClause(Long id, Integer appInsuranceFlag, Long updateBy, Date updateDate) {
        MSErrorCode msErrorCode = msServicePointService.appReadInsuranceClause(id, appInsuranceFlag, updateBy, updateDate);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务是否启用网点保险扣除失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 更新网点地址信息(微服务调用)// add on 2019-9-17
     * @param servicePoint
     */
    public void updateServicePointAddress(ServicePoint servicePoint) {
        MSErrorCode msErrorCode = msServicePointService.updateServicePointAddress(servicePoint);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新网点地址信息失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 更新网点的银行账号信息(微服务调用)// add on 2019-9-17
     * @param servicePoint
     */
    public void updateServicePointBankAccountInfo(ServicePoint servicePoint) {
        MSErrorCode msErrorCode = msServicePointService.updateServicePointBankAccountInfo(servicePoint);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新网点的银行账号信息失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    /**
     * 更新网点的主账号信息(微服务调用)// add on 2019-9-17
     * @param servicePoint
     */
    public void updatePrimaryAccount(ServicePoint servicePoint) {
        MSErrorCode msErrorCode = msServicePointService.updatePrimaryAccount(servicePoint);
        if (msErrorCode.getCode() >0) {
            throw new RuntimeException("调用微服务更新网点的主账号信息失败.失败原因:" + msErrorCode.getMsg());
        }
    }

    public List<Long> getProductIdsById(Long servicePointId) {
        // add on 2019-12-17
        MDServicePointProduct mdServicePointProduct = new MDServicePointProduct();
        mdServicePointProduct.setServicePointId(servicePointId);
        List<Long> productIdListFromMS = msServicePointProductService.findProductIds(mdServicePointProduct);
        if (!org.springframework.util.ObjectUtils.isEmpty(productIdListFromMS)) {
            productIdListFromMS = productIdListFromMS.stream().sorted().collect(Collectors.toList());
        }
        return productIdListFromMS;
    }


    /**
     * 突击客服添加网点// add on 2019-9-17
     * @param servicePoint
     */
    @Transactional(readOnly = false)
    public void insertServicePointForPlan(ServicePoint servicePoint) {
        //
        // 此方法只添加新网点  add on 2020-5-19
        //
        String checkServicePointNo = checkServicePointNo(null,servicePoint.getServicePointNo());
        if(!checkServicePointNo.equalsIgnoreCase("true")){
            throw new RuntimeException("网点编号已经存在");
        }

        String result = checkEngineerMobile(null,servicePoint.getContactInfo1());
        if(!result.equalsIgnoreCase("true")){
            throw new RuntimeException("手机号已经被注册");
        }
        //  检查微服务连接是否通畅
        msCommonQueryService.checkConnection();


        servicePoint.setResetPrice(1);
        MDServicePointUnionDto servicePointUnionDto = new MDServicePointUnionDto();
        Long servicePointId = null;
        String lockkey = null;
        Boolean locked = false;
        ServicePointFinance finance = servicePoint.getFinance();
        String bankNo = finance.getBankNo();
        Dict dictBankIssue = finance.getBankIssue();
        if (StringUtils.isBlank(bankNo) && (dictBankIssue == null || StringUtils.isBlank(dictBankIssue.getValue()) || dictBankIssue.getValue().equalsIgnoreCase("0"))) {
            dictBankIssue = new Dict("1", "暂无付款账号");//1-暂无付款账号
            finance.setBankIssue(dictBankIssue);
        }else{
            Dict nbankIssue = MSDictUtils.getDictByValue(dictBankIssue.getValue(), "BankIssueType");//切换为微服务
            if (nbankIssue != null) {
                finance.setBankIssue(nbankIssue);
            }
        }

        //同步paymentType,bank,bank_no,bank_owner,invoice_flag at 2018/08/30 by ryan
        servicePoint.setPaymentType(finance.getPaymentType());
        servicePoint.setInvoiceFlag(finance.getInvoiceFlag());
        servicePoint.setBank(finance.getBank());
        servicePoint.setBankNo(finance.getBankNo());
        servicePoint.setBankOwner(finance.getBankOwner());
        servicePoint.setBankIssue(finance.getBankIssue());
        servicePoint.setDiscountFlag(finance.getDiscountFlag());//2018/12/07 ryan
        if (finance.getDiscountFlag() == DISCOUNT_FLAG_DISABLED) {
            finance.setDiscount(0.0D);   //add on 2020-4-6
        }
        //end

        //add on 2020-5-20 界面数据获取 begin
        //1. 获取网点产品列表,网点品类列表
        servicePoint.setProductCategoryIds(Lists.newArrayList(servicePoint.getProductCategoryId()));
        servicePointUnionDto.setProductCategories(Lists.newArrayList(servicePoint.getProductCategoryId()));
        List<Long> products = Lists.newArrayList();
        if (StringUtils.isNoneBlank(servicePoint.getProductIds())) {
            products = Arrays.stream(servicePoint.getProductIds().split(","))
                    .map(t -> Long.valueOf(t))
                    .collect(Collectors.toList());

            log.warn("网点产品:{}", products);
            servicePointUnionDto.setProductIds(products);
        }
        //2. 获取网点区域
        Area parentArea = areaService.getSelfAndParentList(servicePoint.getSubArea().getId(),0L,Area.TYPE_VALUE_COUNTY);
        List<Long> areas = Lists.newArrayList(servicePoint.getSubArea().getId());// 服务区域
        areas.add(parentArea.getParentId());
        areas.add(parentArea.getParent().getParentId());
        servicePointUnionDto.setAreaIds(areas);

        List<Long> townAreaIds = Lists.newArrayList();  //乡镇/街道id列表
        if(StringUtils.isNotBlank(servicePoint.getAreaIds())){
            townAreaIds = Arrays.stream(servicePoint.getAreaIds().split(","))
                    .map(t -> Long.valueOf(t))
                    .collect(Collectors.toList());
            log.warn("网点街道:{}", townAreaIds);
        }
        Long serviceCountyId = servicePoint.getSubArea().getId(); //服务区域区县Id
        List<Area> townList = areaService.findListByParent(Area.TYPE_VALUE_TOWN,serviceCountyId);
        Map<Long,Area> townMap = townList.stream().collect(Collectors.toMap(Area::getId, Function.identity(), (key1, key2) -> key2));
        List<AreaDto> AreaDtoList = Lists.newArrayList();
        AreaDto areaDto = null;
        for(Long item:townAreaIds){
             Area town = townMap.get(item);
             if(town!=null){
                 areaDto = new AreaDto();
                 areaDto.setId(town.getId());
                 areaDto.setName(town.getName());
                 areaDto.setParentId(serviceCountyId);
                 AreaDtoList.add(areaDto);

             }
        }
        servicePointUnionDto.setSubAreas(AreaDtoList);

        //3. 主账号师傅区域
        // 主账号师傅负责的区域
        List<Long> engineerAreaIds = Lists.newArrayList(serviceCountyId);
        servicePointUnionDto.setEngineerAreaIds(engineerAreaIds);

        // add on 2020-5-20 begin
        // 微服务调用
        servicePoint.preInsert();  //add on 2020-1-15 添加网点价格时需要用到网点updateBy作为createBy
        Engineer primary = servicePoint.getPrimary();
        primary.setCreateDate(servicePoint.getCreateDate());
        primary.setCreateBy(servicePoint.getCreateBy());
        primary.setServicePoint(servicePoint);
        primary.setMasterFlag(1);
        primary.setArea(servicePoint.getArea());
        primary.setContactInfo(servicePoint.getContactInfo1());
        primary.setAddress(servicePoint.getAddress());  //主帐号的地址默认和网点相同

        MDEngineerAddress engineerAddress = new MDEngineerAddress();
        engineerAddress.setServicePointId(servicePoint.getId());
        engineerAddress.setUserName(servicePoint.getPrimary().getName());
        engineerAddress.setContactInfo(servicePoint.getPrimary().getContactInfo());
        engineerAddress.setAreaId(servicePoint.getArea().getId());
        //
        Area districtArea = areaService.getFromCache(servicePoint.getArea().getId());
        Long cityAreaId = Optional.ofNullable(districtArea).map(Area::getParent).map(Area::getId).orElse(0L);
        Area cityArea = areaService.getFromCache(cityAreaId);
        Long provinceAreaId = Optional.ofNullable(cityArea).map(Area::getParent).map(Area::getId).orElse(0L);
        engineerAddress.setProvinceId(provinceAreaId);
        engineerAddress.setCityId(cityAreaId);
        //
        engineerAddress.setContactInfo(servicePoint.getContactInfo1());
        engineerAddress.setAddress(servicePoint.getArea().getFullName().concat(servicePoint.getSubAddress()));
        engineerAddress.setAddressFlag(MDEngineerEnum.EngineerAddressFlag.SERVICEPOINT.getValue());  // 默认使用网点地址
        primary.setEngineerAddress(engineerAddress);

        MDServicePoint mdServicePoint = mapper.map(servicePoint, MDServicePoint.class);
        mdServicePoint.setQq("");
        mdServicePoint.setAttachment1("");
        mdServicePoint.setAttachment2("");
        mdServicePoint.setAttachment3("");
        mdServicePoint.setAttachment4("");
        mdServicePoint.setDescription("");
        mdServicePoint.setUseDefaultPrice(20);
        mdServicePoint.setStatus(10);
        mdServicePoint.setLevel(1);
        mdServicePoint.setSignFlag(0);
        mdServicePoint.setPaymentChannel(0);  // add on 2020-7-27
        MDEngineer  mdEngineer = mapper.map(primary, MDEngineer.class);
        User creater = UserUtils.getUser();
        if (creater != null) {
            mdEngineer.setCreateById(creater.getId());
            mdEngineer.getEngineerAddress().setCreateById(creater.getId());
        }
        mdEngineer.setLevel(1);
        Date currentDate = new Date();
        mdEngineer.setCreateDate(currentDate);
        mdEngineer.getEngineerAddress().setCreateDate(currentDate);
        servicePointUnionDto.setServicePoint(mdServicePoint);
        servicePointUnionDto.setEngineer(mdEngineer);
        mdServicePoint.setDeveloper(creater.getName());
        try {
            //1.调用网点微服务
            NameValuePair<Long, Long> nameValuePair = msServicePointService.insertServicePointUnionDto(servicePointUnionDto);
            servicePointId = nameValuePair.getName();   // 网点Id
            Long engineerId = nameValuePair.getValue();  // 主账号师傅Id
            if (servicePointId == null || engineerId == null) {
                throw new RuntimeException("调用保存网点微服务后，返回的网点id为空或安维id为空.ServicePointId="+servicePointId+",engineerId="+engineerId);
            }
            servicePoint.setId(servicePointId);
            primary.setId(engineerId);
            servicePoint.setPrimary(primary);
            // .写网点操作日志
            servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.CREATE_SERVICEPOINT, "创建网点",
                    ServicePointLogService.toServicePointJson(servicePoint), servicePoint.getCreateBy());
            //备注
            if (StringUtils.isNotBlank(servicePoint.getRemarks())) {
                servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_REMARK, "网点备注", servicePoint.getRemarks(), UserUtils.getUser());
            }
        } catch (Exception ex) {
            LogUtils.saveLog("新增网点", "ServicePointService.insertServicePoint()-调用微服务", servicePoint.getServicePointNo(), ex, UserUtils.getUser());
            throw new RuntimeException(ex);
        }

        // add on 2020-5-20 微服务调用 end
        Boolean resetPermit = true;//新增强制保存价格
        Dict priceType = new Dict(String.valueOf(servicePoint.getUseDefaultPrice()));
        List<ServicePrice> servicePriceListWhenUpdate = Lists.newArrayList();  // add on 2019-12-21 // 用来保存修改时新增的产品的价格
        //锁
        lockkey = String.format("lock:servicepoint:%s", servicePointId);
        //获得锁
        locked = redisUtils.setNX(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey, 1, 60);//1分钟
        if (!locked) {
            throw new RuntimeException("网点正在修改中，请稍候重试。");
        }
        User actionUser = servicePoint.getCreateBy();
        Integer paymentTypeValue = finance.getPaymentType().getIntValue();
        Integer oldPaymentTypeValue = null;

        try {
            // 保存到Web本地数据  begin
            //网点财务
            finance.setId(servicePoint.getId());
            if (StringUtils.isBlank(finance.getDebtsDescrption())) {
                finance.setDebtsDescrption("");
            }
            dao.insertFI(finance);

            //add sys_user,帐号:手机号 密码:手机号
            User user = new User();
            user.setCompany(new Office(servicePoint.getId()));//网点
            user.setLoginName(primary.getContactInfo());
            user.setName(primary.getName());
            user.setMobile(primary.getContactInfo());
            user.setUserType(User.USER_TYPE_ENGINEER);
            user.setSubFlag(0);
            //user.getRoleList().add(new Role(6L));  //mark on 2020-11-18
            user.setRole(new Role(6L));  //add on 2020-11-18
            user.setPassword(SystemService.entryptPassword(StringUtils.right(primary.getContactInfo().trim(), 6)));
            user.setEngineerId(primary.getId());
            user.setCreateBy(primary.getCreateBy());
            user.setCreateDate(primary.getCreateDate());
            userDao.insert(user);
            //userDao.insertUserRole(user);//角色 //mark on 2020-11-18
            userDao.insertSingleUserRole(user);  //add on 2020-11-18
            MSUserUtils.addUserToRedis(user);//user微服务

            //==== 发送消息 begin======
            if (oldPaymentTypeValue == null || !paymentTypeValue.equals(oldPaymentTypeValue)) {
                int year = DateUtils.getYear(servicePoint.getCreateDate());
                MQInserDefaultsMessage.InsertDefaultsMessage insertDefaultsMessage = MQInserDefaultsMessage.InsertDefaultsMessage.newBuilder()
                        .setServicepointId(servicePointId)
                        .setPaymentType(paymentTypeValue)
                        .setYear(year)
                        .build();
                try {
                    insertFIDefaultsSender.send(insertDefaultsMessage);
                } catch (Exception e) {
                    LogUtils.saveLog("订单对帐.发送网点对帐队列", "FI:insertFIDefaultsSender.send",
                            new JsonFormat().printToString(insertDefaultsMessage), new Exception(e.getLocalizedMessage()), actionUser);
                }
            }

            // 网点价格
            if (resetPermit && servicePoint.getResetPrice() == 1) {
                //发送生成网点价格消息
                MQServicePointPriceMessage.ServicePointPriceMessage.Builder  servicePointPriceMessage =  MQServicePointPriceMessage.ServicePointPriceMessage.newBuilder();
                servicePointPriceMessage.setServicePointId(servicePoint.getId());
                servicePointPriceMessage.setSyncType(MDServicePointPriceSyncTypeEnum.ADD.getValue());
                servicePointPriceMessage.setUserId(actionUser != null && actionUser.getId()!= null?actionUser.getId():0L);
                try {
                    servicePointPriceSender.send(servicePointPriceMessage.build());
                } catch (Exception ex) {
                }
            }

            //ES
            try {
                ServicePointStation servicePointStation = new ServicePointStation();
                servicePointStation.setServicePoint(new ServicePoint(servicePoint.getId()));
                List<ServicePointStation> servicePointStationList = servicePointStationService.findList(servicePointStation);
                if (servicePointStationList != null && !servicePointStationList.isEmpty()) {
                    for(ServicePointStation item:servicePointStationList){
                        item.setIsNewRecord(true);
                        item.setServicePoint(servicePoint);
                        // servicePointStationService.saveForWeb(item); // mark on 2020-11-24 begin 停止往队列 MS:MQ:ES:SYNC:SERVICEPOINT:STATION 发送消息
                    }
                }
            }catch (Exception e){
                LogUtils.saveLog("突击客服新增网点", "ServicePointService.insertServicePointForPlan()-保持ES", servicePoint.getServicePointNo(), e, UserUtils.getUser());
                log.error("突击客服新增网点保持ES失败 id:{} 失败原因:{}",servicePoint.getId(),e.getMessage());
            }

            // 更新网点财务缓存
            try {
                servicePointFinanceService.updateCache(finance);
            } catch (Exception ex) {
                log.error("更新网点财务缓存失败,id:{} 失败原因:{}", finance.getId(), ex);
            }
        } catch (Exception e) {
            LogUtils.saveLog("新增网点" , "ServicePointService.save", servicePoint.getServicePointNo(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (locked && lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }

    /**
     * 按区县/街道/品类 分页查停用派单列表
     * 只查询level 1 ~ 5的,且status=10
     */
    public Page<ServicePoint> findUnbleSelectForPlan(Page<ServicePoint> page, ServicePoint entity) {
        entity.setPage(page);
        List<Long> ids = msServicePointService.findUnbleSelectForPlan(entity);
        page.initialize();
        if (ids != null && !ids.isEmpty()) {
            ServicePoint s;
            for (Long id : ids) {
                s = getFromCache(id);
                if (s != null && s.getId() != null) {
                    page.getList().add(s);
                }
                else {
                    s = get(id);
                    if (s != null) {
                        page.getList().add(s);
                    }
                }
            }
        }
        return page;
    }

    /**
     * 根据网点id获取网点对应品类
     * @param servicePointId
     * @return
     */
    public List<Long> findCategoryListByServicePiontId(Long servicePointId) {
        List<Long> categories = msProductCategoryServicePointService.findListByServicePointIdForMD(servicePointId);
        if (ObjectUtils.isEmpty(categories)) {
            categories = Lists.newArrayList();
        }
        return categories;
    }
    public Integer updateInsuranceFlag(Long id,Integer insuranceFlag){
        ServicePoint servicePoint = new ServicePoint();
        User user = UserUtils.getUser();
        servicePoint.setId(id);
        servicePoint.setInsuranceFlag(insuranceFlag);
        servicePoint.setUpdateBy(user);
        servicePoint.setUpdateDate(new Date());
        ServicePoint pastServicePoint = msServicePointService.getById(servicePoint.getId());
        if(pastServicePoint != null){
            if(pastServicePoint.getDegree() == 30){
                servicePoint.setInsuranceFlag(0);
            }
        }
        return msServicePointService.updateInsuranceFlagForMD(servicePoint);
    }
}

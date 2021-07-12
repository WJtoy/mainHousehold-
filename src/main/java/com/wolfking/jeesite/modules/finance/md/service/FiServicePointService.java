package com.wolfking.jeesite.modules.finance.md.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.common.NameValuePair;
import com.kkl.kklplus.entity.fi.mq.MQInserDefaultsMessage;
import com.kkl.kklplus.entity.md.*;
import com.kkl.kklplus.entity.md.mq.MQServicePointPriceMessage;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.finance.md.dao.FiServicePointDao;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.md.entity.viewModel.ServicePointPlanRemarkModel;
import com.wolfking.jeesite.modules.md.service.*;
import com.wolfking.jeesite.modules.md.utils.ProductUtils;
import com.wolfking.jeesite.modules.mq.sender.InsertFIDefaultsSender;
import com.wolfking.jeesite.modules.mq.sender.ServicePointPriceSender;
import com.wolfking.jeesite.modules.sys.dao.UserDao;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.providermd.service.*;
import com.wolfking.jeesite.ms.providersys.service.MSSysAreaService;
import com.wolfking.jeesite.ms.service.sys.MSUserService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import com.wolfking.jeesite.ms.utils.MSUserUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 服务网点
 * Ryan Lu
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class FiServicePointService {

    @Autowired
    private MSServicePointService msServicePointService;

    @Autowired
    private MSDepositLevelService msDepositLevelService;

    @Autowired
    private MSEngineerService msEngineerService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MSServicePointAreaService msServicePointAreaService;

    @Autowired
    private MSSysAreaService msSysAreaService;

    @Autowired
    private ServicePointFinanceService servicePointFinanceService;

    @Resource
    private FiServicePointDao fiServicePointDao;

    @Autowired
    private AreaService areaService;

    @Autowired
    private MSServicePointProductService msServicePointProductService;

    @Autowired
    private MSProductCategoryServicePointService msProductCategoryServicePointService;

    @Autowired
    private MSCommonQueryService msCommonQueryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MSEngineerAreaService msEngineerAreaService;

    @Autowired
    private ServicePointStationService servicePointStationService;

    @Autowired
    private ServicePointLogService servicePointLogService;

    @Resource
    private UserDao userDao;

    @Autowired
    private InsertFIDefaultsSender insertFIDefaultsSender;

    @Autowired
    private ServicePointPriceSender servicePointPriceSender;

    @Autowired
    private EngineerService engineerService;

    @Autowired
    private MSUserService msUserService;

    @Autowired
    private MSServicePointStationService msServicePointStationService;


    @Autowired
    private MSServicePointPriceService msServicePointPriceService;

    @Autowired
    private MSProductPriceService msProductPriceService;

    private final static int DISCOUNT_FLAG_DISABLED = 0;  //停用扣点标志


    public ServicePoint get(Long id) {
        return getWithExtendPropertyFromMaster(id);
    }

    public Page<ServicePoint> findPage(Page<ServicePoint> page, ServicePoint entity) {
        entity.setPage(page);


        List<Long> ids = findIdListFromMS(entity);  // add on 2019-12-29

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
        if (ids.size() > 0) {
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
            servicePointFinance = fiServicePointDao.getFinanceFromMaster(servicePoint.getId());  //add on 2020-3-18
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


    private List<Long> findIdListFromMS(ServicePoint entity) {
        // add on 2019-12-29
        // 从微服务中获取网点id
        List<Long> ids = Lists.newArrayList();
        if (entity.getFinance() != null) {
            if ((entity.getFinance().getInvoiceFlag() != null && entity.getFinance().getInvoiceFlag() >=0) ||
                    (entity.getFinance().getDiscountFlag() != null && entity.getFinance().getDiscountFlag()>=0)) {
                ids = fiServicePointDao.findServicePointIdsFromFinance(entity.getFinance());
            }
        }
        return msServicePointService.findIdList(entity, ids);
    }


    public Map<Long,String> getServicePointServiceAreas(List<Long> servicePointIds) {

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


    /**
     * 读取网点负责的区域列表
     *
     * @param id 网点id
     * @return
     */
    public List<Integer> getAreaIds(Long id) {

        List<Long> areaIdsFromMS = msServicePointAreaService.findAreaIds(id);
        return areaIdsFromMS != null && !areaIdsFromMS.isEmpty()? areaIdsFromMS.stream().map(Long::intValue).collect(Collectors.toList()) : Lists.newArrayList();
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
        return !ObjectUtils.isEmpty(productIdsFromMS)?productIdsFromMS.stream().map(Long::intValue).collect(Collectors.toList()) : Lists.newArrayList();
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

            servicePoint.setTimeLinessFlag(0);  //返现网点,没有快可立时效
        }
        if(servicePoint.getRemotePriceEnabledFlag() == 1){
            servicePoint.setRemotePriceType(40);
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
                    Long lType = Long.valueOf(strType);
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
            Set<RedisZSetCommands.Tuple> areaSets = redisUtils.zRangeWithScore(RedisConstant.RedisDBType.REDIS_SYS_DB, String.format(RedisConstant.SYS_AREA_TYPE, "4"), 0, -1);
            areaAll = areaSets.stream().map(t -> t.getScore().longValue()).collect(Collectors.toSet());
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
            fiServicePointDao.insertFI(finance);

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
            if (servicePoint.getRemotePriceFlag() == 1) {
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

            servicePoint.setTimeLinessFlag(0);  //返现网点,没有快可立时效
        }
        if(servicePoint.getRemotePriceEnabledFlag() == 1){
            servicePoint.setRemotePriceType(40);
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
        Integer oldPaymentTypeValue;
        boolean resetPermit = false;  //无重置权限

        try {

            Engineer primary = getEngineer(servicePoint.getPrimary().getId());  // 获取主账号
            oldPaymentTypeValue = fiServicePointDao.getServicePointPaymentType(servicePointId);  //从DB中获取付款类型(如月结,日结)

            //备注日志
            if(StringUtils.isNotBlank(servicePoint.getRemarks())){
                assert cachedServicePoint != null;
                if(!servicePoint.getRemarks().equals(cachedServicePoint.getRemarks())) {

                    updateRemarkWithoutSaveToMS(servicePointId, servicePoint.getRemarks()); //add on 2019-10-11
                }
            }
            // update fi
            finance.setId(servicePoint.getId());
            fiServicePointDao.updateFI(finance);

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

            //sys_user
            maps.remove("id");
            maps.put("engineerId", primary.getId());
            userDao.updateUserByEngineerId(maps);

            servicePoint.setPrimary(primary);

            if (SecurityUtils.getSubject().isPermitted("md:servicepoint:defaultpriceedit")) {
                resetPermit = true;
            }

            // 写网点操作日志
            String stringBuilder = ServicePointLogService.toServicePointJson(servicePoint);
            servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT, "编辑网点基本资料",
                    stringBuilder, servicePoint.getUpdateBy());

            msUserService.refreshUserCacheByEngineerId(primary.getId());//更新user微服务

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
            // 更新
            // ServicePoint微服务
            MSErrorCode msErrorCode = msServicePointService.save(servicePoint,false);
            if (msErrorCode.getCode() >0) {
                throw new RuntimeException("调用微服务保存网点信息出错.错误信息:"+msErrorCode.getMsg());
            }


            MSErrorCode msErrorCodeEngineer = msEngineerService.updateEngineerName(servicePoint.getPrimary());
            if (msErrorCodeEngineer.getCode() >0) {
                throw new RuntimeException("调用微服务保存安维人员信息出错.错误信息:"+msErrorCodeEngineer.getMsg());
            }


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
            LogUtils.saveLog("修改网点基础资料", "ServicePointService.save", servicePoint.getServicePointNo(), e, actionUser);
            throw new RuntimeException(e);
        } finally {
            if (lockkey != null) {
                redisUtils.remove(RedisConstant.RedisDBType.REDIS_LOCK_DB, lockkey);
            }
        }
    }


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

    public Engineer getEngineer(Long id) {
        return engineerService.getEngineer(id);
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

    public void updateAutoPlanFlag (Long servicePointId,Integer autoPlanFlag){
        User user = UserUtils.getUser();
        if (org.springframework.util.ObjectUtils.isEmpty(servicePointId) ){return;}
        //TODO: 写网点操作日志
        servicePointLogService.saveServicePointLog(servicePointId, ServicePointLog.ServicePointLogType.EDIT_SERVICEPOINT_AUTOPLANFLAG, "网点自动派单", String.valueOf(autoPlanFlag), user);

        //读取网点再更新缓存
        ServicePoint servicePoint = getFromCache(servicePointId);
        ServicePoint cachedServicePoint = new ServicePoint();
        BeanUtils.copyProperties(servicePoint,cachedServicePoint);
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
     * 检查手机号是否重复
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

    /**
     * 检查银行卡号是否重复
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

        if (customizePriceFlagFromDB == ServicePoint.AUTO_PLAN_FLAG_DISABLED
                && customizePriceFlag == ServicePoint.CUSTOMIZE_PRICE_FLAG_ENABLED
                && !degree) {
            throw new RuntimeException(" 产品：["+strMsg+"] 使用的不是标准价,请调整! 网点现在使用[ "+priceTypeFromDB.getLabel()+" ]价格，不能修改。");
        }
        // add on 2020-3-12 end

        ServicePoint servicePoint = new ServicePoint();
        servicePoint.setId(servicePointId);
        servicePoint.setCustomizePriceFlag(customizePriceFlag);
        msServicePointService.updateCustomizePriceFlag(servicePoint);
        long iEnd = System.currentTimeMillis();
        log.warn("updateCustomizePriceFlag耗时:{}毫秒.", iEnd-iBegin);
    }
}

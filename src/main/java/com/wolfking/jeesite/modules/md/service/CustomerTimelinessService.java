package com.wolfking.jeesite.modules.md.service;


import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDCustomerTimeliness;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.CustomerTimelinessDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.CustomerTimeliness;
import com.wolfking.jeesite.modules.md.entity.TimelinessLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaTimelinessModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.TimelinessChargeModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerTimelinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 客户时效设定Service
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CustomerTimelinessService extends LongIDCrudService<CustomerTimelinessDao, CustomerTimeliness> {

    @Autowired
    private AreaService areaService;

    /*
    @Autowired
    private RedisUtils redisUtils;
    */

    @Autowired
    private MSCustomerTimelinessService msCustomerTimelinessService;

    /**
     * 保存客户的多个时效设定
     * @param entity
     */
    public void saveCustomerTimelinesss(CustomerTimeliness entity){
        List<AreaTimelinessModel> list=entity.getAreaTimelinessModelList();
        List<CustomerTimeliness> customerTimelinessList = Lists.newArrayList();  // add on 2019-8-2 用来保存往微服务传送的数据
        if(list !=null && list.size()>0){
            //先删除该客户下的时效等级 再重新添加
            deleteByCustomerId(entity.getCustomer().getId());

            for (AreaTimelinessModel item: list) {

                List<TimelinessChargeModel>  timelinessChargeModelList= item.getList();

                for (TimelinessChargeModel model:timelinessChargeModelList) {
                    CustomerTimeliness customerTimeliness=new CustomerTimeliness();
                    customerTimeliness.setCustomer(entity.getCustomer());
                    customerTimeliness.setArea(item.getArea());
                    customerTimeliness.setTimelinessLevel(model.getTimelinessLevel());
                    customerTimeliness.setChargeIn(model.getChargeIn());
                    customerTimeliness.setChargeOut(model.getChargeOut());

                    //save(customerTimeliness);  // mark on 2020-1-8
                    customerTimelinessList.add(customerTimeliness); // add on 2019-8-2 主要用来获取从数据库返回的id
                }
            }
            // add on 2019-8-2 调用customerTimeliness微服务
            MSErrorCode msErrorCode = msCustomerTimelinessService.batchInsert(customerTimelinessList);
            if (msErrorCode.getCode()>0) {
                throw new RuntimeException("调用微服务批量添加数据失败.失败原因:"+msErrorCode.getMsg());
            }

            //remCache(entity.getCustomer().getId());  //mark on 2020-1-8
            //findListByCustomerId(entity.getCustomer().getId()); //mark on 2020-1-9
        }
    }

    /**
     * 通过客户id获得区域 时效等级列表
     * @param customerId
     * @return
     */
    public List<AreaTimelinessModel> findListByCustomerId(Long customerId){
        /*
        //mark on 2020-1-8 begin
       String key = String.format(RedisConstant.MD_CUSTOMER_TIMELINESS,customerId);

       List<AreaTimelinessModel> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,key,0,-1,AreaTimelinessModel.class);
       if(list==null || list.size() ==0){
           List<Area> areaList=areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
           list=new ArrayList<>();

           // add on 2019-7-31 begin
           CustomerTimeliness customerTimeliness = new CustomerTimeliness();
           customerTimeliness.setCustomer(new Customer(customerId));
           List<CustomerTimeliness> customerTimelinessList = msCustomerTimelinessService.findListByCustomerId(customerTimeliness);

           Map<Long,List<CustomerTimeliness>> customerTimelinesMap = customerTimelinessList != null && !customerTimelinessList.isEmpty()?customerTimelinessList
                   .stream().collect(Collectors.groupingBy(r->r.getArea().getId(), Collectors.mapping(r->r,Collectors.toList()))):null;
           // add on 2019-7-31 end

           for (Area area:areaList) {
               //查找客户下的区域
               // List<TimelinessChargeModel> customerTimelinesses= dao.findListByCustomerId(customerId,area.getId()); // mark on 2019-7-31
               List<CustomerTimeliness> subCustomerTimeslinessList = null;
               if (customerTimelinesMap != null && customerTimelinesMap.containsKey(area.getId())) {
                   subCustomerTimeslinessList = customerTimelinesMap.get(area.getId());
               }
               List<TimelinessChargeModel> customerTimelinesses = findListByCustomerIdAndAreaId(subCustomerTimeslinessList);  // add on 2019-8-5

                //如果该客户 该区域下有数据再缓存
               if(customerTimelinesses != null && customerTimelinesses.size() > 0){
                   AreaTimelinessModel areaTimelinessModel=new AreaTimelinessModel();
                   areaTimelinessModel.setArea(area);
                   areaTimelinessModel.setList(customerTimelinesses);
                   list.add(areaTimelinessModel);
                   redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB,key,areaTimelinessModel,area.getId(),-1);
               }
           }
       }
//       log.warn("findListByCustomerId:{}", list);
       return list;
       //mark on 2020-1-8 end
       */

        List<Area> areaList=areaService.findListByType(Area.TYPE_VALUE_PROVINCE);
        List<AreaTimelinessModel> list = Lists.newArrayList();

        // add on 2019-7-31 begin
        CustomerTimeliness customerTimeliness = new CustomerTimeliness();
        customerTimeliness.setCustomer(new Customer(customerId));
        List<CustomerTimeliness> customerTimelinessList = msCustomerTimelinessService.findListByCustomerId(customerTimeliness);

        Map<Long,List<CustomerTimeliness>> customerTimelinesMap = customerTimelinessList != null && !customerTimelinessList.isEmpty()?customerTimelinessList
                .stream().collect(Collectors.groupingBy(r->r.getArea().getId(), Collectors.mapping(r->r,Collectors.toList()))):null;
        // add on 2019-7-31 end

        for (Area area:areaList) {
            //查找客户下的区域
            // List<TimelinessChargeModel> customerTimelinesses= dao.findListByCustomerId(customerId,area.getId()); // mark on 2019-7-31
            List<CustomerTimeliness> subCustomerTimeslinessList = null;
            if (customerTimelinesMap != null && customerTimelinesMap.containsKey(area.getId())) {
                subCustomerTimeslinessList = customerTimelinesMap.get(area.getId());
            }
            List<TimelinessChargeModel> customerTimelinesses = findListByCustomerIdAndAreaId(subCustomerTimeslinessList);  // add on 2019-8-5

            //如果该客户 该区域下有数据再缓存
            if(customerTimelinesses != null && customerTimelinesses.size() > 0){
                AreaTimelinessModel areaTimelinessModel=new AreaTimelinessModel();
                areaTimelinessModel.setArea(area);
                areaTimelinessModel.setList(customerTimelinesses);
                list.add(areaTimelinessModel);
            }
        }

        return list;
    }


    /**
     * 根据区域id从客户时效等级查询时效费用数据
     * @param customerTimelinessList
     * @return
     */
    public  List<TimelinessChargeModel>  findListByCustomerIdAndAreaId(List<CustomerTimeliness> customerTimelinessList) {
        List<TimelinessChargeModel> timelinessChargeModelList = Lists.newArrayList();

        if (customerTimelinessList != null && !customerTimelinessList.isEmpty()) {
            List<CustomerTimeliness> subCustomerTimesLinessList = customerTimelinessList.stream().sorted(Comparator.comparing(r->r.getTimelinessLevel().getId())).collect(Collectors.toList());
            if (subCustomerTimesLinessList != null && !subCustomerTimesLinessList.isEmpty()) {
                subCustomerTimesLinessList.stream().forEach(customerTimeliness -> {
                    TimelinessChargeModel timelinessChargeModel = new TimelinessChargeModel();
                    timelinessChargeModel.setTimelinessLevel(new TimelinessLevel(customerTimeliness.getTimelinessLevel().getId()));
                    timelinessChargeModel.setChargeIn(customerTimeliness.getChargeIn());
                    timelinessChargeModel.setChargeOut(customerTimeliness.getChargeOut());
                    timelinessChargeModelList.add(timelinessChargeModel);
                });
            }
        }
        return timelinessChargeModelList;
    }

    /**
     * 按客户id+区域id获得时效设定
     * @param customerId
     * @param areaId 省级别id
     * @return
     */
    public AreaTimelinessModel getAreaTimelinessModel(Long customerId,Long areaId){
        /*
        // mark on 2020-1-8 begin
        String key = String.format(RedisConstant.MD_CUSTOMER_TIMELINESS,customerId);
        AreaTimelinessModel timelinessModel = null;
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB,key)) {
            timelinessModel = (AreaTimelinessModel)redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, key, areaId, areaId, AreaTimelinessModel.class);
        }else{
            List<AreaTimelinessModel> list = findListByCustomerId(customerId);
            if(list != null && list.size()>0){
                timelinessModel = list.stream().filter(t->t.getArea().getId().equals(areaId)).findFirst().orElse(null);
                log.warn("getAreaTimelinessModel：{}", timelinessModel);
            }
        }
        return timelinessModel;
        // mark on 2020-1-8 end
        */

        AreaTimelinessModel timelinessModel = null;
        List<AreaTimelinessModel> list = findListByCustomerId(customerId);
        if(list != null && list.size()>0){
            timelinessModel = list.stream().filter(t->t.getArea().getId().equals(areaId)).findFirst().orElse(null);
        }
        return timelinessModel;
    }

    //删除缓存
    /*
    // mark on 2020-1-8
    public void remCache(Long customerId){
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_CUSTOMER_TIMELINESS,customerId));
    }
    */

    /**
     * 按客户ID删除
     * @param customerId
     */
    public void deleteByCustomerId(Long customerId) {
        //dao.deleteByCustomerId(customerId);  //mark on 2020-1-8
        deleteForMS(customerId);
        //remCache(customerId); mark on 2020-1-8
    }

    /**
     * 微服务删除时效设定
     * @param customerId
     */
    public void deleteForMS(Long customerId) {
        CustomerTimeliness customerTimeliness = new CustomerTimeliness();
        customerTimeliness.setCustomer(new Customer(customerId));
        MSErrorCode msErrorCode = msCustomerTimelinessService.delete(customerTimeliness);
        if (msErrorCode.getCode()>0) {
            throw new RuntimeException("调用微服务删除数据失败.失败原因:"+msErrorCode.getMsg());
        }
    }
}

package com.wolfking.jeesite.modules.md.service;

import com.google.common.collect.Lists;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.entity.md.MDCustomerUrgent;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.config.redis.RedisTuple;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.md.dao.UrgentCustomerlDao;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.UrgentCustomer;
import com.wolfking.jeesite.modules.md.entity.UrgentLevel;
import com.wolfking.jeesite.modules.md.entity.viewModel.AreaUrgentModel;
import com.wolfking.jeesite.modules.md.entity.viewModel.UrgentChargeModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.ms.providermd.service.MSCustomerUrgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UrgentCustomerService extends LongIDCrudService<UrgentCustomerlDao, UrgentCustomer> {
    @Autowired
    private AreaService areaService;

    @Autowired
    private UrgentLevelService urgentLevelService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MSCustomerUrgentService msCustomerUrgentService;

    /**
     * 查询分页数据
     *
     * @param page   分页对象
     * @param entity
     * @return
     */
    @Override
    public Page<UrgentCustomer> findPage(Page<UrgentCustomer> page, UrgentCustomer entity) {
        return msCustomerUrgentService.findList(page, entity);
    }

    /**
     * 保存客户的多个加急等级
     * @param entity
     */
    public void saveUrgentCustomers(UrgentCustomer entity){
        List<AreaUrgentModel> list=entity.getList();
        List<UrgentCustomer> urgentCustomerList = Lists.newArrayList();  // add on 2019-8-2 用来保存往微服务传送的数据
        if(list !=null && list.size()>0){
            //先删除该客户下的加急等级 再重新添加
            deleteByCustomerId(entity.getCustomer().getId());

            for (AreaUrgentModel item: list) {
                List<UrgentChargeModel> chargeModels=item.getList();
                for (UrgentChargeModel model: chargeModels) {
                    UrgentCustomer urgentCustomer=new UrgentCustomer();
                    urgentCustomer.setCustomer(entity.getCustomer());
                    urgentCustomer.setArea(item.getArea());
                    urgentCustomer.setUrgentLevel(model.getUrgentLevel());
                    urgentCustomer.setChargeIn(model.getChargeIn());
                    urgentCustomer.setChargeOut(model.getChargeOut());
                    //save(urgentCustomer); // mark on 2020-1-9

                    urgentCustomerList.add(urgentCustomer);   // add on 2019-8-2 主要用来获取从数据库返回的id
                }
            }

            // add on 2019-8-2 调用customerTimeliness微服务
            MSErrorCode msErrorCode = msCustomerUrgentService.batchInsert(urgentCustomerList);
            if (msErrorCode.getCode()>0) {
                throw new RuntimeException("调用微服务批量添加数据失败.失败原因:"+msErrorCode.getMsg());
            }

            //remCache(entity.getCustomer().getId());  //mark on 2020-1-9
            findListByCustomerId(entity.getCustomer().getId());
        }

    }

    /**
     * 客户id获得列表
     * @param customerId
     * @return
     */
    public List<AreaUrgentModel> findListByCustomerId(Long customerId){
        /*
        // mark on 2020-1-9 begin
        String key = String.format(RedisConstant.MD_CUSTOMER_URGENT,customerId);
        List<AreaUrgentModel> list = redisUtils.zRange(RedisConstant.RedisDBType.REDIS_MD_DB,key,0,-1,AreaUrgentModel.class);
        if(list==null || list.size() ==0) {
            Map<Long, Area> areaMaps = areaService.findMapByType(Area.TYPE_VALUE_PROVINCE);
            list = new ArrayList<>();
            Map<Long, UrgentLevel> urgentLevels = urgentLevelService.findAllMap();
            //查找客户下的区域
//            List<UrgentChargeModel> urgentChargeModels = dao.findListByCustomerId(customerId, null);   // mark on 2019-8-1

            List<UrgentChargeModel> urgentChargeModels = findListByCustomerIdFromMS(customerId);     // add on 2019-8-1 调用微服务获取数据

            if (urgentChargeModels != null && urgentChargeModels.size() > 0) {
                Map<Long, List<UrgentChargeModel>> groups = urgentChargeModels.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getAreaId(),
                                Collectors.mapping(p -> p,
                                        Collectors.toList()
                                )
                        ));
                Set<Map.Entry<Long, List<UrgentChargeModel>>> entrySet = groups.entrySet();
                Iterator<Map.Entry<Long, List<UrgentChargeModel>>> iter = entrySet.iterator();
                Map.Entry<Long, List<UrgentChargeModel>> entry;
                AreaUrgentModel areaUrgentModel;
                List<UrgentChargeModel> items;
                while (iter.hasNext()) {
                    entry = iter.next();
                    if (areaMaps.containsKey(entry.getKey())) {
                        areaUrgentModel = new AreaUrgentModel();
                        areaUrgentModel.setArea(areaMaps.get(entry.getKey()));
                        items = entry.getValue();
                        items.stream().forEach(t -> {
                            t.setUrgentLevel(urgentLevels.get(t.getUrgentLevel().getId()));
                        });
                        areaUrgentModel.setList(items);
                        list.add(areaUrgentModel);
                    }
                }
                Set<RedisZSetCommands.Tuple> sets = list.stream()
                        .map(t -> new RedisTuple(redisUtils.gsonRedisSerializer.serialize(t), t.getArea().getId().doubleValue()))
                        .collect(Collectors.toSet());

                redisUtils.zAdd(RedisConstant.RedisDBType.REDIS_MD_DB, key, sets, -1);
                //redisUtils.zSetEX(RedisConstant.RedisDBType.REDIS_MD_DB, key, areaUrgentModel, area.getId(), -1);
            }
        }//end if
        return list;
        // mark on 2020-1-9 end
       */


        Map<Long, Area> areaMaps = areaService.findMapByType(Area.TYPE_VALUE_PROVINCE);
        List<AreaUrgentModel> list = Lists.newArrayList();
        Map<Long, UrgentLevel> urgentLevels = urgentLevelService.findAllMap();
        //查找客户下的区域
        List<UrgentChargeModel> urgentChargeModels = findListByCustomerIdFromMS(customerId);     // add on 2019-8-1 调用微服务获取数据

        if (urgentChargeModels != null && urgentChargeModels.size() > 0) {
            Map<Long, List<UrgentChargeModel>> groups = urgentChargeModels.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getAreaId(),
                            Collectors.mapping(p -> p,
                                    Collectors.toList()
                            )
                    ));
            Set<Map.Entry<Long, List<UrgentChargeModel>>> entrySet = groups.entrySet();
            Iterator<Map.Entry<Long, List<UrgentChargeModel>>> iter = entrySet.iterator();
            Map.Entry<Long, List<UrgentChargeModel>> entry;
            AreaUrgentModel areaUrgentModel;
            List<UrgentChargeModel> items;
            while (iter.hasNext()) {
                entry = iter.next();
                if (areaMaps.containsKey(entry.getKey())) {
                    areaUrgentModel = new AreaUrgentModel();
                    areaUrgentModel.setArea(areaMaps.get(entry.getKey()));
                    items = entry.getValue();
                    items.stream().forEach(t -> {
                        t.setUrgentLevel(urgentLevels.get(t.getUrgentLevel().getId()));
                    });
                    areaUrgentModel.setList(items);
                    list.add(areaUrgentModel);
                }
            }
        }

        return list;
    }

    /**
     * 根据customerId从微服务中获取加急费用等级列表  // add on 2019-8-1
     * @param customerId
     * @return
     */
    private List<UrgentChargeModel>  findListByCustomerIdFromMS(Long customerId) {
        List<UrgentChargeModel> urgentChargeModelList = Lists.newArrayList();

        UrgentCustomer urgentCustomer = new UrgentCustomer();
        urgentCustomer.setCustomer(new Customer(customerId));
        List<UrgentCustomer> urgentCustomerList = msCustomerUrgentService.findListByCustomerId(urgentCustomer);
        if (urgentCustomerList != null && !urgentCustomerList.isEmpty()) {
            urgentCustomerList.stream().sorted(Comparator.comparing(urgentCustomerEntity -> urgentCustomerEntity.getUrgentLevel().getId())).forEach(urgentCustomerEntity -> {
                UrgentChargeModel urgentChargeModelEntity = new UrgentChargeModel();
                urgentChargeModelEntity.setUrgentLevel(urgentCustomerEntity.getUrgentLevel());
                urgentChargeModelEntity.setAreaId(urgentCustomerEntity.getArea().getId());
                urgentChargeModelEntity.setChargeIn(urgentCustomerEntity.getChargeIn());
                urgentChargeModelEntity.setChargeOut(urgentCustomerEntity.getChargeOut());

                urgentChargeModelList.add(urgentChargeModelEntity);
            });
        }
        return urgentChargeModelList;
    }

    /**
     * 通过客户 和 省ID获取 加急信息
     * @param customerId
     * @param areaId
     * @return
     */
    public AreaUrgentModel getAreaUrgentModel(Long customerId,Long areaId){
        /*
        // mark on 2020-1-9 begin
        String key = String.format(RedisConstant.MD_CUSTOMER_URGENT,customerId);
        AreaUrgentModel areaUrgentModel = null;
        if(redisUtils.exists(RedisConstant.RedisDBType.REDIS_MD_DB,key)) {
            areaUrgentModel = (AreaUrgentModel)redisUtils.zRangeOneByScore(RedisConstant.RedisDBType.REDIS_MD_DB, key, areaId, areaId, AreaUrgentModel.class);
        }else{
            List<AreaUrgentModel> list = findListByCustomerId(customerId);
            if(list != null && list.size()>0){
                areaUrgentModel = list.stream().filter(t->t.getArea().getId().equals(areaId)).findFirst().orElse(null);
            }
        }
        return areaUrgentModel;
        // mark on 2020-1-9 end
        */

        AreaUrgentModel areaUrgentModel = null;
        List<AreaUrgentModel> list = findListByCustomerId(customerId);
        if(list != null && list.size()>0){
            areaUrgentModel = list.stream().filter(t->t.getArea().getId().equals(areaId)).findFirst().orElse(null);
        }
        return areaUrgentModel;
    }

    //删除缓存
    /*
    public void remCache(Long customerId){
        redisUtils.remove(RedisConstant.RedisDBType.REDIS_MD_DB, String.format(RedisConstant.MD_CUSTOMER_URGENT,customerId));
    }
    */

    /**
     * 按客户ID删除
     * @param customerId
     */
    public void deleteByCustomerId(Long customerId){
        //dao.deleteByCustomerId(customerId);  //mark on 2020-1-9
        deleteForMS(customerId);   // add on 2019-8-1
        //remCache(customerId);    //mark on 2020-1-9
    }

    /**
     * 微服务删除加急设定
     * @param customerId
     */
    public void deleteForMS(Long customerId) {
        UrgentCustomer urgentCustomer = new UrgentCustomer();
        urgentCustomer.setCustomer(new Customer(customerId));
        MSErrorCode msErrorCode = msCustomerUrgentService.delete(urgentCustomer);
        if (msErrorCode.getCode()>0) {
            throw new RuntimeException("调用微服务删除数据失败.失败原因:"+msErrorCode.getMsg());
        }
    }
}

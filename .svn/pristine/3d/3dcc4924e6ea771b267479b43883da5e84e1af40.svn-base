package com.wolfking.jeesite.modules.sys.task;

import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.md.service.CustomerService;
import com.wolfking.jeesite.modules.md.service.PlanRadiusService;
import com.wolfking.jeesite.modules.md.service.ServicePointService;
import com.wolfking.jeesite.modules.sd.service.OrderService;
import com.wolfking.jeesite.modules.sys.entity.*;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.DictService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import com.wolfking.jeesite.modules.ws.entity.WSFeedbackStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模组基本资料保存至redis
 * Ryan 2017/5/19.
 */
@Component
@Order(value=1)
public class SysRedisTasks implements CommandLineRunner {

    @Autowired
    AreaService areaService;

    @Autowired
    DictService dictService;
    @Autowired
    CustomerService customerService;
    @Autowired
    OrderService orderService;
    @Autowired
    PlanRadiusService planRadiusService;
    @Autowired
    UserRegionService userRegionService;

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Override
    public void run(String... args) throws Exception {
        boolean loadInRedis = Boolean.valueOf(Global.getConfig("loadInRedis"));
        if(!loadInRedis){
            return;
        }
        System.out.println("服务启动任务：装载SYS模组redis数据：");

        System.out.println("1.区域");
        //zset key- area:type:#type score:id
        /*
        // mark on 2020-11-17 begin
        // sys_area微服务化
        List<Area> points = areaService.findAllList();
        Map<Integer,List<Area>> maps = points.stream().filter(r->!r.getType().equals(Area.TYPE_VALUE_TOWN)).collect(Collectors.groupingBy(Area::getType));
        redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(RedisConstant.RedisDBType.REDIS_SYS_DB.ordinal());
                StringBuffer key = new StringBuffer();
                maps.forEach((k,v)->{
                    key.setLength(0);
                    key.append(String.format(RedisConstant.SYS_AREA_TYPE,k.intValue()));
                    connection.del(key.toString().getBytes(StandardCharsets.UTF_8));//del
                    v.forEach(area->{
                        connection.zAdd(key.toString().getBytes(StandardCharsets.UTF_8),area.getId(),gsonRedisSerializer.serialize(area));
                    });
                });
                return null;
            }
        });

        // 加载街道(乡镇)的区域
        Map<Long,List<Area>> streetMaps = points.stream().filter(r->r.getType().equals(Area.TYPE_VALUE_TOWN)).collect(Collectors.groupingBy(r->r.getParent().getId()));
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(RedisConstant.RedisDBType.REDIS_SYS_DB.ordinal());
                StringBuffer key = new StringBuffer();
                streetMaps.forEach((k,v)->{
                    key.setLength(0);
                    key.append(String.format(RedisConstant.SYS_AREA_TYPE_TOWN,k.longValue()));
                    //System.out.println(key.toString());
                    connection.del(key.toString().getBytes(StandardCharsets.UTF_8));//del
                    v.forEach(area->{
                        connection.zAdd(key.toString().getBytes(StandardCharsets.UTF_8),area.getId(),gsonRedisSerializer.serialize(area));
                    });
                });
                return null;
            }
        });

        List<Area> provinceList = points.stream().filter(t->t.getType().equals(2)).collect(Collectors.toList());
        List<Area> cityList = points.stream().filter(t->t.getType().equals(3)).collect(Collectors.toList());
        List<Area> districtList = points.stream().filter(t->t.getType().equals(4)).collect(Collectors.toList());
        List<Area> streetList = points.stream().filter(t->t.getType().equals(5)).collect(Collectors.toList());

        //区域，用于地址自动适配
        // enable Redis Pipeline
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.select(RedisConstant.RedisDBType.REDIS_SYS_AREA.ordinal());
            connection.flushDb();
            provinceList.forEach(province -> {
                connection.set(String.format("%d:1:%s", province.getType(), province.getName().substring(0, 2)).getBytes(StandardCharsets.UTF_8), province.getId().toString().getBytes(StandardCharsets.UTF_8));
            });
            cityList.forEach(city -> {
                connection.set(String.format("%d:%d:%s", city.getType(), city.getParent().getId(), city.getName()).getBytes(StandardCharsets.UTF_8), city.getId().toString().getBytes(StandardCharsets.UTF_8));
            });
            districtList.forEach(district -> {
                //connection.set(String.format("%d:%d:%s", district.getType(), district.getParent().getId(), district.getName()).getBytes(StandardCharsets.UTF_8), String.format("%d=%s", district.getId(), district.getFullName()).getBytes(StandardCharsets.UTF_8));
                connection.set(String.format("%d:%d:%s", district.getType(), district.getParent().getId(), district.getName()).getBytes(StandardCharsets.UTF_8), String.format("%d=%s", district.getId(), district.getFullName()).getBytes(StandardCharsets.UTF_8));
            });
            streetList.forEach(street -> {
                connection.set(String.format("%d:%d:%s", street.getType(), street.getParent().getId(), street.getName()).getBytes(StandardCharsets.UTF_8), String.format("%d=%s", street.getId(), street.getFullName()).getBytes(StandardCharsets.UTF_8));
            });
            return null;
        });

        provinceList.clear();
        cityList.clear();
        districtList.clear();
        // mark on 2020-11-17 end
        */
        /*
        // mark on 2021-1-6
        System.out.println("2.数据字典");
        //hash,key-DICT:type:#type  field:value
        List<Dict> dicts = dictService.findAllList(new Dict());
        Map<String, List<Dict>> dictMap = dicts.stream().collect(Collectors.groupingBy(Dict::getType));
        StringBuffer sb = new StringBuffer(100);
        redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(RedisConstant.RedisDBType.REDIS_SYS_DB.ordinal());
                dictMap.forEach((k,v)->{
                    sb.setLength(0);
                    sb.append(String.format(RedisConstant.SYS_DICT_TYPE,k));
                    connection.del(sb.toString().getBytes(StandardCharsets.UTF_8));//delete
                    //set
                    v.forEach(item->{
                        connection.hSet(sb.toString().getBytes(StandardCharsets.UTF_8),item.getValue().getBytes(StandardCharsets.UTF_8),gsonRedisSerializer.serialize(item));
                    });
                });
                return null;
            }
        });
        */
        /*
        // mark on 2020-1-9
        System.out.println("3.区域半径");
        List<PlanRadius> planRadiusList = planRadiusService.findAllList();
        Map<Long,List<PlanRadius>> planRadiusMaps = planRadiusList.stream().collect(Collectors.groupingBy(r->{return r.getArea().getId() % PlanRadiusService.MOD_DIVISOR;}));
        redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(RedisConstant.RedisDBType.REDIS_SYS_AREA.ordinal());
                StringBuffer key = new StringBuffer();
                planRadiusMaps.forEach((k,v)->{
                    key.setLength(0);
                    key.append(MessageFormat.format(RedisConstant.MD_AREA_AUTO_PLAN_RADIUS,k.intValue()));
                    connection.del(key.toString().getBytes(StandardCharsets.UTF_8));//del
                    v.forEach(PlanRadius->{
                        connection.hSet(key.toString().getBytes(StandardCharsets.UTF_8),PlanRadius.getArea().getId().toString().getBytes(StandardCharsets.UTF_8),gsonRedisSerializer.serialize(PlanRadius));
                    });
                });
                return null;
            }
        });
        */

        System.out.println("4.用户区域");
        List<UserRegion> userRegionList = userRegionService.findAllList();
        redisTemplate.executePipelined(new RedisCallback<Object>() {    // enable Redis Pipeline
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(RedisConstant.RedisDBType.REDIS_SYS_DB.ordinal());
                StringBuffer key = new StringBuffer();
                key.setLength(0);
                key.append(RedisConstant.SYS_USER_REGION);
                connection.del(key.toString().getBytes(StandardCharsets.UTF_8));  //del
                if (!ObjectUtils.isEmpty(userRegionList)) {
                    Map<Long,List<UserRegion>> userRegionMap = userRegionList.stream().collect(Collectors.groupingBy(UserRegion::getUserId));
                    userRegionMap.forEach((k,v)->{
                        connection.hSet(key.toString().getBytes(StandardCharsets.UTF_8), k.toString().getBytes(StandardCharsets.UTF_8), gsonRedisSerializer.serialize(v));
                    });
                }
                return null;
            }
        });

        System.out.println("数据装载完成");
    }
}

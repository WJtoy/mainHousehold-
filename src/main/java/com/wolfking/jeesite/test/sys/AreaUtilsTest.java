package com.wolfking.jeesite.test.sys;

import com.google.common.collect.Sets;
import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.security.Digests;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.AreaService;
import com.wolfking.jeesite.modules.sys.service.OfficeService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ryan
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AreaUtilsTest {

    @SuppressWarnings("rawtypes")
    @Autowired
    public RedisTemplate redisTemplate;

    @Resource(name = "gsonRedisSerializer")
    public GsonRedisSerializer gsonRedisSerializer;

    @Autowired
    AreaService areaService;

    @Test
    public void testAreaUtilsParseAddress(){
        //String[] parseResult = AreaUtils.parseAddress("广东省深圳市龙华区龙观东路57号尚美时代");
        String[] parseResult = AreaUtils.parseAddress("黑龙江省鸡西市鸡冠区向阳街道园林小区正门进去右侧楼第四个门市房");
        if(parseResult != null && parseResult.length>0){
            for(int i=0,size=parseResult.length;i<size;i++){
                System.out.println(String.format("%d:%s",i,parseResult[i]));
            }
        }
    }

    /**
     * 根据地址获取经纬度
     */
    @Test
    public void testAreaUtilsGetLocation(){
        String[] parseResult = AreaUtils.getLocation("广东省 东莞市 石排镇 石排镇庙边王沙新三街16号新丰弹簧厂");
        if(parseResult != null && parseResult.length>0){
            for(int i=0,size=parseResult.length;i<size;i++){
                System.out.println(String.format("%d:%s",i,parseResult[i]));
            }
        }
    }

    @Test
    public void splitParentIds(){
        String parentIds = "0,1,26,321,";
        String[] ids = StringUtils.split(parentIds,",");
        for(int i=0,size=ids.length;i<size;i++){
            log.info("index:{} id:{}",i,ids[i]);
        }
    }

    @Test
    public void loadAreaCache(){
        List<Area> points = areaService.findAllList();
        Map<Integer,List<Area>> maps = points.stream().collect(Collectors.groupingBy(Area::getType));
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

        List<Area> provinceList = points.stream().filter(t->t.getType().equals(2)).collect(Collectors.toList());
        List<Area> cityList = points.stream().filter(t->t.getType().equals(3)).collect(Collectors.toList());
        List<Area> districtList = points.stream().filter(t->t.getType().equals(4)).collect(Collectors.toList());

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
            return null;
        });

        provinceList.clear();
        cityList.clear();
        districtList.clear();
    }
}

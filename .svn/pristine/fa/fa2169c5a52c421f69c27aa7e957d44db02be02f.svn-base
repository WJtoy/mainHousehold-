package com.wolfking.jeesite.test.md;

import com.wolfking.jeesite.common.config.redis.GsonRedisSerializer;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.md.entity.PlanRadius;
import com.wolfking.jeesite.modules.md.service.PlanRadiusService;
import com.wolfking.jeesite.modules.sys.entity.Area;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Ryan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AreaTest {

    @Autowired
    private PlanRadiusService planRadiusService;

    @Test
    public void JsonToArea(){
        StringBuilder json = new StringBuilder("[{&quot;id&quot;:&quot;2&quot;,&quot;type&quot;:2},{&quot;id&quot;:&quot;34&quot;,&quot;type&quot;:3},{&quot;id&quot;:&quot;534&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;535&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;536&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;537&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;538&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;539&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;540&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;541&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;542&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;543&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;544&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;545&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;546&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;547&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;548&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;549&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;550&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;551&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;3&quot;,&quot;type&quot;:2},{&quot;id&quot;:&quot;35&quot;,&quot;type&quot;:3},{&quot;id&quot;:&quot;565&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;566&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;567&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;568&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;569&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;570&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;571&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;572&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;573&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;574&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;575&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;576&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;577&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;578&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;579&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;580&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;581&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;582&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;583&quot;,&quot;type&quot;:4},{&quot;id&quot;:&quot;584&quot;,&quot;type&quot;:4}]");
        json = new StringBuilder(json.toString().replace("&quot;","\""));
        System.out.println("json:"+json.toString());

        GsonRedisSerializer gsonRedisSerializer = new GsonRedisSerializer(Area.class);

        Area[] areas = (Area[])gsonRedisSerializer.fromJson(json.toString(),Area[].class);
        //Arrays.asList(
//        List<Area> list = (List<Area>)gsonRedisSerializer.fromJson(json.toString(),Area[].class);
        System.out.println(areas.length);
        Area area;
        for(int i=0,size=areas.length;i<size;i++){
            area = areas[i];
            System.out.println(String.format("id:%s type:%s",area.getId(),area.getType()));
        }
    }

    @Test
    public void getPlanRadius(){
        long areaId = 3403l;
        try {
            PlanRadius planRadius = planRadiusService.getAreaIdFromCache(areaId);
            if (planRadius == null) {
                System.out.println("return null");
            } else {
                System.out.println(GsonUtils.getInstance().toGson(planRadius));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

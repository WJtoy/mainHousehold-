package com.wolfking.jeesite.test.ms.sys;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sys.SysUser;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试Sys模组微服务
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@SpringBootTest
public class SysTest {

    //@Autowired
    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private MapperFacade mapper;
    /**
     * 测试获得用户列表
     */
    @Test
    public void testGetUserList() throws ClassNotFoundException {
        Long userId = 49302l;//1862l;
        List<Long> userIds = Lists.newArrayList(userId);
        //String url = "http://120.79.102.58:8860/user/getMapByUserIds";
        String url = "http://192.168.0.110:8860//user/getMapByUserIds";
        /*1.postForEntity ok */
        ResponseEntity responseEntity = restTemplate.postForEntity(url, userIds,String.class);
        System.out.println(responseEntity.getBody());

        MSResponse<Map<String, SysUser>> response = GsonUtils.getInstance().fromJson(responseEntity.getBody().toString(),new MSResponse<Map<String, SysUser>>().getClass());
        Map<Long, User> userMap = null;
        if (MSResponse.isSuccess(response)) {
            //userMap = responseEntity.getData().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, i -> mapper.map(i.getValue(), User.class)));
            userMap = Maps.newHashMap();
            Map<String, SysUser> sysMap = response.getData();
            User user;
            SysUser sysUser;
            for(String key:sysMap.keySet()){
                sysUser = sysMap.get(key);
                user = mapper.map(sysUser,User.class);
                userMap.put(Long.valueOf(key),user);
            }
        }

        /*2.postForObject
        MSResponse<Map<String, SysUser>> responseEntity = restTemplate.postForObject(url,userIds,new MSResponse<Map<String, SysUser>>().getClass());
        System.out.println("json:"+ GsonUtils.getInstance().toGson(responseEntity));
        Map<Long, User> userMap = null;
        if (MSResponse.isSuccess(responseEntity)) {
            //userMap = responseEntity.getData().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, i -> mapper.map(i.getValue(), User.class)));
            userMap = Maps.newHashMap();
            Map<String, SysUser> sysMap = responseEntity.getData();
            User user;
            for(String key:sysMap.keySet()){
                user = mapper.map(sysMap.get(key),User.class);
                userMap.put(Long.valueOf(key),user);
            }
        }
        */
        /* ok */
        for(Long key:userMap.keySet()){
            System.out.print("id: " + key);
            //System.out.println(" json:" + GsonUtils.getInstance().toGson(userMap.get(key)));
            System.out.println(" mobile:" + userMap.get(key).getMobile());
        }
        //Assert.assertTrue("没有用户返回",userMap!=null && userMap.size()>0);
        //Assert.assertTrue("没有返回用户电弧", StringUtils.isNotBlank(userMap.get(userId).getMobile()));

    }

}

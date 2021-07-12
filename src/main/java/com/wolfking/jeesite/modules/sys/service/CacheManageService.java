package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * Created on 2017-05-05.
 */
@Service
public class CacheManageService extends BaseService {

    public <T> List<HashMap<String, Object>> queryCacheList() {
        java.util.List<HashMap<String, Object>> returnList= Lists.newArrayList();

        java.util.List<HashMap<String, Object>> mdItemList= Lists.newArrayList();

        //MD/基础资料
        HashMap<String,Object> group = Maps.newHashMap();
        group.put("id",2);
        group.put("name","基本资料");
        group.put("code","MD");
        returnList.add(group);

        HashMap<String,Object> map = Maps.newHashMap();
        map.put("id",2001);
        map.put("name","产品分类");
        map.put("code","productCategory");
        map.put("delete",0);
        map.put("reload",1);
        mdItemList.add(map);
        
        //map = Maps.newHashMap();
        //map.put("id",2002);
        //map.put("name","所有产品列表");
        //map.put("code","product");
        //map.put("delete",0);
        //map.put("reload",0);
        //map.put("type","");
        //mdItemList.add(map);

        //map = Maps.newHashMap();
        //map.put("id",2003);
        //map.put("name","所有套组产品列表");
        //map.put("code","productSet");
        //map.put("delete",0);
        //map.put("reload",0);
        //map.put("type","");
        //mdItemList.add(map);

        //map = Maps.newHashMap();
        //map.put("id",2004);
        //map.put("name","所有非套组产品列表");
        //map.put("code","productSingle");
        //map.put("delete",0);
        //map.put("reload",0);
        //map.put("type","");
        //mdItemList.add(map);

        map = Maps.newHashMap();
        map.put("id",2010);
        map.put("name","服务类型");
        map.put("code","serviceType");
        map.put("delete",0);
        map.put("reload",1);
        map.put("type","");
        mdItemList.add(map);

        map = Maps.newHashMap();
        map.put("id",2011);
        map.put("name","客评项目");
        map.put("code","grade");
        map.put("delete",0);
        map.put("reload",1);
        map.put("type","");
        mdItemList.add(map);

//        map = Maps.newHashMap();
//        map.put("id",2012);
//        map.put("name","所有网点");
//        map.put("code","servicePointAll");
//        map.put("delete",0);
//        map.put("reload",1);
//        map.put("type","");
//        mdItemList.add(map);

        map = Maps.newHashMap();
        map.put("id",2013);
        map.put("name","单个网点");
        map.put("code","servicePoint");
        map.put("delete",0);
        map.put("reload",1);
        map.put("type","input");
        mdItemList.add(map);

        group.put("itemList",mdItemList);

        //SD/订单
        group = Maps.newHashMap();
        group.put("id",3);
        group.put("name","订单");
        group.put("code","SD");
        returnList.add(group);

        mdItemList = Lists.newArrayList();

        map = Maps.newHashMap();
        map.put("id",3001);
        map.put("name","消息提醒");
        map.put("code","noticeMessage");
        map.put("delete",0);
        map.put("reload",1);
        mdItemList.add(map);

        map = Maps.newHashMap();
        map.put("id",3002);
        map.put("name","订单缓存");
        map.put("code","removeOrder");
        map.put("delete",1);
        map.put("reload",0);
        map.put("type","input");
        mdItemList.add(map);

        map = Maps.newHashMap();
        map.put("id",3003);
        map.put("name","3个月内重单缓存");
        map.put("code","orderRepeateCache");
        map.put("delete",0);
        map.put("reload",1);
        map.put("type","");
        mdItemList.add(map);

        map = Maps.newHashMap();
        map.put("id",3004);
        map.put("name","自动对账未处理订单转手动对账");
        map.put("code","updateToManualCharge");
        map.put("delete",1);
        map.put("reload",0);
        map.put("type","");
        mdItemList.add(map);

        group.put("itemList",mdItemList);


        return returnList;
    }
}

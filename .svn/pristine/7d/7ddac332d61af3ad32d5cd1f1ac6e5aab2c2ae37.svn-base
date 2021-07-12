package com.wolfking.jeesite.test.fi;

import com.google.common.collect.Maps;
import com.kkl.kklplus.utils.SimpleSignUtil;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.fi.entity.EngineerCurrency;
import com.wolfking.jeesite.modules.fi.service.ServicePointCurrencyService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class EngineerCurrencyTest {
    @Autowired
    private ServicePointCurrencyService servicePointCurrencyService;

//    @Test
//    public void UpdateEngineerCurrency(){
//        List<EngineerCurrency> engineerCurrencyList = servicePointCurrencyService.getModifyList();
//        for (EngineerCurrency engineerCurrency : engineerCurrencyList){
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("servicePointId", engineerCurrency.getServicePoint().getId());
//            map.put("amount", engineerCurrency.getAmount());
//            map.put("deleteCreateDate", engineerCurrency.getCreateDate());
//            map.put("deleteId", engineerCurrency.getId());
//            servicePointCurrencyService.updateCurrency(map);
//        }
//    }

    @Test
    public void insertNotExistCurrency(){
        servicePointCurrencyService.insertNotExistCurrency();
    }

    @Test
    public void testRechargeUtils(){
        Map<String,String> params = Maps.newHashMap();
        params.put("id","1147393544978305025");
        params.put("site","CW");
        params.put("referId","1584");
        params.put("referName","测试客户名称");
        params.put("tradeNo","RC201907270001");
        params.put("rechargeType","1");
        params.put("payType","1");
        params.put("amount","0.10");
        params.put("createAt",String.valueOf(new Date().getTime()));
        params.put("createById","1");
        params.put("createBy","账号1");
        params.put("timestamp", String.valueOf(DateUtils.addMinutes(new Date(),-1).getTime()));//时间戳
        String key = "011b712cb1a811e98df800163e048532";
        Map<String,String> resMap = SimpleSignUtil.buildRequestPara(params,key);
        System.out.println(GsonUtils.getInstance().toGson(resMap));
        if(SimpleSignUtil.verifySign(resMap,key)){
            System.out.println("verifySign ok");
        }else{
            System.out.println("verifySign fail");
        }
    }
}

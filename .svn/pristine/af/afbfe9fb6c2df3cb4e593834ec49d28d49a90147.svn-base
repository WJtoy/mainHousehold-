package com.wolfking.jeesite.test.api;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.modules.api.entity.sd.RestOrder;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestSaveAccessoryItemRequest;
import com.wolfking.jeesite.modules.api.entity.sd.request.RestSaveAccessoryRequest;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;

//@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class apiOrderTest {

    @Test
    public void jsonOfSaveAccessory(){
        RestSaveAccessoryRequest request = new RestSaveAccessoryRequest();
        request.setRemarks("故障说明");
        request.setApplyType(2);//厂家寄发
        request.setOrderId("908712425849888768");
        request.setQuarter("20173");

        //57
        //items
        List<RestSaveAccessoryItemRequest> items = Lists.newArrayList();

        RestSaveAccessoryItemRequest item = new RestSaveAccessoryItemRequest();
        //item.setProductId("75");//油烟机
        item.setMaterialId("57");//电容
        item.setPrice(0.0);
        item.setQty(1);
        items.add(item);

        item = new RestSaveAccessoryItemRequest();
        //item.setProductId("72");//燃气灶
        item.setMaterialId("15");//连接排线
        item.setPrice(5.0);
        item.setQty(2);
        items.add(item);

        request.setItems(items);

        //System.out.println(GsonUtils.getInstance().toGson(request));

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        //mapperFactory.classMap(RestSaveAccessoryRequest.class, RestSaveAccessoryRequest.class)
        //        .byDefault()
        //        .register();
        MapperFacade mapper = mapperFactory.getMapperFacade();
        RestSaveAccessoryRequest to = new RestSaveAccessoryRequest();
        mapper.map(request,to);
        System.out.println(GsonUtils.getInstance().toGson(to));
    }

    @Test
    public void JsonToRestOrder(){
        String json = "{\n  \"orderServiceType\" : 1,\n  \"quarter\" : \"20174\",\n  \"engineer\" : {\n    \"name\" : \"周承业\",\n    \"id\" : \"28245\"\n  },\n  \"acceptDate\" : \"2017-12-11-11:08:08\",\n  \"appointDate\" : 0,\n  \"serviceAddress\" : \"广东省 深圳市 龙华新区 测试地址\",\n  \"orderServiceTypeName\" : \"安装单\",\n  \"userName\" : \"王星瑞\",\n  \"servicePhone\" : \"13980583769\",\n  \"orderNo\" : \"K2017112400002\",\n  \"orderId\" : \"934051107959869440\",\n  \"remarks\" : \"\",\n  \"status\" : {\n    \"value\" : \"40\",\n    \"label\" : \"已派单\",\n    \"description\" : \"\"\n  }\n}";
        json = json.replace("\n","");
        RestOrder order = GsonUtils.getInstance().fromJson(json,RestOrder.class);
        Assert.assertNotNull(order);
        Assert.assertEquals(order.getOrderServiceType(),new Integer(1));
        Assert.assertEquals(order.getQuarter(),"quarter");
        Assert.assertEquals(order.getOrderId(),Long.valueOf("934051107959869440"));
        Assert.assertEquals(order.getOrderNo(),"K2017112400002");
        Assert.assertEquals(order.getStatus().getId(),new Long(40));

    }

    @Test
    public void JsonToSaveAccessory(){
        String json = "{\n  \"orderId\":\"908712425849888768\",\n  \"quarter\":\"20173\",\n  \"productId\":\"75\",\n  \"applyType\":\"2\",\n  \"remarks\":\"\\u6545\\u969c\\u8bf4\\u660e\",\n  \"items\":[{\n  \"materialId\":\"57\",\n  \"qty\":1,\"price\":0},{\"materialId\":\"15\",\"qty\":2,\"price\":5}]}";
        //json = json.replace("\n","");
        RestSaveAccessoryRequest order = GsonUtils.getInstance().fromJson(json,RestSaveAccessoryRequest.class);
        Assert.assertNotNull(order);
        Assert.assertEquals(order.getQuarter(),"20173");
        Assert.assertEquals(order.getOrderId(),"908712425849888768");
        Assert.assertEquals(order.getApplyType(),new Integer(2));
        Assert.assertEquals(order.getProductId(),"75");
        Assert.assertNotNull(order.getItems());
        Assert.assertEquals(order.getItems().size(),2);
        RestSaveAccessoryItemRequest item = order.getItems().get(0);

        Assert.assertEquals(item.getMaterialId(),"57");
        Assert.assertEquals(item.getQty(),new Integer(1));
        Assert.assertEquals(item.getPrice(),new Double(0));
        item = order.getItems().get(1);
        //Assert.assertEquals(item.getProductId(),"72");
        Assert.assertEquals(item.getMaterialId(),"15");
        Assert.assertEquals(item.getQty(),new Integer(2));
        Assert.assertEquals(item.getPrice(),new Double(5));
        //output
        System.out.println("orderId:" + order.getOrderId());
        System.out.println("quarter:" + order.getQuarter());
        System.out.println("applyType:" + order.getApplyType());
        System.out.println("product id:" + order.getProductId());
        System.out.println("remarks:" + order.getRemarks());
        //items
        System.out.println("items:");
        for(RestSaveAccessoryItemRequest im:order.getItems()){
            System.out.println(String.format(
                    "  product id:%s qty:%d price:%10.2f",
                    im.getMaterialId(),
                    im.getQty(),
                    im.getPrice()
                    )
            );
        }
    }
    /*
    @Test
    public void testDiffOfJsonObjectLibs(){
        JSONObject jo = new JSONObject();
        jo.put("userId", 1);
        jo.put("session", "ryan lu");
        log.info("net.sf.json.toString:{}",jo.toString());

        org.json.simple.JSONObject jsonObject = new org.json.simple.JSONObject();
        jsonObject.put("userId", 1);
        jsonObject.put("session", "ryan lu");
        log.info("org.json.simple.JSONObject.toJSONString:{}",jsonObject.toJSONString());
    }
    */

    @Test
    public void testJodaTime(){
        long millis = System.currentTimeMillis();
        System.out.println("millis:" + millis);
        DateTime date = new DateTime();
        DateTime appointmentDate = new DateTime(2020,6,9,17,0,0);
        System.out.println("appointmentDate: " + appointmentDate.toString("yyyy-MM-dd HH:mm:ss"));
        Interval interval = new Interval(date.minusHours(1), date.plusHours(48));
        System.out.println("date range:" + interval.getStart().toString("yyyy-MM-dd HH:mm:ss") + " ~ " + interval.getEnd().toString("yyyy-MM-dd HH:mm:ss"));
        if(interval.contains(appointmentDate)){
            System.out.println("预约日期在now()-1h ~ now()+48h 范围之内");
        }else{
            System.out.println("预约日期在now()-1h ~ now()+48h 范围之外");
        }
        DateTime conDate = new DateTime(2020,6,8,17,0,0);
        System.out.println(conDate.toString("yyyy-MM-dd HH:mm:ss"));
        if(interval.contains(conDate)){
            System.out.println("预约日期在now()-1h ~ now()+48h 范围之内");
        }else{
            System.out.println("预约日期在now()-1h ~ now()+48h 范围之外");
        }
        conDate = conDate.withDayOfMonth(11).withHourOfDay(11);
        System.out.println(conDate.toString("yyyy-MM-dd HH:mm:ss"));
        if(interval.contains(conDate)){
            System.out.println("预约日期在now()-1h ~ now()+48h 范围之内");
        }else{
            System.out.println("预约日期在now()-1h ~ now()+48h 范围之外");
        }

        if(appointmentDate.getHourOfDay() == 17){
            appointmentDate = appointmentDate.minusHours(1);
        }
        //appointmentDate = appointmentDate.plusMonths(5).plusDays(5);
        System.out.println("getMillis:" + appointmentDate.getMillis());
        StringBuffer sbAppointDate = new StringBuffer();
        sbAppointDate.append(appointmentDate.toString("M月d日", Locale.CHINESE));
        int hour = appointmentDate.getHourOfDay();
        System.out.println("hour:" + hour);
        if (hour < 13) {
            sbAppointDate.append(" 上午");
        } else if (hour < 18) {
            sbAppointDate.append(" 下午");
        } else {
            sbAppointDate.append(" 晚上");
        }
        System.out.println(sbAppointDate.toString());
    }
}

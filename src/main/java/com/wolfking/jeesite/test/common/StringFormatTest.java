package com.wolfking.jeesite.test.common;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sd.utils.OrderUtils;
import com.wolfking.jeesite.modules.sys.entity.Log;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.assertj.core.util.Lists;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yanshenglu
 * 测试String format
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class StringFormatTest {

    @Test
    public void testDate() throws ParseException {
        //Date startDate = DateUtils.getDate(2017,12,16);//时间是当前时间

        //Date startDate = DateUtils.parseDate("22017-12-16");
        //System.out.println(DateUtils.formatDateTime(startDate));
        //System.out.println("year:" + DateUtils.getYear(startDate));
        /*
        String year=String.format("%tY", startDate);
        String mon=String .format("%tm", startDate);
        String day=String .format("%td", startDate);
        System.out.println(year);
        System.out.println(mon);
        System.out.println(day);
        */
        //
        //Date endDate = DateUtils.parseDate("2017-12-16 23:59:59.999",new String[]{"yyyy-MM-dd HH:mm:ss.SSS"});
        //System.out.println(DateUtils.formatDateTime(endDate));

        //Long time = new Date().getTime();
        //Date date = DateUtils.longToDate(time);
        //System.out.println(DateUtils.formatDateTime(date));

        DateTime start = new DateTime(new Date().getTime());
        start = start.minusMonths(6);
        Date startDate = OrderUtils.getGoLiveDate();
        System.out.println("startDate:" + DateUtils.formatDateTime(startDate));
        if(start.toDate().after(startDate)){
            startDate = start.toDate();
        }
        System.out.println("startDate:" + DateUtils.formatDateTime(startDate));
    }

    /**
     * 测试字符串截取
     */
    @Test
    public void testStringUtilsLeft(){
        String str = "1234567890";
        //str.substring(0,15);//exception
        log.info(StringUtils.left(str,5));
        log.info(StringUtils.left(str,15));
    }

    @Test
    public void testGetDate() throws ParseException {
        //Date startDate = DateUtils.getDate(2017,12,16);//时间是当前时间

        //Date startDate = DateUtils.parseDate("2017-12-16");
        //System.out.println(DateUtils.formatDateTime(startDate));
        //
        //Date endDate = DateUtils.parseDate("2017-12-16 23:59:59.999",new String[]{"yyyy-MM-dd HH:mm:ss.SSS"});
        //System.out.println(DateUtils.formatDateTime(endDate));

        //Long time = new Date().getTime();
        //Date date = DateUtils.longToDate(time);
        //System.out.println(DateUtils.formatDateTime(date));

        Date date = DateUtils.getDate(DateUtils.addDays(new Date(),1),9,0,0);
        System.out.println(DateUtils.formatDate(date,"yyyy-MM-dd HH:mm:ss.SSS"));

    }

    @Test
    public void testFormat() {
        String str = String.format("修改订单，只要整数:%.0f 原金额:%.2f 现金额:%.2f 差异金额:%.2f", 58.56, 60.0, 40.0, -20.0);
        // --> 修改订单，原金额:60.00 现金额:40.00 差异金额:-20.00
        log.info(str);
    }

    @Test
    public void validatePassword() {
        String password = "222222";
        String encodepwd = "a45b02881d8d972bd353b23959b18d07841b5ddab24abea89d7ce018";
        Boolean isValide = SystemService.validatePassword(password, encodepwd);
        Assert.assertTrue("password is valid", isValide);
    }

    @Test
    public void substring() {
        String title = "123中国人";
        System.out.println(StringUtils.substring(title, 0, 3));

        String servicePointIds = "";
        if (StringUtils.isNotBlank(servicePointIds)) {
            if (!servicePointIds.startsWith(",")) {
                servicePointIds = ",".concat(servicePointIds);
            }
            if (!servicePointIds.endsWith(",")) {
                servicePointIds = servicePointIds.concat(",");
            }
        }
        System.out.println(servicePointIds);
    }

    @Test
    public void IntegerEquals() {
        Integer delFlag = 1;
        if (delFlag.intValue() == 1) {
            System.out.println("true");
        }
        if (delFlag.equals(1)) {
            System.out.println("true");
        }
    }

    @Test
    public void testChineseLength(){
        String chinese = "中国";
        System.out.println("size:" + chinese.length());

        String str = "ABC";
        System.out.println("size:" + str.length());
    }

    @Test
    public void DateFormat() {
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        try {
//            Date date = dateFormat.parse("2014-06-30 00:00:00");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        /*
        String date = DateUtils.formatDate(new Date(),"yyyyMM");
        System.out.println("date:" + date);
        */
        /*
        String date = DateUtils.formatDate(new Date(),"yyyyMMddHHmmss");
        System.out.println("date:" + date);

        */

        String newFileName = String.format("%s_%s.%s", DateUtils.formatDate(new Date(), "yyyyMMddHHmmss"), java.util.concurrent.ThreadLocalRandom.current().nextInt(1000), "txt");
        System.out.println("new file name:" + newFileName);

        /* getDateStart
        Date datestart =DateUtils.getDateStart(new Date());
        System.out.println("getDateStart:" + DateUtils.formatDate(datestart,"yyyy-MM-dd HH:mm:ss"));
        */

        /* getDateEnd
        Date dateend =DateUtils.getDateEnd(new Date());
        System.out.println("getDateEnd:" + DateUtils.formatDate(dateend,"yyyy-MM-dd HH:mm:ss.SSS"));

        if(DateUtils.isSameDate(datestart,dateend,"yyyy-MM-dd HH:mm:ss")){
            System.out.println("date is same");
        }
        */
        /*
        String time = DateUtils.formatDate(new Date(),"yyyy-MM-dd 08:00:00");
        try {
            Date date = DateUtils.parse(time, "yyyy-MM-dd HH:00:00");
            System.out.println(DateUtils.formatDate(date,"yyyy-MM-dd HH:mm:ss"));
        }catch (Exception e){
            e.printStackTrace();
        }
        */
        /*
        Timestamp timestamp = DateUtils.nowTimeStamp();
        System.out.println("timestamp:" + timestamp);
        String date = DateUtils.timestampToDate(timestamp);
        System.out.println("date:" + date);
        */

    }

    @Test
    public void TestSubstring() {
        /*
        StringBuffer sb = new StringBuffer("读取订单错误:");
        System.out.println("len:" + sb.length());//7

        sb.insert(0, "[").append("]");
        System.out.println(sb.toString());
        */

        /*
        String str = StringUtils.leftPad("",100,"1234567890");
        if(str.length()>60){
            System.out.println("读取订单错误:"+StringUtils.left(str,240));
        }else{
            System.out.println("读取订单错误:"+ str);
        }*/

        String orgStr = "石师傅,现有: 赵辉13920663603 河东区 大桥道萦东花园8号楼4门602九阳燃气灶1台 需要安装，请2小时内联系用户确认安维环境并预约上门时间，务必48小时内上门，严禁对产品作任何评价，带齐相应的工具和配件，现场有问题请联系客服林小姐0757-26169180/4006663653尊敬的用户，您好，您的售后工单，由于您的原因暂时无法上门，请您在时间方便时，自行联系师傅或客服预约上门时间等配件：尊敬的用户，您好，您的售后工单，由于需要等待商家寄发配件，请您在收到配件后，及时联系师傅或客服预约上门时间";
        System.out.println("length:" + orgStr.length());
        String content = StringUtils.left(orgStr,200);
        String other = StringUtils.substring(orgStr,200);
        System.out.println(content);
        System.out.println(other);
    }

    @Test
    public void TestStringEscapeUtils() {

        //转义
        System.out.println(StringEscapeUtils.escapeHtml4("<div></div>"));


        //反转义
        System.out.println(StringEscapeUtils.unescapeHtml4("<div></div>"));

        //反转义
        System.out.println(StringEscapeUtils.escapeHtml4("a\r\nb"));
    }

    @Test
    public void TestPageGson() {
        Page<Long> page = new Page(1, 10);
        page.setList(Lists.newArrayList(1l, 2l, 3l, 4l, 5l));
        String json = GsonUtils.getInstance().toGson(page);
        System.out.println(json);
    }

    @Test
    public void TestDate() {
        Date date = new Date();
        Date dtparse = new Date(date.getTime());
        System.out.println(DateUtils.formatDateTime(dtparse));
    }

    @Test
    public void testRegex() {
        ////String pattern = "(\\/userfiles\\/|\\/uploads\\/)+";
        ////String pattern = "(/userfiles/|/uploads/)+";
        //String pattern = "/userfiles/|/uploads/";
        //String str = "http://localhost:8080/uploads/123213.jpg";
        //Pattern r = Pattern.compile(pattern);
        //if(str.matches(pattern)){
        //    System.out.println("true");
        //}else{
        //    System.out.println("false");
        //}
        ////Matcher m = r.matcher(str);
        ////System.out.println(m.matches());

        String txt = "http://localhost:8080/userfiles/123213.jpg";

        String re1 = ".*?";    // Non-greedy match on filler
        String re2 = "(/uploads/|/userfiles/)";    // Word 1

        Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        System.out.println(m.find());
        //if (m.find())
        //{
        //    String word1=m.group(1);
        //    System.out.print("("+word1.toString()+")"+"\n");
        //}
    }

    @Test
    public void isImageRegex() {
        String reg = ".+(.JPEG|.JPG|.PNG|.GIF|.BMP|.TIF)$";
        String imgp = "Redocn_2012100818523401.png";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(imgp);
        System.out.println(matcher.find());
    }

    @Test
    public void escapeHtml() {
        System.out.println(Encodes.escapeHtml("https://m.v.qq.com/x/page/a/7/b/a0384v8zq7b.html?coverid=&favid=39"));
        System.out.println(Encodes.escapeXml("https://m.v.qq.com/x/page/a/7/b/a0384v8zq7b.html?coverid=&favid=39"));
    }

    @Test
    public void escapeJson() {
        String json = StringEscapeUtils.unescapeHtml4("tm\\\\")
                .replace("\"", "")
                .replace(":", "|")
                .replace("http|", "http:")
                .replace("https|", "https:")
                .replace("\\\\", "")
                .replace("\\", "");
        System.out.println(json);
    }

    /**
     * 比较Json
     */
    @Test
    public void differenceOfJsonString() {
        String json1 = "{\"name\":\"ABC\", \"city\":\"XYZ\", \"state\":\"CA\"}";
        String json2 = "{\"city\":\"XYZ\", \"state\":\"CA1\",\"street\":\"123 anyplace\", \"name\":\"ABC\"}";

        Gson g = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> firstMap = g.fromJson(json1, mapType);
        Map<String, Object> secondMap = g.fromJson(json2, mapType);
        //System.out.println(Maps.difference(firstMap, secondMap));
        MapDifference differenceMap = Maps.difference(firstMap, secondMap);
        boolean areEqual = differenceMap.areEqual();

        System.out.println(areEqual + "...........");

        if (!areEqual) {

            Map<String, Object> entriesDiffering = differenceMap.entriesDiffering();

            System.out.println("entriesDiffering:" + entriesDiffering.size());
            Object leftObject, rightObject;
            for (String key : entriesDiffering.keySet()) {

                MapDifference.ValueDifference maps = (MapDifference.ValueDifference) entriesDiffering.get(key);

                //OrderInfo leftOrder = maps.leftValue();
                //
                //OrderInfo rightOrder = maps.rightValue();
                //
                //if(!leftOrder.getOrderId() .equals( rightOrder.getOrderId())){
                //
                //    System.out.println("key= "+ key + " and value= " + leftOrder.toString()+ rightOrder.toString());
                //
                //}
                leftObject = maps.leftValue();
                rightObject = maps.rightValue();
                if (!leftObject.equals(rightObject)) {
                    System.out.println("key= " + key + " value=[ " + leftObject.toString() + "," + rightObject.toString() + "]");
                }
            }

            Map<Object, Object> entriesOnlyOnLeft = differenceMap.entriesOnlyOnLeft();

            System.out.println("entriesOnlyOnLeft:");

            for (Map.Entry<Object, Object> entry : entriesOnlyOnLeft.entrySet()) {

                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue().toString());

            }

            Map<Object, Object> entriesOnlyOnRight = differenceMap.entriesOnlyOnRight();

            System.out.println("entriesOnlyOnRight:");

            for (Map.Entry<Object, Object> entry : entriesOnlyOnRight.entrySet()) {

                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue().toString());

            }

            Map<Object, Object> entriesInCommon = differenceMap.entriesInCommon();

            System.out.println("entriesInCommon:" + entriesInCommon.size());

            for (Map.Entry<Object, Object> entry : entriesInCommon.entrySet()) {

                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue().toString());

            }

        }
    }

    @Test
    public void GsonUtilsDifference() {
        String json1 = "{\"name\":\"ABC\", \"city\":\"XYZ\", \"state\":\"CA\"}";
        String json2 = "{\"city\":\"XYZ\", \"state\":\"CA1\",\"street\":\"123 anyplace\", \"name\":\"ABC\"}";
        String difference = GsonUtils.difference(json1, json2,null);
        System.out.println(difference);
    }

    @Test
    public void testGuavaSpliter(){
        String str = "0-24- ";
        String str1 = " 0 ~ 24 - 36 ~";
        List<String> ranges = Splitter.onPattern("[~|-]")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(str);
        for(int i=0,size=ranges.size();i<size;i++){
            log.info("{}:{}",i,ranges.get(i));
        }
        List<String> ranges1 = Splitter.onPattern("[~|-]")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(str1);
        for(int i=0,size=ranges1.size();i<size;i++){
            log.info("{}:{}",i,ranges1.get(i));
        }
    }

    @Test
    public void testGuavaSpilter2(){
        //Splitter还有更强大的功能，做二次拆分，
        //这里二次拆分的意思是拆分两次，例如我们可以将a=b;c=d这样的字符串拆分成一个Map<String,String>
        String toSplitString = "a = b;c=d , e=f";
        Map<String,String> kvs = Splitter
                .onPattern("[,;]{1,}")
                .withKeyValueSeparator('=')
                .split(toSplitString);
        for (Map.Entry<String,String> entry : kvs.entrySet()) {
            log.info("{}={}", entry.getKey(),entry.getValue());
        }
    }

    @Test
    public void testGuavaJoin(){
        Set<String> ids = Sets.newHashSetWithExpectedSize(10);
        ids.add("123");
        ids.add("100");
        ids.add("256");
        System.out.println(Joiner.on(",").join(ids));
        System.out.println(String.format(",%s,",Joiner.on(",").join(ids)));

    }

    @Test
    public void tesytRandomUtils(){
        int val;
        for(int i=0;i<1000;i++){
            val = RandomUtils.nextInt(60,120);
            log.info("index:{} ,val:{}",i,val);
        }
    }

    @Test
    public void testStringUtils(){
        //去除字符串首尾的空白符(空白符主要包括' '，'\t'，'\r'，'\n'等等，
        // 具体的空白符可以参考Java API中Character类中isWhiteSpace()方法中的描述)：
        //System.out.println(StringUtils.strip(" \t\r\n abc    ")); // abc
        //System.out.println(StringUtils.strip("  郭小娇")); // abc
        System.out.println(StringUtils.toLong(null));
        System.out.println(StringUtils.toLong("undefined"));
        System.out.println(StringUtils.toLong("null"));
        System.out.println(StringUtils.toLong(""));
        System.out.println(StringUtils.toLong(123L));
        System.out.println(StringUtils.toInteger("undefined"));
        System.out.println(StringUtils.toInteger("null"));
        System.out.println(StringUtils.toInteger(""));
        System.out.println(StringUtils.toInteger(null));
        System.out.println(StringUtils.toInteger(12345768L));
    }

    @Test
    public void testDateUtilshourToDateTimeFormat(){
        System.out.println("6:"+DateUtils.minuteToTimeString(6,"时","分"));
        System.out.println("12:" + DateUtils.minuteToTimeString(12,"时","分"));
        System.out.println("25:" + DateUtils.minuteToTimeString(25,"时","分"));
        System.out.println("55:" + DateUtils.minuteToTimeString(55,"时","分"));
        System.out.println("60:" + DateUtils.minuteToTimeString(60,"时","分"));
        System.out.println("65:" + DateUtils.minuteToTimeString(65,"时","分"));
        System.out.println("90:" + DateUtils.minuteToTimeString(90,"时","分"));
        System.out.println("110:" + DateUtils.minuteToTimeString(110,"时","分"));
        System.out.println("120:" + DateUtils.minuteToTimeString(120,"时","分"));
        System.out.println("150:" + DateUtils.minuteToTimeString(150,"时","分"));
        System.out.println("350:" + DateUtils.minuteToTimeString(350,"时","分"));
    }
}
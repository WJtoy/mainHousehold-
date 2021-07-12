package com.wolfking.jeesite.test.common;

import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.ServicePoint;
import com.wolfking.jeesite.modules.sd.entity.OrderCondition;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;


/**
 * Created by yanshenglu
 * java8特性测试类
 */
@SpringBootTest
@Slf4j
public class Java8Test {

    /**
     * 如果serviceCategory.value 为空(null or empty),返回新的实例；
     * 否则返回本身
     */
    @Test
    public void testOptinal(){
        Dict serviceCategory = new Dict("1","安装");
        Dict dict = Optional.ofNullable(serviceCategory)
                .filter(t-> StringUtils.isNotBlank(t.getValue()))
                .orElseGet(() -> {
                    return new Dict("0","");
                });
        if(dict == null){
            log.error("return null object");
        }else{
            System.out.printf("return { value:%s ,label: %s }",dict.getValue(),dict.getLabel());
        }
    }

    @Test
    public void TestOptionalOrElse(){
        //order.getServicePoint().getArea().getName()
        OrderCondition order = new OrderCondition();
        ServicePoint servicePoint = new ServicePoint(1L);
        servicePoint.setName("网点名称");
        order.setServicePoint(servicePoint);
        Area area = new Area(1314L,"深圳市");
        servicePoint.setArea(area);
        String areaName = Optional.ofNullable(order)
                .map(c->c.getServicePoint())
                .map(r->r.getArea())
                .map(u->u.getName())
                .orElse("");
        if(areaName == null) {
            System.out.println("area Name: null");
        }else if (areaName== ""){
            System.out.println("area Name: empty");
        }else{
            System.out.println("area Name: " + areaName);
        }
    }

    public static void main(String[] args) {
        /* 1.Math.floorDiv()除运算，向下取整
        int size = 1;
        size = Math.floorDiv(size,2)+1;
        System.out.println("size:" + size);
         */
        /* 2.流调用前clear,流中取不到元素
        List<User> users = Lists.newArrayList(
                new User(1L,"Ryan"),
                new User(2L,"Mike"),
                new User(3L,"Jack")
        );
        Supplier<Stream<User>> supplier = () -> users.stream();
        users.clear();//将元素清除了，流中取不到
        supplier.get().forEach(
                s -> System.out.println("forEach: " + s.getLoginName())
        );
        */
        /* 3.测试Map.clear()
        List<User> users = Lists.newArrayList(
                new User(1L,"Ryan"),
                new User(2L,"Mike"),
                new User(3L,"Jack"),
                new User(4L,"Rose")
        );
        int size = Math.floorDiv(users.size(),2)+1;
        Map<Long,User> evenIdUsers = Maps.newHashMapWithExpectedSize(size);
        users.stream().filter(t->t.getId() % 2 == 0)
                .forEach(
                        u -> evenIdUsers.put(u.getId(),u)
                );
        users.clear();
        for(Map.Entry<Long,User> entry: evenIdUsers.entrySet()){
            System.out.println(entry.getKey().toString() + ": " + entry.getValue().getLoginName());
        }
        */
        /*
        List<User> users = Lists.newArrayList(
                new User(1L,"Ryan"),
                new User(2L,"Mike"),
                new User(3L,"Jack"),
                new User(4L,"Rose")
        );

        User user = users.get(0);
        user = null; //设为null不改变users里的元素
        user.setLoginName("Jone");//生效，改变users中元素
        for (User user1 : users) {
            if (user1 != null) {
                System.out.println(user1.getLoginName());
            }else{
                System.out.println("user is null");
            }
        }
        */

        /*
        Map<Long,User> userMap = Maps.newHashMapWithExpectedSize(4);
        userMap.put(1L, new User(1L,"Ryan"));
        userMap.put(2L,new User(2L,"Mike"));
        userMap.put(3L,new User(3L,"Jack"));
        userMap.put(4L,new User(4L,"Rose"));
        testUserMap(userMap);
        for(Map.Entry<Long,User> entry: userMap.entrySet()){
            System.out.println(entry.getKey().toString() + ": " + entry.getValue().getLoginName());
        }
        */

        DateTime startDate = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        System.out.println("date:" + startDate.toString("yyyy-MM-dd HH:mm:ss.SSS"));
        DateTime endDate = new DateTime().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
        System.out.println("date:" + endDate.toString("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public static void testUserMap(Map<Long,User> users){
        //users.clear();//生效
        users = null;// 不生效
    }

}

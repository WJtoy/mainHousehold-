package com.wolfking.jeesite.test.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sd.entity.LongTwoTuple;
import com.wolfking.jeesite.modules.sd.entity.MaterialMaster;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.common.utils.Collections3.distinctByKey;

//import com.wolfking.jeesite.common.utils.JedisTemplate;

/**
 * Created by yanshenglu on 2017/4/5.
 * 测试list 交/并/差集
 */
//@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ListTest {

    @Test
    public void testInner(){
        //交集
        List<String> ids = Lists.newArrayList();
        ids.add("1");
        ids.add("2");
        ids.addAll(Lists.newArrayList("1","3"));
        ids.stream().forEach(t->{
            System.out.println(t);
        });
    }

    @Test
    public void testSet(){
        Set<String> ids = Sets.newHashSet();
        ids.add("1");
        ids.add("2");

        ids = Sets.union(ids,Sets.newHashSet("3"));
        ids = Sets.union(ids,Sets.newHashSet("1"));
        ids = Sets.union(ids,Sets.newHashSet("4"));
        ids.stream().forEach(t->{
            System.out.println(t);
        });
    }

    @Test
    public void testSetDistinct(){
        Set<String> ids = Sets.newHashSet();
        ids.add(",1,");
        ids.add(",2,");
        ids.add(",1,");
        ids.add(",3,");
        ids.stream().forEach(t->{
            System.out.println(t);
        });
    }

    @Test
    public void testDiff(){
        //差集
        List<ServicePrice> prices = Lists.newArrayList();
        ServicePoint point = new ServicePoint();
        point.setId(1l);
        point.setName("网点1");

        ServicePrice p1 = new ServicePrice();
        p1.setServicePoint(point);

        p1.setServiceType(new ServiceType(1l,"Install","安装"));
        p1.setProduct(new Product(1l,"产品1"));
        p1.setPrice(45.0);
        p1.setDiscountPrice(30);

        prices.add(p1);
        //2
        ServicePrice p2 = new ServicePrice();
        p2.setServicePoint(point);

        p2.setServiceType(new ServiceType(2l,"Repair","维修"));
        p2.setProduct(new Product(2l,"产品2"));
        p2.setPrice(30.0);
        p2.setDiscountPrice(25.0);

        prices.add(p2);

        //another list
        List<ServicePrice> bprices = Lists.newArrayList();
        ServicePrice p3 = new ServicePrice();

        p3.setServiceType(new ServiceType(2l,"Repair","维修"));
        p3.setProduct(new Product(2l,"产品2"));
        p3.setPrice(0.0);
        p3.setDiscountPrice(0.0);

        bprices.add(p3);

        ServicePrice p4 = new ServicePrice();

        p4.setServiceType(new ServiceType(3l,"Repair","维修"));
        p4.setProduct(new Product(3l,"产品3"));
        p4.setPrice(55.0);
        p4.setDiscountPrice(45.0);
        bprices.add(p4);

        bprices.removeAll(prices);//差集

        prices.addAll(bprices);//并集

        for (ServicePrice p:prices) {
            System.out.println("sid:"+p.getServiceType().getId()+" pid:"+p.getProduct().getId() + " price:" + p.getPrice());
        }

    }


    @Test
    public void testListToMap(){
        //测试 List -> Map<ServiePoint,Map<Product,List<ServicePrice>>>
        List<ServicePrice> prices = Lists.newArrayList();
        ServicePoint point = new ServicePoint();
        point.setId(1l);
        point.setName("网点1");

        ServicePrice p1 = new ServicePrice();
        p1.setServicePoint(point);

        p1.setServiceType(new ServiceType(1l,"Install","安装"));
        p1.setProduct(new Product(1l,"产品1"));
        p1.setPrice(45.0);
        p1.setDiscountPrice(30);

        prices.add(p1);
        //2
        ServicePrice p2 = new ServicePrice();
        p2.setServicePoint(point);

        p2.setServiceType(new ServiceType(2l,"Repair","维修"));
        p2.setProduct(new Product(2l,"产品2"));
        p2.setPrice(30.0);
        p2.setDiscountPrice(25.0);

        prices.add(p2);

        /*网点2*/
        ServicePoint point2 = new ServicePoint();
        point2.setId(2l);
        point2.setName("网点2");
        ServicePrice p3 = new ServicePrice();
        p3.setServicePoint(point2);
        p3.setServiceType(new ServiceType(2l,"Repair","维修"));
        p3.setProduct(new Product(2l,"产品2"));
        p3.setPrice(0.0);
        p3.setDiscountPrice(0.0);

        prices.add(p3);

        ServicePrice p4 = new ServicePrice();
        p4.setServicePoint(point2);
        p4.setServiceType(new ServiceType(3l,"Repair","维修"));
        p4.setProduct(new Product(3l,"产品3"));
        p4.setPrice(55.0);
        p4.setDiscountPrice(45.0);
        prices.add(p4);

        Map<ServicePoint, Map<Product, List<ServicePrice>>> groupByPointThenProduct = prices.stream().collect(
                Collectors.groupingBy(ServicePrice::getServicePoint,Collectors.groupingBy(ServicePrice::getProduct)
        ));
        for (Map.Entry<ServicePoint, Map<Product, List<ServicePrice>>> entry:groupByPointThenProduct.entrySet()) {
            System.out.println("point:" + entry.getKey().getName());
            for (Map.Entry<Product,List<ServicePrice>> e: entry.getValue().entrySet()) {
                System.out.println("product:" + e.getKey().getName());
                for (ServicePrice p:e.getValue()) {
                    System.out.println("price- service type:" + p.getServiceType().getName() + " price:" + p.getPrice());
                }
            }

        }

    }


    @Test
    public void testListToList(){
        //List<CustomerPrice> -> List<Long> 返回不同客户id
        //OK
        List<CustomerPrice> list = Lists.newArrayList();
        //客户1-2
        CustomerPrice p1 = new CustomerPrice();
        p1.setCustomer(new Customer(1l));
        p1.setServiceType(new ServiceType(1l));
        p1.setProduct(new Product(1l));
        list.add(p1);

        CustomerPrice p2 = new CustomerPrice();
        p1.setCustomer(new Customer(1l));
        p1.setServiceType(new ServiceType(1l));
        p1.setProduct(new Product(2l));
        list.add(p1);

        //客户2-1
        p1 = new CustomerPrice();
        p1.setCustomer(new Customer(2l));
        p1.setServiceType(new ServiceType(2l));
        p1.setProduct(new Product(3l));
        list.add(p1);

        //客户3-1
        p1 = new CustomerPrice();
        p1.setCustomer(new Customer(3l));
        p1.setServiceType(new ServiceType(1l));
        p1.setProduct(new Product(2l));
        list.add(p1);

        List<Long> customerIds = list.stream()
                .collect(Collectors.groupingBy(CustomerPrice::getCustomer,Collectors.counting()))
                .keySet().stream().map(t->t.getId()).collect(Collectors.toList());

        Assert.assertFalse("无数据",customerIds==null || customerIds.size()==0);
        customerIds.forEach(t->System.out.println(t));

    }


    @Test
    public void testArrayUtilsContain(){
        int[] materialStatus = new int[] {2,3,4};
        /*
        Assert.assertTrue(ArrayUtils.contains(materialStatus,2));
        return;
        */

        List<MaterialMaster> masters = Lists.newArrayList();
        MaterialMaster master = new MaterialMaster();
        master.setOrderDetailId(0l);
        master.setProductId(1l);
        master.setStatus(new Dict("1","待确认"));
        master.setTotalPrice(10.0);
        masters.add(master);

        MaterialMaster master2 = new MaterialMaster();
        master2.setOrderDetailId(1l);
        master2.setProductId(1l);
        master2.setStatus(new Dict("2","待发货"));
        master2.setTotalPrice(20.0);
        masters.add(master2);

        MaterialMaster master3 = new MaterialMaster();
        master3.setOrderDetailId(0l);
        master3.setProductId(1l);
        master3.setStatus(new Dict("2","已发货"));
        master3.setTotalPrice(30.0);
        masters.add(master3);

        Long productId = 1l;
        Double materailAccount = 0.0d;
        materailAccount = masters.stream().filter(
                t ->    ArrayUtils.contains(materialStatus,Integer.parseInt(t.getStatus().getValue()))
                        && Objects.equals(t.getOrderDetailId(), 0l)
                        && Objects.equals(t.getProductId(), productId)
        )
                .collect(Collectors.summingDouble(MaterialMaster::getTotalPrice));

        System.out.println(materailAccount);
        Assert.assertTrue("不成功：等于0",materailAccount>0);

    }


    @Test
    public void testArrayUtils(){
        String[] ids = new String[]{"a","b","c"};
        ids = org.apache.commons.lang3.ArrayUtils.remove(ids, 0);
        for (String id:ids) {
            System.out.println(id);
        }
    }

    @Test
    public void testStreamDistinct(){
        //测试List去重
        Customer lokesh = new Customer(1l, "Lokesh");
        Customer brian = new Customer(2l, "Brian");
        Customer alex = new Customer(3l, "Alex");
        Collection<Customer> list = Arrays.asList(lokesh,brian,alex,lokesh,brian,lokesh);

        // Get distinct only
        List<Customer> distinctElements = list.stream().filter(distinctByKey(p -> p.getId())).collect(Collectors.toList());
        System.out.println(distinctElements);

    }

    /**
     * 测试找出List中重复元素
     */
    @Test
    public void testStreamGroup(){
        List<Integer> list = Lists.newArrayList(1,2,1,2,3,4);
        Map<Integer,Long> map = list.stream().collect(Collectors.groupingBy(Integer::intValue,Collectors.counting()));
        for(Map.Entry<Integer,Long> entry:map.entrySet()){
            System.out.println("key:" + entry.getKey() + " count:" + entry.getValue());
        }
    }
    /*
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }*/

    @Test
    public void testAppCompleteSet(){
        Set<String> appCompletSet = Sets.newHashSet("compeled_all","compeled_all_notest","compeled_maintain");
        if(appCompletSet.contains("compeled_ALL".toLowerCase().trim())) {
            log.info("contains:compeled_all");
        }else{
            log.error("not contain:compeled_all");
        }
    }

    @Test
    public void testBitActions(){
        /* fail
        int a = 1;
        int b = 2;
        System.out.println("1&2=" + (a&b));//0
        System.out.println("3|2=" + (3|2));//3
        */
        /* ok
        System.out.println(1<<0);//1*2y y=0 -> 1
        System.out.println(1<<1);//1*2y y=1 -> 2
        System.out.println(1<<2);//1*2y y=2 -> 4
        System.out.println(1<<3);//1*2y y=3 -> 8
        System.out.println(1<<4);//1*2y y=4 -> 16
        */
        List<Integer> types = Lists.newArrayList(1,2,3,5);
        int userTypes = 0;
        for(int i=0,size=types.size();i<size;i++){
            userTypes = userTypes + (1<<types.get(i));
        }
        System.out.println("values:" + userTypes);//2+4+8+32 = 46
    }

    @Test
    public void testListToMap2(){
        List<Long> users = Lists.newArrayList(
                1L,
                2L,
                3L,
                4L
        );
        Map<Long,String> userMap = users.stream().collect(Collectors.toMap(
                t -> t,
                t -> t.toString()
        ));
        for(Map.Entry<Long,String> entry:userMap.entrySet()){
            System.out.println("key:" + entry.getKey() + " vlaue:" + entry.getValue());
        }
    }

    @Test
    public void testListFilterAndMin(){
        Long exceptId = 1L;
        Integer minStatus = null;
        List<LongTwoTuple> complainStatus = Lists.newArrayList(
                new LongTwoTuple(1L,1L)
        );
        List<Long> statusRange = Lists.newArrayList(0L, 1L, 3L);
        minStatus = complainStatus.stream().filter(t -> statusRange.contains(t.getBElement()) && !t.getAElement().equals(exceptId)).map(t -> t.getBElement().intValue()).min(Integer::compareTo).orElse(null);
        System.out.println(MessageFormat.format("1.minStatus expect:-1 real: {0}",minStatus));
        complainStatus.add(new LongTwoTuple(2L,0L));
        minStatus = complainStatus.stream().filter(t -> statusRange.contains(t.getBElement()) && !t.getAElement().equals(exceptId)).map(t -> t.getBElement().intValue()).min(Integer::compareTo).orElse(null);
        System.out.println(MessageFormat.format("2.minStatus expect:0 real: {0}",minStatus));

        complainStatus  = Lists.newArrayList(
                new LongTwoTuple(1L,1L),
                new LongTwoTuple(2L,3L)
        );
        minStatus = complainStatus.stream().filter(t -> statusRange.contains(t.getBElement()) && !t.getAElement().equals(exceptId)).map(t -> t.getBElement().intValue()).min(Integer::compareTo).orElse(null);
        System.out.println(MessageFormat.format("3.minStatus expect:3 real: {0}",minStatus));

    }
}

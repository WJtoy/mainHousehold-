package com.wolfking.jeesite.test.common;

import com.google.common.collect.Lists;
import com.wolfking.jeesite.common.utils.QuarterUtils;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sys.entity.User;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 测试对象释放
 * @autor Ryan Lu
 * @date 2019/08/09
 */
@SpringBootTest
public class TestJVMGC {

    @Test
    public void testListClear() {
        User user = new User(0L,"管理员");
        System.out.println("user hashCode:" + user.hashCode());
        List<User> list = Lists.newArrayList();
        list.add(user);
        user = null;
        System.out.println("list hashCode:" + list.hashCode());
        //clear
        user = list.get(0);
        list.clear();
        System.out.println("=========== clear after =============");
        System.out.println("user hashCode:" + user.hashCode());
        System.out.println("list hashCode:" + list.hashCode());
        list = null;
        System.out.println("=========== null after =============");
        System.out.println("user hashCode:" + user.hashCode());
        // clear 之后,user 并没有释放
    }

    @Test
    public void testArray() {
        OrderSearchModel searchModel = new OrderSearchModel();
        System.out.println("hashCode:" + searchModel.hashCode());
        List<String> quarters = Lists.newArrayList("20191", "20192");
        searchModel.setQuarters(quarters);
        quarters = null;
        System.out.println("after hashCode:" + searchModel.hashCode());
        System.out.println("quarters:" + searchModel.getQuarters());
    }
}

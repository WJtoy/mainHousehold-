package com.wolfking.jeesite.test.sys;

//import com.sun.tools.javac.util.Assert;

import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.service.OfficeService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by ryan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OfficeTest {

    @Autowired
    private OfficeService officeService;
    @Autowired
    private SystemService systemService;

    @Autowired
    private RedisUtils redisUtils;


    @Test
    public void getKefuDepartment(){
        List<Dict> dicts = MSDictUtils.getDictList("KefuDeptCode");
        for(Dict dict:dicts) {
//            Office office = officeService.getByCode(dict.getValue());
//            System.out.println(String.format("id:%s code:%s name:%s",office.getId(),office.getCode(),office.getName()));
        }
    }

    @Test
    public void getSubOfficeOfKefu(){
        List<Dict> dicts = MSDictUtils.getDictList("KefuDeptCode");
        for(Dict dict:dicts) {
            System.out.println(String.format("=== Kefu:%s ===",dict.getValue()));
            /*
            List<Office> offices = officeService.getSubListByParentCode(dict.getValue());
            offices.stream().forEach(item->{
                System.out.println(String.format("id:%s code:%s name:%s",item.getId(),item.getCode(),item.getName()));
            });
            */
//            System.out.println(String.format("id:%s code:%s name:%s",office.getId(),office.getCode(),office.getName()));
        }
    }

    @Test
    public void getOfficeAccount(){
        /* 业务员
        List<User> users = systemService.findOfficeAccountList(38l,7);
        for (User user: users) {
            System.out.println("id:" + user.getId());
        }
        */
        /* 业务主管 */
        List<String> ids = UserUtils.getOfficeAccountIds(39l,7);
        for(String id :ids){
            System.out.println("id:" + id);
        }

    }

    @Test
    public void getOfficeLeader(){
        /*客服主管
        List<User> users = systemService.findOfficeLeaderList(38l,2,null);
        for (User user: users) {
            System.out.println("id:" + user.getId() +",name:" + user.getName());
        }
        */

        /*
        业务主管 */

        List<User> users = systemService.findOfficeLeaderList(36l,null,"salesleader");
        for (User user: users) {
            System.out.println("id:" + user.getId() + ",name:" + user.getName());
        }


    }


    @Test
    public void testKefuLeader(){
        /*User user = systemService.getUserFromDb(76l);
        if(user.isKefuLeader()){
            System.out.println("leader");
        }else{
            System.out.println("no");
        }*/

        List<Office> officeList = officeService.findList(true);
        System.out.println(officeList);

        officeList = officeService.findList(false);
        System.out.println(officeList);
    }


}

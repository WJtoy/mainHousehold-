package com.wolfking.jeesite.test.common;


import com.wolfking.jeesite.modules.sys.entity.Office;
import com.wolfking.jeesite.modules.sys.entity.User;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

//import com.wolfking.jeesite.common.utils.JedisTemplate;

/**
 * Bean转换测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BeanCopyTest {

    @Autowired
    private MapperFacade mapper;

    @Bean
    public MapperFactory getFactory(){
        return new DefaultMapperFactory.Builder().build();
    }

    @Test
    public void testSameClass(){
        //简单类型
        TestBean from = new TestBean();
        from.setId(1l);
        from.setName("银行");

        TestBean to = mapper.map(from,TestBean.class);
        System.out.println(String.format("from - id:%s name:%s",from.getId(),from.getName()));
        System.out.println(String.format("to - id:%s name:%s",to.getId(),to.getName()));
        Assert.assertTrue(from == to);
    }

    @Test
    public void testCopyUser(){
        User user = new User();
        user.setId(1l);
        user.setName("Ryan");
        Office office = new Office();
        office.setId(1l);
        office.setName("office");
        user.setOffice(office);

        //先注册以下映射方式
        getFactory().classMap(User.class, UserVO.class)
                .mapNulls(false)//设置正向空值不复制
                .mapNullsInReverse(false)//反向空值不复制
                .field("office.id","company.id").mapNulls(false).mapNullsInReverse(false)
                .field("office.name","company.name").mapNulls(false).mapNullsInReverse(false)
                //List, Array, Map类型的复制方式
                //.field("listProp3{}", "prop3{}").mapNulls(true)
                //不转换字段定义
                //.fieldMap("myproperty").exclude().add()
                .byDefault()//剩余的字段映射
                .register();
        UserVO uservo = getFactory().getMapperFacade().map(user, UserVO.class);
        //进行集合复制
        //List<CopyUser> copyUsers = mapperFactory.getMapperFacade().mapAsList(userList, CopyUser.class);
        System.out.println(
                String.format(
                        "id:%s ,name:%s ",
                        uservo.getId(),
                        uservo.getName()
                )
        );
    }

}

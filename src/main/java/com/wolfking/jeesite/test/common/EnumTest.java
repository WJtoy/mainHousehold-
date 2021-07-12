package com.wolfking.jeesite.test.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wolfking.jeesite.modules.md.entity.*;
import com.wolfking.jeesite.modules.sd.entity.MaterialMaster;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.common.utils.Collections3.distinctByKey;

//import com.wolfking.jeesite.common.utils.JedisTemplate;

/**
 * Created by yanshenglu on 2017/4/5.
 * 枚举测试
 */
//@RunWith(SpringRunner.class)
@SpringBootTest
public class EnumTest {

    public enum Color {
        RED("红色", 1), GREEN("绿色", 2), BLANK("白色", 3), YELLO("黄色", 4);
        // 成员变量
        public String name;
        public int index;
        // 构造方法
        private Color(String name, int index) {
            this.name = name;
            this.index = index;
        }
        //覆盖方法
        @Override
        public String toString() {
            return this.index+"_"+this.name;
        }
    }

    @Test
    public void test(){
        System.out.println(Color.GREEN.index + "," + Color.GREEN.name);
    }


}

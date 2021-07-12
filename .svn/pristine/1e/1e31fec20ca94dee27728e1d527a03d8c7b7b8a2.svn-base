package com.wolfking.jeesite.test.common;

import com.google.common.base.Splitter;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yanshenglu
 * 手机号验证
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class MobileCheckTest {

    @Test
    public void testCheck() throws ParseException {
        StringBuilder sbMobile = new StringBuilder(30);
        /* 中国电信号段 133、149、153、173、174、177、180、181、189、199
           中国联通号段 130、131、132、145、146、155、156、166、171、175、176、185、186
           中国移动号段 134(0-8)、135、136、137、138、139、147、148、150、151、152、157、158、159、165、172、178、182、183、184、187、188、198
           其他号段
                14号段以前为上网卡专属号段，如中国联通的是145，中国移动的是147等等。
           虚拟运营商
                电信：1700、1701、1702
                移动：1703、1705、1706
                联通：1704、1707、1708、1709
           卫星通信：1349
     */
        //电信 133、149、153、173、174、177、180、181、189、199
        System.out.println("电信 133、149、153、173、174、177、180、181、189、199");
        sbMobile.append("13312345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("14912345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15312345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("17312345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("17412345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("17712345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18012345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18112345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18912345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("19912345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        //联通 130、131、132、145、146、155、156、166、171、175、176、185、186
        System.out.println("联通 130、131、132、145、146、155、156、166、171、175、176、185、186");
        sbMobile.setLength(0);
        sbMobile.append("13012345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("13112345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("13212345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("14512345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("14612345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15512345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15612345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("16612345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());
        //171、175、176、185、186
        sbMobile.setLength(0);
        sbMobile.append("17112345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("17512345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("17612345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18512345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18612345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        //移动号段 134(0-8)、135、136、137、138、139、147、148、150、151、152、157、158、159、165、172、178、182、183、184、187、188、198
        System.out.println("移动号段 134(0-8)、135、136、137、138、139、147、148、150、151、152、157、158、159、165、172、178、182、183、184、187、188、198");

        sbMobile.setLength(0);
        sbMobile.append("13412345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("13512345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("13612345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("13712345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("13812345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("13912345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        //147、148、150、151、152、157、158、159
        sbMobile.setLength(0);
        sbMobile.append("14712345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("14812345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15012345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15112345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15212345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15712345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15812345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("15912345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        //165、172、178、182、183、184、187、188、198

        sbMobile.setLength(0);
        sbMobile.append("16512345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("17212345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("17812345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18212345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18312345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18412345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18712345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        sbMobile.setLength(0);
        sbMobile.append("18912345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());


        sbMobile.setLength(0);
        sbMobile.append("19812345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

        System.out.println("虚拟运营商 170");
        sbMobile.setLength(0);
        sbMobile.append("17012345678");
        sbMobile.append(":").append(StringUtils.isPhone(sbMobile.toString().replaceAll(":","")));
        System.out.println(sbMobile.toString());

    }

    @Test
    public void testRandomUtils(){
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
        System.out.println(String.valueOf(RandomUtils.nextInt(0,2)));
    }

}
package com.wolfking.jeesite.test.sys;

import com.wolfking.jeesite.common.utils.HttpClientUtils;
import com.kkl.kklplus.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SpringBootTest
public class HttpTest {

    @Test
    public void HttpTest() {
        try {
            String returnStr = "";
            StringBuffer sb = new StringBuffer("https://api.map.baidu.com/cloudgc/v1?geotable_id=179498&ak=mCsv4SGZGqZaNmnZZ7yXbpYWvgL4O5uh&address=");
            sb.append("浙江省 湖州市 安吉县 递铺镇玫瑰园6幢203".replace("#", "").

                    replace(",", "").

                    replace(" ", ""));
            URL url = new URL(sb.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Host", "api.map.baidu.com");
            if (200 == connection.getResponseCode())

            {
                //得到输入流
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while (-1 != (len = is.read(buffer))) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                returnStr = baos.toString("utf-8");
                String s = "";
            }
        }catch (Exception e){

        }
    }

    @Test
    public void cellPhoneTest(){
        System.out.println(StringUtils.getCellphone("罗彪 13152526838 0772-5376990 广西壮族自治区柳州市城中区城中街道三中路89号柳建大院18栋3单元旁独院 545001   油烟机单件安装单\n" +
                "b334 "));
        System.out.println(StringUtils.getCellphone("吴婵13885188883如何联系买家？\n" +
                "联系地址:贵州省贵阳市南明区花果园星苑湾\uD83C\uDDEF栋15楼1号    下单安装油烟机 "));
        System.out.println(StringUtils.getCellphone("刘莹 13521251798 北京，北京市，海淀区，苏家坨镇苏家坨 北京市海淀区苏家坨镇前沙涧村同泽园（西）里2号楼5单元201 油烟机 方讯 1  安装+拆机  用户已付安装费。辛苦上门前和客户核对安装环境及安装项目，如需额外购买其他配件需得到线上购买人同意方可，或联系商家客服18024172263协助处理 莹莹莹1798   81295926082990816\n" +
                "630电商"));
        System.out.println(StringUtils.getCellphone("刘园园,13603485022,河南省 开封市 金明区 集英街香缇湾38号楼,475002     安装烟机 灶具 热水器\n" +
                "b301"));
        System.out.println(StringUtils.getCellphone("邓小姐,13798611682,广东省 佛山市 南海区 狮山镇罗村花苑B区1号楼B座301房 已读 \n" +
                "烟灶热\n" +
                "907电商 "));

//        System.out.println(StringUtils.getChineseName("罗彪 13152526838 0772-5376990 广西壮族自治区柳州市城中区城中街道三中路89号柳建大院18栋3单元旁独院 545001   油烟机单件安装单\n" +
//                "b334 "));
//        System.out.println(StringUtils.getChineseName("吴婵13885188883如何联系买家？\n" +
//                "联系地址:贵州省贵阳市南明区花果园星苑湾\uD83C\uDDEF栋15楼1号    下单安装油烟机 "));
//        System.out.println(StringUtils.getChineseName("刘莹 13521251798 北京，北京市，海淀区，苏家坨镇苏家坨 北京市海淀区苏家坨镇前沙涧村同泽园（西）里2号楼5单元201 油烟机 方讯 1  安装+拆机  用户已付安装费。辛苦上门前和客户核对安装环境及安装项目，如需额外购买其他配件需得到线上购买人同意方可，或联系商家客服18024172263协助处理 莹莹莹1798   81295926082990816\n" +
//                "630电商"));
//        System.out.println(StringUtils.getChineseName("刘园园,13603485022,河南省 开封市 金明区 集英街香缇湾38号楼,475002     安装烟机 灶具 热水器\n" +
//                "b301"));
//        System.out.println(StringUtils.getChineseName("邓小姐,13798611682,广东省 佛山市 南海区 狮山镇罗村花苑B区1号楼B座301房 已读 \n" +
//                "烟灶热\n" +
//                "907电商 "));

        System.out.println(StringUtils.getChineseName(" 13152526838 0772-5376990 广西壮族自治区柳州市城中区城中街道三中路89号柳建大院18栋3单元旁独院 545001  罗彪 油烟机单件安装单"));
        System.out.println(StringUtils.getChineseName("13885188883 如何联系买家？联系地址:贵州省贵阳市南明区花果园星苑湾栋15楼1号    下单安装油烟机 吴婵"));
        System.out.println(StringUtils.getChineseName(" 13521251798刘莹 北京，北京市，海淀区，苏家坨镇苏家坨 北京市海淀区苏家坨镇前沙涧村同泽园（西）里2号楼5单元201 油烟机 方讯 1  安装+拆机  用户已付安装费。辛苦上门前和客户核对安装环境及安装项目，如需额外购买其他配件需得到线上购买人同意方可，或联系商家客服18024172263协助处理 莹莹莹1798   81295926082990816\n" +
                "630电商"));
        System.out.println(StringUtils.getChineseName("13603485022 刘园园,河南省 开封市 金明区 集英街香缇湾38号楼,475002     安装烟机 灶具 热水器"));
        System.out.println(StringUtils.getChineseName("13798611682,邓小姐,广东省 佛山市 南海区 狮山镇罗村花苑B区1号楼B座301房 已读"));
    }

    //测试超时
    @Test
    public void testTimeout() throws InterruptedException {
        HttpClientUtils.post("http://localhost:8080/td/requestTimeOut","","","",8000,18000);
        Thread.sleep(20000);
    }
}


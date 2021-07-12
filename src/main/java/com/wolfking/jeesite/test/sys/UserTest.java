package com.wolfking.jeesite.test.sys;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.security.Digests;
import com.wolfking.jeesite.common.utils.Encodes;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderSearchModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Role;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.UserRegion;
import com.wolfking.jeesite.modules.sys.service.OfficeService;
import com.wolfking.jeesite.modules.sys.service.SystemService;
import com.wolfking.jeesite.modules.sys.service.UserRegionService;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ryan
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserTest {

    @Autowired
    private OfficeService officeService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private UserRegionService userRegionService;


//    @Autowired
//    private RedisUtils redisUtils;

    /**
     * 测试密码是否正确
     * 具体代码在SystemService.validatePassword()
     */
    @Test
    public void checkPassword(){
        //String encodePassword = "";
        //byte[] salt = Encodes.decodeHex(encodePassword.substring(0,16));
        //Assert.assertEquals(encodePassword.substring(16),ByteSource.Util.bytes(salt));
        String plainPassword="admin";//明文密码
        String password = "8ab5e43888824bf95f33d403552e487dd675286c481894d83714c59f";
        String plain = Encodes.unescapeHtml(plainPassword);
        byte[] salt = Encodes.decodeHex(password.substring(0, 16));
        byte[] hashPassword = Digests.sha1(plain.getBytes(), salt, SystemService.HASH_INTERATIONS);
        Assert.assertEquals(password,Encodes.encodeHex(salt) + Encodes.encodeHex(hashPassword));
    }

    @Test
    public void testCheckMobile(){
        String mobile = "13002662373";
        List<User> users =  systemService.findByMobile(mobile.trim(),"",null);
        users.forEach(t->{
            System.out.println(
                    String.format("id:%s name:%s mobile:%s",t.getId(),t.getName(),t.getMobile())
            );
        });
    }


    /**
     * 测试根据角色id,分批获得用户Id
     */
    @Test
    public void testGetUserIdListOfRole(){
        Role role = new Role(1l);
        Page<Role> page = new Page<>(1, 1);
        List<Long> userIds;
        Page<Long> userPage = systemService.getUserIdListOfRole(page, role);
        System.out.println("page count:" + userPage.getPageCount());
        System.out.println("row count:" + userPage.getCount());
        if(userPage != null && userPage.getList() != null && userPage.getList().size()>0){
            System.out.println(">>>Page:1");
            userIds = userPage.getList();
            for(int i=0,size=userIds.size();i<size;i++){
                System.out.println("user id:" + userIds.get(i).toString());
            }
            //clearUserMenuCache(userIds);
            while(userPage.getPageCount()>userPage.getPageNo()){
                page.setPageNo(userPage.getPageNo()+1);
                System.out.println(">>>Page:" + page.getPageNo());
                userPage = systemService.getUserIdListOfRole(page, role);
                if(userPage != null && userPage.getList() != null && userPage.getList().size()>0) {
                    userIds = userPage.getList();
                    //clearUserMenuCache(userIds);
                    for(int i=0,size=userIds.size();i<size;i++){
                        System.out.println("user id:" + userIds.get(i).toString());
                    }
                }
            }
        }
    }

    @Test
    public void testGetUser(){
        User user = systemService.getUser(1l);
        System.out.println(GsonUtils.getInstance().toGson(user));
    }

    @Test
    public void diff(){
        StringBuffer sbjson = new StringBuffer("{\"id\":1,\"company\":{\"id\":1,\"name\":\"快可立全国联保\"},\"office\":{\"id\":39,\"name\":\"运营部\"},\"name\":\"系统管理员\",\"loginName\":\"admin\",\"password\":\"8ab5e43888824bf95f33d403552e487dd675286c481894d83714c59f\",\"mobile\":\"15209840493\",\"phone\":\"0354-5774551\",\"userType\":1,\"subFlag\":0,\"engineerId\":0,\"appLoged\":0,\"appFlag\":0,\"shortMessageFlag\":0,\"customerList\":[],\"roleList\":[{\"id\":1,\"name\":\"系统管理员\",\"enname\":\"admin\",\"roleType\":\"assignment\",\"dataScope\":1}]}");
        User user = GsonUtils.getInstance().fromJson(sbjson.toString(),User.class);
        //modify
        user.getCompany().setId(2L);
        user.getCompany().setName("广东快可立联保家电有限公司");
        //customerList
        Customer customer = new Customer();
        customer.setId(1l);
        customer.setName("0002优盟电器");
        user.getCustomerList().add(customer);
        String json = GsonUtils.toGsonString(user);
        Set<String> excludes = Sets.newHashSet("subFlag");
        String diff = GsonUtils.difference(sbjson.toString(),json,excludes);
        System.out.println(diff);
    }

    @Test
    public void testStaticMethods(){
        User user = new User(1l,"我是系统管理员管理员管理员管理员管理员管理员管理员管理员管理员管理员管理员","");
        UserUtils.substrUserName(user,10);
        System.out.println("user name:" + user.getName());
    }

    @Test
    public void createIMToken(){
        String token = UserUtils.getIMToken(1l,"fc82394781734ac6996f3d935ea133ee",1);
        System.out.println("token:" + token);
        String[] infos = UserUtils.parseIMUserInfo(token);
        System.out.println("infos:" + StringUtils.join(infos,","));

    }


    @Test
    public void testGetUserRegions(){
        User user = new User(49528L,"何嘉敏","");
        List<UserRegion> regions = userRegionService.getUserRegions(49528L);
        System.out.println("user resiongs:" + GsonUtils.getInstance().toGson(regions));
    }

    /**
     * 用户(客服，开发)新区域语句生成方法
     * 将json转insert语句
     */
    @Test
    public void manuMatainUserRegion() {
        long userId = 57434;
        String json = "[{\"areaId\":0,\"cityId\":0,\"provinceId\":\"13\",\"areaType\":2}]";
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = (JsonArray) jsonParser.parse(json);
        JsonObject regionObj;
        JsonElement jsonElement;
        StringBuilder sql = new StringBuilder(1000);
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonElement = jsonArray.get(i);
            regionObj= jsonElement.getAsJsonObject();
            //String name = featuresObj.get("name").toString();
            //String pinyin = featuresObj.get("pinyin").toString();
            sql.append(String.format("insert into sys_user_region (user_id,area_type,province_id,city_id,area_id) values (%d,%d,%d,%d,%d); \r\n",
                    userId,
                    regionObj.get("areaType").getAsInt(),
                    regionObj.get("provinceId").getAsLong(),
                    regionObj.get("cityId").getAsLong(),
                    regionObj.get("areaId").getAsLong()
                    )
            );
        }
        System.out.println(sql.toString());
    }

    @Test
    public void testcheckUserHasRegionPermission(){
        OrderSearchModel searchModel = new OrderSearchModel();
        searchModel.setProvinceList(Lists.newArrayList());
        searchModel.setCityList(Lists.newArrayList());
        searchModel.setAreaList(Lists.newArrayList());
        Area area = new Area();
        Integer areaLevel;
        Boolean checkResult;
        List<UserRegion> regions = Lists.newArrayListWithCapacity(100);
        UserRegion.UserRegionBuilder builder = UserRegion.builder();
        /* 1.省
        areaLevel = 0;
        searchModel.setAreaLevel(areaLevel);
        area.setId(13L);//广东省
        searchModel.setArea(area);
        //1.1.全国
        //regions.add(builder.areaType(1).build());
        //1.2.市
        //regions.add(builder.areaType(3).provinceId(13).cityId(174).build());
        //regions.add(builder.areaType(3).provinceId(13).cityId(175).build());
        //1.3.区
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1591).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1592).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1593).build());
        */

        /* 2.市
        areaLevel = 1;
        searchModel.setAreaLevel(areaLevel);
        area.setId(176L);//深圳市
        area.setParent(new Area(13L));//广东省
        searchModel.setArea(area);
        //全国
        //regions.add(builder.areaType(1).build());
        //全省
        regions.add(builder.areaType(2).provinceId(12).build());
        //regions.add(builder.areaType(2).provinceId(13).build());
        //市
        regions.add(builder.areaType(3).provinceId(13).cityId(174).build());
        regions.add(builder.areaType(3).provinceId(13).cityId(175).build());
        //regions.add(builder.areaType(3).provinceId(13).cityId(176).build());
        //区
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1591).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1592).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1593).build());
        */

        /* 3.区 */
        areaLevel = 2;
        searchModel.setAreaLevel(areaLevel);
        area.setId(1593L);//宝安区
        Area city = new Area(176L);//深圳市
        city.setParent(new Area(13L));//广东省
        area.setParent(city);
        searchModel.setArea(area);
        //全国
        //regions.add(builder.areaType(1).build());
        //全省
        regions.add(builder.areaType(2).provinceId(12).build());
        //regions.add(builder.areaType(2).provinceId(13).build());
        //市
        regions.add(builder.areaType(3).provinceId(13).cityId(174).build());
        regions.add(builder.areaType(3).provinceId(13).cityId(175).build());
        //regions.add(builder.areaType(3).provinceId(13).cityId(176).build());
        //区
        regions.add(builder.areaType(4).provinceId(13).cityId(177).areaId(1599).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1591).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1592).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1593).build());
        regions.add(builder.areaType(4).provinceId(13).cityId(176).areaId(1594).build());

        checkResult = checkUserHasRegionPermission(searchModel,regions,area,areaLevel);
        System.out.println(checkResult?"true":"false");
        System.out.println("search:" + searchModel.toString());
    }

    /**
     * 检查用户是否有目标区域的权限
     * 1.先检查是否有全国的权限
     * 2.再逐一检查是否有省市区/县的权限
     * @return boolean
     */
    private static boolean checkUserHasRegionPermission(OrderSearchModel searchModel, List<UserRegion> regions, Area area, Integer areaLevel){
        if(searchModel != null) {
            searchModel.setProvinceList(null);
            searchModel.setCityList(null);
            searchModel.setAreaList(null);
        }
        if(ObjectUtils.isEmpty(regions)){
            return false;
        }
        if(area == null || area.getId() == null || area.getId()<=0){
            return true;
        }
        Supplier<Stream<UserRegion>> streamSupplier = () -> regions.stream();
        UserRegion userRegion = streamSupplier.get()
                .filter(t->t.getAreaType() == 1)
                .findFirst()
                .orElse(null);
        //ALL(国家)
        if(userRegion != null){
            return true;
        }

        // 以下按选择等级判断
        long areaId = area.getId().longValue();
        boolean checkResult = true;
        //省
        switch (areaLevel){
            case 0://省
                userRegion = streamSupplier.get()
                        .filter(t->t.getAreaType() == 2 && t.getProvinceId() == areaId)
                        .findFirst()
                        .orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                // 1.上级
                // 国家，上面代码已判断
                // 2.下级
                // 2.1.是否有下属的市
                List<Long> cities = streamSupplier.get()
                        .filter(t->t.getAreaType() == 3 && t.getProvinceId() == areaId)
                        .map(t->t.getCityId())
                        .distinct()
                        .collect(Collectors.toList());
                if(!ObjectUtils.isEmpty(cities)){
                    if(searchModel != null){
                        searchModel.setCityList(cities);
                    }
                }
                //2.2.是否有下属的区县
                List<Long> areas = streamSupplier.get()
                        .filter(t->t.getAreaType() == 4 && t.getProvinceId() == areaId)
                        .map(t->t.getAreaId())
                        .distinct()
                        .collect(Collectors.toList());
                if(!ObjectUtils.isEmpty(areas)){
                    if(searchModel != null){
                        searchModel.setAreaList(areas);
                    }
                }
                checkResult = (searchModel.getCityList() != null || searchModel.getAreaList() != null);
                break;
            case 1://市
                userRegion = streamSupplier.get()
                        .filter(t->t.getAreaType() == 3 && t.getCityId() == areaId)
                        .findFirst()
                        .orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                // 1.上级
                // 1.1.省
                Area province = area.getParent();
                if(province == null || province.getId() == null || province.getId() <= 0){
                    checkResult = false;
                    break;
                }
                userRegion = streamSupplier.get()
                        .filter(t->t.getAreaType() == 2 && t.getProvinceId() == province.getId().longValue())
                        .findFirst()
                        .orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }

                // 2.下级
                // 2.1.区县
                List<Long> areas1 = streamSupplier.get()
                        .filter(t->t.getAreaType() == 4 && t.getCityId() == areaId)
                        .map(t->t.getAreaId())
                        .distinct()
                        .collect(Collectors.toList());
                if(ObjectUtils.isEmpty(areas1)){
                    checkResult = false;
                    break;
                }
                if(searchModel != null){
                    searchModel.setAreaList(areas1);
                }
                checkResult = true;
                break;
            case 2://区
                userRegion = streamSupplier.get().filter(t->t.getAreaType() == 4 && t.getAreaId() == areaId).findFirst().orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                // 1.上级
                // 1.1.市
                Area city3 = area.getParent();
                if(city3 == null || city3.getId() == null || city3.getId() <=0){
                    checkResult = false;
                    break;
                }
                userRegion = streamSupplier.get().filter(t->t.getAreaType() == 3 && t.getCityId() == city3.getId().longValue()).findFirst().orElse(null);
                if(userRegion != null){
                    checkResult = true;
                    break;
                }
                //省
                Area province3 = city3.getParent();
                if(province3 == null || province3.getId() == null || province3.getId() <=0){
                    checkResult = false;
                    break;
                }
                userRegion = streamSupplier.get().filter(t->t.getAreaType() == 2 && t.getProvinceId() == province3.getId().longValue()).findFirst().orElse(null);
                checkResult = userRegion != null;
                //2.下级
                // 无下级
                break;
            default:
                checkResult =  false;
                break;
        }
        return checkResult;
    }
}

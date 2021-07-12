package com.wolfking.jeesite.ms.tmall.md.utils;

import com.google.common.collect.Maps;
import com.kkl.kklplus.entity.common.MSBase;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.dao.CustomerDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.utils.UserUtils;
import com.wolfking.jeesite.ms.tmall.md.entity.B2bCustomerMap;
import com.wolfking.jeesite.ms.tmall.md.service.B2bCustomerMapService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import org.apache.commons.httpclient.NameValuePair;

import java.util.List;
import java.util.Map;

/**
 * B2B关联工具类
 */
public class B2BMapUtils {

    private static RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);
    private static B2bCustomerMapService customerMapService = SpringContextHolder.getBean(B2bCustomerMapService.class);

    /**
     * 为实体设置创建者、创建时间、更新者、更新时间
     */
    public static List<B2bCustomerMap> getAllShopList(int dataSource) {
        return customerMapService.getAllShopList(dataSource);
    }


    public static Map<Integer, Map<String, B2bCustomerMap>> getAllShopMap() {
        List<Dict> dataSources = MSDictUtils.getDictList("order_data_source");
        Map<Integer, Map<String, B2bCustomerMap>> resultMap = Maps.newConcurrentMap();
        if (dataSources != null && dataSources.size() > 0) {
            int dataSourceId = 0;
            Map<String, B2bCustomerMap> shopMap = null;
            for (Dict dataSource : dataSources) {
                dataSourceId = StringUtils.toInteger(dataSource.getValue());

                List<B2bCustomerMap> shopList = getAllShopList(dataSourceId);
                if (shopList != null && shopList.size() > 0) {
                    shopMap = Maps.newConcurrentMap();
                    for (B2bCustomerMap item : shopList) {
                        shopMap.put(item.getShopId(), item);
                    }
                    resultMap.put(dataSourceId, shopMap);
                }
            }
        }
        return resultMap;
    }

    public static B2bCustomerMap getShop(Integer dataSourceId, String shopId, Map<Integer, Map<String, B2bCustomerMap>> allShopMap) {
        B2bCustomerMap shop = null;
        if (dataSourceId != null && dataSourceId != 0 &&
                StringUtils.isNotEmpty(shopId) &&
                allShopMap != null && allShopMap.size()>0) {
            Map<String, B2bCustomerMap> shopMap = allShopMap.get(dataSourceId);
            if (shopMap != null && shopMap.size() > 0) {
                shop = shopMap.get(shopId);
            }
        }
        return shop;
    }

}

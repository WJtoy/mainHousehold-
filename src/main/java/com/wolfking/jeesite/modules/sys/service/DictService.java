/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.modules.sys.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.persistence.Page;
import com.wolfking.jeesite.common.service.LongIDCrudService;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.entity.md.RestCheckUpdate;
import com.wolfking.jeesite.modules.api.entity.md.RestDict;
import com.wolfking.jeesite.modules.api.entity.md.RestGetOptionList;
import com.wolfking.jeesite.modules.api.util.RestEnum;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import com.wolfking.jeesite.modules.sys.dao.DictDao;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.ms.service.sys.MSDictService;
import com.wolfking.jeesite.ms.utils.MSDictUtils;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wolfking.jeesite.common.config.redis.RedisConstant.SYS_DICT_TYPE;

/**
 * 字典Service
 *
 * @author ThinkGem
 * @version 2014-05-16
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
public class DictService extends LongIDCrudService<DictDao, Dict> {
    //	private static RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MapperFacade mapper;

    @Autowired
    private MSDictService msDictService;

    /**
     * 查询字段类型列表
     *
     * @return
     */
    public List<String> findTypeList() {
        return dao.findTypeList(new Dict());
    }

    @Transactional(readOnly = false)
    public void save(Dict dict) {
        super.save(dict);
        try {
            if (redisUtils.hexist(RedisConstant.RedisDBType.REDIS_SYS_DB, String.format(SYS_DICT_TYPE, dict.getType()), dict.getValue().toString())) {
                redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SYS_DB, String.format(SYS_DICT_TYPE, dict.getType()), dict.getValue().toString());
            }
            redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_SYS_DB, String.format(SYS_DICT_TYPE, dict.getType()), dict.getValue().toString(), dict, 0L);
        } catch (Exception e) {
            log.error("[DictService.save]{}", ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Transactional(readOnly = false)
    public void delete(Dict dict) {
        super.delete(dict);
        if (redisUtils.hexist(RedisConstant.RedisDBType.REDIS_SYS_DB, String.format(SYS_DICT_TYPE, dict.getType()), dict.getValue())) {
            redisUtils.hdel(RedisConstant.RedisDBType.REDIS_SYS_DB, String.format(SYS_DICT_TYPE, dict.getType()), dict.getValue());
        }
//		CacheUtils.remove(DictUtils.CACHE_DICT_MAP);
    }

//	public Page<Dict> findPaymentType(Page<Dict> page, Dict dict, Integer aloneManagement) {
//		dict.setPage(page);
//		dict.setType("PaymentType");
//		//dict.setAloneManagement(aloneManagement);
//		page.setList(super.findList(dict));
//		return page;
//	}

    public Page<Dict> findList(Page<Dict> page, Dict dict, Integer aloneManagement) {
        dict.setPage(page);
        page.setList(super.findList(dict));
        return page;
    }

    /**
     * 从缓存读取数据字典信息，当缓存未命中则从数据库装载至缓存
     *
     * @param type
     * @return
     */
    public List<Dict> getDictListByIdFromCache(String type) {
        String key = String.format(SYS_DICT_TYPE, type);
        List<Dict> values = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        Map<String, byte[]> maps = redisUtils.hGetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key);
        if (maps != null && maps.size() > 0) {
            for (Map.Entry<String, byte[]> entry : maps.entrySet()) {
                Dict dict = (Dict) redisUtils.gsonRedisSerializer.fromJson(StringUtils.toString(entry.getValue()), Dict.class);
                values.add(dict);
            }
        }
        if (values == null || values.size() == 0) {
            Dict dict = new Dict();
            dict.setType(type);
            values = dao.findList(dict);
            if (values != null) {
                Map<String, Object> hashmaps = Maps.newHashMap();
                for (Dict d : values) {
                    hashmaps.put(d.getValue(), d);
                }
                redisUtils.hmSetAll(RedisConstant.RedisDBType.REDIS_SYS_DB, key, hashmaps, 0l);
            }
        }
        return values.stream().sorted(Comparator.comparing(Dict::getSort)).collect(Collectors.toList());
    }

    //region api functions

    /**
     * 检查更新
     *
     * @param checkUpdate
     * @return
     */
    public RestResult<Object> checkUpdate(RestCheckUpdate checkUpdate) {
//        String typeString = "";
        StringBuilder typeString = new StringBuilder();
        if (checkUpdate.getPhoneType() == RestEnum.PhoneType.iPhone.ordinal()) {
//            typeString = "IOSAppVersion";
            typeString.append("IOSAppVersion");
        } else if (checkUpdate.getPhoneType() == RestEnum.PhoneType.AndroidPhone.ordinal()) {
//            typeString = "AndroidAppVersion";
            typeString.append("AndroidAppVersion");
        }
        if (StringUtils.isNotBlank(checkUpdate.getApp())) {
            typeString.append(checkUpdate.getApp().trim());
        }
        //切换为微服务
//        List<Dict> dictList = msDictService.findListByType(typeString);
        List<Dict> dictList = msDictService.findListByType(typeString.toString());
        if (dictList != null && dictList.size() > 0) {
            Dict dict = dictList.get(0);
//			dict.setValue((String) redisUtils.get(RedisConstant.RedisDBType.REDIS_SEQ_DB, typeString, String.class));
            if (dict != null) {
                //版本比较
                String receivedVersion[] = checkUpdate.getVersion().split("\\.");
                String dbVersion[] = dict.getValue().split("\\.");
                Boolean needUpdate = false;

                for (int i = 0; i < (dbVersion.length < receivedVersion.length ? dbVersion.length : receivedVersion.length); i++) {
                    if (Integer.parseInt(dbVersion[i]) > Integer.parseInt(receivedVersion[i])) {
                        needUpdate = true;
                        break;
                    }
                    if (Integer.parseInt(dbVersion[i]) < Integer.parseInt(receivedVersion[i])) {
                        break;
                    }
                }

                if (needUpdate) {
                    Dict appForceUpdateFlagDict = MSDictUtils.getDictByValue("1", Dict.DICT_TYPE_APP_FORCE_UPDATE_FLAG);
                    int appForceUpdateFlag = appForceUpdateFlagDict != null && appForceUpdateFlagDict.getIntValue() > 0 ? 1 : 0;
                    JsonObject jo = new JsonObject();
                    jo.addProperty("version", dict.getValue());
                    jo.addProperty("updateTime", DateUtils.formatDate(dict.getUpdateDate()));
                    jo.addProperty("updateLog", dict.getDescription());
                    jo.addProperty("updateURL", dict.getRemarks());
                    jo.addProperty("appForceUpdateFlag", appForceUpdateFlag);
                    return RestResultGenerator.success(jo);
                }
            }
        }
        return RestResultGenerator.success();
    }

    /**
     * 根据type获取数据字典列表
     * 切换为微服务
     *
     * @param getOptionList
     * @return
     */
    public RestResult<Object> getDictListByType(RestGetOptionList getOptionList) {
        String typeString = "";
        if (getOptionList.getType() == 0) {
            //切换为微服务
            typeString = "completed_type";
        } else if (getOptionList.getType() == 1) {
            //切换为微服务
            typeString = "PendingType";
        }
        //切换为微服务
        else if (getOptionList.getType() == 2) {
            typeString = "order_abnormal_reason";
        }
        //切换为微服务
        else if (getOptionList.getType() == 3) {
            typeString = "material_apply_type";
        }
        //切换为微服务
        else if (getOptionList.getType() == 4) {
            typeString = "express_type";
        }
        else if (getOptionList.getType() == 5) {
            typeString = Dict.DICT_TYPE_BANK_TYPE;
        }
        List<Dict> dictList = Lists.newArrayList();
        if (getOptionList.getType() != 1) {
            dictList = MSDictUtils.getDictList(typeString);
        } else {
            dictList = MSDictUtils.getDictExceptList(typeString, "7"); //APP端不显示待跟进
        }

        List<RestDict> restDictList = Lists.newArrayList();
        if (dictList != null && dictList.size() > 0) {
            restDictList = mapper.mapAsList(dictList, RestDict.class);
        }
        return RestResultGenerator.success(restDictList);
    }
    //endregion api functions
}

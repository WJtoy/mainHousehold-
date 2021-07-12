package com.wolfking.jeesite.ms.providerrpt.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wolfking.jeesite.common.config.redis.GsonIgnoreStrategy;
import com.wolfking.jeesite.common.mapper.adapters.StringConverter;
import com.kkl.kklplus.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisGsonUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisGsonUtils.class);
    private static Gson gson = (new GsonBuilder()).serializeNulls().registerTypeAdapter(String.class, new StringConverter()).disableHtmlEscaping().addSerializationExclusionStrategy(new GsonIgnoreStrategy()).create();

    public static <T> String toJson(T obj) {
        String json = "";
        if (obj != null) {
            try {
                json = gson.toJson(obj);
            } catch (Exception e) {
                log.error("[RedisGsonUtils.toJson]", e);
            }
        }

        return json;
    }


    public <T> T fromJson(String json, Class<T> clazz) {
        T objOfT = null;
        if (StringUtils.isNotBlank(json) && clazz != null) {
            try {
                objOfT = gson.fromJson(json, clazz);
            } catch (Exception e) {
                log.error("[RedisGsonUtils.fromJson]", e);
            }
        }
        return objOfT;
    }

}

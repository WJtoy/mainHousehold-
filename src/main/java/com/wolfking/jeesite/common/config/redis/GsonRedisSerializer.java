package com.wolfking.jeesite.common.config.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.wolfking.jeesite.common.mapper.adapters.StringConverter;
import com.kkl.kklplus.utils.StringUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by yanshenglu on 2017/4/10.
 */
public class GsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private Gson gson;
    private Class<T> clazz;

    public Gson getGson(){
        return gson;
    }

    public GsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
        gson =new GsonBuilder()
                //序列化null
                .serializeNulls()
                //null <-> String
                .registerTypeAdapter(String.class, new StringConverter())
                //禁止转义html标签
                .disableHtmlEscaping()
                //.excludeFieldsWithoutExposeAnnotation() // <---
                .addSerializationExclusionStrategy(new GsonIgnoreStrategy())
                .create();
    }

    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return gson.toJson(t).getBytes();
    }

    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        return (T) gson.fromJson(str,clazz);
    }

    public T deserialize(byte[] bytes,Class<T> clazz) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
//        return (T) new Gson().fromJson(str,clazz);
        return (T) gson.fromJson(str,clazz);
    }

    public T deserialize(byte[] bytes,TypeAdapter adapter) throws SerializationException,IOException {
        if (bytes == null || bytes.length <= 0 || adapter == null) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        //return (T) gson.fromJson(str,clazz);
        return (T) adapter.fromJson(str);
    }

    public String toJson(T t) throws SerializationException {
        if (t == null) {
            return "";
        }
        return gson.toJson(t);
    }

    public T fromJson(String json,Class<T> clazz) throws SerializationException {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return (T)gson.fromJson(json,clazz);
    }

}

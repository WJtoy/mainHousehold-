package com.wolfking.jeesite.modules.api.config;

public class Constant {
    /**
     * 数据请求返回码
     */
//    public static final int RESCODE_SUCCESS = 1000;				//成功
//    public static final int RESCODE_SUCCESS_MSG = 1001;			//成功(有返回信息)
    public static final int RESCODE_EXCEPTION = 1002;			//请求抛出异常
    public static final int RESCODE_NOLOGIN = 1003;				//未登陆状态
    public static final int RESCODE_NOEXIST = 1004;				//查询结果为空
    public static final int RESCODE_NOAUTH = 1005;				//无操作权限

    /**
     * jwt
     */
    public static final String JWT_AuthHeaderPrefix = "KKLAPP";
    public static final String JWT_ID = "kkl_app";
    public static final String JWT_SECRET = "3fabff41c36811e7a5ca00163e087291";//密钥
    public static final long JWT_TTL = 5*24*60*60*1000;  //登录的验证码过期时间（毫秒）5天
    public static final long JWT_REFRESH_INTERVAL = 55*60*1000;  //millisecond
    public static final long JWT_REFRESH_TTL = 12*60*60*1000;  //millisecond

    /**
     * im
     */
    public static final String JWT_IM_ID = "kkl.im";
}

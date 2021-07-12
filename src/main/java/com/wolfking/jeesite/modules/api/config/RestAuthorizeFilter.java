package com.wolfking.jeesite.modules.api.config;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.GsonUtils;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.api.entity.md.RestSession;
import com.wolfking.jeesite.modules.api.util.ErrorCode;
import com.wolfking.jeesite.modules.api.util.JwtUtil;
import com.wolfking.jeesite.modules.api.util.RestResult;
import com.wolfking.jeesite.modules.api.util.RestResultGenerator;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Rest安全验证过滤器
 * 过滤地址:/api/*
 * 登录地址除外(/api/security/login)
 */
@Component
public class RestAuthorizeFilter extends GenericFilterBean {

    private final RedisUtils redisUtils;


    public RestAuthorizeFilter(RedisUtils redisUtils)
    {
        this.redisUtils = redisUtils;
    }

    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException,RuntimeException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(req, res);
        } else {
            if(request.getRequestURI().toLowerCase().equals("/api/security/login") ||
                    request.getRequestURI().toLowerCase().equals("/api/common/checkupdate") ||
                    request.getRequestURI().toLowerCase().equals("/api/common/getverifycode")||
                    request.getRequestURI().toLowerCase().equals("/api/security/resetpassword")){
                chain.doFilter(req, res);
            }else {
                RestResult result = checkHTTPBasicAuthorize(request);
                if(result.getCode() == ErrorCode.NO_ERROR.code) {
                    chain.doFilter(req, res);
                }else{
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json; charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_OK);//SC_UNAUTHORIZED
                    response.getWriter().write(GsonUtils.getInstance().toGson(result));
                    return;
                }
            }
        }
    }

    private RestResult checkHTTPBasicAuthorize(HttpServletRequest request)
    {
        try
        {
            String authHeader = request.getHeader("Authorization");
            if ((authHeader != null) && (authHeader.length() > Constant.JWT_AuthHeaderPrefix.length()+1))
            {
                final String token = authHeader.substring(Constant.JWT_AuthHeaderPrefix.length() + 1);
                try {
                    final Claims claims = JwtUtil.parseJWT(token);
                    if(claims.isEmpty() || !claims.containsKey("jti") || !claims.containsKey("sub") || !claims.containsKey("exp")){
                        return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：认证错误");
                    }
                    String strValue = new String();
                    //jwt id
                    strValue = claims.getId();
                    if(!strValue.equalsIgnoreCase(Constant.JWT_ID)){
                        return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：认证错误");
                    }
                    //iat(Issue At):签发时间
                    //exp:过期时间
                    if(claims.getExpiration().before(new Date())){
                        return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"认证已过期");
                    }
                    strValue = claims.getSubject();
                    if(StringUtils.isBlank(strValue)){
                        return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：认证错误");
                    }
                    try {
                        RestSession session = GsonUtils.getInstance().fromJson(strValue, RestSession.class);
                        if(StringUtils.isBlank(session.getSession()) || StringUtils.isBlank(session.getUserId())){
                            return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：认证错误");
                        }
                        String key = String.format(RedisConstant.APP_SESSION, session.getUserId());
                        String sessionValue  = redisUtils.hGet(RedisConstant.RedisDBType.REDIS_NEW_APP_DB,key,"session",String.class);
                        if(StringUtils.isBlank(sessionValue)){
                            return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"未登录或登录过期");
                        }
                        if(!sessionValue.equalsIgnoreCase(session.getSession())){
                            return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"帐号已在其他设备上登录");
                        }
                        request.setAttribute("sessionUserId", session.getUserId());
                    }catch (Exception e){
                        return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：认证错误");
                    }
                    return RestResultGenerator.success("OK");
                }catch (ExpiredJwtException e){
                    return RestResultGenerator.custom(ErrorCode.MEMBER_SESSION_TIMEOUT.code,ErrorCode.MEMBER_SESSION_TIMEOUT.message);
                }catch (SignatureException e){
                    return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"认证签名错误");
                }catch (UnsupportedJwtException | MalformedJwtException e){
                    return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"认证不支持或信息完整");
                }catch (IllegalArgumentException e) {
                    return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"认证参数不正确");
                }catch (Exception e) {
                    return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：认证错误");
                }
            }
            return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：认证错误");
        }
        catch(Exception ex)
        {
            return RestResultGenerator.custom(ErrorCode.INVALID_TOKEN.code,"非法请求：无认证码");
        }

    }
}

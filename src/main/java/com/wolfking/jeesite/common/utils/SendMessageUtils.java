/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.wolfking.jeesite.common.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;

import com.google.common.collect.Maps;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;
import com.wolfking.jeesite.modules.td.entity.Message2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 
 * @author Kody huang
 * @version 2015-10-13
 */
@Service
@Slf4j
public class SendMessageUtils {

	/**
	 * 旧版

	public static String  SendMessage2(Message2 msg)  throws IOException {
		try {

			//发送内容
			//String content = "KKLGO示例测试";
//			String sign = Global.getConfig("MsgSign");
			// 创建StringBuffer对象用来操作字符串
			StringBuffer sb = new StringBuffer(Global.getConfig("shortmessage.url") + "?");

			// 向StringBuffer追加用户名
			sb.append("name=").append(Global.getConfig("shortmessage.name"));

			// 向StringBuffer追加密码（登陆网页版，在管理中心--基本资料--接口密码，是28位的）
			sb.append("&pwd=").append(Global.getConfig("shortmessage.password"));
//			Global.getConfig("MsgPassword"));

			// 向StringBuffer追加手机号码
			sb.append("&mobile=").append(msg.getMobile());
			//sb.append("&mobile=13421351106");

			// 向StringBuffer追加消息内容转URL标准码
			sb.append("&content=").append(URLEncoder.encode(msg.getContent(), "UTF-8"));

			//追加发送时间，可为空，为空为及时发送
			sb.append("&stime=");

			//加签名
//			sb.append("&sign="+URLEncoder.encode(sign,"UTF-8"));
//			sb.append("&sign=")
//					.append(URLEncoder.encode(Global.getConfig("shortmessage.sign"), "UTF-8"));

			sb.append("&sign=");

			//type为固定值pt  extno为扩展码，必须为数字 可为空
			sb.append("&type=pt&extno=").append(msg.getExtno());
			// 创建url对象
			//String temp = new String(sb.toString().getBytes("GBK"),"UTF-8");
			//System.out.println("sb:" + sb.toString());
			URL url = new URL(sb.toString());

			// 打开url连接
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// 设置url请求方式 ‘get’ 或者 ‘post’
			connection.setRequestMethod("POST");

			// 发送
			InputStream is = url.openStream();

			//转换返回值
			String returnStr = convertStreamToString(is);

			// 返回结果为‘0，20140009090990,1，提交成功’ 发送成功   具体见说明文档
			//System.out.println(returnStr);

			// 返回发送结果
			return "OK";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "failed";
		} catch (IOException e) {
			e.printStackTrace();
			return "failed";
		}

	}
	 */

	/** commented at 2019-03-02 by ryan 改为消息队列发送短信
	 * 新版
	 * 增加http请求及处理的超时设置
	 * 连接：8秒超时，处理:20秒
	 * @param msg
	 * @return

    public static String  SendMessage(Message2 msg)  {
        try {
            Map<String,String> map = Maps.newHashMap();
            map.put("name",Global.getConfig("shortmessage.name"));
            map.put("pwd",Global.getConfig("shortmessage.password"));
            map.put("mobile",msg.getMobile().trim());
            map.put("content",StringUtils.left(msg.getContent(),500));
            map.put("stime","");
            map.put("sign","");
            map.put("extno",msg.getExtno().trim());
            map.put("type","pt");
            String rtn = HttpClientUtils.post(Global.getConfig("shortmessage.url"),map,null,20000,60000);
			return StringUtils.isBlank(rtn)?"failed":rtn;
        }catch (Exception e) {
        	LogUtils.saveLog("发送短信","SendMessageUtils",String.format("%s,%s",msg.getMobile(),msg.getContent()),e,null);
            return "failed";
        }

    }
	 */

    /**
	 * 转换返回值类型为UTF-8格式.
	 * @param is
	 * @return

	public static String convertStreamToString(InputStream is) {    
        StringBuilder sb1 = new StringBuilder();    
        byte[] bytes = new byte[4096];  
        int size = 0;  
        
        try {    
        	while ((size = is.read(bytes)) > 0) {  
                String str = new String(bytes, 0, size, "UTF-8");  
                sb1.append(str);  
            }  
        } catch (IOException e) {    
            e.printStackTrace();    
        } finally {    
            try {    
                is.close();    
            } catch (IOException e) {    
               e.printStackTrace();    
            }    
        }    
        return sb1.toString();    
    }
	 */

	/* 11-29 短信平台出问题，临时提供的接口
	public static String  SendMessage(String mobile,String content)  throws IOException {
		Map<String,String> map = Maps.newHashMap();
		map.put("action","send");
		map.put("account",Global.getConfig("shortmessage.name"));
		map.put("password",Global.getConfig("shortmessage.password"));
		map.put("mobile",mobile);
		//map.put("content",StringUtils.left(content,500));
		map.put("content",StringUtils.left(content,500));
		map.put("extno",Global.getConfig("shortmessage.extno"));
		map.put("rt","json");
		String rtn =  HttpClientUtils.httpPost(Global.getConfig("shortmessage.url"),map,null);
		try {
			ShortMessageResponse response = GsonUtils.getInstance().fromJson(rtn, ShortMessageResponse.class);
			if (response == null) {
				return "false";
			}
			if(response.getStatus().equals("0")){
				return "OK";
			}
			return "false";
		}catch (Exception e){
			LogUtils.saveLog("发送短信错误","SendMessageUtils.sendMessage",map.toString(),e,null);
			return "false";
		}
	}

	public static String  SendMessage(Message msg)  throws IOException {
		return SendMessage(msg.getMobile(),msg.getContent());
	}
	*/

	/*
	public static String  TestMessage(String msg) {
		try {
			// 创建StringBuffer对象用来操作字符串
			StringBuffer sb = new StringBuffer("http://10.161.34.165:8080/KKLService/portal/receiveMessage?");
			// 向StringBuffer追加用户名
			sb.append("name=");

			// 向StringBuffer追加密码（登陆网页版，在管理中心--基本资料--接口密码，是28位的）
			sb.append("&pwd=");
			// 向StringBuffer追加密码（登陆网页版，在管理中心--基本资料--接口密码，是28位的）
			sb.append("&args=18750241669#@#1#@#2016/1/20 9:46:02#@#08142     ");

			// 创建url对象
			//String temp = new String(sb.toString().getBytes("GBK"),"UTF-8");
			System.out.println("sb:" + sb.toString());
			URL url = new URL(sb.toString());

			// 打开url连接
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// 设置url请求方式 ‘get’ 或者 ‘post’
			connection.setRequestMethod("POST");
			// 发送
			InputStream is = url.openStream();
			//转换返回值
			String returnStr = convertStreamToString(is);
			// 返回结果为‘0，20140009090990,1，提交成功’ 发送成功   具体见说明文档
			System.out.println(returnStr);
			// 返回发送结果
			return "OK";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "failed";
		} catch (IOException e) {
			e.printStackTrace();
			return "failed";
		}
	}
	*/

}

/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.common.servlet;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import com.wolfking.jeesite.common.utils.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * 生成随机验证码
 * @author ThinkGem
 * @version 2014-7-27
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/servlet/validateCodeServlet")
@Slf4j
public class ValidateCodeServlet extends HttpServlet {

	//private static RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);

	//@Autowired
	private RedisUtils redisUtils;

	public static final String VALIDATE_CODE = "validateCode";
	public static final String VALIDATE_CODE_KEY = "validcode:%s";
	private static Random random = new Random();//随机数
	/*
	 * 随机字符字典
	 */
	private static final char[] CHARS = { '2', '3', '4', '5', '6', '7', '8',
			'9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
			'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };


	@Override
	public void init() throws ServletException {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		ServletContext servletContext = this.getServletContext();
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		this.redisUtils = context.getBean(RedisUtils.class);
	}

	public ValidateCodeServlet() {
		super();
	}
	
	public void destroy() {
		super.destroy(); 
	}


	/**
	 * 验证码验证
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String validateCode = request.getParameter(VALIDATE_CODE); // AJAX验证，成功返回true
		if (StringUtils.isNotBlank(validateCode)){
			response.getOutputStream().print(validate(request, validateCode)?"true":"false");
		}else{
			this.doPost(request, response);
		}
	}

	/**
	 * 产生验证码 POST请求
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		getRandcode(request,response);
	}

	/**
	 * 验证码验证
	 * @param request
	 * @param validateCode
	 * @return
	 */
	public  boolean validate(HttpServletRequest request, String validateCode){
		//String code = (String)request.getSession().getAttribute(VALIDATE_CODE);
		String sessionId = request.getRequestedSessionId();
		String code = (String)redisUtils.get(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,String.format(VALIDATE_CODE_KEY,sessionId),String.class);//60秒
		return validateCode.toUpperCase().equals(code);
	}

	/*
	 * 获取6位随机数
	 */
	private static String getRandomString() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			buffer.append(CHARS[random.nextInt(CHARS.length)]);
		}
		return buffer.toString();
	}

	/*
	 * 获取随机数颜色
	 */
	private static Color getRandomColor() {
		return new Color(random.nextInt(255), random.nextInt(255), random
				.nextInt(255));
	}

	/*
	 * 返回某颜色的反色
	 */
	private static Color getReverseColor(Color c) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c
				.getBlue());
	}

	/**
	 * 生成验证码
	 *
	 * @return String
	 */
	public void getRandcode(HttpServletRequest request, HttpServletResponse response) {

		try {
			int width = 63;
			int height = 37;
			//设置response头信息
			//禁止缓存
			response.setContentType("image/jpeg");
			response.setHeader("Pragma", "no-cache");//不要缓存此内容
			response.setHeader("Cache-Control", "no-cache");//不要缓存此内容
			response.setDateHeader("Expires", 0);

			//生成缓冲区image类
			BufferedImage image = new BufferedImage(width, height, 1);
			//产生image类的Graphics用于绘制操作
			Graphics g = image.getGraphics();
			//Graphics类的样式
			g.setColor(this.getRandColor(200, 250));
			g.setFont(new Font("Times New Roman",0,28));
			g.fillRect(0, 0, width, height);
			//绘制干扰线
			for(int i=0;i<40;i++){
				g.setColor(this.getRandColor(130, 200));
				int x = random.nextInt(width);
				int y = random.nextInt(height);
				int x1 = random.nextInt(12);
				int y1 = random.nextInt(12);
				g.drawLine(x, y, x + x1, y + y1);
			}

			//绘制字符
			String strCode = "";
			for(int i=0;i<4;i++){
				String rand = String.valueOf(random.nextInt(10));
				strCode = strCode + rand;
				g.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
				g.drawString(rand, 13*i+6, 28);
			}
			//将字符保存到session中用于前端的验证
			String sessionId = request.getRequestedSessionId();
			redisUtils.setEX(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,String.format(VALIDATE_CODE_KEY,sessionId),strCode,90);//90秒
			request.getSession().setAttribute(VALIDATE_CODE, strCode);
			g.dispose();

			ImageIO.write(image, "JPEG", response.getOutputStream());
			response.getOutputStream().flush();
		} catch (Exception e) {
			//log.error("将内存中的图片通过流动形式输出到客户端失败>>>>   ", e);
			//e.printStackTrace();
			log.error("产生验证码图片失败",e);
		}
	}

	Color getRandColor(int fc,int bc){
		if(fc>255)
			fc = 255;
		if(bc>255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r,g,b);
	}
	
}

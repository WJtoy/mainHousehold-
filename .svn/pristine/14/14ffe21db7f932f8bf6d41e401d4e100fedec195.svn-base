/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.wolfking.jeesite.common.service;

import com.google.common.collect.Maps;
import com.kkl.kklplus.common.response.MSResponse;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.persistence.TreeDao;
import com.wolfking.jeesite.common.persistence.TreeEntity;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.wolfking.jeesite.common.utils.Reflections;
import com.kkl.kklplus.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件上传服务类
 * 文件上传统计调用此类的方法，防止文件写入目录规则不🙆
 * @author Ryan
 * @version 2020-01-14
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NEVER)
public class UploadFileService extends BaseService {

	/**
	 * 获得上传物理路径(0)及相对路径(1)
	 */
	public Map<Long,String> getRootFolder(String realPath) {
		StringBuilder savePath = new StringBuilder();
		savePath.append(Global.getUploadfilesDir());
		if(StringUtils.isBlank(savePath.toString())){
			savePath.append(StringUtils.trimToEmpty(realPath)).append("/uploads/");
		}
		String dayPath = DateUtils.getDate("yyyy/MM/dd");
		savePath.append(dayPath).append("/");
		File f1 = new File(savePath.toString().trim());
        //System.out.println(savePath);
		if (!f1.exists()) {
			f1.mkdirs();
		}
		Map<Long,String> map = Maps.newHashMapWithExpectedSize(2);
		map.put(0L,savePath.toString().trim());
		map.put(1L, dayPath.concat("/"));
		return map;
	}

	/**
	 * 上传单个文件
	 * @param savePath 物理路径
	 * @param relatePath 相对路径
	 * @param request
	 * @param response
	 */
	public String uploadSingle(Long id,String savePath,String relatePath,HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		StringBuffer json = new StringBuffer("");
		Map<String, MultipartFile> fileMap = null;
		try {
			MultipartResolver resolver = new StandardServletMultipartResolver();
			MultipartHttpServletRequest mRequest = resolver.resolveMultipart(request);
			fileMap = mRequest.getFileMap();
		} catch (Exception ex) {
			log.error("读取上传文件失败:",ex);
			json.append("{id:'',fileName:'读取上传文件失败',status:'false',origalName:'','isImage':false}");
			response.getWriter().print(json.toString());
			return json.toString().trim();
		}
		String reg = ".+(.JPEG|.JPG|.PNG|.GIF|.BMP|.TIF)$";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher;
		String oldFileName="";
		String name = "";
		String extName = "";
		int success = 0;
		String fileId;
		MultipartFile file = null;
		for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
			if(entry.getValue() != null){
				file = entry.getValue();
				break;
			}
		}
		if(file == null){
			log.error("读取上传文件失败");
			json.append("{id:'',fileName:'读取上传文件失败',status:'false',origalName:'','isImage':false}");
			response.getWriter().print(json.toString());
			return json.toString().trim();
		}
		name = file.getOriginalFilename();
		if (name == null || name.trim().equals("")) {
			log.error("文件名为空");
			json.append("{id:'',fileName:'文件名为空',status:'false',origalName:'','isImage':false}");
			response.getWriter().print(json.toString());
			return json.toString().trim();
		}
		//扩展名格式
		if (name.lastIndexOf(".") >= 0) {
			extName = name.substring(name.lastIndexOf("."));
		}
		oldFileName = name;
		//生成文件名：
		fileId = UUID.randomUUID().toString();
		name = fileId + extName;
		try {
			FileUtils.copyInputStreamToFile(file.getInputStream(), new File(savePath, name));
			if(json.length()>0){
				json.append(",");
			}
			String host = StringUtils.trimToEmpty(Global.getConfig("userfiles.host"));
			StringBuilder filePath = new StringBuilder(100);
			filePath.append(relatePath).append("/").append(name);
			json.append("{id:'").append(id.toString()).append("'")
					//.append(",fileUrl':'").append(host).append("/").append(relatePath).append("/").append(name).append("'")
					.append(",fileName:'").append(StringUtils.replace(filePath.toString(),"//","/")).append("'")
					.append(",status:'success'")
					.append(",origalName:'").append(oldFileName).append("'");
			matcher = pattern.matcher("."+extName.toUpperCase().trim());
			if(matcher.find()){
				json.append(",'isImage':true");
			}else{
				json.append(",'isImage':false");
			}
			json.append("}");
			success++;
		} catch (Exception e) {
			log.error("上传文件失败,file name:{}",oldFileName,e);
		}


		response.setStatus(200);
		if(success == 0){
			json.setLength(0);//clear
			json.append("{id:'',fileName:'',status:'false',origalName:'").append(oldFileName).append("','isImage':false}");
		}
		//response.getWriter().print(json.toString());
		return json.toString().trim();
	}


}

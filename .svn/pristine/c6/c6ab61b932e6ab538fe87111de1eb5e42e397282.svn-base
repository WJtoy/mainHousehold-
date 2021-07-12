package com.wolfking.jeesite.common.servlet;

import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.common.config.Global;
import com.wolfking.jeesite.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/servlet/UploadForMD")
public class FilesUploadMDServlet extends HttpServlet {
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        StringBuffer json = new StringBuffer("");
    	String savePath = Global.getUploadfilesDir();
    	String typeName = request.getParameter("type");
//        savePath="";
    	if(StringUtils.isBlank(savePath)){
            savePath = this.getServletConfig().getServletContext().getRealPath("");
            savePath = savePath + "/uploads/";
        }

//        String savePath = this.getServletConfig().getServletContext()
//                .getRealPath("");

        String year=DateUtils.getYear();
        String month=DateUtils.getMonth();
        String day=DateUtils.getDay();

//        savePath = savePath + "/uploads/"+year+"/"+month+"/"+day+"/";
        if(typeName != null && !typeName.isEmpty()){
            savePath = savePath + typeName +"/"+ year+"/"+month+"/"+day+"/";
        }


//        String savePath=Global.getFrontPath()+"/uploadFiles/";
        File f1 = new File(savePath);
//        System.out.println(savePath);
        if (!f1.exists()) {
            f1.mkdirs();
        }
        DiskFileItemFactory fac = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(fac);
        upload.setHeaderEncoding("utf-8");
        List fileList = null;
        try {
            fileList = upload.parseRequest(request);
        } catch (FileUploadException ex) {
            return;
        }
        String reg = ".+(.JPEG|.JPG|.PNG|.GIF|.BMP|.TIF)$";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher;
        String oldFileName="";

        Iterator<FileItem> it = fileList.iterator();
        String name = "";
        String extName = "";
        int success = 0;
        String id;
        while (it.hasNext()) {
            FileItem item = it.next();
            if (!item.isFormField()) {
                name = item.getName();
                long size = item.getSize();
                String type = item.getContentType();
//                System.out.println(size + " " + type);
                if (name == null || name.trim().equals("")) {
                    continue;
                }

                //扩展名格式：
                if (name.lastIndexOf(".") >= 0) {
                    extName = name.substring(name.lastIndexOf("."));
                }

                oldFileName=name;

                //生成文件名：
//            	if(request.getParameter("fileName")!=null&&request.getParameter("fileName").length()>0)
//            	{
//            		name=request.getParameter("fileName");
//            	}
//            	else
//            	{
                    id = UUID.randomUUID().toString();
            		name = id +extName;
//            	}

//                do {
//                    //生成文件名：
//                	if(request.getParameter("fileName")!=null&&request.getParameter("fileName").length()>0)
//                	{
//                		name=request.getParameter("fileName");
//                	}
//                	else
//                	{
//                		name = UUID.randomUUID().toString();
//                	}
//                    file = new File(savePath + name + extName);
//                } while (file.exists());
                File saveFile = new File(savePath + name);
                try {
                    item.write(saveFile);
                    if(json.length()>0){
                        json.append(",");
                    }
                    json.append("{id:'").append(id).append("',fileName:'").append(typeName).append('/').append(year).append("/").append(month).append("/").append(day).append("/").append(name).append("',status:'success',origalName:'").append(oldFileName).append("'");
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
            }
        }

        response.setStatus(200);
        /* old
        String retxt ="{fileName:'"+year+"/"+month+"/"+day+"/"+name+"',status:'success',origalName:'"+oldFileName+"'}";
        response.getWriter().print(retxt);
        */
        if(success ==0){
            json.setLength(0);//clear
            json.append("{id:'',fileName:'',status:'false',origalName:'").append(oldFileName).append("','isImage':false}");
        }else if(success>1){
            json.insert(0,"[").append("]");
        }
        response.getWriter().print(json.toString());
    }












//    	req.setCharacterEncoding("utf-8");
//    	resp.setContentType("text/html;charset=utf-8");
//    	//为解析类提供配置信息
//    	DiskFileItemFactory factory = new DiskFileItemFactory();
//    	//创建解析类的实例
//    	ServletFileUpload sfu = new ServletFileUpload(factory);
//    	//开始解析
//    	sfu.setFileSizeMax(1024*400);
//    	//每个表单域中数据会封装到一个对应的FileItem对象上
//    	try {
//    	List<FileItem> items = sfu.parseRequest(req);
//    	//区分表单域
//    	for (int i = 0; i < items.size(); i++) {
//    	FileItem item = items.get(i);
//    	//isFormField为true，表示这不是文件上传表单域
//    	if(!item.isFormField()){
//    	ServletContext sctx = getServletContext();
//    	//获得存放文件的物理路径
//    	//upload下的某个文件夹 得到当前在线的用户 找到对应的文件夹
//
//    	String path = sctx.getRealPath("/upload");
//    	System.out.println(path);
//    	//获得文件名
//    	String fileName = item.getName();
//    	System.out.println(fileName);
//    	//该方法在某些平台(操作系统),会返回路径+文件名
//    	fileName = fileName.substring(fileName.lastIndexOf("/")+1);
//    	File file = new File(path+"\\"+fileName);
//    	if(!file.exists()){
//    	item.write(file);
//    	//将上传图片的名字记录到数据库中
//
//    	resp.sendRedirect("/upload/ok.html");
//    	}
//    	}
//    	}
//    	} catch (Exception e) {
//    	e.printStackTrace();
//    	}







//    }
}
